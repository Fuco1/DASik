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
package cz.dasnet.dasik.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A wrapper around {@code java.util.Properties} class providing some useful methods.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class Config {

    private Properties properties;

    /**
     * Load "config.properties" file.
     * 
     * @throws IOException If the config file is unavailable to reading.
     */
    public Config() throws ConfigException {
        properties = new Properties();
        try {
            InputStream is = new BufferedInputStream(new FileInputStream("config.properties"));
            properties.load(is);
        } catch (IOException ex) {
            throw new ConfigException("Unable to load \"config.properties\"", ex);
        }
    }

    /**
     * Reload the config.
     *
     * @throws ConfigException If the config file is unavailable to reading.
     */
    public void reload() throws ConfigException {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream("config.properties"));
            properties.load(is);
        } catch (IOException ex) {
            throw new ConfigException("Unable to load \"config.properties\"", ex);
        }
    }

    /**
     * Return the named property as int value.
     *
     * @param name Name of the property
     * @return Integer value of the property
     * @throws ConfigException If the property is not found or the value cannot
     *                         be converted to integer
     */
    public int getPropertyInt(String name) throws ConfigException {
        try {
            return Integer.parseInt(properties.getProperty(name));
        } catch (NumberFormatException ex) {
            throw new ConfigException("Cannot convert property with name " + name + " to type Integer", ex);
        }
    }

    /**
     * Return the named property as int value, or return default value if property
     * is not found.
     *
     * @param name Name of the property
     * @param def Default value in case property is not found or an error occured
     * @return Integer value of the property or the supplied default value
     */
    public int getPropertyInt(String name, int def) {
        try {
            return getPropertyInt(name);
        } catch (ConfigException ex) {
            return def;
        }
    }

    /**
     * Return the named property as long value.
     *
     * @param name Name of the property
     * @return Long value of the property
     * @throws ConfigException If the property is not found or the value cannot
     *                         be converted to long
     */
    public long getPropertyLong(String name) throws ConfigException {
        try {
            return Long.parseLong(properties.getProperty(name));
        } catch (NumberFormatException ex) {
            throw new ConfigException("Cannot convert property with name " + name + " to type Long", ex);
        }
    }

    /**
     * Return the named property as long value, or return default value if property
     * is not found.
     *
     * @param name Name of the property
     * @param def Default value in case property is not found or an error occured
     * @return Long value of the property or the supplied default value
     */
    public long getPropertyLong(String name, long def) {
        try {
            return getPropertyLong(name);
        } catch (ConfigException ex) {
            return def;
        }
    }

    /**
     * Return the named property as boolean value. All of the following are
     * converted to true: "true", "yes", "on", "1". The matching is case-insensitive.
     *
     * Any other value is converted to false.
     *
     * @param name Name of the property
     * @return Boolean value of the property
     * @throws ConfigException If the property is not found or the value cannot
     *                         be converted to boolean
     */
    public boolean getPropertyBool(String name) throws ConfigException {
        boolean re = false;
        String prop = properties.getProperty(name);

        if (prop == null) {
            throw new ConfigException("Property with name " + name + " not found");
        }

        if (prop.equalsIgnoreCase("true")
                || prop.equalsIgnoreCase("on")
                || prop.equalsIgnoreCase("yes")
                || prop.equalsIgnoreCase("1")) {
            re = true;
        }
        return re;
    }

    /**
     * Return the named property as boolean value, or return default value if property
     * is not found.
     *
     * @param name Name of the property
     * @param def Default value in case property is not found or an error occured
     * @return Boolean value of the property or the supplied default value
     */
    public boolean getPropertyBool(String name, boolean def) {
        try {
            return getPropertyBool(name);
        } catch (ConfigException ex) {
            return def;
        }
    }

    /**
     * Return the named property as {@code String} value.
     *
     * @param name Name of the property
     * @return {@code String} value of the property
     */
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    /**
     * Return the named property as {@code String} value, or return default value if property
     * is not found.
     *
     * @param name Name of the property
     * @param def Default value in case property is not found or an error occured
     * @return {@code String} value of the property or the supplied default value
     */
    public String getProperty(String name, String def) {
        return properties.getProperty(name, def);
    }

    /**
     * Split the property on whitespace characters and return the resulting array.
     *
     * @param name Name of the property
     * @return {@code String[]} array of whitespace-delimited values.
     */
    public String[] getPropertyAsStringArray(String name) throws ConfigException {
        try {
            return properties.getProperty(name).split("\\s+");
        } catch (NullPointerException ex) {
            throw new ConfigException("Property with name " + name + " not found", ex);
        }
    }

    /**
     * Split the property on whitespace characters and return the resulting array.
     * If the property is not found, return the default array.
     *
     * @param name Name of the property
     * @param def Default value in case property is not found or an error occured
     * @return {@code String[]} array of whitespace-delimited values or the supplied default value
     */
    public String[] getPropertyAsStringArray(String name, String[] def) {
        try {
            return getPropertyAsStringArray(name);
        } catch (ConfigException ex) {
            return def;
        }
    }
}
