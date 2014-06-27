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

import cz.dasnet.dasik.util.Pair;
import java.util.ArrayList;
import cz.dasnet.dasik.entities.Channel;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.codehaus.groovy.control.CompilationFailedException;
import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;
import java.util.Random;
import cz.dasnet.dasik.Dasik;
import cz.dasnet.dasik.entities.Learn;
import cz.dasnet.dasik.entities.User;
import java.security.AccessControlException;
import java.util.List;
import org.apache.log4j.Logger;
import static cz.dasnet.dasik.util.StringUtils.longestCommonPrefix;
import static cz.dasnet.dasik.util.ArrayUtils.contains;

/**
 * Learn plugin.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class LearnPlugin extends Plugin {

    private static Logger log = Logger.getLogger(LearnPlugin.class);
    private Random r;
    private SimpleDateFormat f;
    private String[] modes;

    @Override
    public void loaded(Dasik bot) {
        f = new SimpleDateFormat();
        f.applyPattern("yyyy/MM/dd HH:mm:ss");
        r = new Random();
        modes = new String[]{"j", "p", "q"};
        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        log.debug("Unloaded");
    }

    private Learn findTrigger(String msg, String type, Dasik bot) {
        String trig = msg.split("\\s+")[0].toLowerCase().replaceAll("\\\\", "\\\\\\\\");
        msg = msg.replaceAll("\\s+", "_");

        List<Learn> learns = bot.getLearnDao().findAll(trig, type);

        Learn max = null;
        int maxLen = 0;
        for (Learn l : learns) {
            String maxP = longestCommonPrefix(msg, l.getTrigger());

            if (maxP.endsWith("_")) {
                maxP = maxP.substring(0, maxP.length() - 1);
            }

            if (maxP.equalsIgnoreCase(l.getTrigger())) {
                if (maxP.length() > maxLen) {
                    max = l;
                    maxLen = maxP.length();
                }
            }
        }

        return max;
    }

    private String evaluate(String channel, String nick, String message, Learn learn, Dasik bot) {
        String[] token = {""};
        String reply = learn.getReply();

        if (message != null) {
            token = message.split("\\s+");
        }

        if (learn.getAccess() >= 70) {
            Binding binding = new Binding();
            binding.setVariable("r", r);
            binding.setVariable("nick", nick);
            binding.setVariable("auth", bot.nickToAuth(nick));
            binding.setVariable("mask", bot.nickToMask(nick));
            binding.setVariable("chan", channel);
            binding.setVariable("t", message);
            binding.setVariable("t0", token.length);

            Channel c = bot.getActiveChannels().get(channel);
            binding.setVariable("nicklist", new ArrayList<String>(c.getUsers().keySet()));

            for (int i = 0; i < token.length; i++) {
                binding.setVariable("t" + new Integer(i + 1).toString(), token[i]);
            }
            GroovyShell shell = new GroovyShell(binding);
            //length 205
            String baseCode = "def _b = { binding[it] };"
                    + " def _t = { sp = it.split(\"-\", -1); re = \"\";"
                    + " if (sp.length == 1) { _b(\"t\" + sp[0]) }"
                    + " else { (sp[0]..sp[1]).each{re += \"${_b[\"t\" + it]}${(it!=sp[1])?\" \":\"\"}\"}; re;}}; ";

            GroovyCodeSource source = new GroovyCodeSource(baseCode + reply, "UntrustedScript", "/restrictedClient");

            Object value = "org.codehaus.groovy.control.CompilationFailedException";
            try {
                value = shell.evaluate(source);
            } catch (CompilationFailedException ex) {
                value = ex.getClass().getName() + ": " + ex.getMessage().replaceAll("\\s+", " ");
                log.error("Learn trigger compilation failed", ex);
            } catch (AccessControlException ex) {
                value = ex.getClass().getName() + ": " + ex.getMessage().replaceAll("\\s+", " ");
                log.error("Access denied", ex);
            }

            return value.toString();
        } else {
            if (reply.startsWith("\"") && reply.endsWith("\"")) {
                return reply.substring(1, reply.length() - 1);
            } else {
                return reply;
            }
        }
    }

    private Pair<String, String> getModeAndTrig(String command, String baseCommand, String[] token, Dasik bot) {
        String mode = null;
        String trig = null;
        int comLen = command.length();
        int bComLen = baseCommand.length();

        if (baseCommand.equals(command)) {
            mode = "m";
            trig = token[1];
        } else if (comLen == bComLen + 1) {
            mode = command.substring(comLen - 1, comLen);
            if (contains(modes, mode)) {
                String mask = bot.nickToMask(token[1]);
                trig = (mask == null) ? token[1] : mask;
            } else {
                mode = null;
            }
        }

        return Pair.pairOf(mode, trig);
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        User u = bot.getUserDao().find(login + "@" + hostname);
        if (u == null) {
            return;
        }

        boolean privMsg = sender.equals(channel);

        // process commands
        String command = bot.getCommand(message);
        String[] token = message.trim().split("\\s+", 3);
        if (command != null) {
            if (!privMsg && command.startsWith("learn") && u.getAccess() >= 10) {
                Pair<String, String> modeAndTrig = getModeAndTrig(command, "learn", token, bot);
                String mode = modeAndTrig.fst();
                String trig = modeAndTrig.snd();

                if (mode == null) {
                    return;
                }

                if (token.length == 3) {
                    trig = trig.toLowerCase();
                    Learn old = bot.getLearnDao().find(trig, mode);
                    if (old == null) {
                        Learn l = new Learn(trig, token[2], mode, channel, u.getId());
                        bot.getLearnDao().addLearn(l);
                    } else {
                        old.setReply(token[2]);
                        old.setUserId(u.getId());
                        old.setChannel(channel);
                        old.setCreated(System.currentTimeMillis());
                        bot.getLearnDao().updateLearn(old);
                    }
                    return;
                }
            } else if (command.startsWith("look") && u.getAccess() >= 10) {
                Pair<String, String> modeAndTrig = getModeAndTrig(command, "look", token, bot);
                String mode = modeAndTrig.fst();
                String trig = modeAndTrig.snd();

                if (mode == null) {
                    return;
                }

                if (token.length == 2) {
                    trig = trig.toLowerCase();
                    Learn learn = bot.getLearnDao().find(trig, mode);
                    String author = learn.getMask();
                    if (bot.maskToNick(author) != null) {
                        author = bot.maskToNick(author);
                    } else {
                        author = author.split("@")[0];
                    }
                    bot.send(channel, learn.getTrigger() + " (by " + author + ", " + f.format(new Date(learn.getCreated()))
                            + "): " + learn.getReply());
                    return;
                }
            } else if (!privMsg && command.startsWith("unlearn") && u.getAccess() >= 10) {
                Pair<String, String> modeAndTrig = getModeAndTrig(command, "unlearn", token, bot);
                String mode = modeAndTrig.fst();
                String trig = modeAndTrig.snd();

                if (mode == null) {
                    return;
                }

                if (token.length == 2) {
                    trig = trig.toLowerCase();
                    bot.getLearnDao().removeLearn(trig, mode);
                    bot.describe(channel, "Trigger " + trig + "[" + mode + "] removed");
                    return;
                }
            }
        }

        Learn learn = findTrigger(message, "m", bot);

        if (learn == null) {
            return;
        }

        bot.send(channel, evaluate(channel, sender, message, learn, bot));
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message, Dasik bot) {
        onMessage(sender, sender, login, hostname, message, bot);
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname, Dasik bot) {
        Learn learn = findTrigger(login + "@" + hostname, "j", bot);
        if (learn == null) {
            return;
        }

        if (learn.getChannel().equals(channel)) {
            bot.send(channel, evaluate(channel, sender, null, learn, bot));
        }
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname, Dasik bot) {
        Learn learn = findTrigger(login + "@" + hostname, "p", bot);
        if (learn == null) {
            return;
        }

        if (learn.getChannel().equals(channel)) {
            bot.send(channel, evaluate(channel, sender, null, learn, bot));
        }
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason, Dasik bot) {
        Learn learn = findTrigger(sourceLogin + "@" + sourceHostname, "q", bot);
        if (learn == null) {
            return;
        }
        bot.send(learn.getChannel(), evaluate(learn.getChannel(), sourceNick, reason, learn, bot));
    }
}
