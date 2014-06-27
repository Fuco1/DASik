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
package cz.dasnet.dasik.plugin;

import cz.dasnet.dasik.Dasik;
import cz.dasnet.dasik.dao.UserDaoImpl;
import cz.dasnet.dasik.entities.User;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import org.jibble.pircbot.Colors;
import static cz.dasnet.dasik.util.StringUtils.shorten;

/**
 * Plugin tracking the last action of a user.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class LastAction extends Plugin {

    private static Logger log = Logger.getLogger(LastAction.class);
    private SimpleDateFormat f;

    @Override
    public void loaded(Dasik bot) {
        f = new SimpleDateFormat();
        f.applyPattern("[yyyy/MM/dd HH:mm:ss]");
        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        log.debug("Unloaded");
    }

    private User getUser(String mask, Dasik bot) {
        User u = bot.getUserDao().find(mask);
        if (u == null) {
            u = new User(mask, 10);
            bot.getUserDao().addUser(u);
        }
        return u;
    }

    @Override
    public void onAction(String sender, String login, String hostname, String target, String action, Dasik bot) {
        if (target.startsWith("#")) {
            onMessage(target, sender, login, hostname, action, bot);
        }
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        String command = bot.getCommand(message);
        if (command != null) {
            String[] token = message.split("\\s+");
            if ("seen".equals(command) && token.length >= 2) {
                String sql = "SELECT DISTINCT * FROM users WHERE lastseen_nick like ? ORDER BY lastseen DESC";
                List<User> users = bot.getUserDao().query(sql, UserDaoImpl.userRowMapper,
                        "%" + token[1] + "%");

                if (users.isEmpty()) {
                    bot.send(channel, "Never seen this nick");
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (User u : users) {
                    sb.append(u.getLastseenNick()).append(" on ").
                            append(f.format(new Date(u.getLastseen()))).append(" ").
                            append(u.getLastseenAction()).append(" | ");
                }

                if (users.size() >= 1) {
                    sb = sb.delete(sb.length() - 3, sb.length());
                }

                String msg = Colors.removeFormattingAndColors(shorten(sb.toString(), 350));
                bot.send(channel, msg);
                return;
            }
        }

        User u = getUser(login + "@" + hostname, bot);
        u.setLastseen(System.currentTimeMillis());
        u.setLastseenAction("posting on " + channel + ": " + shorten(Colors.removeFormattingAndColors(message), 60));
        u.setLastseenNick(sender);
        bot.getUserDao().updateUser(u);
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname, Dasik bot) {
        User u = getUser(login + "@" + hostname, bot);
        u.setLastseen(System.currentTimeMillis());
        u.setLastseenAction("joining " + channel);
        u.setLastseenNick(sender);
        bot.getUserDao().updateUser(u);
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname, Dasik bot) {
        User u = getUser(login + "@" + hostname, bot);
        u.setLastseen(System.currentTimeMillis());
        u.setLastseenAction("parting " + channel);
        u.setLastseenNick(sender);
        bot.getUserDao().updateUser(u);
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason, Dasik bot) {
        User u = getUser(sourceLogin + "@" + sourceHostname, bot);
        u.setLastseen(System.currentTimeMillis());
        u.setLastseenAction("quitting (" + shorten(Colors.removeFormattingAndColors(reason), 60) + ")");
        u.setLastseenNick(sourceNick);
        bot.getUserDao().updateUser(u);
    }

    @Override
    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason, Dasik bot) {
        User u = getUser(bot.nickToMask(recipientNick), bot);
        u.setLastseen(System.currentTimeMillis());
        u.setLastseenAction("being kicked from " + channel + " by " + kickerNick
                + " (" + shorten(Colors.removeFormattingAndColors(reason), 60) + ")");
        u.setLastseenNick(recipientNick);
        bot.getUserDao().updateUser(u);
    }
}
