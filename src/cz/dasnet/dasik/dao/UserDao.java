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

import cz.dasnet.dasik.entities.User;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

/**
 * User DAO.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public interface UserDao {

    User find(int id);

    User find(String mask);

    void addUser(User user);

    void updateUser(User user);

    void removeUser(User user);

    int getUserCount();

    void invalidateUser(String mask);

    void invalidateCache();

    /**
     * Execute custom query on the datasource bound to this DAO. Make sure to invalidate
     * any user that might be affected by the query! (Users are cached for faster access).
     *
     * @param sql SQL query to be executed
     * @param args Arguments for the query.
     * @return Number of rows changed
     */
    int update(String sql, Object... args);

    /**
     * Query for a {@link List} of <code>Objects</code> of type <code>T</code> using
     * the supplied {@link RowMapper} to the query results to the object.
     * Uses sql with the standard '?' placeholders for parameters
     * 
     * @param sql the SQL query to run
     * @param rm the @{@link RowMapper} to use for result mapping
     * @param args the variable number of arguments for the query
     * @see JdbcOperations#queryForObject(String, org.springframework.jdbc.core.RowMapper)
     * @see JdbcOperations#queryForObject(String, Object[], org.springframework.jdbc.core.RowMapper)
     */
    public <T> List<T> query(String sql, RowMapper<T> rm, Object... args) throws DataAccessException;
}
