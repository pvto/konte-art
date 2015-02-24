
package org.konte.expression;

import org.konte.generate.RuleWriter;
import org.konte.model.BackReferenceFactory;
import org.konte.lang.Tokens.Constant;
import org.konte.lang.Tokens.InnerExpressiveToken;
import org.konte.lang.Tokens.InnerToken;
import org.konte.lang.Language;
import org.konte.lang.Tokens.Token;
import org.konte.model.Model;
import org.konte.parse.ParseException;

/**<p>Besides value, this is another type of expression that must be directly
 * evaluable, i.e. it will not contain any other expressions than itself.
 * Named expressions are, in konte, either names of global constants (definitions)
 * or local constants, which include shape transform tokens and user defined
 * tokens.
 *
 * @author pvto
 */
public class Name implements ExpressionFinal {

    public static Model model = null;
    public static RuleWriter gene = null;
    String name;
    int id;

    protected Name() {
    }

    protected Name(String name) {
        this.name = name;
    }
    private static int exprcounter = 0;

    public boolean findExpression(Class c) {
        return (c == Name.class) ? true : false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public static Expression createExpressionFinalName(String name) throws ParseException 
    {
        Token token = Language.tokenByName(name);
        if (token instanceof InnerToken) {
            if (!(token instanceof InnerExpressiveToken)) {
                throw new ParseException(String.format("Token %s cannot be backreferenced.", token));
            }
            InnerExpressiveToken matchingToken = (InnerExpressiveToken) token;
            Name backRefExpression = BackReferenceFactory.newBackReference(matchingToken, name, model);
            return backRefExpression;
        }
        else
        {
            if (token != null) {
                Name n = BackReferenceFactory.newBackReference(token, model);
                if (n != null) {
                    return n;
                }
            }
            int ind = model.bitmapCache.getIndex(name);
            if (ind != -1)
                return new Value((float)ind);
        }
        Name n = new Name(name);
        if (model != null) {
            model.registerNameExpression(n);
        }
        return n;
    }
    private boolean constancyEvaluated = false;
    private Constant constant = null;

    public Float evaluate() throws ParseException 
    {
        Float d;
        if (constancyEvaluated) {
            if (constant != null) {
                if (constant.isMacro) {
                    d = constant.value.evaluate();
                    return d;
                }
                else
                {
                    return constant.constVal;
                }
            }
            else
            {
                return (d = this.gene.model.context.getDef(id)) == null ? 0f : d;
            }
        }

        constant = model.constants.get(name);
        constancyEvaluated = true;
        if (constant == null) {
            return (d = this.gene.model.context.getDef(id)) == null ? 0f : d;
        }
        else
        {

            Expression val;
            if (constant.isMacro) {
                d = constant.value.evaluate();

                return d;
            }
            else
            {
                return constant.constVal;
            }
        }

    }

    public String toString() {
        return name;
    }
}

