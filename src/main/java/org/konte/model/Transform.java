package org.konte.model;

import org.konte.parse.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import org.konte.lang.AffineTransform;
import org.konte.lang.Tokens.InnerToken;
import org.konte.lang.Language;
import org.konte.lang.Tokens.Token;
import org.konte.expression.Expression;
import org.konte.misc.Matrix4;
import org.konte.model.DrawingContext.Def;
import org.konte.expression.Name;
import org.konte.expression.Value;
import org.konte.image.Camera;
import org.konte.lang.func.Mathm;
import org.konte.generate.Runtime;

/**<p>This is the abstract definition of a set of transforms that are applied
 * to a branch at generate time. 
 * <p>A transform can carry Definitions, Continuations, and Expressions.
 * Definitions are user defined generationtime values.
 * Continuations define children for PEEK and POP structures.
 * Expressions are either spatial transforms or other modifiers 
 * that are strictly defined in org.konte.Language. Each
 * instantiated InnerToken in Language has a TransformModifier counterpart.
 * <h3>Definitions</h3>
 * 
 * <h3>Continuations</h3>
 * 
 * <h3>Expression</h3>
 * <p>Expressions are either spatial transforms (stored in list 
 * acqTrs in this class) or other modifiers (stored in list acqExps).
 * <p>Each spatial transform will maintain an affine transform superMatrix, and 
 * the Transform itself will maintain one super matrix. If all 
 * spatial transforms are solvable at compile time, i.e.
 * they contain neither backreferences nor non-constant (macro)
 * definitions, the super matrix will be pre-evaluated at compile time.
 * In the other case, only evaluable spatial matrices will be evaluated, and
 * the super evaluation is postponed to generate time.
 *
 * @author pvto
 */
public class Transform {

    
    public Model model;
    public int index;
    public NonDeterministicRule rule = null;
    public int indexedNd = -1;
    public String ruleName = null;
    private Matrix4 superMatrix;
    public ArrayList<String> continuationNames;
    public ArrayList<Integer> continuationStack;
    public ArrayList<TransformModifier> acqExps;   // other expressions
    public ArrayList<TransformModifier> acqTrs;    // transforms
    public ArrayList<Definition> defs;
    public boolean terminatingShape = false;
    public boolean repeatStructure = false;
    public boolean conditionalStructure = false;

