package org.konte.expression;

/**<p>This is an evaluated, constant expression of value type.
 * Should always return the same value when once instantiated.
 *
 * @author pto
 */
public class Value implements ExpressionFinal {

    private Float value;

    public Value(Float value) {
        this.value = value;
    }

    public Float evaluate() {
        return value;
    }

    public boolean findExpression(Class c) {
        if (c == Value.class) {
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format("%.7f", value);
    }
    public static final Value ONE = new Value(1f);
    public static final Value ZERO = new Value(0f);
}
