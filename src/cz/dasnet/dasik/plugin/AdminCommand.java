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

import java.util.List;
import cz.dasnet.dasik.Dasik;
import cz.dasnet.dasik.config.ConfigException;
import cz.dasnet.dasik.entities.ChannelConfig;
import cz.dasnet.dasik.entities.User;
import org.apache.log4j.Logger;
import static cz.dasnet.dasik.util.CollectionUtils.newArrayList;

/**
 * Class implementing basic admin commands like join/part/quit and so on.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class AdminCommand extends Plugin {

    private static Logger log = Logger.getLogger(AdminCommand.class);

    @Override
    public void loaded(Dasik bot) {
        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        log.debug("Unloaded");
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        String command = bot.getCommand(message, true);
        if (command == null) {
            return;
        }

        String[] token = message.split("\\s+");

        if (bot.isMasterAuthorised(sender)) {
            if ("quit".equals(command)) {
                if (token.length >= 2) {
                    if (!bot.getNick().equalsIgnoreCase(token[1])) {
                        return;
                    }
                }
                bot.quitServer("Leaving!");
                bot.close();
            } else if ("join".equals(command)) {
                for (int i = 1; i < token.length; i++) {
                    bot.joinChannel(token[i]);
                }
            } else if ("part".equals(command)) {
                for (int i = 1; i < token.length; i++) {
                    bot.partChannel(token[i]);
                }
            } else if ("nick".equals(command) && token.length >= 2) {
                bot.changeNick(token[1]);
            } else if ("o".equals(command)) {
                int i = 1;
                if (channel.equals(sender)) {
                    channel = token[1];
                    i++;
                }
                for (; i < token.length; i++) {
                    bot.op(channel, token[i]);
                }
            } else if ("do".equals(command)) {
                int i = 1;
                if (channel.equals(sender)) {
                    channel = token[1];
                    i++;
                }
                for (; i < token.length; i++) {
                    bot.deOp(channel, token[i]);
                }
            } else if ("v".equals(command)) {
                int i = 1;
                if (channel.equals(sender)) {
                    channel = token[1];
                    i++;
                }
                for (; i < token.length; i++) {
                    bot.voice(channel, token[i]);
                }
            } else if ("dv".equals(command)) {
                int i = 1;
                if (channel.equals(sender)) {
                    channel = token[1];
                    i++;
                }
                for (; i < token.length; i++) {
                    bot.deVoice(channel, token[i]);
                }
            } else if ("config".equals(command) && token.length >= 2) {
                String subcommand = token[1];
                if ("reload".equals(subcommand)) {
                    try {
                        bot.getConfig().reload();
                        bot.send(channel, "Config reloaded");
                    } catch (ConfigException ex) {
                        bot.send(channel, "Failed to reload config.");
                        log.error("Failed to reload config", ex);
                    }
                }
            } else if ("auth".equals(command)) {
                bot.send(channel, bot.nickToAuth(token[1]));
            } else if ("whois".equals(command) && token.length >= 2) {
                String mask = bot.nickToMask(token[1]);
                if (mask == null) {
                    mask = token[1];
                }
                User u = bot.getUserDao().find(mask);
                if (u == null) {
                    bot.send(channel, "User not found");
                    return;
                }

                String nick = bot.maskToNick(mask) != null ? " (" + bot.maskToNick(mask) + ")" : "";
                bot.send(channel, "[User " + u.getMask() + nick + "] Access: " + u.getAccess()
                        + " | Total words: " + u.getWords() + " | Today's words: " + u.getWordsDaily());
            } else if ("merge".equals(command) && token.length >= 3) {
                String mask = bot.nickToMask(token[1]);
                if (mask == null) {
                    mask = token[1];
                }

                User u = bot.getUserDao().find(mask);
                if (u == null) {
                    bot.send(channel, "User " + mask + " not found");
                    return;
                }

                List<User> users = newArrayList();
                for (int i = 2; i < token.length; i++) {
                    String mmask = bot.nickToMask(token[i]);
                    if (mmask == null) {
                        mmask = token[i];
                    }
                    User uu = bot.getUserDao().find(mmask);
                    if (uu != null) {
                        users.add(uu);
                    }
                }

                for (User uu : users) {
                    u.incWords(uu.getWords());
                    u.incWordsDaily(uu.getWordsDaily());
                    u.setAccess(Math.max(u.getAccess(), uu.getAccess()));
                }

                for (User uu : users) {
                    bot.getUserDao().removeUser(uu);
                }

                bot.getUserDao().updateUser(u);
                String nick = bot.maskToNick(mask) != null ? " (" + bot.maskToNick(mask) + ")" : "";
                bot.send(channel, "*** The merging is complete ***");
                bot.send(channel, "[User " + u.getMask() + nick + "] Access: " + u.getAccess()
                        + " | Total words: " + u.getWords() + " | Today's words: " + u.getWordsDaily());
            } else if ("ignore".equals(command) && token.length >= 2) {
                String mask = bot.nickToMask(token[1]);
                if (mask == null) {
                    mask = token[1];
                }

                bot.getIgnoredHosts().add(mask.toLowerCase());
                bot.send(channel, "Ignoring " + mask + "!");
            } else if ("unignore".equals(command) && token.length >= 2) {
                String mask = bot.nickToMask(token[1]);
                if (mask == null) {
                    mask = token[1];
                }

                if (bot.getIgnoredHosts().remove(mask.toLowerCase())) {
                    bot.send(channel, "Unignoring " + mask + "!");
                }
            } else if ("chaninfo".equals(command)) {
                String chan = channel;
                if (token.length >= 2) {
                    chan = token[1];
                }
                ChannelConfig c = bot.getChannelConfigs().get(chan);
                if (c == null) {
                    bot.send(channel, "Channel not found");
                    return;
                }

                bot.send(channel, "Ignoring plugins: " + c.getIgnoredPlugins());
            } else if ("say".equals(command)) {
                if (token.length < 3) {
                    return;
                }
                String msg = message.split("\\s+", 3)[2];
                bot.send(token[1], msg);
            }
        }
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message, Dasik bot) {
        onMessage(sender, sender, login, hostname, message, bot);
    }
}
