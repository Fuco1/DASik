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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A class providing basic collections utilities not found in {@code java.util.Collections}.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class CollectionUtils {

    // ensure noninstantiability
    private CollectionUtils() {
    }

    /**
     * Construct and return new empty {@code HashMap}.
     *
     * @param <K> Key type
     * @param <V> Value type
     * @return Empty {@code HashMap}
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * Construct and return new epmty {@code ArrayList}.
     *
     * @param <T> Value type
     * @return Empty {@code ArrayList}
     */
    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    /**
     * Construct and return new epmty {@code ArrayList} with given initial capacity.
     *
     * @param <T> Value type
     * @param initialCapacity Initial capacity
     * @return Empty {@code ArrayList} with given initial capacity
     */
    public static <T> ArrayList<T> newArrayList(int initialCapacity) {
        return new ArrayList<T>(initialCapacity);
    }

    /**
     * Construct and return new empty {@code HashSet}.
     *
     * @param <T> Value type
     * @return Empty {@code HashSet}
     */
    public static <T> HashSet<T> newHashSet() {
        return new HashSet<T>();
    }

    /**
     * Return filtered {@code ArrayList} containing only items satisfying the predicate.
     *
     * @param <T> Value type
     * @param list List to be filtered
     * @param predicate Predicate that has to be satisfied
     * @return Newly created {@code ArrayList} containing items satisfying the predicate
     */
    public static <T> ArrayList<T> filter(ArrayList<T> list, Functions.Predicate<T> predicate) {
        ArrayList<T> re = newArrayList();
        for (T t : list) {
            if (predicate.filter(t)) {
                re.add(t);
            }
        }
        return re;
    }

    /**
     * Return the number of elements satisfying the predicate.
     *
     * @param <T> Value type
     * @param collection List to be filtered
     * @param predicate Predicate that has to be satisfied
     * @return Number of elements satisfying the predicate
     */
    public static <T> int filterCount(Collection<T> collection, Functions.Predicate<T> predicate) {
        int count = 0;
        for (T t : collection) {
            if (predicate.filter(t)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Return true if any element in this collection satisfies the predicate.
     *
     * @param <T> Value type
     * @param collection Collection to be filtered
     * @param predicate Predicate that has to be satisfied
     * @return True if at least one element satisfied this predicate, false otherwise
     */
    public static <T> boolean any(Collection<T> collection, Functions.Predicate<T> predicate) {
        for (T t : collection) {
            if (predicate.filter(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return new {@code ArrayList} of all items modified by the map function.
     *
     * @param <A> Type of input items
     * @param <T> Type of resulting items
     * @param list Input list
     * @param map Map function
     * @return New {@code ArrayList} containing transformed items
     */
    public static <A, T> List<T> map(Collection<A> list, Functions.Map<A, T> map) {
        List<T> re = new ArrayList<T>(list.size());
        for (A a : list) {
            re.add(map.f(a));
        }
        return re;
    }

    /**
     * Reduce the input {@code Collection} to a single value by distributing a function
     * between all the items. This version start distribution from the left:<br>
     * <pre>
     * f(...(f(f(f(a,b),c),d)...)
     * </pre>
     * 
     * @param <A> Type of input items
     * @param <T> Type of the result
     * @param collection Input collection
     * @param fold Folding (reducing) function
     * @param nul Identity value w.r.t. folding function
     * @return Value obtained by repeated application of given function on the input collection.
     */
    public static <A, T> T foldl(Collection<A> collection, Functions.Fold<A, T> fold, T nul) {
        T re = nul;
        for (A a : collection) {
            re = fold.f(a, re);
        }
        return re;
    }

    /**
     * Reduce the input {@code List} to a single value by distributing a function
     * between all the items. This version start distribution from the right:<br>
     * <pre>
     * f(...(f(a,f(b,f(c,d)))...)
     * </pre>
     *
     * @param <T> Type of the result
     * @param <A> Type of input items
     * @param list Input list
     * @param fold Folding (reducing) function
     * @param nul Identity value w.r.t. folding function
     * @return Value obtained by repeated application of given function on the input list.
     */
    public static <A, T> T foldr(List<A> list, Functions.Fold<A, T> fold, T nul) {
        T re = nul;
        for (int i = list.size() - 1; i >= 0; i--) {
            re = fold.f(list.get(i), re);
        }
        return re;
    }

    /**
     * Return the last item of the {@code List}. Null is returned if the list is null or empty.
     *
     * @param <T> Type of the items in the list
     * @param list Input list
     * @return Last item of the list or null if the list is empty
     */
    public static <T> T getLast(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(list.size() - 1);
    }
}