    public Definition setDef(String lastName, Expression lexpr, boolean isUndef)
    {
        Definition def = new Definition(lastName, lexpr);
        def.isUndef = isUndef;
        int i = -1;
        for (int j=0; j < defs.size(); j++)
        {
            if (defs.get(j).name.equals(def.name))
                i = j;
        }
        if(i>=0)
        {
            defs.get(i).definition = lexpr; // forsake existing definition
            defs.get(i).isUndef = isUndef;
            return defs.get(i);
        }
        else
        {
            defs.add(def);
            return def;
        }
    }
    /** Run this before attaching a RuleWriter to Model. */
    public void initialize(Model model) throws ParseException 
    {
        this.model = model;
        NonDeterministicRule ndr;
        for (int cnind = 0; cnind < continuationNames.size(); cnind++)
        {
            String name = continuationNames.get(cnind);
            boolean cont = false;
            for(Untransformable u : Language.untransformables())
            {
                if (u.compareTo(name)==0)
                {
                    cont = true; 
                    Runtime.sysoutln("CONT(UNTR): " + u,0);
                    continuationStack.set(cnind, u.getId());
                    break;
                }
            }
            if (cont)    continue;
            if ((ndr = model.rules.get(name)) == null)
            {
                throw new ParseException(String.format(
                        "No rule matching \"%s\" in %s",
                        continuationNames.get(cnind),
                        this));
            }
            Runtime.sysoutln("CONT: " + ndr,0);
            continuationStack.set(cnind, ndr.id);
        }        
        if (Language.peek.compareTo(ruleName) == 0)
        {
            indexedNd = -2;
        } else if (Language.pop.compareTo(ruleName) == 0)
        {
            indexedNd = -3;    
        }        
        boolean matrixIsResolved = true;
        ArrayList<TransformModifier> tmplist = new ArrayList<TransformModifier>();
        tmplist.addAll(acqExps);
        tmplist.addAll(acqTrs);
        Runtime.sysoutln("Init expressions: " + tmplist, 0);
        Value tmpe = null;
        for (TransformModifier aq : tmplist)
        {
            boolean isEvaluated = true;
            Float[] res = new Float[aq.n];

            for (int i = 0 ; i < aq.n ; i++)
            {
                if (aq instanceof TransformModifier.fov)
                {
                   for (int j =0; j < model.cameras.size(); j++)
                   {
                        Camera c = model.cameras.get(j);
                        if (c.getName().equals(aq.exprs.get(i).toString()))
                        {
                            Runtime.sysoutln("camera attached!",-1);
                            tmpe = new Value((float)j);
                            aq.exprs.set(i,tmpe);
                        }
                    }                        
                } else if (aq instanceof TransformModifier.shading)
                {
                   for (int j =0; j < model.colorSpaces.size(); j++)
                   {
                        ColorSpace s = model.colorSpaces.get(j);
                        if (s.getName().equals(aq.exprs.get(i).toString()))
                        {
                            Runtime.sysoutln("shading attached!",-1);
                            tmpe = new Value((float)j);
                            aq.exprs.set(i,tmpe);
                        }
                    }                        
/*                } else if (aq instanceof TransformModifier.img)
                {
                    int ind = model.bitmapCache.getIndex(
                            aq.exprs.get(i).toString());
                    if (ind == -1)
                        throw new ParseException("bitmap referent not found: " +
                                aq.exprs.get(i).toString());
                    aq.exprs.set(i, new Value((float)ind));*/
                }
                else
                {
                    try {
                        if ((res[i] = aq.exprs.get(i).evaluate()) == null)
                        {
                            isEvaluated = false;
                        } else if (!(aq.exprs.get(i) instanceof Value))
                        {
                            if (!aq.exprs.get(i).findExpression(Mathm.ERandom.class))
                            {
                                tmpe = new Value(res[i]);
                                aq.exprs.set(i,tmpe);
                            }
                        }
                    }
                    catch(NullPointerException npe)
                    {
                        // definitions are not defined yet - 
                        isEvaluated = false;
                    }
                }
            }
            
            if (isEvaluated)
            {
                aq.resolved = true;
                aq.transform = aq.createTransform(res);
            }            
            if (!isEvaluated && aq.isInTransform())
            {
                matrixIsResolved = false;
            }
        }
        if (matrixIsResolved)
        {
            superMatrix = createTransformMatrix();
        }
        for(int i = 0; i < continuationNames.size(); i++)
        {
            String s = continuationNames.get(i);
            int ind = continuationStack.get(i);
            if (ind == -1)
            {
                Runtime.sysoutln("Setting PUSH target to " + s, 0);
                
            }
        }
    }
    public boolean hasTransformType(Class c)
    {
        for (TransformModifier e : this.acqExps)
            if (c.isInstance(e))
                return true;
        for (TransformModifier e : this.acqTrs)
            if (c.isInstance(e))
                return true;
        return false;
    }
    private Matrix4 createTransformMatrix() throws ParseException 
    {
        Matrix4 tmp = null; // = Matrix4.IDENTITY;
        try {
            if (acqTrs != null)
                for (TransformModifier aq : acqTrs)
                {
                    Float[] f = new Float[aq.n];
                    for (int i = 0; i < aq.n; i++)
                    {
                        f[i] = aq.exprs.get(i).evaluate();            
                    }
                    if (tmp == null)
                    {
                        tmp = aq.getTransform(f);
                        if (tmp == null) throw new NullPointerException();
                    }
                    else
                    {
                        tmp = tmp.multiply(aq.getTransform(f));
                    }
                }
            if (tmp == null) tmp = Matrix4.IDENTITY;
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            throw new ParseException("Null Transform " + this.ruleName);
        }
        return tmp;
    }

    public Matrix4 getTransformMatrix() throws ParseException 
    {
        if (superMatrix != null) return superMatrix;
        return createTransformMatrix();
    }

    public Transform()
    {
        continuationNames = new ArrayList<String>();
        continuationStack = new ArrayList<Integer>();
        acqExps = new ArrayList<TransformModifier>();   // other expressions
        acqTrs = new ArrayList<TransformModifier>();    // transforms
        defs = new ArrayList<Definition>();        
    }

    public Transform(String ruleName)
    {
        this();
        this.ruleName = ruleName;
    }
/*
    public void toImplementRestOfTr()
    {
        }
    }
*/
    public NonDeterministicRule getRule()
    {
        return rule;
    }

