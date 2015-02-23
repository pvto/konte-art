
package org.konte.model;

import org.konte.expression.BooleanExpression;

public class BlockStructure extends ConditionalStructure {


    public BlockStructure()
    {
        super();
        conditionalStructure = true;
        conditional = new BooleanExpression.Dummy(true);
    }



    public String toString()
    {
        StringBuilder bd = new StringBuilder();
        bd.append("Block ").append(onCondition);
        return bd.toString();
    }
}


