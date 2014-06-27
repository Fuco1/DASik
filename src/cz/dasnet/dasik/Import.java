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

import cz.dasnet.dasik.config.ConfigException;
import cz.dasnet.dasik.entities.Learn;
import cz.dasnet.dasik.entities.User;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;

/**
 * Import script for database from Dasik v2.0
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Import {

    public static void main(String[] args) throws ConfigException, FileNotFoundException, UnsupportedEncodingException, IOException {
        Dasik bot = new Dasik();
        //importUsers(bot);
        //importLearns(bot);
    }

    public static void importLearns(Dasik bot) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        LineNumberReader lr = new LineNumberReader(
                new BufferedReader(new InputStreamReader(new FileInputStream("learndump.txt"), "UTF-8")));

        String line;
        while ((line = lr.readLine()) != null) {
            String[] tok = line.split("\\s+", 3);
            if (tok.length < 3) {
                continue;
            }
            
            User u = bot.getUserDao().find(tok[0]);
            int id = 1;
            if (u != null) {
                id = u.getId();
            }

            Learn l = new Learn(tok[1], tok[2], "m", "#das.net", id);
            bot.getLearnDao().addLearn(l);
        }
    }

    public static void importUsers(Dasik bot) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        LineNumberReader lr = new LineNumberReader(
                new BufferedReader(new InputStreamReader(new FileInputStream("userdump.txt"), "UTF-8")));

        String line;

        while ((line = lr.readLine()) != null) {
            String[] tok = line.split("\\s+");
            User u = new User(tok[0], Integer.parseInt(tok[2]));
            u.setWords(Integer.parseInt(tok[1]));
            bot.getUserDao().addUser(u);
        }
    }
}
