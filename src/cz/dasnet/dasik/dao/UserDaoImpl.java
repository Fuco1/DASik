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
package cz.dasnet.dasik.dao;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import cz.dasnet.dasik.entities.User;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import static cz.dasnet.dasik.util.CollectionUtils.newHashMap;

/**
 * User DAO implementation.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class UserDaoImpl extends SimpleJdbcTemplate implements UserDao {

    private static Logger log = Logger.getLogger(UserDao.class);
    public static final RowMapper<User> userRowMapper = ParameterizedBeanPropertyRowMapper.newInstance(User.class);
    private Map<Integer, String> idToMask = newHashMap();
    private Map<String, User> userCache = newHashMap();

    public UserDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    public User find(int id) {
        if (idToMask.containsKey(id)) {
            String mask = idToMask.get(id);
            return new User(userCache.get(mask));
        }

        try {
            String sql = "SELECT * FROM users WHERE id = ?";
            User u = queryForObject(sql, userRowMapper, id);
            cacheUser(u);
            return u;
        } catch (DataAccessException ex) {
            //log.error("User with id " + id + " not found", ex);
            return null;
        }
    }

    public User find(String mask) {
        if (userCache.containsKey(mask)) {
            return new User(userCache.get(mask));
        }

        try {
            String sql = "SELECT * FROM users WHERE mask = ?";
            User u = queryForObject(sql, userRowMapper, mask);
            cacheUser(u);
            return u;
        } catch (DataAccessException ex) {
            //log.error("User with mask " + mask + " not found", ex);
            return null;
        }
    }

    private void cacheUser(User u) {
        idToMask.put(u.getId(), u.getMask());
        userCache.put(u.getMask(), u);
    }

    public void invalidateUser(String mask) {
        if (userCache.containsKey(mask)) {
            int id = userCache.get(mask).getId();
            idToMask.remove(id);
            userCache.remove(mask);
        }
    }

    public void invalidateCache() {
        idToMask.clear();
        userCache.clear();
    }

    public int getUserCount() {
        String sql = "SELECT count(*) FROM users";
        return queryForInt(sql);
    }

    public void addUser(User user) {
        String sql = "INSERT INTO users "
                + "(mask, words, words_daily, access, lastseen, lastseen_action, lastseen_nick) VALUES "
                + "(:mask, :words, :wordsDaily, :access, :lastseen, :lastseenAction, :lastseenNick)";

        SqlParameterSource userParameters = new BeanPropertySqlParameterSource(user);
        update(sql, userParameters);
        invalidateUser(user.getMask());
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET "
                + "words = :words, words_daily = :wordsDaily, "
                + "access = :access, "
                + "lastseen = :lastseen, lastseen_action = :lastseenAction, lastseen_nick = :lastseenNick "
                + "WHERE id = :id";
        SqlParameterSource userParameters = new BeanPropertySqlParameterSource(user);
        update(sql, userParameters);
        invalidateUser(user.getMask());
    }

    public void removeUser(User user) {
        String sql = "DELETE FROM users WHERE id = ?";
        update(sql, user.getId());
        invalidateUser(user.getMask());
    }
}