    public void setRule(NonDeterministicRule rule)
    {
        this.rule = rule;
        this.ruleName = rule.getName();
    }

    public String getRuleName()
    {
        return ruleName;
    }

    /**Should be called from the parser only.
     * As of dynamic generation, the possibility is open for future versions.
     * 
     * @param t
     * @param lexprs
     * @throws org.konte.model.ParseException
     */
    @SuppressWarnings( 
        value = {"unchecked"}
    )
    public void setShapeTransform(Token t, ArrayList<Expression> lexprs) throws ParseException 
    {
//        Runtime.sysoutln("SETTING " + t + ": " + lexprs, 0);
        Expression lexpr =  lexprs.get(0);
        
        if (t instanceof InnerToken)
        {
            if (t == Language.push)
            {
                for (int i = 0; i < lexprs.size(); i++)
                {
                    if (i > 0)
                        lexpr = lexprs.get(i);
                    if (!(lexpr instanceof Name))
                    {
                        throw new ParseException("Push directive must be followed by a rule name instead of:  " + lexpr);
                    }
                    continuationNames.add(((Name)lexpr).toString());
                    continuationStack.add(-1);  // will be replaced by Model.initialize() 
                }
            }
            else
            {
                
                
                
                boolean takesArray = (((InnerToken)t).higherParamCountAllowed(1) ? true : false);
                TransformModifier te = null;
//                try {
                    if (takesArray)
                        te = ((InnerToken)t).newInstance(lexprs, t);
                        //te = (TransformModifier)((InnerToken)t).getReferenceClass().getConstructor(ArrayList.class,Token.class).newInstance(lexprs,t);
                    else
                        te = ((InnerToken)t).newInstance(lexpr, t);
                        //te = (TransformModifier)((InnerToken)t).getReferenceClass().getConstructor(Expression.class,Token.class).newInstance(lexpr,t);
                    if (t instanceof AffineTransform)
                        acqTrs.add(te);
                    else
                        acqExps.add(te);
/*                } catch(InstantiationException e)
                {
                    Runtime.sysoutln("Could not initialize " +t);
                    e.printStackTrace();
                }
                catch(NoSuchMethodException e)
                {
                    Runtime.sysoutln("No constructor for " +t);
                    e.printStackTrace();
                }
                catch(IllegalAccessException e)
                {
                    Runtime.sysoutln("Illegal access near " +t);
                    e.printStackTrace();
                }
                catch(InvocationTargetException e)
                {
                    Runtime.sysoutln("Target not clear: " +t);
                    e.printStackTrace();
                }*/
            }
        }
        else {
            throw new ParseException("Unrecognized transform token " + t.name);
        }
        
    }

    
    
    
    
