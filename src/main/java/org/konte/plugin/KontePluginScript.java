/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.plugin;

import org.konte.model.Model;
import org.konte.model.Rule;

/**
 *
 * @author pto
 */
public interface KontePluginScript {

    void setModel(Model model);
    void setRule(Rule rule);

    void execute();
}
