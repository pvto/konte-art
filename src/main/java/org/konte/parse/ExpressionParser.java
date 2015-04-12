
package org.konte.parse;

import org.konte.expression.*;
import static org.konte.lang.Tokens.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.konte.expression.Operator;
import org.konte.lang.Language;
import org.konte.model.Model;

/**
 *
 * @author pvto
 */
public class ExpressionParser {

    private int getSmallestPriority(int i, int i0, int[] priority, ArrayList<Token> ttOrig) throws ParseException 
    {
        int minN = priority[i];
        int place = -1;
        while(ttOrig.get(i)==Language.left_bracket&&ttOrig.get(i0)==Language.right_bracket)
        {
            i++; i0--;
            if (i>i0)
                throw new ParseException("No content between ()");
            minN = priority[i];
        }
        for (int j = i; j < i0; j++)
        {
            if (ttOrig.get(j)==Language.comma)
                continue;
            if (minN >= priority[j])
            {
                place = j;
                minN = priority[j];
            }
        }
        return place;
    }

    private void pickArguments2(int arguments, int i, Expression[] exps, int[] priority, int[] ref, List<Token> tt) throws ParseException 
    {
        org.konte.expression.Operator ee = (org.konte.expression.Operator) exps[i];
        int curPrio = priority[i];
        if (arguments == 0 || arguments == -1)
        {

            int j = i - 1;
            int k = j;
            while (exps[k] == null && k > 0)
            {
                k--;
            }
            int min = priority[k];            
            int place = k;
            while (true)
            {
                if (exps[j] != null)
                {
                    if (priority[j] < curPrio 
                            )
                            {
                        break;
                    }
                    if (priority[j] < min)
                    {
                        if (ref[j] != 0 && priority[j] <= curPrio)
                        {
                            break;
                        }
                        min = priority[j];
                        place = j;
                    }
                }
                else
                {
                    if (priority[j] < curPrio)
                    {
                        break;
                    }
                }
                if (--j < 0)
                {
                    break;
                }
            }
            ee.setLeading(exps[place]);
            ref[place] = 1;
        }
        if (arguments == 0 || arguments == 1)
        {
            int j = i + 1;
            int k = j;

            while (exps[k] == null && k < exps.length)
            {
                k++;
            }
            int min = priority[k];
            int place = k;

            while (true)
            {
                if (exps[j] != null)
                {
                    if (priority[j] <= curPrio 
                            )
                            {
                        break;
                    }
                    if (priority[j] <= min)
                    {
                        min = priority[j];
                        place = j;
                    }
                }
                else
                {
                    if (priority[j] < curPrio)
                    {
                        break;
                    }
                }
                if (++j >= exps.length)
                {
                    break;
                }
            }
            ee.setTrailing(exps[place]);
        }
    }

    private int oneToOneContextualF = 1;

