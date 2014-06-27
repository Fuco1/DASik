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

import cz.dasnet.dasik.ChannelAccess;
import java.util.EnumSet;

/**
 * Class representing one IRC channel user.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class ChannelUser {

    private String nick;
    private EnumSet<ChannelAccess> mode;

    public ChannelUser() {
    }

    public ChannelUser(String nick, EnumSet<ChannelAccess> mode) {
        this.nick = nick;
        this.mode = mode;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public EnumSet<ChannelAccess> getMode() {
        return mode;
    }

    public void setMode(EnumSet<ChannelAccess> mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChannelUser other = (ChannelUser) obj;
        if ((this.nick == null) ? (other.nick != null) : !this.nick.equals(other.nick)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.nick != null ? this.nick.hashCode() : 0);
        return hash;
    }
}
