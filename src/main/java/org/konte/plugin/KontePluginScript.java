
package org.konte.plugin;

import org.konte.model.Model;
import org.konte.model.Rule;

/**
 *
 * @author pvto
 */
public interface KontePluginScript {

    void setModel(Model model);
    void setRule(Rule rule);

    void execute();
}
