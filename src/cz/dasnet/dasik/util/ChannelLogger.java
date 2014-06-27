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
package cz.dasnet.dasik.util;

import cz.dasnet.dasik.entities.Channel;
import org.apache.log4j.Logger;
import java.util.Arrays;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jibble.pircbot.Colors;
import cz.dasnet.dasik.Dasik;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import cz.dasnet.dasik.ChannelEventAdapter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.jibble.pircbot.User;
import static cz.dasnet.dasik.util.CollectionUtils.newHashMap;
import static cz.dasnet.dasik.util.CollectionUtils.any;

/**
 * Class providing methods for logging events on channels
 * 
 * @author Matus Goljer
 * @version 1.0
 */
public class ChannelLogger extends ChannelEventAdapter {

    private static Logger log = Logger.getLogger(ChannelLogger.class);
    /** Base path where channel logs are stored */
    private String basePath;
    private Map<String, PrintWriter> logs = newHashMap();
    private SimpleDateFormat f = new SimpleDateFormat();

    public ChannelLogger(String basePath) {
        this.basePath = basePath;
        f.applyPattern("[yyyy/MM/dd HH:mm:ss]");
    }

    private void checkWriter(String target) {
        if (!logs.containsKey(target)) {
            try {
                String file = basePath + System.getProperty("file.separator") + target + ".log";
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                        new OutputStreamWriter(
                        new FileOutputStream(file, true), "UTF-8")));

                out.println("--- Session opened: " + f.format(new Date()));
                out.flush();
                logs.put(target, out);
            } catch (FileNotFoundException ex) {
            } catch (UnsupportedEncodingException ex) {
            }
        }
    }

    public void close() {
        for (PrintWriter out : logs.values()) {
            try {
                out.println("--- Session closed: " + f.format(new Date()));
                out.flush();
                out.close();
            } catch (Exception ex) {
            }
        }
        logs.clear();
    }

    @Override
    public void onAction(String sender, String login, String hostname, String target, String action, Dasik bot) {
        String source;
        if (target.equals(bot.getNick())) {
            source = sender;
        } else {
            source = target;
        }
        checkWriter(source);

        action = Colors.removeFormattingAndColors(action);
        PrintWriter out = logs.get(source);
        out.println(getTimeStamp() + " --> " + sender + " " + action);
        out.flush();
    }

    public void onSentAction(String target, String action, Dasik bot) {
        checkWriter(target);

        action = Colors.removeFormattingAndColors(action);
        PrintWriter out = logs.get(target);
        out.println(getTimeStamp() + " --> " + bot.getNick() + " " + action);
        out.flush();
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        checkWriter(channel);

        message = Colors.removeFormattingAndColors(message);
        PrintWriter out = logs.get(channel);
        out.println(getTimeStamp() + " <" + sender + "> " + message);
        out.flush();
    }

    public void onSentMessage(String channel, String message, Dasik bot) {
        checkWriter(channel);

        message = Colors.removeFormattingAndColors(message);
        PrintWriter out = logs.get(channel);
        out.println(getTimeStamp() + " <" + bot.getNick() + "> " + message);
        out.flush();
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message, Dasik bot) {
        checkWriter(sender);

        message = Colors.removeFormattingAndColors(message);
        PrintWriter out = logs.get(sender);
        out.println(getTimeStamp() + " <" + sender + "> " + message);
        out.flush();
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname, Dasik bot) {
        checkWriter(channel);

        PrintWriter out = logs.get(channel);
        out.println(getTimeStamp() + " --> " + sender + " (" + login + "@" + hostname + ") has joined " + channel);
        out.flush();

        out = logs.get(sender);
        if (out != null) {
            out.println(getTimeStamp() + " --> " + sender + " (" + login + "@" + hostname + ") has joined " + channel);
            out.flush();
        }
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname, Dasik bot) {
        checkWriter(channel);

        PrintWriter out = logs.get(channel);
        out.println(getTimeStamp() + " <-- " + sender + " (" + login + "@" + hostname + ") has left " + channel);
        out.flush();

        out = logs.get(sender);
        if (out != null) {
            out.println(getTimeStamp() + " <-- " + sender + " (" + login + "@" + hostname + ") has left " + channel);
            out.flush();
        }
    }

    @Override
    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason, Dasik bot) {
        checkWriter(channel);

        PrintWriter out = logs.get(channel);
        out.println(getTimeStamp() + " <-- " + recipientNick + " was kicked from "
                + channel + " by " + kickerNick + " (" + reason + ")");
        out.flush();

        out = logs.get(recipientNick);
        if (out != null) {
            out.println(getTimeStamp() + " <-- " + recipientNick + " was kicked from "
                    + channel + " by " + kickerNick + " (" + reason + ")");
            out.flush();
        }
    }

    @Override
    public void onQuit(final String sourceNick, String sourceLogin, String sourceHostname, String reason, Dasik bot) {
        for (Channel c : bot.getActiveChannels().values()) {
            if (any(c.getUsers().keySet(), new Functions.Predicate<String>() {

                public boolean filter(String item) {
                    return item.equalsIgnoreCase(sourceNick);
                }
            })) {
                PrintWriter out = logs.get(c.getName());
                out.println(getTimeStamp() + " <-- " + sourceNick + " (" + sourceLogin + "@" + sourceHostname + ") has quit (" + reason + ")");
                out.flush();
            }
        }
        PrintWriter out = logs.get(sourceNick);
        if (out != null) {
            out.println(getTimeStamp() + " <-- " + sourceNick + " (" + sourceLogin + "@" + sourceHostname + ") has quit (" + reason + ")");
            out.flush();
            out.close();
            logs.remove(sourceNick);
        }
    }

    @Override
    public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode, Dasik bot) {
        checkWriter(channel);

        PrintWriter out = logs.get(channel);
        out.println(getTimeStamp() + " --> " + sourceNick + " sets mode: " + mode);
        out.flush();
    }

    @Override
    public void onTopic(String channel, String topic, String setBy, long date, boolean changed, Dasik bot) {
        checkWriter(channel);

        PrintWriter out = logs.get(channel);
        if (changed) {
            out.println(getTimeStamp() + " --> " + setBy + " changed the topic of " + channel + " to: " + topic);
        } else {
            out.println(getTimeStamp() + " --> Topic of " + channel + " is: " + topic);
        }
        out.flush();
    }

    @Override
    public void onNickChange(final String oldNick, String login, String hostname, final String newNick, Dasik bot) {
        if (logs.containsKey(oldNick)) {
            logs.put(newNick, logs.remove(oldNick));
        }

        for (String s : logs.keySet()) {
            if (s.startsWith("#")) {
                if (any(Arrays.asList(bot.getUsers(s)), new Functions.Predicate<User>() {

                    public boolean filter(User item) {
                        return item.getNick().equals(oldNick) || item.getNick().equals(newNick);
                    }
                })) {
                    PrintWriter out = logs.get(s);
                    out.println(getTimeStamp() + " --> " + oldNick + " is now known as " + newNick);
                    out.flush();
                }
            }
        }

        PrintWriter out = logs.get(newNick);
        if (out != null) {
            out.println(getTimeStamp() + " --> " + oldNick + " is now known as " + newNick);
            out.flush();
        }
    }

    private String getTimeStamp() {
        return f.format(new Date());
    }
}
