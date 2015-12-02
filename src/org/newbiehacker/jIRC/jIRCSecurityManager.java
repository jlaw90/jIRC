package org.newbiehacker.jIRC;

import java.security.Permission;

/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 01-Oct-2006</p>
 * <p>Time: 02:55:37</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public class jIRCSecurityManager extends SecurityManager {
    public void checkPermission(Permission perm) {
        if (!getThreadGroup().equals(CommandThread.controlled))
            return;
        String actions = perm.getActions();
        String name = perm.getName();
        if (actions.equals("execute"))
            throw new SecurityException("No execution allowed");
        if (actions.equals("read"))
            return;
        if (name.equals("createSecurityManager"))
            throw new SecurityException("Changing SecurityManager not allowed >.>");
        if (name.equals("createClassLoader"))
            return;
        if (name.equals("showWindowWithoutWarningBanner"))
            return;
        if (name.equals("accessClassInPackage.sun.reflect"))
            return;
        if (name.equals("accessEventQueue"))
            return;
        if (name.equals("accessDeclaredMembers"))
            return;
        if (name.equals("suppressAccessChecks"))
            return;
        //jIRC.msg(jIRC.getCurrentChannel(), "checkPermision: " + perm.getName() + ", Actions: " + perm.getActions());
        throw new SecurityException("Unchecked rule! (name:" + perm.getName() + ", actions:" + perm.getActions() + ")");
    }
}
