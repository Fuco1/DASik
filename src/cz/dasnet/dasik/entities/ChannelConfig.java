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

import java.util.Collections;
import java.util.Set;
import static cz.dasnet.dasik.util.CollectionUtils.newHashSet;

/**
 *
 * @author Matus
 */
public class ChannelConfig {

    private String name;
    private Set<String> ignoredPlugins = newHashSet();

    public ChannelConfig(String name) {
        this.name = name;
    }

    public void ignorePlugin(String name) {
        ignoredPlugins.add(name);
    }

    public void unignorePlugin(String name) {
        ignoredPlugins.remove(name);
    }

    public boolean isIgnoringPlugin(String name) {
        return ignoredPlugins.contains(name);
    }

    public Set<String> getIgnoredPlugins() {
        return Collections.unmodifiableSet(ignoredPlugins);
    }

    public String getName() {
        return name;
    }
}
