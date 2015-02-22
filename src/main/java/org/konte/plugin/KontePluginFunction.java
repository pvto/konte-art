
package org.konte.plugin;

import org.konte.lang.Tokens.Function;

/**
 *
 * @author pvto
 */
public abstract class KontePluginFunction extends Function {

    public KontePluginFunction() {
        this.setName(this.getName());
    }
    public abstract String getName();
    
}
