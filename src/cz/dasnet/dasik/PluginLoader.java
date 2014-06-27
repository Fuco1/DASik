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
package cz.dasnet.dasik;

import java.util.Collection;
import java.util.Map;
import cz.dasnet.dasik.plugin.Plugin;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.log4j.Logger;
import static cz.dasnet.dasik.util.CollectionUtils.newHashMap;

/**
 * Plugin loader.
 *
 * @author Matus Goljer
 * @version 1.0
 */
public class PluginLoader {

    private static Logger log = Logger.getLogger(PluginLoader.class);
    private Dasik bot;

    private class FinderClassLoader extends URLClassLoader {

        public FinderClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.contains("cz.dasnet.dasik.plugin") && !name.equals("cz.dasnet.dasik.plugin.Plugin")) {
                return super.findClass(name);
            } else {
                ClassLoader p = getParent();
                Class<?> c = p.loadClass(name);
                if (c == null) {
                    c = super.findClass(name);
                }
                return c;
            }
        }
    }

    public PluginLoader(Dasik bot) {
        this.bot = bot;
    }

    /**
     * Load a plugin. Plugin's {@code loaded} method is automatically invoked.
     *
     * @param name Name of the plugin to be loaded
     * @return New instance of loaded plugin
     */
    public Plugin loadPlugin(String name) {
        return loadPlugin(name, true);
    }

    /**
     * Load a plugin.
     *
     * @param name Name of the plugin to be loaded
     * @param invokeLoad True if we should invoke {@code loaded} method on just loaded plugin
     * @return New instance of loaded plugin
     */
    public Plugin loadPlugin(String name, boolean invokeLoad) {
        String basePath = bot.getConfig().getProperty("command.basePath", "build/classes");
        String basePackage = bot.getConfig().getProperty("command.basePackage");

        if (basePath == null || basePackage == null) {
            return null;
        }

        try {
            URL u = new File(basePath).toURI().toURL();
            ClassLoader parentCl = FinderClassLoader.class.getClassLoader();
            ClassLoader cl = new FinderClassLoader(new URL[]{u}, parentCl);
            Class<?> clazz = cl.loadClass(basePackage + "." + name);
            if (clazz == null) {
                return null;
            }
            Plugin com = (Plugin) clazz.newInstance();
            if (invokeLoad) {
                com.loaded(bot);
            }
            return com;
        } catch (MalformedURLException ex) {
            log.error("Unable to load plugin from specified URL", ex);
        } catch (ClassNotFoundException ex) {
            log.error("Unable to load plugin: not found", ex);
        } catch (IllegalAccessException ex) {
            log.error("Unable to load plugin: access denied", ex);
        } catch (InstantiationException ex) {
            log.error("Unable to load plugin: instantiation error", ex);
        } catch (Throwable ex) {
            log.error("Unable to load plugin: error", ex);
        }

        return null;
    }

    /**
     * Load all plugins from the collection. Plugins' {@code loaded} method is automatically invoked.
     *
     * @param names Collection of plugin names
     * @param invokeLoad True if we should invoke {@code loaded} method on just loaded plugin
     * @return New map instance mapping names of plugins to new plugin instances
     */
    public Map<String, Plugin> loadPlugins(Collection<String> names) {
        return loadPlugins(names, true);
    }

    /**
     * Load all plugins from the collection.
     *
     * @param names Collection of plugin names
     * @param invokeLoad True if we should invoke {@code loaded} method on just loaded plugin
     * @return New map instance mapping names of plugins to new plugin instances
     */
    public Map<String, Plugin> loadPlugins(Collection<String> names, boolean invokeLoad) {
        Map<String, Plugin> plugins = newHashMap();

        for (String name : names) {
            Plugin c = loadPlugin(name, invokeLoad);
            if (c != null) {
                plugins.put(name, c);
            }
        }

        return plugins;
    }
}
