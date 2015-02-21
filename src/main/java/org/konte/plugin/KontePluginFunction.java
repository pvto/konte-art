
package org.konte.plugin;

import org.konte.lang.Tokens.Function;

/**
 *
 * @author pto
 */
public abstract class KontePluginFunction extends Function {

    public KontePluginFunction() {
        this.setName(this.getName());
    }
    public abstract String getName();
    
}
