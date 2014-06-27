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
 * A class providing basic string utilities not found in {@code java.lang.String}.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class StringUtils {

    // ensure noninstantiability
    private StringUtils() {
    }

    /**
     * Shorten the given string if it's longer than specified length.
     *
     * @param msg Message to be shortened
     * @param length Max length of the message
     * @return Shortened string
     */
    public static String shorten(String msg, int length) {
        if (msg.length() > length) {
            return msg.substring(0, length - 16) + "... [shortened]";
        }
        return msg;
    }

    /**
     * Decode HTML entities.
     *
     * @param line Line to be decoded
     * @return Decoded line
     */
    public static String decodeEntities(String line) {
        line = line.replace("\n", " ");
        line = line.replace("&apm;", "&");
        line = line.replace("&gt;", ">");
        line = line.replace("&lt;", "<");
        line = line.replace("&bull;", " - ");
        line = line.replace("&dash;", " - ");
        line = line.replace("&mdash;", " - ");
        line = line.replace("&edash;", " - ");
        line = line.replace("&#8211;", " - ");
        line = line.replace("&quot;", "\"");
        line = line.replace("&#39;", "'");
        line = line.replace("&apos;", "'");
        line = line.replace("&#x202a;", "");
        line = line.replace("&#x202c;", "");
        line = line.replace("&rlm;", "");
        line = line.replace("&nbsp;", " ");
        line = line.replace("&raquo;", " > ");
        line = line.replace("&laquo;", " < ");
        return line;
    }

    /**
     * Adds a separator every 3rd digit.
     * 
     * @param number Number to be converted
     * @param sep Separator
     * @return Number with separators
     */
    public static String addMagSeparator(String number, String sep) {
        StringBuilder sb = new StringBuilder(sep);
        int s = 3 - (number.length() % 3);
        for (int i = 0; i < number.length(); i++) {
            sb.append(number.charAt(i));
            s = (s + 1) % 3;
            if (s == 0) {
                sb.append(sep);
            }
        }
        return sb.delete(0, 1).delete(sb.length() - 1, sb.length()).toString();
    }

    public static String longestCommonPrefix(String s1, String s2) {
        if (s1.length() > s2.length()) {
            String tmp = s1;
            s1 = s2;
            s2 = tmp;
        }

        int i = 0;
        while (i < s1.length() && Character.toUpperCase(s1.charAt(i))
                == Character.toUpperCase(s2.charAt(i))) {
            i++;
        }

        return s1.substring(0, i);
    }
}
