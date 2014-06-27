/*
 *  Copyright (C) 2011 Matus Goljer
 *  This file is a part of Project DASik, an IRC bot.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.dasnet.dasik;

/**
 * Abstract implementation of {@code ChannelEventListener} interface.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public abstract class ChannelEventAdapter implements ChannelEventListener {

    public void onAction(String sender, String login, String hostname, String target, String action, Dasik bot) {
    }

    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
    }

    public void onPrivateMessage(String sender, String login, String hostname, String message, Dasik bot) {
    }

    public void onJoin(String channel, String sender, String login, String hostname, Dasik bot) {
    }

    public void onPart(String channel, String sender, String login, String hostname, Dasik bot) {
    }

    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason, Dasik bot) {
    }

    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason, Dasik bot) {
    }

    public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode, Dasik bot) {
    }

    public void onTopic(String channel, String topic, String setBy, long date, boolean changed, Dasik bot) {
    }

    public void onNickChange(String oldNick, String login, String hostname, String newNick, Dasik bot) {
    }

    public void onUserAuth(String userNick, Dasik bot) {
    }

    public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot) {
    }

    public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot) {
    }

    public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot) {
    }

    public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot) {
    }

    public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask, Dasik bot) {
    }

    public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask, Dasik bot) {
    }
}
