package org.konte.plugin;

/** @author pvto */
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import org.konte.lang.Language;
import org.konte.generate.Runtime;

public class PluginLoader {

    static {
        process();
    }

    @SuppressWarnings(
        value = {"unchecked"}
    )
    private static void process()
    {

        try {

            File pluginList = new File("plugin/");
            String[] jars = pluginList.list(new java.io.FilenameFilter()
            {

                @Override
                public boolean accept(File dir, String name)
                {
                    return name.toString().toLowerCase().endsWith(".jar") ? true : false;
                }
            });

            int loadedCount = 0, totalCount = 0;
            if (jars != null)
            {
                for (String s : jars)
                {
                    String filename = pluginList.getCanonicalPath();
                    filename += filename.matches(".*/.*") ? (filename.endsWith("/") ? "" : "/" + s) : (filename.endsWith("\\") ? "" : "\\" + s);
                    Runtime.sysoutln("Loading " + filename, 5);

                    URLClassLoader urlLoader = new URLClassLoader(
                            new URL[]{new URL("file", null, filename)});

                    JarInputStream jis = new JarInputStream(new FileInputStream(filename));
                    JarEntry entry = jis.getNextJarEntry();

                    while (entry != null)
                    {
                        String name = entry.getName();
                        if (name.endsWith(".class"))
                        {
                            totalCount++;
                            name = name.substring(0, name.length() - 6);
                            name = name.replace('/', '.');
                            Runtime.sysout("> " + name, 10);

                            try {
                                urlLoader.loadClass(name);
                                Class pluginClass = Class.forName(name, true, urlLoader);
                                Runtime.sysoutln("\t- loaded ", 10);

                                try {
                                    Class<KontePluginFunction> funcClass = pluginClass.asSubclass(KontePluginFunction.class);
                                    KontePluginFunction func = funcClass.newInstance();
                                    Language.addToken(func);
                                    Runtime.sysoutln("Function added to Language as \"" + func.getName() + "\"", 10);
                                    loadedCount++;
                                    continue;
                                }
                                catch (ClassCastException ee)
                                {
                                    
                                }
                                try {
                                    Class<KonteScriptExtension> scriptClass = pluginClass.asSubclass(KonteScriptExtension.class);
                                    KonteScriptExtension scriptExt = scriptClass.newInstance();
                                    Language.scriptExtensions.add(scriptExt);
                                    Runtime.sysoutln("Script framework " + scriptExt + " initiated", 10);
                                    loadedCount++;
                                }
                                catch (ClassCastException ff)
                                {

                                }
                            }
                            catch (Throwable e)
                            {
                                Runtime.sysoutln("\t- not loaded", 10);
                                Runtime.sysoutln("\t " + e.getClass().getName() + ": " + e.getMessage(), 10);
                            }

                        }
                        entry = jis.getNextJarEntry();
                    }
                }
            }
            Runtime.sysoutln("\n---------------------", 10);
            Runtime.sysoutln("Classes under plugin:", 10);
            Runtime.sysoutln("\tWielded:\t" + loadedCount, 10);
            Runtime.sysoutln("\tUnknown:\t" + (totalCount - loadedCount), 10);
            Runtime.sysoutln("\tTotal:\t" + totalCount, 10);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /*
    private static URLClassLoader getURLClassLoader(URL jarURL)
    {
    return new URLClassLoader(new URL[]{jarURL});
    }*/

    public static void main(String[] args)
    {

    }
}