    @SuppressWarnings(
        value = {"unchecked"}
    )
    public Expression parse(ArrayList<Token> ttOrig, int startpos, Model model) throws ParseException 
    {

        List<Token> tt = ttOrig; // ttOrig.subList(startpos+1, ii + 1);
        int[] priority = new int[tt.size()];
        Expression[] exps = new Expression[tt.size()];
        int[] ref = new int[tt.size()];
        Token t = null, u = null;

        boolean lastWasNeg = false;

        ArrayDeque<Integer> prioStack = new ArrayDeque<Integer>();
        ArrayDeque<Integer> lbracketStack = new ArrayDeque<Integer>();
        ArrayDeque<Integer> funcStack = new ArrayDeque<Integer>();
        ArrayDeque<Integer> argStack = new ArrayDeque<Integer>();
        ArrayDeque<Integer> curargStack = new ArrayDeque<Integer>();
        int cur = 0, min = 0, max = 0, curArg = 0;

        for (int i = 0; i < tt.size(); i++)
        {
            if (cur < min)
            {
                min = cur;
            } else if (cur > max)
            {
                max = cur;
            }
            ref[i] = -1;
            t = tt.get(i);

            if (t == Language.left_bracket)
            {
                if (i > 0)
                {
                    if (exps[i-1] != null && exps[i-1] instanceof org.konte.expression.Name)
                    {
                        throw new ParseException("Unrecognized function: " + u);
                    } else
                    if (!(u instanceof Comparator || u instanceof org.konte.lang.Tokens.Operator
                        || u instanceof org.konte.lang.Tokens.Function  ||
                        (u==Language.comma && funcStack.size()>0) ||
                        u==Language.left_bracket))
                        throw new ParseException("Incorrect expression syntax near " + t);
                }
                prioStack.push(cur);
                lbracketStack.push(i);
                cur += Language.BRACKET_PRIORITY;
                prioStack.push(cur);
                priority[i] = cur;
            } else if (t == Language.right_bracket)
            {
                if (lbracketStack.size() < 1)
                    throw new ParseException("Orphaned  ) ");
                priority[i] = prioStack.pop();
                ref[i] = lbracketStack.pop();
                ref[ref[i]] = i;
                cur = prioStack.pop();

                if (ref[i] > 0 &&
                        (exps[ref[i] - 1] instanceof ExpressionFunction /*||
                        (ttOrig.get(ref[i]-1)==Language.comma && 
                        exps[ref[ref[i]-1]] instanceof ExpressionFunction)*/))
                        {

                    int place = getSmallestPriority(argStack.pop() + 1, i, priority, ttOrig);

                    int curAN = curargStack.pop();
                    if (place != -1)
                    {
                        ((ExpressionFunction) exps[funcStack.pop()]).setArg(curAN, exps[place]);
                    }
                    else
                    {
                        funcStack.pop();
                    }

                }
                if (ref[i] != -1)
                {
                    if (exps[ref[i]] == null)
                    {

                    }
                    else
                    {
                        throw new ParseException("Unknown content at left bracket");
                    }
                }
                else
                {
                    throw new ParseException("missing left bracket ");
                }

            } else if (t == Language.comma)
            {
                if (funcStack.isEmpty())
                    throw new ParseException("misplaced comma (misnamed function?)");
                priority[i] = priority[funcStack.peek()];
                ref[i] = funcStack.peek();
                int place = getSmallestPriority(argStack.pop() + 1, i, priority, ttOrig);
                if (place == -1)
                {
                    throw new ParseException("No expression found inside function declaration");
                }
                int curAN = curargStack.pop();
                if (curAN+2 > ((ExpressionFunction) exps[funcStack.peek()]).opCount())
                    throw new ParseException("Too many arguments to function: " + exps[funcStack.peek()]);
                ((ExpressionFunction) exps[funcStack.peek()]).setArg(curAN, exps[place]);
                argStack.push(i);
                curargStack.push(++curAN);
            } else if (t == Language.add || t == Language.subtract)
            {
                priority[i] = cur + Language.ADD_PRIORITY;
                if (u == Language.subtract)
                    throw new ParseException("doubled negation: -+ or -- ");
                if (t == Language.add)
                {
                    exps[i] = new Addition(null, null);
                } else if (t == Language.subtract)
                {
                    if (u == null || u instanceof org.konte.lang.Tokens.Operator ||
                            u == Language.left_bracket ||
                            u instanceof org.konte.lang.Tokens.Comparator ||
                            u == Language.comma)
                            {
                        exps[i] = new Negation(null);
                        priority[i] = cur + Language.NEGATION_PRIORITY;
                    }
                    else
                    {
                        exps[i] = new Subtraction(null, null);
                    }
                }
            } else if (t == Language.multiply || t == Language.divide || t == Language.modulo)
            {
                if (u instanceof org.konte.lang.Tokens.Operator)
                    throw new ParseException("Too many operators: " + u + " " + t);
                priority[i] = cur + Language.MULTIPLY_PRIORITY;
                if (t == Language.multiply)
                {
                    exps[i] = new Multiplication(null, null);
                } else if (t == Language.divide)
                {
                    exps[i] = new Division(null, null);
                } else if (t == Language.modulo)
                {
                    exps[i] = new Modulation(null, null);
                }
            } else if (t == Language.pow_op)
            {
                if (u instanceof org.konte.lang.Tokens.Operator)
                    throw new ParseException("Too many operators: " + u + " " + t);
                priority[i] = cur + Language.POWER_PRIORITY;
                exps[i] = new Power(null, null);
            } else if (t instanceof Comparator)
            {
                priority[i] = cur + Language.COMPARISON_PRIORITY;
                if (t == Language.equals)
                {
                    exps[i] = new BooleanExpression.Equals();
                } else if (t == Language.ne)
                {
                    exps[i] = new BooleanExpression.Ne();
                } else if (t == Language.lt)
                {
                    exps[i] = new BooleanExpression.Lt();
                } else if (t == Language.lte)
                {
                    exps[i] = new BooleanExpression.Lte();
                } else if (t == Language.gt)
                {
                    exps[i] = new BooleanExpression.Gt();
                } else if (t == Language.gte)
                {
                    exps[i] = new BooleanExpression.Gte();                    
                } else if (t == Language.and)
                {
                    exps[i] = new BooleanExpression.And();
                    priority[i] = cur + Language.AND_PRIORITY;
                } else if (t == Language.or)
                {
                    exps[i] = new BooleanExpression.Or();
                    priority[i] = cur + Language.OR_PRIORITY;
                }
            } else if (t instanceof org.konte.lang.Tokens.Function)
            {
                if (!(i==0 || u instanceof Comparator || u instanceof org.konte.lang.Tokens.Operator
                        || (u==Language.comma && funcStack.size()>0) ||
                    u==Language.left_bracket))
                    throw new ParseException("Incorrect expression syntax near " + t);
                priority[i] = cur + Language.FUNCTION_PRIORITY;
                if (t instanceof ContextualFunction)
                {
                    if (t instanceof ContextualOneToOneFunction)
                    {
                        ContextualOneToOneFunction coto = (ContextualOneToOneFunction)t;
                        String name = coto.name;
                        if (tt.get(i+2)==Language.right_bracket)
                        {
                            name += "%FOO%"+(oneToOneContextualF++);
                        }
                        else
                        {
//                            if (tt.get(i+3)!=Language.right_bracket)
//                            {
//                                throw new ParseException("after " + t + " (<name>) or () expected");
//                            }
                            name += ":" + tt.get(i+2).toString();
                        }
                        t = (Token)model.globalvar.get(name);
                        if (t == null)
                        {
                            try {
                                t = coto.getClass().getConstructor(String.class, Model.class).newInstance(name, model);
                                model.globalvar.put(name, t);
                            }
                            catch(Exception ex)
                            {
                                throw new ParseException("failed to create contextual function " + t + ": " + ex.getMessage());
                            }
                        }
                    }
                    ((ContextualFunction)t).setModel(model);
                }
                exps[i] = new ExpressionFunction((Function) t);
                funcStack.push(i);
                argStack.push(i + 1);
                curargStack.push(0);
            }
            else
            {
                if (!(i==0 || u instanceof Comparator || u instanceof org.konte.lang.Tokens.Operator
                    || u==Language.left_bracket || 
                    (funcStack.size()>0 && u == Language.comma)))
                    throw new ParseException("Incorrect expression syntax near " + t);                
                priority[i] = cur + Language.VALUE_PRIORITY;
                if (t instanceof Constant)
                {
                    exps[i] = ((Constant)t).value;
                }
                else
                {
                    Float tmp = Language.returnAsValue(t.name);
                    if (tmp != null)
                    {
                        exps[i] = new Value(tmp);
                    }
                    else
                    {
                        exps[i] = Name.createExpressionFinalName(t.name);
                    }
                }
            }

            u = t;
        }
        if (lbracketStack.size() > 0)
            throw new ParseException("Orphaned  ( ");


        for (int i = 0; i < tt.size(); i++)
        {
            ref[i] = 0;
        }

        max += Language.BRACKET_PRIORITY;
        cur = max;

        int i = 0;
        int subf = -1;
        int subl = -1;
        int maxi = priority.length;

        while (cur >= min)
        {
            if (priority[i] == cur)
            {
                if (exps[i] instanceof Negation)
                {
                    if (i == maxi - 1)
                    {
                        throw new ParseException("Missing parameter to negation - ");
                    }
                    pickArguments2(1, i, exps, priority, ref, tt);
                } else if (exps[i] instanceof BooleanExpression)
                {
                    if (i == 0 || i == maxi - 1)
                    {
                        throw new ParseException("Missing parameter to operator " + tt.get(i));
                    }
                    pickArguments2(0, i, exps, priority, ref, tt);                    
                } else if (exps[i] instanceof ExpressionFunction)
                {
                    ExpressionFunction func = (ExpressionFunction)exps[i];
                    boolean nullArg = false;
                    for (int j = 0; j < func.getArgs().length; j++)
                    {
                        if (func.getArgs()[j] == null)
                            nullArg = true;
                    }
                    if (nullArg ||
                            !func.getToken().nArgsAllowed(func.getArgs().length)
                            )
                        throw new ParseException("Wrong number of arguments to " + func.getToken());
                } else if (exps[i] instanceof org.konte.expression.Operator &&
                        exps[i] != Language.comma && exps[i] != Language.semicolon)
                        {
                    if (i == 0 || i == maxi - 1)
                    {
                        throw new ParseException("Missing parameter to operator " + tt.get(i));
                    }
                    pickArguments2(0, i, exps, priority, ref, tt);
                }
            }
            i++;
            if (i == priority.length)
            {
                i = 0;
                subf = subl = -1;
                cur--;
            }
        }
        Expression ret = null;

        i = min;
        int exprInd = -1;
        while (i <= min || ( i > min && ret == null) )
        {
            for (int k = 0; k < tt.size(); k++)
            {
                if (priority[k] == i && exps[k] != null)
                {
                    ret = exps[exprInd = k];
                }
            }
            i++;
        }
        
        testReachability(exps, exps[exprInd]);
        return ret;
    }

    public void testReachability(Expression[] exps, Expression root) throws ParseException
    {
        for(int i = exps.length - 1; i >= 0; i --)
        {
            if (exps[i] != null)
            {
                if (!reachable(exps[i], root))
                {
                    throw new ParseException("Internal expression parse error: " + exps[i] + " is not reachable from " + root);
                }
            }
        }

    }
    public boolean reachable(Expression exp, Expression parent)
    {
        Expression ii = parent;
        if (ii == null)
            return false;
        if (exp == parent)
            return true;
        if (Operator.class.isAssignableFrom(parent.getClass()))
        {
            Operator op = (Operator)parent;
            if (op.getLeading() != null)
            {
                if (reachable(exp, op.getLeading()))
                    return true;
            }
            if (op.getTrailing() != null)
            {
                if (reachable(exp, op.getTrailing()))
                    return true;
            }                
        }
        if (ExpressionFunction.class.isAssignableFrom(parent.getClass()))
        {
            ExpressionFunction ef = (ExpressionFunction)parent;
            for(Expression efa : ef.getArgs())
                if (reachable(exp, efa))
                    return true;
        }

        return false;
    }
}
