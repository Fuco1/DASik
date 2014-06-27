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
 * A class providing basic array utilities not found in {@code java.util.Arrays}.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class ArrayUtils {

    private ArrayUtils() {
    }

    /**
     * Check whether the specified array contains the item. This method runs in O(n)
     * relative to the length of the array.
     *
     * @param array Array to be searched
     * @param item Item to look for
     * @return True if the item is in the array, false otherwise
     */
    public static <T> boolean contains(T[] array, T item) {
        for (T i : array) {
            if (i.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
