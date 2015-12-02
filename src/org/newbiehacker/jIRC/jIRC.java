package org.newbiehacker.jIRC;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 30-Sep-2006</p>
 * <p>Time: 14:21:47</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class jIRC implements ChangeListener {
    static JFrame client = new JFrame("jIRC v1 ~ copyright 2006 newbiehacker, All rights reserved");
    static JTabbedPane jtp;
    public static HashMap<String, jIRCInstance> instances = new HashMap<String, jIRCInstance>();
    public static Properties cmds = new Properties();
    public static ArrayList<String> controllers = new ArrayList<String>();
    public static ArrayList<String> supers = new ArrayList<String>();
    public static boolean DEBUG = false;

    private jIRC() {
    }

    public static void startThread(Runnable r, int priority) {
        Thread t = new Thread(r);
        t.setPriority(priority);
        t.start();
    }

    public void stateChanged(ChangeEvent e) {
        jtp.setForegroundAt(jtp.getSelectedIndex(), Color.BLACK);
    }

    public static void main(String[] args) {
        if (Thread.currentThread().getThreadGroup().equals(CommandThread.controlled))
            System.exit(-1);
        System.setSecurityManager(new jIRCSecurityManager());
        System.out.println("Starting jIRC");
        try {
            jtp = new JTabbedPane(JTabbedPane.TOP);
            jtp.addChangeListener(new jIRC());
            load();
            client.add(jtp, BorderLayout.CENTER);
            client.setPreferredSize(new Dimension(640, 480));
            client.pack();
            client.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("dat/config.ini")));
        String s;
        jIRCInstance t = null;
        while ((s = br.readLine()) != null) {
            //System.out.println(s);
            if (s.trim().startsWith("[server="))
                t = new jIRCInstance(s.replace("[server=", "").replace("]", ""));
            else if (s.equals("[/server]") && t != null) {
                if (instances.containsKey(t.server))
                    continue;
                new Thread(t).start();
            } else if (t != null && !s.trim().startsWith("#") && s.contains("="))
                t.config.setProperty(s.split("=", 2)[0], s.split("=", 2)[1]);
        }
        br = new BufferedReader(new InputStreamReader(new FileInputStream("dat/supers.ini")));
        supers.clear();
        while ((s = br.readLine()) != null)
            supers.add(s);
        br.close();
        br = new BufferedReader(new InputStreamReader(new FileInputStream("dat/controllers.ini")));
        controllers.clear();
        while ((s = br.readLine()) != null)
            controllers.add(s);
        br.close();
        cmds.clear();
        br = new BufferedReader(new InputStreamReader(new FileInputStream("dat/cmds.ini")));
        while ((s = br.readLine()) != null)
            cmds.put(s.split("=")[0], s.substring(s.split("=")[0].length() + 1));
        br.close();
    }

    public static void save() throws Exception {
        PrintStream ps = new PrintStream("dat/supers.ini");
        for (String aSuper : supers)
            ps.println(aSuper);
        ps.close();
        ps = new PrintStream("dat/controllers.ini");
        for (String aController : controllers)
            ps.println(aController);
        ps.close();
        ps = new PrintStream("dat/cmds.ini");
        for (Object key : cmds.keySet()) {
            ps.println(key + "=" + cmds.get(key));
        }
        ps.close();
    }

    static void addServerInstance(jIRCInstance ji) {
        instances.put(ji.server, ji);
        jtp.addTab(ji.server, ji.panel);
    }

    public static void flashBar(String server, String channel, Color c) {
        if (true)
            return;
        Console con = (Console) jtp.getComponentAt(jtp.indexOfComponent(instances.get(server).panel));
        if (jtp.getSelectedComponent() != con)
            jtp.setForegroundAt(jtp.indexOfComponent(instances.get(server).panel), c);
        int idx = instances.get(server).channelList.getSelectedIndex();
        jIRCInstance i = instances.get(server);
        if (i.channelList.getSelectedValue() != i.tabs.get(channel)) {
            i.channelList.setSelectedValue(i.tabs.get(channel), false);
            i.channelList.setSelectionForeground(c);
            i.channelList.setSelectedIndex(idx);
        }
    }

    public static void loadScript(jIRCInstance t, String script, String host, String channel, String nick, String login) {
        try {
            if (!isSudo(host)) {
                t.sendMessage(channel, "Nice try, but I thought of that already");
                return;
            }
            String[] args = script.split("\\s+");
            String file = args[0];
            File f = new File("Scripts/" + file + ".jbs");
            byte[] bytes = new byte[(int) f.length()];
            new FileInputStream(f).read(bytes);
            String cmdBody = new String(bytes);
            File defaults = new File("dat/autoCommands.bsh");
            bytes = new byte[(int) defaults.length()];
            new FileInputStream(defaults).read(bytes);
            String data = new String(bytes);
            cmdBody = data + "\r\n" + cmdBody;
            cmdBody = cmdBody.replace("$$", "^@$@^");
            if (script.length() > file.length())
                args[0] = script.substring(file.length() + 1);
            else
                args[0] = "";
            Pattern p = Pattern.compile("\\$0\\d+\\b");
            Matcher m = p.matcher(cmdBody);
            while (m.find()) {
                String ss = cmdBody.substring(m.start(), m.end());
                int i = Integer.parseInt(ss.substring(2));
                if (args.length < i + 2)
                    continue;
                StringBuilder sb = new StringBuilder(file + " ");
                for (int i1 = 1; i1 <= i; i1++)
                    sb.append(args[i1]).append(" ");
                cmdBody = m.replaceAll(script.substring(sb.length()));
                //cmdBody = cmdBody.replace(ss, command.substring(sb.length()));
            }
            for (int i = 0; i < args.length; i++)
                if (cmdBody.contains("$" + i))
                    cmdBody = cmdBody.replaceAll("\\$" + i + "\\b", args[i].replace("$", "\\$"));
            if (channel != null)
                cmdBody = cmdBody.replace("$channel", channel);
            cmdBody = cmdBody.replace("$server", t.server);
            cmdBody = cmdBody.replace("$host", host);
            cmdBody = cmdBody.replace("$nick", nick);
            cmdBody = cmdBody.replace("$sender", nick);
            cmdBody = cmdBody.replace("$login", login);
            cmdBody = cmdBody.replace("$bot", t.nick);
            cmdBody = cmdBody.replace("$script", file);
            cmdBody = cmdBody.replace("^@$@^", "$");
            new Thread(new CommandThread(cmdBody)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isController(String host) {
        for (String controller : controllers)
            if (host.equalsIgnoreCase(controller))
                return true;
        return false;
    }

    public static boolean isSudo(String host) {
        for (String aSuper : supers)
            if (host.equalsIgnoreCase(aSuper))
                return true;
        return false;
    }

    public static void parse(jIRCInstance t, String command, String host, String channel, String nick, String login) {
        try {
            int priv = isController(host) ? 1 : isSudo(host) ? 2 : 0;
            command = command.replace("$$", "^@$@^");
            String[] posCmds = command.split("\\s\\|\\|\\s");
            for (String posCmd : posCmds) {
                String[] args = posCmd.split("\\s+");
                String cmd = args[0];
                String cmdBody = cmds.getProperty(cmd);
                if (cmdBody == null || cmdBody.equals("")) {
                    return;
                }
                cmdBody = cmdBody.replace("$$", "^@$@^");
                int reqPriv = 0;
                String[] check;
                if ((check = cmdBody.split("\\^priv\\^")).length > 1) {
                    reqPriv = Integer.parseInt(check[1]);
                    cmdBody = check[0];
                }
                if(jIRC.DEBUG)
                    t.sendMessage(channel, "Hostname: " + host + ", Priveleges: " + (priv == 2? "supercontroller": priv == 1? "controller": priv == 0? "Public": "None"));
                //t.sendMessage(channel, "Required priveleges for execution: " + (reqPriv == 2? "sudo": priv == 1? "controller": "Public"));
                if (priv < reqPriv) {
                    t.sendMessage(channel, "Required priveleges: " + (reqPriv == 2 ? "sudo" : reqPriv == 1 ? "controller" : "WTF hax?") + ", Yours: " + (priv == 1 ? "controller" : "public user"));
                    return;
                }
                if (posCmd.length() > cmd.length())
                    args[0] = posCmd.substring(cmd.length() + 1);
                else
                    args[0] = "";
                Pattern p = Pattern.compile("\\$0\\d+\\b");
                Matcher m = p.matcher(cmdBody);
                while (m.find()) {
                    String ss = cmdBody.substring(m.start(), m.end());
                    int i = Integer.parseInt(ss.substring(2));
                    if (args.length < i + 2)
                        continue;
                    StringBuilder sb = new StringBuilder(cmd + " ");
                    for (int i1 = 1; i1 <= i; i1++)
                        sb.append(args[i1]).append(" ");
                    cmdBody = m.replaceAll(posCmd.substring(sb.length()));
                    //cmdBody = cmdBody.replace(ss, command.substring(sb.length()));
                }
                for (int i = 0; i < args.length; i++)
                    if (cmdBody.contains("$" + i))
                        cmdBody = cmdBody.replaceAll("\\$" + i + "\\b", args[i].replace("$", "\\$"));
                if (DEBUG)
                    t.sendRawLine("PRIVMSG " + channel + " " + cmdBody);
                File defaults = new File("dat/autoCommands.bsh");
                byte[] bytes = new byte[(int) defaults.length()];
                new FileInputStream(defaults).read(bytes);
                String data = new String(bytes);
                cmdBody = data + cmdBody;
                if (channel != null)
                    cmdBody = cmdBody.replace("$channel", channel);
                cmdBody = cmdBody.replace("$server", t.server);
                cmdBody = cmdBody.replace("$host", host);
                cmdBody = cmdBody.replace("$nick", nick);
                cmdBody = cmdBody.replace("$sender", nick);
                cmdBody = cmdBody.replace("$login", login);
                cmdBody = cmdBody.replace("$bot", t.nick);
                cmdBody = cmdBody.replace("$privs", String.valueOf(priv));
                cmdBody = cmdBody.replace("^@$@^", "$");
                if (priv == 2) {
                    cmdBody = cmdBody.replace("$myPass", t.config.getProperty("nickserv-pass"));
                    new Thread(CommandThread.unControlled, new CommandThread(cmdBody), "CommandThread-" + nick + "-" + cmd).start();
                } else {
                    Thread[] ts = new Thread[CommandThread.controlled.activeCount()];
                    CommandThread.controlled.enumerate(ts);
                    for (Thread t1 : ts)
                        if (t1.getName().contains(nick)) {
                            t.sendMessage(channel, "You already have a Thread running, only sudos can run more than one Thread concurrently");
                            return;
                        }
                    new Thread(CommandThread.controlled, new CommandThread(cmdBody), "CommandThread-SandBoxed-" + nick + "-" + cmd).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
