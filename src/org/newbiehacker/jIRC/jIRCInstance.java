package org.newbiehacker.jIRC;

import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
import org.jibble.pircbot.PircBot;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 07-Oct-2006</p>
 * <p>Time: 00:08:57</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class jIRCInstance extends PircBot implements Runnable {
    public IRCUserManager myManager;
    ArrayList<jIRCListener> listeners = new ArrayList<jIRCListener>();
    JDesktopPane jdp = new JDesktopPane();
    final JList channelList;
    JPanel panel = new JPanel(new BorderLayout());
    public final HashMap<String, Console> tabs = new HashMap<String, Console>();
    public Properties config = new Properties();
    String nick;
    String server;
    boolean connected = false;

    public jIRCInstance(String server) {
        myManager = new IRCUserManager(server);
        if(jIRC.DEBUG)
            setVerbose(true);
        this.server = server;
        channelList = new JList();
        channelList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                jdp.getDesktopManager().activateFrame(tabs.get(channelList.getSelectedValue()));
            }
        });
        channelList.setPreferredSize(new Dimension(100, 0));
        panel.add(new JScrollPane(channelList), BorderLayout.WEST);
        panel.add(jdp, BorderLayout.CENTER);
        setVersion("jIRC version 0.8a copyright 2006 newbiehacker");
    }

    private Console getConsole(String channel) {
        if (!tabs.containsKey(channel)) {
            Console c = new Console(channel, this);
            jdp.add(c);
            jdp.getDesktopManager().maximizeFrame(c);
            try {
                c.setMaximum(true);
            } catch (Exception e) {
            }
            tabs.put(channel, c);
            channelList.setModel(new ListModel() {
                public int getSize() {
                    return tabs.size();
                }

                public Object getElementAt(int index) {
                    return tabs.values().toArray()[index].toString();
                }

                public void addListDataListener(ListDataListener l) {
                }

                public void removeListDataListener(ListDataListener l) {
                }
            });
        }
        return tabs.get(channel);
    }

    public void run() {
        try {
            System.out.println(server + ": run()");
            setAutoNickChange(true);
            super.setMessageDelay(0);
            nick = config.getProperty("nick");
            //jtp.setPreferredSize(new Dimension(640, 480));
            System.out.println("Connecting to " + server + "...");
            setName(nick);
            setLogin(config.getProperty("login"));
            connect(server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addListener(jIRCListener jl) {
        listeners.add(jl);
    }

    public void removeListener(jIRCListener jl) {
        listeners.remove(jl);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    protected void onConnect() {
        if (!connected) {
            connected = true;
            System.out.println("Connected to " + server + "!");
            sendRawLine("PRIVMSG nickserv :IDENTIFY " + config.getProperty("nickserv-pass"));
            String[] channels = config.getProperty("channels").split("\\s?,\\s?");
            for (String chan : channels) {
                joinChannel(chan);
                System.out.println("Joining channel " + chan + "...");
            }
            jIRC.addServerInstance(this);
        }
        for (jIRCListener listener : listeners) listener.onConnect();
    }

    protected void onDisconnect() {
        try {
            System.out.println("Disconnected from " + server + "!");
            new Thread(this).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (jIRCListener listener : listeners) listener.onDisconnect();
    }

    protected void onServerResponse(int code, String response) {
        if (!connected)
            onConnect();
        //System.out.println(code + ": " + response);
        if (code == 352) {
            String[] data = response.split(" ", 8);
            IRCUser ic = new IRCUser();
            ic.setLogin(data[2]);
            ic.setHost(data[3]);
            ic.setServer(data[4]);
            String nick = data[5];
            if (nick.startsWith("~") || nick.startsWith("&") || nick.startsWith("@") || nick.startsWith("%") || nick.startsWith("+"))
                nick = nick.substring(1);
            String prefix = "";
            if (data[5].contains("~"))
                prefix += "~";
            if (data[5].contains("&"))
                prefix += "&";
            if (data[5].contains("@"))
                prefix += "@";
            if (data[5].contains("%"))
                prefix += "%";
            if (data[5].contains("+"))
                prefix += "+";
            if (!prefix.equals(""))
                ic.setPrefix(prefix);
            ic.setNick(nick);
            myManager.add(ic, data[1]);
            getConsole(data[1]).addUser(nick);
        } else if (code == 5) {
            if (response.contains("NETWORK=")) {
                int idx1 = response.indexOf("NETWORK=");
                int idx2 = response.indexOf(' ', idx1);
                jIRC.jtp.setTitleAt(jIRC.jtp.indexOfComponent(panel), response.substring(idx1 + 8, idx2));
            }
        }
        for (jIRCListener listener : listeners) listener.onServerResponse(code, response);
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        getConsole(channel).println(new SimpleDateFormat("[hh:mm:ss]").format(new Date()) + "<" + sender + ">: " + message);
        jIRC.flashBar(server, channel, Color.RED);
        for (jIRCListener listener : listeners) listener.onMessage(channel, sender, login, hostname, message);
        if (message.startsWith("!"))
            jIRC.parse(this, message.substring(1), hostname, channel, sender, login);
    }

    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
        getConsole(sender).println(": " + message);
        for (jIRCListener listener : listeners) listener.onPrivateMessage(sender, login, hostname, message);
        if (message.startsWith("!"))
            jIRC.parse(this, message.substring(1), hostname, null, sender, login);
    }

    protected void onAction(String sender, String login, String hostname, String target, String action) {
        for (jIRCListener listener : listeners) listener.onAction(sender, login, hostname, target, action);
        if (tabs.containsKey(target))
            getConsole(target).println("* " + sender + " " + action);
    }

    protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
        for (jIRCListener listener : listeners) listener.onNotice(sourceNick, sourceLogin, sourceHostname, target, notice);
        getConsole(target).println("* [" + sourceNick + "] (Notice) " + notice);
    }

    protected void onJoin(String channel, String sender, String login, String hostname) {
        for (jIRCListener listener : listeners) listener.onJoin(channel, sender, login, hostname);
        sendRawLine("WHO " + channel);
        jIRC.flashBar(server, channel, Color.GREEN);
        getConsole(channel).println("* " + sender + " joined channel");
    }

    protected void onPart(String channel, String sender, String login, String hostname) {
        for (jIRCListener listener : listeners) listener.onPart(channel, sender, login, hostname);
        getConsole(channel).println("* " + sender + " parted channel");
        jIRC.flashBar(server, channel, Color.GREEN);
        getConsole(channel).removeUser(sender);
        if (sender.equalsIgnoreCase(nick)) {
            System.out.println("We parted " + channel);
            jdp.remove(getConsole(channel));
            tabs.remove(channel);
            channelList.setModel(new ListModel() {
                public int getSize() {
                    return tabs.size();
                }

                public Object getElementAt(int index) {
                    return tabs.values().toArray()[index].toString();
                }

                public void addListDataListener(ListDataListener l) {
                }

                public void removeListDataListener(ListDataListener l) {
                }
            });
        }
    }

    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        for (jIRCListener listener : listeners) listener.onNickChange(oldNick, login, hostname, newNick);
        myManager.rename(oldNick, newNick);
        if (oldNick.equals(nick))
            nick = newNick;
        for (Console c : tabs.values()) {
            if (c.containsUser(oldNick)) {
                c.println("*" + oldNick + " is now known as " + newNick);
                c.removeUser(oldNick);
                c.addUser(newNick);
            }
            if (c.channel.equals(oldNick)) {
                tabs.remove(oldNick);
                tabs.put(newNick, c);
            }
        }
        jIRC.flashBar(server, ((Console) jdp.getSelectedFrame()).channel, Color.GREEN);
    }

    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        for (jIRCListener listener : listeners) listener.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason);
        getConsole(channel).print("* " + recipientNick + " was kicked by " + kickerNick + " (" + reason + ")");
        jIRC.flashBar(server, channel, Color.GREEN);
        getConsole(channel).removeUser(recipientNick);
        if (recipientNick.equalsIgnoreCase(getNick())) {
            jdp.remove(getConsole(channel));
            tabs.remove(channel);
            joinChannel(channel);
        }
    }

    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        for (jIRCListener listener : listeners) listener.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
        if (sourceNick.equals(nick)) {
            jdp.removeAll();
            tabs.clear();
        } else {
            for (Console c : tabs.values()) {
                if (c.containsUser(sourceNick)) {
                    c.println("*" + sourceNick + "[" + sourceLogin + "@" + sourceHostname + "] has quit (" + reason + ")");
                    c.removeUser(sourceNick);
                }
            }
        }
    }

    protected void onChannelInfo(String channel, int userCount, String topic) {
        for (jIRCListener listener : listeners) listener.onChannelInfo(channel, userCount, topic);
        getConsole(channel).println("* channel contains " + userCount + "\nTopic: " + topic);
    }

    protected void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        for (jIRCListener listener : listeners) listener.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);
        getConsole(channel).println("* " + sourceNick + " sets mode " + mode);
        sendRawLine("WHO " + channel);
    }

    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        for (jIRCListener listener : listeners) listener.onUserMode(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
        for (Console c : tabs.values())
            if (c.containsUser(targetNick)) {
                c.println("* " + sourceNick + " sets mode " + mode);
                sendRawLine("WHO " + targetNick);
            }
    }

    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        for (jIRCListener listener : listeners) listener.onOp(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        for (jIRCListener listener : listeners) listener.onDeop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    protected void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        for (jIRCListener listener : listeners) listener.onVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    protected void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        for (jIRCListener listener : listeners) listener.onDeVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    protected void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
        for (jIRCListener listener : listeners) listener.onSetChannelKey(channel, sourceNick, sourceLogin, sourceHostname, key);
    }

    protected void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key) {
        for (jIRCListener listener : listeners) listener.onRemoveChannelKey(channel, sourceNick, sourceLogin, sourceHostname, key);
    }

    protected void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit) {
        for (jIRCListener listener : listeners) listener.onSetChannelLimit(channel, sourceNick, sourceLogin, sourceHostname, limit);
    }

    protected void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemoveChannelLimit(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
        for (jIRCListener listener : listeners) listener.onSetChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
    }

    protected void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
        for (jIRCListener listener : listeners) listener.onRemoveChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask);
    }

    protected void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onSetTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemoveTopicProtection(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onSetNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemoveNoExternalMessages(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onSetInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemoveInviteOnly(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onSetModerated(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemoveModerated(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onSetPrivate(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemovePrivate(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onSetSecret(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname) {
        for (jIRCListener listener : listeners) listener.onRemoveSecret(channel, sourceNick, sourceLogin, sourceHostname);
    }

    protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
        for (jIRCListener listener : listeners) listener.onInvite(targetNick, sourceNick, sourceLogin, sourceHostname, channel);
    }

    protected void onIncomingFileTransfer(DccFileTransfer transfer) {
        for (jIRCListener listener : listeners) listener.onIncomingFileTransfer(transfer);
    }

    protected void onFileTransferFinished(DccFileTransfer transfer, Exception e) {
        for (jIRCListener listener : listeners) listener.onFileTransferFinished(transfer, e);
    }

    protected void onIncomingChatRequest(DccChat chat) {
        for (jIRCListener listener : listeners) listener.onIncomingChatRequest(chat);
    }

    protected void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        for (jIRCListener listener : listeners) listener.onVersion(sourceNick, sourceLogin, sourceHostname, target);
    }

    protected void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue) {
        for (jIRCListener listener : listeners) listener.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
    }

    protected void onServerPing(String response) {
        super.onServerPing(response);
        for (jIRCListener listener : listeners) listener.onServerPing(response);
    }

    protected void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        for (jIRCListener listener : listeners) listener.onTime(sourceNick, sourceLogin, sourceHostname, target);
    }

    protected void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target) {
        for (jIRCListener listener : listeners) listener.onFinger(sourceNick, sourceLogin, sourceHostname, target);
    }
}
