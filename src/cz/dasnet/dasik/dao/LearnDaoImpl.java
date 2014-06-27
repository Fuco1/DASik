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

import cz.dasnet.dasik.entities.Learn;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Learn DAO implementation.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class LearnDaoImpl extends SimpleJdbcTemplate implements LearnDao {

    private static Logger log = Logger.getLogger(LearnDao.class);
    public static final RowMapper<Learn> learnRowMapper = new ParameterizedRowMapper<Learn>() {

        public Learn mapRow(ResultSet rs, int rowNum) throws SQLException {
            Learn l = new Learn();
            l.setId(rs.getInt(1));
            l.setUserId(rs.getInt(2));
            l.setTrigger(rs.getString(3));
            l.setReply(rs.getString(4));
            l.setType(rs.getString(5));
            l.setChannel(rs.getString(6));
            l.setCreated(rs.getLong(7));
            l.setMask(rs.getString(8));
            l.setAccess(rs.getInt(9));
            return l;
        }
    };

    public LearnDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    public Learn find(String trigger, String type) {
        try {
            String sql = "SELECT learns.*, users.mask, users.access "
                    + "FROM learns INNER JOIN users ON learns.user_id = users.id "
                    + "WHERE learns.trigger = ? AND learns.type = ?";
            Learn l = queryForObject(sql, learnRowMapper, trigger, type);
            return l;
        } catch (DataAccessException ex) {
            //log.error("Learn with trigger " + trigger + " of type " + type + " not found", ex);
            return null;
        }
    }

    public List<Learn> findAll(String trigger, String type) {
        try {
            String sql = "SELECT learns.*, users.mask, users.access "
                    + "FROM learns INNER JOIN users ON learns.user_id = users.id "
                    + "WHERE learns.trigger LIKE ? AND learns.type = ?";
            List<Learn> l = query(sql, learnRowMapper, trigger + "%", type);
            return l;
        } catch (DataAccessException ex) {
            //log.error("Learn with trigger " + trigger + " of type " + type + " not found", ex);
            return null;
        }
    }

    public void addLearn(Learn learn) {
        String sql = "INSERT INTO learns "
                + "(user_id, `trigger`, reply, type, channel, created) VALUES "
                + "(:userId, :trigger, :reply, :type, :channel, :created)";

        SqlParameterSource learnParameters = new BeanPropertySqlParameterSource(learn);
        update(sql, learnParameters);
    }

    public void updateLearn(Learn learn) {
        String sql = "UPDATE learns SET user_id = :userId, "
                + "`trigger` = :trigger, reply = :reply, "
                + "type = :type, channel = :channel, "
                + "created = :created WHERE id = :id";

        SqlParameterSource learnParameters = new BeanPropertySqlParameterSource(learn);
        update(sql, learnParameters);
    }

    public void removeLearn(Learn learn) {
        String sql = "DELETE FROM learns WHERE id = ?";
        update(sql, learn.getId());
    }

    public void removeLearn(String trigger, String type) {
        String sql = "DELETE FROM learns WHERE `trigger` = ? AND type = ?";
        update(sql, trigger, type);
    }

    public int getLearnCount() {
        String sql = "SELECT count(*) FROM learns";
        return queryForInt(sql);
    }
}
