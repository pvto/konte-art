/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.model;

import org.konte.expression.Expression;

/**<p>RepeatStructure is the * structure in the language. Such a construct 
 * defines a finite iteration over a received value in a konte space.  
 * Each RepeatStructure points to a Transform (repeatedTransform), which may be a
 * RepeatStructure itself.  For this convenience, RepeatStructure is a Transform.    
 * The repeat progression is defined as n successive transforms. For each
 * successive transform that alters the calling transform, the 
 * repeatedTransform is called.  Thus RepeatStructure is comparable to for { ; ; } in 
 * its syntax.
 * <p>At generate time, a RepeatStructure with repeats=10 generates 10 new branches,
 * if none are blocked by pre or post entry rules.
 * <p>Note that the transform that is applied for each iteration, is defined
 * by the member field repeatTransform - not RepeatStructure itself. This is for 
 * parsing convenience.
 *
 * @author Paavo Toivanen
 */
public class RepeatStructure extends Transform {
    
    public Transform repeatTransform = null;
    public Transform repeatedTransform = null;
    public Expression repeats;

    public RepeatStructure() {
        super();
        repeatStructure = true;
    }



    public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append("Repeat ").append(repeats).append("*");
        String hp = repeatTransform.toString();
        bd.append(hp.substring(hp.indexOf("{"))).
                append(repeatedTransform);
        return bd.toString();        
    }    
    
}
