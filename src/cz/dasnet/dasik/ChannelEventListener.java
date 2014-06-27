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
 * Interface specifying various types of channel events.
 * 
 * @author Matus Goljer
 * @version 1.0
 */
public interface ChannelEventListener {

    void onAction(String sender, String login, String hostname, String target, String action, Dasik bot);

    void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot);

    void onPrivateMessage(String sender, String login, String hostname, String message, Dasik bot);

    void onJoin(String channel, String sender, String login, String hostname, Dasik bot);

    void onPart(String channel, String sender, String login, String hostname, Dasik bot);

    void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason, Dasik bot);

    void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason, Dasik bot);

    void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode, Dasik bot);

    void onTopic(String channel, String topic, String setBy, long date, boolean changed, Dasik bot);

    void onNickChange(String oldNick, String login, String hostname, String newNick, Dasik bot);

    void onUserAuth(String userNick, Dasik bot);

    void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot);

    void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot);

    void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot);

    void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient, Dasik bot);

    void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask, Dasik bot);

    void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask, Dasik bot);
}
