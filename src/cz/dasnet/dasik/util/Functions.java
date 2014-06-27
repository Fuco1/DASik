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

/**
 * Class containing various abstract function definitions.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Functions {

    private Functions() {
    }

    public static interface Predicate<T> {

        boolean filter(T item);
    }

    public static interface Map<A, T> {

        T f(A a);
    }

    public static interface Fold<A, T> {

        T f(A v, T acc);
    }
}
