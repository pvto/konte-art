
package org.konte.plugin;

/**
 *
 * @author pvto
 */
public interface KonteScriptExtension {

    boolean isScript(String str);

    KontePluginScript getInstance(String str) throws Exception;
}
