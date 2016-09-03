
package org.konte.model;

/**<p>This is the parse time counterpart of lang.Untransformable.
 *
 * @author pvto
 */
public class TerminatingShape extends Transform {

    public Untransformable shape;
//    private OutputShape os = new OutputShape();

    public TerminatingShape(String s, int lineNr, int caretPos)
    {
        super(s, lineNr, caretPos);
        terminatingShape = true;
    }
    

}
