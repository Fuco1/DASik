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
package cz.dasnet.dasik.util;

import java.io.Serializable;

/**
 * A generic Pair class.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Pair<T extends Serializable, U extends Serializable> implements Serializable {

    private static final long serialVersionUID = 5000;
    private T t;
    private U u;

    public Pair(T t, U u) {
        this.t = t;
        this.u = u;
    }

    public static <T extends Serializable, U extends Serializable> Pair pairOf(T t, U u) {
        return new Pair<T, U>(t, u);
    }

    public T fst() {
        return t;
    }

    public U snd() {
        return u;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<T, U> other = (Pair<T, U>) obj;
        if (this.t != other.t && (this.t == null || !this.t.equals(other.t))) {
            return false;
        }
        if (this.u != other.u && (this.u == null || !this.u.equals(other.u))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.t != null ? this.t.hashCode() : 0);
        hash = 73 * hash + (this.u != null ? this.u.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "(" + t.toString() + ", " + u.toString() + ")";
    }
}
