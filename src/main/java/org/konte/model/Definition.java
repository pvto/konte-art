/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.model;

import org.konte.expression.Expression;

/**
 *
 * @author pto
 */
public class Definition implements Comparable<Definition> {
    /* name is derived from grammar  
     */
    public String name;
    /* name is derived from grammar - 
     *corresponding int value can be shared by multiple Definitions.
     * This guarantees that the same name inside different point
     * transforms in a grammar will be mapped to the same value instance
     * at generation time.
     */ 
    public int nameId = -1;      
    /**<p>Individual id for a definition in a given grammar, used only at parse time.
     * 
     */
    public Integer id;
    /**<p>This is built by the parser.
     * 
     */
    public Expression definition;
    /**<p>Undef will remove the given definition, if any, from a local branch
     * stack at generate time. 0 will always be returned by that name after that
     * until a redefinition.
     * 
     */
    public boolean isUndef;

    public Definition(String name, Expression definition) {
        this.name = name;
        this.definition = definition;
    }

    public int compareTo(Definition o) {
        return this.name.compareTo(o.name);
    }
    
    public String toString() {
        return name + 
                (nameId == -1 ? "" : "[" + nameId + "]") + 
                (id == null ? "" : "_" + id) ;
    }
    
    
    
    
    /** Used by Model to map grammar definitions with the same name to a single id */ 
    public static class NameMap implements Comparable<NameMap> {
        int id;
        String name;

        public NameMap(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }
        
        public int compareTo(NameMap o) {
            return name.compareTo(o.name);
        }
        
    }

    void setNameId(int ipos) {
        this.nameId = ipos;
    }
}
