
package org.konte.expression;

import org.konte.parse.ParseException;

/**<p>Note; Backreference (each attached to a type of transform )
 * is defined in org.konte.lang.BackReference.
 * <p>Boolean Expressions are defined in BooleanExpression.
 *
 * @author pto
 */
public interface Expression {

    /**Evaluates this expression which may contain one or multiple expressions.
     * If evaluation is impossible due to a value being not available,
     * throws a ParseException.
     */
    public Float evaluate() throws ParseException;

    /** Tells whether this expression can locate the type of expression given
     * by c through object reference.
     * @param c
     * @return
     */
    public boolean findExpression(Class c);
}
