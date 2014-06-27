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

import cz.dasnet.dasik.Dasik;
import cz.dasnet.dasik.entities.User;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 * Plugin for weather forecast.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class WeatherService extends Plugin {

    private static Logger log = Logger.getLogger(WeatherService.class);
    private SAXReader reader = new SAXReader();
    private Properties aliases;

    @Override
    public void loaded(Dasik bot) {
        log.debug("Loading aliases ...");
        aliases = new Properties();
        FileReader fr = null;
        try {
            fr = new FileReader("weather_aliases.properties");
            aliases.load(fr);
        } catch (FileNotFoundException ex) {
            log.error("Unable to reload aliases", ex);
        } catch (IOException ex) {
            log.error("Unable to reload aliases", ex);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ex) {
                }
            }
        }
        log.debug("Loaded");
    }

    @Override
    public void unloaded(Dasik bot) {
        log.debug("Unloaded");
    }

    private String expand(String str) {
//        if ("ba".equalsIgnoreCase(str)) {
//            return "Bratislava";
//        } else if ("bb".equalsIgnoreCase(str)) {
//            return "Banska Bystrica";
//        }
//        return null;
        return aliases.getProperty(str);
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message, Dasik bot) {
        String[] token = message.split("\\s+", 3);
        String command = bot.getCommand(message);
        if (command != null) {
            if ("addalias".equals(command) && token.length >= 3) {
                boolean save = false;
                String a = aliases.getProperty(token[1]);
                if (a == null) {
                    aliases.put(token[1], token[2]);
                    save = true;
                } else {
                    User u = bot.getUserDao().find(login + "@" + hostname);
                    if (u == null || u.getAccess() < 70) {
                        bot.send(channel, "You need at least level 70 access to overwrite this alias.");
                    } else {
                        aliases.put(token[1], token[2]);
                        save = true;
                    }
                }

                if (save) {
                    FileWriter fw = null;
                    try {
                        fw = new FileWriter("weather_aliases.properties");
                        aliases.store(fw, "");
                        bot.send(channel, "Alias added and saved");
                    } catch (IOException ex) {
                    } finally {
                        if (fw != null) {
                            try {
                                fw.close();
                            } catch (IOException ex) {
                            }
                        }
                    }
                }
            }
            return;
        }

        token = message.split("\\s+", 2);

        if (".w".equals(token[0])) {
            String loc = "Brno";
            if (token.length >= 2) {
                loc = token[1];
            }

            if (expand(loc) != null) {
                loc = expand(loc);
            }

            try {
                Document document = reader.read(new URL("http://www.google.com/ig/api?weather=" + URLEncoder.encode(loc, "UTF-8")));
                Node forecastInfo = document.selectSingleNode("//xml_api_reply/weather/forecast_information");
                Node current = document.selectSingleNode("//xml_api_reply/weather/current_conditions");

                String location = forecastInfo.valueOf("city/@data");
                String date = forecastInfo.valueOf("current_date_time/@data");

                String condition = current.valueOf("condition/@data");
                String temp = current.valueOf("temp_c/@data");
                String hum = current.valueOf("humidity/@data");
                String wind = current.valueOf("wind_condition/@data");
                Matcher m = Pattern.compile("(\\d.*?)\\s").matcher(wind);
                if (m.find()) {
                    double ws = Integer.parseInt(m.group(1)) * 0.44704;
                    wind += " (" + String.format("%.1f", ws) + " m/s)";
                }
                String out = "Location: " + location + " | Date: " + date
                        + " | Condition: " + condition + " | Temp: " + temp + " | " + hum + " | "
                        + wind;
                bot.send(channel, out);

            } catch (UnsupportedEncodingException ex) {
            } catch (MalformedURLException ex) {
            } catch (DocumentException ex) {
            }
        }
    }
}
