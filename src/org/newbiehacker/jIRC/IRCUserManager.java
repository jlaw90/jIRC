package org.newbiehacker.jIRC;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 01-Oct-2006</p>
 * <p>Time: 01:20:34</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public final class IRCUserManager {
    private HashMap<String, ArrayList<IRCUser>> users = new HashMap<String, ArrayList<IRCUser>>();
    private String server;

    public synchronized ArrayList<IRCUser> getUsers(String channel) {
        if (users.containsKey(channel))
            return users.get(channel);
        return null;
    }

    public synchronized IRCUser get(String nick, String channel) {
        if (users.containsKey(channel))
            for (IRCUser iu : users.get(channel))
                if (iu.getNick().equalsIgnoreCase(nick))
                    return iu;
        return null;
    }

    public final boolean contains(String channel, String nick) {
        for (IRCUser iu : users.get(channel))
            if (iu.nick.equalsIgnoreCase(nick))
                return true;
        return false;
    }

    public final int indexOf(String channel, String nick) {
        ArrayList<IRCUser> us = users.get(channel);
        for (int i = 0; i < us.size(); i++)
            if (us.get(i).getNick().equalsIgnoreCase(nick))
                return i;
        return -1;
    }

    public synchronized void add(IRCUser iu, String channel) {
        if (users.get(channel) == null)
            users.put(channel, new ArrayList<IRCUser>());
        remove(iu.nick, channel);
        users.get(channel).add(iu);
        save();
    }

    public synchronized void rename(String orig, String neww) {
        for (ArrayList<IRCUser> list : users.values())
            for (IRCUser iu : list)
                if (iu.getNick().equalsIgnoreCase(orig))
                    iu.setNick(neww);
        save();
    }

    public synchronized void remove(String nick, String channel) {
        for (IRCUser iu1 : users.get(channel))
            if (iu1.getNick().equalsIgnoreCase(nick))
                users.get(channel).remove(iu1);
    }

    public synchronized IRCUser getByHost(String host, String channel) {
        for (IRCUser iu1 : users.get(channel))
            if (iu1.getHost().equalsIgnoreCase(host))
                return iu1;
        return null;
    }

    private synchronized void save() {
        try {
            File f = new File("dat/usercache/" + server + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(users);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IRCUserManager(String server) {
        this.server = server;
        try {
            File f = new File("dat/usercache/" + server + ".dat");
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Object o = ois.readObject();
            users = (HashMap<String, ArrayList<IRCUser>>) o;
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}