    private static ArrayList<DrawingContext.Def> tmpdef = new ArrayList<Def>();
    /**This continuation is picked up by RuleWriter at generate time (it is
     * removed by transform() in POP).
     * 
     */
    public int poppedContinuation = Integer.MIN_VALUE;
    /** Accepts the given DrawingContext and transforms it to a new according to all
     * the transformations that have been set in this Transform.
     * @param old
     * @return
     * @throws org.konte.model.ParseException
     */
    public DrawingContext transform(DrawingContext old) throws ParseException 
    {
        DrawingContext newt = new DrawingContext();
        if (terminatingShape)
            newt.shape = ((TerminatingShape)this).shape;
        // init all fields from st
        if (indexedNd < 0)
        {
            if (old.pushstack == null || old.pushstack.length == 0)
            {
                poppedContinuation = Integer.MIN_VALUE;
            }
            else
            {
                poppedContinuation = old.pushstack[old.pushstack.length-1]; // will be picked up by rulewriter
                if (indexedNd == -3) { // POP
                    if (old.pushstack.length > 1)
                    {
                        old.pushstack = Arrays.copyOf(old.pushstack, old.pushstack.length-1);
                    }
                    else
                    {
                        old.pushstack = null;
                    }
                }
            }
        }
        if (this.continuationStack.size() > 0)
        {
            int oldsize = old.pushstack != null ? old.pushstack.length : 0;
            int requestedSize = oldsize+this.continuationStack.size();
            int size = Math.min(requestedSize,model.pushStackSize);
            newt.pushstack = new int[size];
            int oldpos = requestedSize - size;
            requestedSize = oldpos;
            int i = 0;            
            while(oldpos < oldsize)
                newt.pushstack[i++] = old.pushstack[oldpos++];
            if (oldpos != requestedSize)
                oldpos--;
            oldpos = Math.max(0, this.continuationStack.size() - size);
            while(i < newt.pushstack.length)
                newt.pushstack[i++] = this.continuationStack.get(oldpos++);
        }
        else
        {
             newt.pushstack = old.pushstack;
        }


        // copy general parameters
        newt.d = old.d;      
        // decrement iteration level counter, if necessary
        // when the counter reaches zero, that branch will be cut from recursion
        if (newt.d > 0)
        {
            newt.d--;
        }        
        // copy colors
        newt.R = old.R;
        newt.G = old.G;
        newt.B = old.B;
        newt.A = old.A;
        newt.shading = old.shading;
//        newt.bitmap = old.bitmap;
        newt.col0 = old.col0;
        newt.layer = old.layer;
        newt.fov = old.fov;
        
        // update matrix
        newt.matrix = old.matrix.multiply(this.getTransformMatrix());        
        // update other expressions
        for (TransformModifier aq : this.acqExps)
        {
            if (aq.n > 1)
                aq.updateSTVal(newt, aq.evaluateAll());
            else {
                aq.evaluateAll();
                try {
                    aq.updateSTVal(newt, aq.values[0]);
                } catch(Exception ex) {
                    aq.evaluateAll();
                    throw ex;
                }
            }
        }
        // apply shading - moved to ruleWriter (only for final shapes)
        
        // update definition table
        if (this.defs.size() == 0)
        {
            newt.defs = old.defs;
        // if there are changes to parent,    
        // first add all definitions from old object for the transform            
        }
        else
        {
            int thisind = 0;            
            tmpdef.clear();
            if (old.defs != null) 
                for(int i=0; i < old.defs.length; i++)
                    tmpdef.add(new Def(old.defs[i].nameid,old.defs[i].defval));
            // then - assuming both tmpdef and st.defs are ordered -
            // add nonpresent definitions from st.defs to tmpdef, and
            // remove undefs of st.defs from tmpdef

            int stind = 0;
            while (thisind < tmpdef.size() || stind < this.defs.size())
            {
                Definition sd = stind>=this.defs.size() ? null : this.defs.get(stind);
                Def dd = thisind>=tmpdef.size() ? null : tmpdef.get(thisind);
                if (sd == null)
                {
                    break;
                } else if (dd == null || sd.nameId == dd.nameid)
                {
                    if (sd.isUndef)
                    {
                        if (dd!=null) tmpdef.remove(thisind);
                        stind++;
                    }
                    else
                    {
                        if (dd!=null)
                        {
                            dd.defval = sd.definition.evaluate();
                        }
                        else
                        {
                            tmpdef.add(thisind,new Def(sd.nameId,sd.definition.evaluate()));
                        }
                        thisind++;                    
                        stind++;
                    }                
                } else if (sd.nameId > dd.nameid)
                {
                    thisind++;
                } else if (sd.nameId < dd.nameid)
                {
                    tmpdef.add(thisind,new Def(sd.nameId,sd.definition.evaluate()));
                    thisind++;
                    stind++;   
                }   
            }
            newt.defs = new Def[tmpdef.size()];
            thisind = 0;
            for(Def dd : tmpdef)
                newt.defs[thisind++] = dd;                
        }
        return newt;
            
    }
    
    
    public String toString()
    {
        return String.format("\"%s\"", ruleName != null ? ruleName : "");
    }
    
    public String toStringVerbose()
    {
        StringBuilder bd = new StringBuilder();
        bd.append("{ ").append(ruleName != null ? ruleName : "").append("   ");
        for (TransformModifier aq: acqTrs)
        {
            bd.append(aq.token.name).append(":").append(aq.exprs).append(" ");
        }
        for (TransformModifier aq: acqExps)
        {
            bd.append(aq.token.name).append(":").append(aq.exprs).append(" ");
        }
        for(String ss: continuationNames)
            bd.append(" PUSH:").append(ss);
        if (defs.size()>0) bd.append(" DEFS:").append(defs);        
        bd.append("}");

        return bd.toString();
    }
}
