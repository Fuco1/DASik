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
import java.util.List;

/**
 * Learn DAO.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public interface LearnDao {

    Learn find(String trigger, String type);

    List<Learn> findAll(String trigger, String type);

    void addLearn(Learn learn);

    void updateLearn(Learn learn);

    void removeLearn(Learn learn);

    void removeLearn(String trigger, String type);

    int getLearnCount();
}
