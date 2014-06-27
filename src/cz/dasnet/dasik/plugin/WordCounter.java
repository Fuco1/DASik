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

import cz.dasnet.dasik.ChannelAccess;
import cz.dasnet.dasik.dao.UserDaoImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import cz.dasnet.dasik.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import cz.dasnet.dasik.Dasik;
import cz.dasnet.dasik.util.Functions;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import static cz.dasnet.dasik.util.CollectionUtils.foldl;
import static cz.dasnet.dasik.util.CollectionUtils.getLast;

/**
 * Class implementing word counting service.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class WordCounter extends Plugin {

    private static Logger log = Logger.getLogger(WordCounter.class);
    private long lastUpdate = System.currentTimeMillis();
    private Calendar now = new GregorianCalendar();
    private Calendar last = new GregorianCalendar();

    private class DailyInfo {

        int userId;
        int wordsDaily;
    }

    @Override
    public void loaded(Dasik bot) {
        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        log.debug("Unloaded");
    }

    public String getTop10(Dasik bot, int from) {
        String sql = "SELECT * FROM users WHERE words > 0 ORDER BY words DESC LIMIT " + from + ", 10";
        List<User> users = bot.getUserDao().query(sql, UserDaoImpl.userRowMapper);

        sql = "SELECT SUM(words) FROM users";
        double sum = bot.getUserDao().query(sql, new RowMapper<Integer>() {

            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt(1);
            }
        }).get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("Top spammers [").append(from + 1).append("-").append(from + users.size()).append("]: ");

        int i = from + 1;
        for (User u : users) {
            if (u.getWords() == 0) {
                break;
            }
            String nick = bot.maskToNick(u.getMask());
            if (nick == null) {
                if (u.getMask().contains("users.quakenet.org")) {
                    nick = u.getMask().split("@")[1].split("\\.", 2)[0];
                } else {
                    nick = u.getMask().split("@")[0];
                }
            }
            sb.append(i).append(". ").append(nick).append("(").append(u.getWords()).
                    append(" :: ").append(String.format("%1.1f%%", u.getWords() * 100 / sum)).append(") ");
            i++;
        }
        return sb.toString();
    }

    public String getTop10Daily(Dasik bot, int from) {
        String sql = "SELECT * FROM users WHERE words_daily > 0 ORDER BY words_daily DESC LIMIT " + from + ", 10";
        List<User> users = bot.getUserDao().query(sql, UserDaoImpl.userRowMapper);

        sql = "SELECT SUM(words_daily) FROM users";
        double sum = bot.getUserDao().query(sql, new RowMapper<Integer>() {

            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt(1);
            }
        }).get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("Today's top spammers [").append(from + 1).append("-").append(from + users.size()).append("]: ");

        int i = from + 1;
        for (User u : users) {
            if (u.getWordsDaily() == 0) {
                break;
            }
            String nick = bot.maskToNick(u.getMask());
            if (nick == null) {
                if (u.getMask().contains("users.quakenet.org")) {
                    nick = u.getMask().split("@")[1].split("\\.", 2)[0];
                } else {
                    nick = u.getMask().split("@")[0];
                }
            }
            sb.append(i).append(". ").append(nick).append("(").append(u.getWordsDaily()).
                    append(" :: ").append(String.format("%1.0f%%", u.getWordsDaily() * 100 / sum)).append(") ");
            i++;
        }
        return sb.toString();
    }

    private boolean isAuthorised(User u, String nick, String channel, Dasik bot) {
        ChannelAccess ca = bot.getChannelAccess(nick, channel);
        if (ca == ChannelAccess.OP || ca == ChannelAccess.VOICE || u.getAccess() >= 30) {
            return true;
        }
        return false;
    }

    private void resetDailyStats(String channel, Dasik bot) {
        bot.describe(channel, getTop10Daily(bot, 0));

        // copy daily spam stats to dailyspam table
        String sql = "SELECT id, words_daily FROM users WHERE words_daily > 0";
        List<DailyInfo> dailyInfo = bot.getUserDao().query(sql, new RowMapper<DailyInfo>() {

            public DailyInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                DailyInfo d = new DailyInfo();
                d.userId = rs.getInt(1);
                d.wordsDaily = rs.getInt(2);
                return d;
            }
        });

        for (DailyInfo di : dailyInfo) {
            sql = "INSERT INTO dailyspam (user_id, date, words) VALUES ("
                    + di.userId + ", " + lastUpdate + ", " + di.wordsDaily + ")";
            bot.getUserDao().update(sql);
        }

        // reset stats
        sql = "UPDATE users SET words_daily = 0";
        int rows = bot.getUserDao().update(sql);
        log.debug(rows + " users affected while reseting daily word stats");
        bot.getUserDao().invalidateCache();
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        // get user
        User u = bot.getUserDao().find(login + "@" + hostname);
        if (u == null) {
            u = new User(login + "@" + hostname, 10);
            bot.getUserDao().addUser(u);
        }

        // process commands
        String command = bot.getCommand(message);
        String[] token = message.split("\\s+");
        if (command != null) {
            boolean isAuthorised = sender.equals(channel) || isAuthorised(u, sender, channel, bot);
            if ("top".equals(command) && isAuthorised) {
                int from = 0;
                if (token.length >= 2) {
                    from = Integer.parseInt(token[1]);
                }
                bot.describe(channel, getTop10(bot, from));
            } else if ("ttop".equals(command) && isAuthorised) {
                int from = 0;
                if (token.length >= 2) {
                    from = Integer.parseInt(token[1]);
                }
                bot.describe(channel, getTop10Daily(bot, from));
            } else if ("spamstats".equals(command) || "spam".equals(command)) {
                bot.describe(channel, "[" + sender + "'s stats] Total words: " + u.getWords()
                        + " | Today's words: " + u.getWordsDaily());
            } else if ("resetdaily".equals(command) && bot.isMasterAuthorised(sender)) {
                resetDailyStats(channel, bot);
            }
            //ignore other commands
            return;
        }

        command = bot.getCommand(message, true);
        if (command != null) {
            return;
        }

        if (channel.equals(sender)) {
            return;
        }

        // count the words in the message
        List<String> folded = foldl(Arrays.asList(token), new Functions.Fold<String, List<String>>() {

            public List<String> f(String v, List<String> acc) {
                if (v.length() >= 2 && !v.equals(getLast(acc))) {
                    acc.add(v);
                }
                return acc;
            }
        }, new ArrayList<String>());

        int count = folded.size();
        // determine if we should reset the daily counter
        now.setTime(new Date());
        last.setTime(new Date(lastUpdate));

        if (last.get(Calendar.DAY_OF_MONTH) != now.get(Calendar.DAY_OF_MONTH)) {
            resetDailyStats(channel, bot);
            u.setWordsDaily(0);
        }

        // update the counters and save the user
        u.incWords(count);
        u.incWordsDaily(count);
        bot.getUserDao().updateUser(u);

        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void onAction(String sender, String login, String hostname, String target, String action, Dasik bot) {
        if (target.startsWith("#")) {
            onMessage(target, sender, login, hostname, action, bot);
        }
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message, Dasik bot) {
        onMessage(sender, sender, login, hostname, message, bot);
    }
}
