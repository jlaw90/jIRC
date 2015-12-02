package org.newbiehacker.jIRC;

import bsh.EvalError;
import bsh.Interpreter;

import java.util.ArrayList;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 30-Sep-2006</p>
 * <p>Time: 22:14:22</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class CommandThread extends Thread {
    public static final ThreadGroup controlled = new ThreadGroup("Internal sandbox");
    public static final ThreadGroup unControlled = new ThreadGroup("Internal");
    public static ArrayList<Thread> runningThreads = new ArrayList<Thread>();
    String command;
    Interpreter i = new Interpreter();

    public CommandThread(String data) {
        this.command = data;
    }

    public void set(String var, Object value) throws EvalError {
        i.set(var, value);

    }

    public void run() {
        runningThreads.add(this);
        try {
            i.eval("import org.newbiehacker.jIRC.*;try{\r\n" + command + "\r\n}catch(SecurityException se){System.out.println(\"Not enough priveleges: \" + se.toString());}catch(Throwable t){System.out.println(\"Error!: \" + t.toString());}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        runningThreads.remove(this);
    }
}
