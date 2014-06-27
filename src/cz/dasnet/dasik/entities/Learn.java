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
 * JavaBean representing one Learn entity.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Learn {

    private int id;
    private int userId;
    private String trigger;
    private String reply;
    private String type;
    private String channel;
    private long created;
    private String mask;
    private int access;

    @Override
    public String toString() {
        return id + " "
                + userId + " "
                + trigger + " "
                + reply + " "
                + type + " "
                + channel + " "
                + created + " "
                + mask + " "
                + access;
    }

    public Learn() {
    }

    public Learn(Learn l) {
        this.id = l.id;
        this.userId = l.userId;
        this.trigger = l.trigger;
        this.reply = l.reply;
        this.type = l.type;
        this.channel = l.channel;
        this.created = l.created;
        this.mask = l.mask;
        this.access = l.access;
    }

    public Learn(String trigger, String reply, String type, String channel, int userId) {
        this.userId = userId;
        this.trigger = trigger;
        this.reply = reply;
        this.type = type;
        this.channel = channel;
        this.created = System.currentTimeMillis();
    }

    public Learn(int id, int userId, String trigger, String reply, String type, String channel, long created) {
        this.id = id;
        this.userId = userId;
        this.trigger = trigger;
        this.reply = reply;
        this.type = type;
        this.channel = channel;
        this.created = created;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
