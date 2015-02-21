/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.plugin;

/**
 *
 * @author pto
 */
public interface KonteScriptExtension {

    boolean isScript(String str);

    KontePluginScript getInstance(String str) throws Exception;
}
