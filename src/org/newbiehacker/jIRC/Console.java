package org.newbiehacker.jIRC;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 30-Sep-2006</p>
 * <p>Time: 14:28:40</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class Console extends JInternalFrame implements ActionListener {
    private JTextArea jta;
    private JScrollPane jtaScroller;
    public String channel;
    private JTextField input;
    private JList userList;
    private jIRCInstance inst;
    final ArrayList<IRCUser> users = new ArrayList<IRCUser>();

    public Console(String channel, jIRCInstance inst) {
        super(channel, true, true, true, true);
        this.inst = inst;
        this.channel = channel;
        this.jta = new JTextArea("jIRC v1 ~ copyright 2006 newbiehacker, All rights reserved.\n\n");
        this.jtaScroller = new JScrollPane(jta);
        input = new JTextField();
        this.setLayout(new BorderLayout());
        this.add(jtaScroller, BorderLayout.CENTER);
        this.add(input, BorderLayout.SOUTH);
        userList = new JList();
        userList.setPreferredSize(new Dimension(100, 0));
        this.add(userList, BorderLayout.EAST);
        input.addActionListener(this);
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent evt) {
        String text = evt.getActionCommand();
        if (text.startsWith("/"))
            jIRC.parse(inst, text.substring(1), jIRC.supers.get(0), this.channel, inst.nick, "login");
        else {
            inst.sendMessage(channel, text);
            println(new SimpleDateFormat("[hh:mm:ss]").format(new Date()) + "<" + inst.nick + ">" + text);
        }
        input.setText("");
    }

    private void fixScrollBar() {
        jtaScroller.getVerticalScrollBar().setValue(jtaScroller.getVerticalScrollBar().getMaximum());
    }

    public void print(Object obj) {
        jta.append(obj.toString());
    }

    public void print(String s) {
        jta.append(s);
    }

    public void println(Object x) {
        jta.append(x.toString() + "\n");
        fixScrollBar();
    }

    public void println(String x) {
        jta.append(x + "\n");
        fixScrollBar();
    }

    public void addUser(String nick) {
        if (!users.contains(inst.myManager.get(nick, channel)))
            users.add(inst.myManager.get(nick, channel));
        Collections.sort(users);
        userList.setModel(new ListModel() {
            public int getSize() {
                return users.size();
            }

            public Object getElementAt(int index) {
                return users.get(index);
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
    }

    public void removeUser(String nick) {
        users.remove(inst.myManager.get(nick, channel));
        userList.setModel(new ListModel() {
            public int getSize() {
                return users.size();
            }

            public Object getElementAt(int index) {
                return users.get(index);
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
    }

    public boolean containsUser(String nick) {
        return users.contains(inst.myManager.get(nick, channel));
    }


    public void doDefaultCloseAction() {
        inst.partChannel(channel, "Closed window =/");
        inst.tabs.remove(this);
        inst.channelList.setModel(new ListModel() {
            public int getSize() {
                return inst.tabs.size();
            }

            public Object getElementAt(int index) {
                return inst.tabs.values().toArray()[index].toString();
            }

            public void addListDataListener(ListDataListener l) {
            }

            public void removeListDataListener(ListDataListener l) {
            }
        });
        dispose();
    }

    public String toString() {
        return channel;
    }
}
