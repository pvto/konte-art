
package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.expression.BooleanExpression;

public class ConditionalStructure extends Transform {

    public List<Transform> onCondition = new ArrayList<Transform>();
    public BooleanExpression conditional;

    public ConditionalStructure() {
        super();
        conditionalStructure = true;
    }



    public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append("Conditional ").append(conditional).append("? ");
        bd.append(onCondition);
        return bd.toString();
    }
}
