package org.newbiehacker.jIRC;
import org.jibble.pircbot.DccChat;
import org.jibble.pircbot.DccFileTransfer;
/**
 * <p>Created by James Lawrence.</p>
 * <p>Date: 30-Sep-2006</p>
 * <p>Time: 19:14:44</p>
 * <p>Intellectual Property of newbiehacker</p>
 * <p>Redistribution or modification of this work is strictly prohibited.</p>
 * <p>Removal or modification of this notice in any way is also breaching copyright.</p>
 * <p>Copyright 2006 James Lawrence, All Rights Reserved.</p>
 *
 * @author James Lawrence
 */
public interface jIRCListener {
    public void onConnect();
    public void onDisconnect();
    public void onServerResponse(int code, String response);
    public void onMessage(String channel, String sender, String login, String hostname, String message);
    public void onPrivateMessage(String sender, String login, String hostname, String message);
    public void onAction(String sender, String login, String hostname, String target, String action);
    public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice);
    public void onJoin(String channel, String sender, String login, String hostname);
    public void onPart(String channel, String sender, String login, String hostname);
    public void onNickChange(String oldNick, String login, String hostname, String newNick);
    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason);
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason);
    public void onChannelInfo(String channel, int userCount, String topic);
    public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode);
    public void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode);
    public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient);
    public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient);
    public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient);
    public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient);
    public void onSetChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key);
    public void onRemoveChannelKey(String channel, String sourceNick, String sourceLogin, String sourceHostname, String key);
    public void onSetChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname, int limit);
    public void onRemoveChannelLimit(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask);
    public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask);
    public void onSetTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onRemoveTopicProtection(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onSetNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onRemoveNoExternalMessages(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onSetInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onRemoveInviteOnly(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onSetModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onRemoveModerated(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onSetPrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onRemovePrivate(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onSetSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onRemoveSecret(String channel, String sourceNick, String sourceLogin, String sourceHostname);
    public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel);
    public void onIncomingFileTransfer(DccFileTransfer transfer);
    public void onFileTransferFinished(DccFileTransfer transfer, Exception e);
    public void onIncomingChatRequest(DccChat chat);
    public void onVersion(String sourceNick, String sourceLogin, String sourceHostname, String target);
    public void onPing(String sourceNick, String sourceLogin, String sourceHostname, String target, String pingValue);
    public void onServerPing(String response);
    public void onTime(String sourceNick, String sourceLogin, String sourceHostname, String target);
    public void onFinger(String sourceNick, String sourceLogin, String sourceHostname, String target);
}
