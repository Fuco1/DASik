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

import org.jsoup.Jsoup;
import java.net.URLConnection;
import cz.dasnet.dasik.Dasik;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import static cz.dasnet.dasik.util.StringUtils.shorten;
import static cz.dasnet.dasik.util.StringUtils.decodeEntities;
import static cz.dasnet.dasik.util.StringUtils.addMagSeparator;

/**
 * Pull info from posted links.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class URLInfo extends Plugin {

    private static Logger log = Logger.getLogger(URLInfo.class);
    private static Pattern youtube = Pattern.compile(".*?(http://|www\\.|http://www\\.){0,1}youtube\\.com/.*?v=(.*?)($|&.*?|\\s+)");
    private static Pattern youtubeShort = Pattern.compile(".*?(http://){0,1}youtu\\.be/(.*?)($|&.*?|\\s+)");
    private static Pattern generalUrl = Pattern.compile(".*((http:\\/\\/www\\.|(www\\.|http:\\/\\/))[_a-zA-Z0-9\\.\\-]+\\.[a-zA-Z]{2,4}\\/{0,1}[-_~&=\\?\\.a-zA-Z0-9\\/]*).*");
    private SAXReader reader = new SAXReader();

    @Override
    public void loaded(Dasik bot) {
        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        log.debug("Unloaded");
    }

    private String toMS(String len) {
        int l = Integer.parseInt(len);
        return (l / 60) + "m " + (l % 60) + "s";
    }

    @Override
    public void onAction(String sender, String login, String hostname, String target, String action, Dasik bot) {
        if (target.startsWith("#")) {
            onMessage(target, sender, login, hostname, action, bot);
        }
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        Matcher m = youtube.matcher(message);
        String ytId = null;
        if (m.find()) {
            ytId = m.group(2);
        }
        if (ytId == null) {
            m = youtubeShort.matcher(message);
            if (m.find()) {
                ytId = m.group(2);
            }
        }
        if (ytId != null) {
            try {
                // pull info from youtube api
                org.dom4j.Document document = reader.read(new URL("http://gdata.youtube.com/feeds/api/videos/" + ytId + "?v=2"));
                Node titleNode = document.selectSingleNode("//entry/media:group/media:title");
                Node descNode = document.selectSingleNode("//entry/media:group/media:description");
                Node statNode = document.selectSingleNode("//entry/yt:statistics");
                Node ratingNode = document.selectSingleNode("//entry/yt:rating");
                Node durNode = document.selectSingleNode("//entry/media:group/yt:duration");

                String title = decodeEntities(titleNode.getText());
                String desc = decodeEntities(descNode.getText());
                String views = statNode.valueOf("@viewCount");
                String likes = ratingNode.valueOf("@numLikes");
                String dislikes = ratingNode.valueOf("@numDislikes");
                String dur = durNode.valueOf("@seconds");

                bot.send(channel, "Title: " + shorten(title, 200) + " [" + addMagSeparator(views, ",")
                        + " views; Rating: " + addMagSeparator(likes, ",") + "/"
                        + addMagSeparator(dislikes, ",") + "; Length: " + toMS(dur) + "]");
                //bot.send(channel, "Description: " + shorten(desc, 300));
            } catch (DocumentException ex) {
            } catch (MalformedURLException ex) {
            }
            return;
        }

        m = generalUrl.matcher(message);
        if (m.find()) {
            // general URL
            String mURL = m.group(1);
            if (!mURL.startsWith("http://")) {
                mURL = "http://" + mURL;
            }

            URLConnection httpConn = null;
            try {
                URL url = new URL(mURL);
                httpConn = url.openConnection();
                httpConn.setDoInput(true);
                httpConn.connect();
                String mime = httpConn.getContentType();

                if (mime.contains("text")) {
                    org.jsoup.nodes.Document document = Jsoup.connect(url.toString()).get();
                    String title = document.select("title").first().text();
                    bot.send(channel, shorten("Title: " + title, 350) + " (at " + url.getHost() + ")");
                }
            } catch (IOException ex) {
                log.error("Error while downloading url: " + mURL, ex);
            } finally {
                if (httpConn != null) {
                    try {
                        httpConn.getInputStream().close();
                    } catch (IOException exx) {
                        log.error("Error closing connection to: " + mURL, exx);
                    }
                }
            }
            return;
        }
    }
}