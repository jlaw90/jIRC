package org.newbiehacker.jIRC;

import java.io.Serializable;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 01-Oct-2006</p>
 * <p>Time: 00:57:12</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class IRCUser implements Serializable, Comparable {
    public String login;
    public String host;
    public String server;
    public String prefix = "";
    public String nick;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String toString() {
        return nick + "!" + login + "@" + host;
    }


    public int compareTo(Object o) {
        if (o instanceof IRCUser) {
            IRCUser iu = (IRCUser) o;
            if (iu.getPrefix().length() > 0 && getPrefix().length() <= 0)
                return 1;
            if (iu.getPrefix().length() <= 0 && getPrefix().length() > 0)
                return -1;
            if (iu.getPrefix().length() > 0 && iu.getPrefix().length() > 0) {
                char p = iu.getPrefix().toCharArray()[0];
                int pp = 0;
                char p1 = getPrefix().toCharArray()[0];
                int pp1 = 0;

                if (p == '~')
                    pp = 5;
                if (p == '&')
                    pp = 4;
                if (p == '@')
                    pp = 3;
                if (p == '%')
                    pp = 2;
                if (p == '+')
                    pp = 1;

                if (p1 == '~')
                    pp1 = 5;
                if (p1 == '&')
                    pp1 = 4;
                if (p1 == '@')
                    pp1 = 3;
                if (p1 == '%')
                    pp1 = 2;
                if (p1 == '+')
                    pp1 = 1;

                if (pp != pp1)
                    return pp - pp1;
            }
            for (int i = 0; i <= nick.length(); i++) {
                if (i == nick.length() || i >= iu.getNick().length())
                    return iu.getNick().length() - nick.length();
                if (nick.charAt(i) != iu.getNick().charAt(i) &&
                        nick.charAt(i) != Character.toUpperCase(iu.getNick().charAt(i)) &&
                        Character.toUpperCase(nick.charAt(i)) != iu.getNick().charAt(i))
                    return Character.toLowerCase(nick.charAt(i)) - Character.toLowerCase(iu.getNick().charAt(i));
            }
        }
        return 0;
    }
}
