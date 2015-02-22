
package org.konte.model;

/**<p>This is the parse time counterpart of lang.Untransformable.
 *
 * @author pvto
 */
public class TerminatingShape extends Transform {

    public Untransformable shape;
//    private OutputShape os = new OutputShape();

    public TerminatingShape(String s) {
        super(s);
        terminatingShape = true;
    }
    

}
