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

import cz.dasnet.dasik.util.ArrayUtils;
import java.util.EnumSet;
import java.io.FileWriter;
import org.dom4j.io.XMLWriter;
import java.util.Date;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import cz.dasnet.dasik.entities.Channel;
import cz.dasnet.dasik.dao.LearnDaoImpl;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import cz.dasnet.dasik.dao.UserDao;
import org.jibble.pircbot.User;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import cz.dasnet.dasik.plugin.Plugin;
import java.util.List;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import cz.dasnet.dasik.config.Config;
import cz.dasnet.dasik.config.ConfigException;
import cz.dasnet.dasik.dao.LearnDao;
import cz.dasnet.dasik.dao.UserDaoImpl;
import cz.dasnet.dasik.entities.ChannelConfig;
import cz.dasnet.dasik.entities.ChannelUser;
import cz.dasnet.dasik.util.ChannelLogger;
import cz.dasnet.dasik.util.Functions;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import static cz.dasnet.dasik.util.CollectionUtils.newHashMap;
import static cz.dasnet.dasik.util.CollectionUtils.newHashSet;
import static cz.dasnet.dasik.util.CollectionUtils.newArrayList;
import static cz.dasnet.dasik.util.CollectionUtils.any;

/**
 * Main class representing a bot instance.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Dasik extends PircBot {

    private static Logger log = Logger.getLogger(Dasik.class);
    private Config config;
    private DataSource dataSource;
    private UserDao userDao;
    private LearnDao learnDao;
    private Map<String, String> channels = newHashMap();
    private Map<String, String> nickToAuth = newHashMap();
    private Map<String, String> authToNick = newHashMap();
    private Map<String, String> nickToMask = newHashMap();
    private Map<String, String> maskToNick = newHashMap();
    private Map<String, ChannelEventListener> channelListeners = new LinkedHashMap<String, ChannelEventListener>();
    private Map<String, Channel> activeChannels = newHashMap();
    private Map<String, ChannelConfig> channelConfigs = newHashMap();
    private Set<String> plugins = newHashSet();
    private ChannelLogger chanlog;
    private PluginLoader pluginLoader;
    private boolean authed = false;
    private Set<String> ignoredHosts = newHashSet();
    private boolean masterCommandHandled = false;

    public Dasik() throws ConfigException {
        log.info("");
        log.info("Creating new bot instance ...");
        this.config = new Config();

        log.info("Setting up datasource ...");
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(config.getProperty("datasource.driver", "com.mysql.jdbc.Driver"));
        ds.setUrl(config.getProperty("datasource.connection", "jdbc:mysql://localhost:3306/dasik"));
        ds.setUsername(config.getProperty("datasource.username", "root"));
        ds.setPassword(config.getProperty("datasource.password", "root"));
        dataSource = ds;

        log.info("Creating User DAO ...");
        userDao = new UserDaoImpl(dataSource);

        log.info("Creating Learn DAO ...");
        learnDao = new LearnDaoImpl(dataSource);

        log.info("Loading channel list ...");
        String channs = config.getProperty("irc.channel.autojoin");
        if (channs != null) {
            for (String chan : channs.split("\\s+")) {
                String pass = config.getProperty("irc.channel.pass." + chan, "");
                channels.put(chan, pass);
            }
        }

        log.info("Initializing logger ...");
        chanlog = new ChannelLogger(config.getProperty("log.basepath", "logs"));
        channelListeners.put("@log", chanlog);

        log.info("Creating plugin loader ...");
        pluginLoader = new PluginLoader(this);

        log.info("Loading plugins ...");
        String commands = config.getProperty("command.autoload");
        if (commands != null) {
            plugins.addAll(Arrays.asList(commands.split("\\s+")));
        }

        Map<String, Plugin> loadedPlugins = pluginLoader.loadPlugins(plugins);
        channelListeners.putAll(loadedPlugins);

        log.info("Loading channel configs ...");
        for (String plugin : plugins) {
            String ignore = config.getProperty("command.ignoreOn." + plugin);
            if (ignore != null) {
                String[] chans = ignore.split("\\s+");
                for (String chan : chans) {
                    ChannelConfig c = channelConfigs.get(chan);
                    if (c == null) {
                        c = new ChannelConfig(chan);
                        channelConfigs.put(chan, c);
                    }
                    c.ignorePlugin(plugin);
                }
            }
        }

        log.info("Bot created");
    }

    public static void main(String[] args) throws ConfigException, UnsupportedEncodingException {
        Dasik bot = new Dasik();

        bot.setName(bot.getConfig().getProperty("irc.name", "DASik"));
        bot.setLogin(bot.getConfig().getProperty("irc.name", "DASik"));
        bot.setFinger(bot.getConfig().getProperty("irc.name", "DASik"));
        bot.setAutoNickChange(true);
        bot.setVerbose(true);
        bot.setEncoding("UTF-8");

        try {
            //bot.startIdentServer();
            bot.connect(bot.getConfig().getProperty("irc.server", "irc.quakenet.org"),
                    bot.getConfig().getPropertyInt("irc.port", 6667));
        } catch (IrcException ex) {
            log.error("Unable to connect to server", ex);
        } catch (IOException ex) {
            log.error("Unable to connect to server", ex);
        }
    }

    public void close() {
        close(true);
    }

    public void close(boolean exit) {
        chanlog.close();
        if (exit) {
            for (ChannelEventListener cl : channelListeners.values()) {
                if (cl instanceof Plugin) {
                    Plugin plugin = (Plugin) cl;
                    plugin.unloaded(this);
                }
            }
            log.info("Session closed");
            System.exit(0);
        }
        log.info("Session closed");
    }

    @Override
    protected void onConnect() {
        log.info("Connected on " + getServer());
        auth();
        if (authed) {
            requestInvites();
        }
        final Dasik bot = this;
        try {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    bot.joinChannels();
                }
            }, 1000);

            TimerTask dumpTask = new TimerTask() {

                @Override
                public void run() {
                    Document document = DocumentHelper.createDocument();
                    Element channelinfo = document.addElement("channelinfo");

                    for (String c : activeChannels.keySet()) {
                        Element channel = channelinfo.addElement("channel");
                        Element name = channel.addElement("name");
                        name.setText(c);
                        Element size = channel.addElement("size");
                        size.setText("" + getUsers(c).length);
                    }

                    Element updatetime = channelinfo.addElement("updatetime");
                    updatetime.setText(new Long(new Date().getTime() / 1000).toString());

                    try {
                        XMLWriter writer = new XMLWriter(new FileWriter("channelinfo.xml"));
                        writer.write(document);
                        writer.close();
                    } catch (IOException ex) {
                        log.error("Unable to dump channel info", ex);
                    }
                }
            };

            Timer dump = new Timer("dump", true);
            dump.schedule(dumpTask, 10000, 60000);
        } catch (Exception ex) {
            this.joinChannels();
            log.error("Channel autojoin timer failed to schedule the task.", ex);
        }
    }

    @Override
    public void onDisconnect() {
        activeChannels.clear();
        while (!isConnected()) {
            try {
                Thread.sleep(10000);
                reconnect();
            } catch (Exception ex) {
                log.error("An error occured while reconnecting", ex);
            }
        }
    }

    private void auth() {
        String target = config.getProperty("irc.auth.service");
        String msg = config.getProperty("irc.auth.message");
        if (target != null && msg != null) {
            sendMessage(target, msg);
        }
        log.debug("Auth request sent ...");
        setMode(getNick(), "+x");
    }

    private void joinChannels() {
        for (Map.Entry<String, String> channel : channels.entrySet()) {
            if (channel.getValue().isEmpty()) {
                joinChannel(channel.getKey());
            } else {
                joinChannel(channel.getKey(), channel.getValue());
            }
            log.debug("Channel join request sent: " + channel.getKey() + " ...");
        }
    }

    private void requestInvites() {
        send("q", "invite");
        log.debug("Channel invite request sent");
    }

    private void removeNickFromIAL(String nick) {
        removeNickFromIAL(nick, false);
    }

    private void removeNickFromIAL(String nick, boolean quit) {
        String auth = nickToAuth(nick);
        String mask = nickToMask(nick);

        boolean noCommonChannel = true;
        for (Channel c : activeChannels.values()) {
            if (c.getUsers().containsKey(nick)) {
                noCommonChannel = false;
                break;
            }
        }

        if (quit || noCommonChannel) {
            nickToAuth.remove(nick.toLowerCase());
            nickToMask.remove(nick.toLowerCase());
            maskToNick.remove(mask);
            authToNick.remove(auth);
        }
    }

    private void sendWhoOnUser(String nick) {
        if (!nickToAuth.containsKey(nick)) {
            sendRawLine("WHO " + nick + " n%nahu");
            log.debug("WHO user request sent for " + nick + " ...");
        } else {
            log.debug("WHO user request ignored for " + nick + ", nick already in database!");
        }
    }

    private void sendWhoOnChannel(String channel) {
        sendRawLine("WHO " + channel + " %nahu");
        log.debug("WHO channel request sent on " + channel + " ...");
    }

    @Override
    protected void onServerResponse(int code, String response) {
        String[] tokens = response.split("\\s+");

        if (code == 354) {
            //:servercentral.il.us.quakenet.org 354 DASik2 ~DASik ircnavod.users.quakenet.org DASik2 ircnavod
            String user = tokens[1];
            String host = tokens[2];
            String nick = tokens[3];
            String auth = tokens[4];
            String fullhost = user + "@" + host;
            if (!authed && auth.equalsIgnoreCase(config.getProperty("irc.auth.account"))) {
                authed = true;
                boolean requestInvite = config.getPropertyBool("irc.autoInvite", false);
                if (requestInvite) {
                    requestInvites();
                }
            }

            if (!auth.equals("0")) {
                nickToAuth.put(nick.toLowerCase(), auth.toLowerCase());
                authToNick.put(auth.toLowerCase(), nick);
                onUserAuth(nick);
            }

            nickToMask.put(nick.toLowerCase(), fullhost.toLowerCase());
            maskToNick.put(fullhost.toLowerCase(), nick);

            log.info("User WHO processed: nick: " + nick + " host: " + fullhost + " auth: " + auth);
        }
    }

    public void send(String target, String message) {
        sendMessage(target, message);
        chanlog.onSentMessage(target, message, this);
    }

    public void describe(String target, String message) {
        sendAction(target, message);
        chanlog.onSentAction(target, message, this);
    }

    public Config getConfig() {
        return config;
    }

    public Set<String> getIgnoredHosts() {
        return ignoredHosts;
    }

    public Map<String, ChannelConfig> getChannelConfigs() {
        return channelConfigs;
    }

    public Map<String, Channel> getActiveChannels() {
        return activeChannels;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public LearnDao getLearnDao() {
        return learnDao;
    }

    public String authToNick(String auth) {
        return authToNick.get(auth.toLowerCase());
    }

    public String maskToNick(String mask) {
        return maskToNick.get(mask.toLowerCase());
    }

    public String nickToAuth(String nick) {
        return nickToAuth.get(nick.toLowerCase());
    }

    public String nickToMask(String nick) {
        return nickToMask.get(nick.toLowerCase());
    }

    public String getCommand(String message) {
        return getCommand(message, false);
    }

    public String getCommand(String message, boolean admin) {
        String prefix;
        if (admin) {
            prefix = config.getProperty("command.adminPrefix", "@");
        } else {
            prefix = config.getProperty("command.prefix", "!");
        }

        if (prefix.contains(message.substring(0, 1))) {
            return message.split("\\s+")[0].substring(1);
        }
        return null;
    }

    public boolean isMasterAuthorised(String nick) {
        String[] master = config.getProperty("command.master").split("\\s+");
        final String mask = nickToMask(nick);

        if (any(Arrays.asList(master), new Functions.Predicate<String>() {

            public boolean filter(String item) {
                return item.equalsIgnoreCase(mask);
            }
        })) {
            return true;
        }
        return false;
    }

    public ChannelAccess getChannelAccess(String nick, String channel) {
        org.jibble.pircbot.User[] users = getUsers(channel);
        for (org.jibble.pircbot.User u : users) {
            if (u.getNick().equals(nick)) {
                if (u.isOp()) {
                    return ChannelAccess.OP;
                }
                if (u.hasVoice()) {
                    return ChannelAccess.VOICE;
                }
                return ChannelAccess.NORMAL;
            }
        }
        return ChannelAccess.NORMAL;
    }

    @Override
    protected void onInvite(String targetNick, String sourceNick, String sourceLogin,
            String sourceHostname, String channel) {
        if (sourceNick.equalsIgnoreCase("q")) {
            String[] list = channel.split(" ");
            String[] ignore = config.getProperty("irc.channel.ignore", "").split("\\s+");
            channel = list[list.length - 1];
            if (!ArrayUtils.contains(ignore, channel)) {
                joinChannel(channel);
            }
        }
    }

    public void onUserAuth(String userNick) {
        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onUserAuth(userNick, this);
        }
    }

    @Override
    public void onAction(String sender, String login, String hostname, String target, String action) {
        if (target.equalsIgnoreCase(sender)) {
            return;
        }

        if (ignoredHosts.contains(nickToMask(sender)) && !isMasterAuthorised(sender)) {
            return;
        }

        ChannelConfig c = channelConfigs.get(target);
        for (Map.Entry<String, ChannelEventListener> cl : channelListeners.entrySet()) {
            if (c == null || !c.isIgnoringPlugin(cl.getKey()) || cl.getKey().equals("AdminCommand")) {
                cl.getValue().onAction(sender, login, hostname, target, action, this);
            }
        }

//        for (ChannelEventListener cl : channelListeners.values()) {
//            cl.onAction(sender, login, hostname, target, action, this);
//        }
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        String command = getCommand(message, true);
        String[] token = message.split("\\s+");

        if (isMasterAuthorised(sender)) {
            if ("plugin".equals(command) && token.length >= 2) {
                String subcommand = token[1];
                if ("reloadall".equals(subcommand)) {
                    Map<String, Plugin> loadedPlugins = pluginLoader.loadPlugins(plugins, false);
                    for (String s : loadedPlugins.keySet()) {
                        if (channelListeners.containsKey(s)) {
                            ChannelEventListener old = channelListeners.remove(s);
                            if (old instanceof Plugin) {
                                ((Plugin) old).unloaded(this);
                            }
                        }
                        channelListeners.put(s, loadedPlugins.get(s));
                        loadedPlugins.get(s).loaded(this);
                    }
                    send(channel, loadedPlugins.size() + " of " + plugins.size() + " reloaded successfuly");
                    masterCommandHandled = true;
                    return;
                } else if ("reload".equals(subcommand) && token.length >= 3) {
                    List<String> successful = newArrayList();
                    List<String> failed = newArrayList();
                    for (int i = 2; i < token.length; i++) {
                        Plugin loadedPlugin = pluginLoader.loadPlugin(token[i], false);
                        if (loadedPlugin != null) {
                            if (channelListeners.containsKey(token[i])) {
                                ChannelEventListener old = channelListeners.remove(token[i]);
                                if (old instanceof Plugin) {
                                    ((Plugin) old).unloaded(this);
                                }
                            }
                            loadedPlugin.loaded(this);
                            channelListeners.put(token[i], loadedPlugin);
                            successful.add(token[i]);
                            plugins.add(token[i]);
                        } else {
                            failed.add(token[i]);
                        }
                    }
                    if (!successful.isEmpty()) {
                        send(channel, "Plugins " + successful + " reloaded successfuly");
                    }
                    if (!failed.isEmpty()) {
                        send(channel, "An error occured while reloading plugins: " + failed);
                    }
                    masterCommandHandled = true;
                    return;
                } else if ("ignore".equals(subcommand) && token.length >= 3) {
                    String pluginName = token[2];
                    List<String> chans = newArrayList();
                    for (int i = 3; i < token.length; i++) {
                        chans.add(token[i]);
                    }
                    if (chans.isEmpty() && !channel.equalsIgnoreCase(sender)) {
                        chans.add(channel);
                    }

                    for (String chan : chans) {
                        ChannelConfig c = channelConfigs.get(chan);
                        if (c == null) {
                            c = new ChannelConfig(chan);
                            channelConfigs.put(chan, c);
                        }
                        c.ignorePlugin(pluginName);
                        if (plugins.contains(pluginName)) {
                            send(channel, "Ignoring plugin " + pluginName + " on " + chan);
                        } else {
                            send(channel, "Ignoring plugin " + pluginName + " on " + chan
                                    + " (warning: plugin with such name does not exist)");
                        }
                    }
                    masterCommandHandled = true;
                    return;
                } else if ("unignore".equals(subcommand) && token.length >= 3) {
                    String pluginName = token[2];
                    List<String> chans = newArrayList();
                    for (int i = 3; i < token.length; i++) {
                        chans.add(token[i]);
                    }
                    if (chans.isEmpty() && !channel.equalsIgnoreCase(sender)) {
                        chans.add(channel);
                    }

                    for (String chan : chans) {
                        ChannelConfig c = channelConfigs.get(chan);
                        if (c == null) {
                            continue;
                        }
                        c.unignorePlugin(pluginName);
                        if (plugins.contains(pluginName)) {
                            send(channel, "Unignoring plugin " + pluginName + " on " + chan);
                        } else {
                            send(channel, "Unignoring plugin " + pluginName + " on " + chan
                                    + " (warning: plugin with such name does not exist)");
                        }
                    }
                    masterCommandHandled = true;
                    return;
                } else if ("list".equals(subcommand)) {
                    StringBuilder sb = new StringBuilder("Plugin list: ");

                    for (String s : channelListeners.keySet()) {
                        sb.append(s).append(", ");
                    }
                    send(channel, sb.toString());
                }
            }
        }

        if (channel.equalsIgnoreCase(sender)) {
            return;
        }

        if (ignoredHosts.contains(nickToMask(sender)) && !isMasterAuthorised(sender)) {
            return;
        }

        ChannelConfig c = channelConfigs.get(channel);
        for (Map.Entry<String, ChannelEventListener> cl : channelListeners.entrySet()) {
            if (c == null || !c.isIgnoringPlugin(cl.getKey()) || cl.getKey().equals("AdminCommand")) {
                cl.getValue().onMessage(channel, sender, login, hostname, message, this);
            }
        }
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message) {
        String command = getCommand(message, true);

        if (command != null && isMasterAuthorised(sender)) {
            masterCommandHandled = false;
            onMessage(sender, sender, login, hostname, message);
            if (masterCommandHandled) {
                return;
            }
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onPrivateMessage(sender, login, hostname, message, this);
        }
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        // handle auths
        if (sender.equals(getNick())) {
            if (!activeChannels.containsKey(channel)) {
                Channel c = new Channel(channel);
                activeChannels.put(channel, c);
            }
        } else {
            sendWhoOnUser(sender);
        }

        Channel c = activeChannels.get(channel);
        c.addUser(new ChannelUser(sender, EnumSet.noneOf(ChannelAccess.class)));

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onJoin(channel, sender, login, hostname, this);
        }
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname) {
        if (sender.equals(getNick())) {
            activeChannels.remove(channel);
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onPart(channel, sender, login, hostname, this);
        }

        Channel c = activeChannels.get(channel);
        if (c != null) {
            c.removeUser(sender);
        }

        removeNickFromIAL(sender);
    }

    @Override
    public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        if (recipientNick.equals(getNick())) {
            activeChannels.remove(channel);
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onKick(channel, kickerNick, kickerLogin, kickerHostname, recipientNick, reason, this);
        }

        Channel c = activeChannels.get(channel);
        if (c != null) {
            c.removeUser(recipientNick);
        }

        removeNickFromIAL(recipientNick);
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onQuit(sourceNick, sourceLogin, sourceHostname, reason, this);
        }

        for (Channel c : activeChannels.values()) {
            c.removeUser(sourceNick);
        }

        removeNickFromIAL(sourceNick, true);
    }

    @Override
    public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode, this);
        }
    }

    @Override
    public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onTopic(channel, topic, setBy, date, changed, this);
        }
    }

    @Override
    public void onNickChange(String oldNick, String login, String hostname, String newNick) {
        for (Channel c : activeChannels.values()) {
            ChannelUser cu = c.getUser(oldNick);
            if (cu != null) {
                c.removeUser(cu.getNick());
                cu.setNick(newNick);
                c.addUser(cu);
            }
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onNickChange(oldNick, login, hostname, newNick, this);
        }

        removeNickFromIAL(oldNick);
        sendWhoOnUser(newNick);
    }

    @Override
    public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        Channel c = activeChannels.get(channel);
        if (c != null) {
            ChannelUser cu = c.getUser(recipient);
            cu.getMode().add(ChannelAccess.OP);
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onOp(channel, sourceNick, sourceLogin, sourceHostname, recipient, this);
        }
    }

    @Override
    public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        Channel c = activeChannels.get(channel);
        if (c != null) {
            ChannelUser cu = c.getUser(recipient);
            cu.getMode().remove(ChannelAccess.OP);
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onDeop(channel, sourceNick, sourceLogin, sourceHostname, recipient, this);
        }
    }

    @Override
    public void onVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        Channel c = activeChannels.get(channel);
        if (c != null) {
            ChannelUser cu = c.getUser(recipient);
            cu.getMode().add(ChannelAccess.VOICE);
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient, this);
        }
    }

    @Override
    public void onDeVoice(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient) {
        Channel c = activeChannels.get(channel);
        if (c != null) {
            ChannelUser cu = c.getUser(recipient);
            cu.getMode().remove(ChannelAccess.VOICE);
        }

        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onDeVoice(channel, sourceNick, sourceLogin, sourceHostname, recipient, this);
        }
    }

    @Override
    public void onSetChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onSetChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask, this);
        }
    }

    @Override
    public void onRemoveChannelBan(String channel, String sourceNick, String sourceLogin, String sourceHostname, String hostmask) {
        for (ChannelEventListener cl : channelListeners.values()) {
            cl.onRemoveChannelBan(channel, sourceNick, sourceLogin, sourceHostname, hostmask, this);
        }
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        if (!activeChannels.containsKey(channel)) {
            Channel c = new Channel(channel);
            activeChannels.put(channel, c);
        }

        Channel c = activeChannels.get(channel);

        for (User u : users) {
            String p = u.getPrefix();
            EnumSet<ChannelAccess> mode = EnumSet.noneOf(ChannelAccess.class);
            if (p.contains("@")) {
                mode.add(ChannelAccess.OP);
            }
            if (p.contains("+")) {
                mode.add(ChannelAccess.VOICE);
            }
            c.addUser(new ChannelUser(u.getNick(), mode));
        }

        sendWhoOnChannel(channel);
    }
}
