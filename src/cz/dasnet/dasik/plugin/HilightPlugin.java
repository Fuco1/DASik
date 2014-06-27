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
package cz.dasnet.dasik.plugin;

import org.jibble.pircbot.Colors;
import java.util.regex.Matcher;
import cz.dasnet.dasik.Dasik;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import static cz.dasnet.dasik.util.CollectionUtils.newArrayList;

/**
 * Class implementing basic hilight facilities.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class HilightPlugin extends Plugin {

    private static Logger log = Logger.getLogger(HilightPlugin.class);
    private PrintWriter out;
    private List<Pattern> patterns;
    private SimpleDateFormat f = new SimpleDateFormat();

    @Override
    public void loaded(Dasik bot) {
        log.debug("Setting hilight log writer ...");
        try {
            String basePath = bot.getConfig().getProperty("log.basepath", "logs");
            File file = new File(basePath + System.getProperty("file.separator") + "hilight.log");
            out = new PrintWriter(
                    new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(file, true), "UTF-8")));
        } catch (FileNotFoundException ex) {
        } catch (UnsupportedEncodingException ex) {
        }

        log.debug("Compiling patterns ...");
        patterns = newArrayList();
        String[] pats = bot.getConfig().getPropertyAsStringArray("hilight.patterns", new String[]{});
        for (String pat : pats) {
            patterns.add(Pattern.compile(pat, Pattern.CASE_INSENSITIVE));
        }

        f.applyPattern("[yyyy/MM/dd HH:mm:ss]");

        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        try {
            out.println("--- Session closed: " + f.format(new Date()));
            out.flush();
            out.close();
        } catch (Exception ex) {
        }

        patterns.clear();
        log.debug("Unloaded");
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        for (Pattern p : patterns) {
            Matcher m = p.matcher(message);
            if (m.find()) {
                message = Colors.removeFormattingAndColors(message);
                out.println(getTimeStamp() + " " + channel + " <" + sender + "> " + message + " (matched: " + p.pattern() + ")");
                out.flush();
                System.out.println((char) 7);
                break;
            }
        }
    }

    private String getTimeStamp() {
        return f.format(new Date());
    }
}
