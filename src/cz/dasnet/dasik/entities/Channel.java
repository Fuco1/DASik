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

import java.util.Set;
import java.util.Map;
import static cz.dasnet.dasik.util.CollectionUtils.newHashMap;

/**
 * Class representing one IRC Channel.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Channel {

    private String topic;
    private String name;
    private Map<String, ChannelUser> users = newHashMap();

    public Channel() {
    }

    public Channel(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ChannelUser> getUsers() {
        return users;
    }

    public int size() {
        return users.size();
    }

    public Set<String> getUserSet() {
        return users.keySet();
    }

    public void addUser(ChannelUser user) {
        users.put(user.getNick(), user);
    }

    public ChannelUser removeUser(String nick) {
        return users.remove(nick);
    }

    public ChannelUser getUser(String nick) {
        return users.get(nick);
    }
}
