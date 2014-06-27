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
package cz.dasnet.dasik.entities;

/**
 * JavaBean representing one IRC user.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class User {

    private int id;
    private String mask;
    private int words;
    private int wordsDaily;
    private int access;
    private long lastseen;
    private String lastseenAction;
    private String lastseenNick;

    @Override
    public String toString() {
        return id + " "
                + mask + " "
                + words + " "
                + wordsDaily + " "
                + access + " "
                + lastseen + " "
                + lastseenAction + " "
                + lastseenNick;
    }

    public User() {
    }

    public User(User u) {
        this.id = u.id;
        this.mask = u.mask;
        this.words = u.words;
        this.wordsDaily = u.wordsDaily;
        this.access = u.access;
        this.lastseen = u.lastseen;
        this.lastseenAction = u.lastseenAction;
        this.lastseenNick = u.lastseenNick;
    }

    public User(String mask, int access) {
        this.mask = mask;
        this.words = 0;
        this.wordsDaily = 0;
        this.access = access;
        this.lastseen = System.currentTimeMillis();
        this.lastseenAction = "User created";
        this.lastseenNick = mask.split("@")[0];
    }

    public User(String mask, int words, int wordsDaily, int access, long lastseen, String lastseenAction) {
        this.mask = mask;
        this.words = words;
        this.wordsDaily = wordsDaily;
        this.access = access;
        this.lastseen = lastseen;
        this.lastseenAction = lastseenAction;
        this.lastseenNick = mask.split("@")[0];
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLastseen() {
        return lastseen;
    }

    public void setLastseen(long lastseen) {
        this.lastseen = lastseen;
    }

    public String getLastseenAction() {
        return lastseenAction;
    }

    public void setLastseenAction(String lastseenAction) {
        this.lastseenAction = lastseenAction;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public int getWords() {
        return words;
    }

    public void setWords(int words) {
        this.words = words;
    }

    public void incWords(int words) {
        this.words += words;
    }

    public int getWordsDaily() {
        return wordsDaily;
    }

    public void setWordsDaily(int wordsDaily) {
        this.wordsDaily = wordsDaily;
    }

    public void incWordsDaily(int wordsDaily) {
        this.wordsDaily += wordsDaily;
    }

    public String getLastseenNick() {
        return lastseenNick;
    }

    public void setLastseenNick(String lastseenNick) {
        this.lastseenNick = lastseenNick;
    }
}
