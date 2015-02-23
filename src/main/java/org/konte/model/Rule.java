
package org.konte.model;

import java.util.ArrayList;
import java.util.List;
import org.konte.expression.BooleanExpression;
import org.konte.lang.Tokens.Constant;
import org.konte.plugin.KontePluginScript;

/**
 *
 * @author pvto
 */
public class Rule {
    
    
    public ArrayList<BooleanExpression> pre = new ArrayList<BooleanExpression>();
    
    public ArrayList<BooleanExpression> post = new ArrayList<BooleanExpression>();
    
    public ArrayList<Transform> transforms = new ArrayList<Transform>();
    
    public ArrayList<Constant> macros = new ArrayList<Constant>();

    public List<KontePluginScript> scripts;
    private NonDeterministicRule nonDeterministicRule;
    public int id;
    

    public Rule(NonDeterministicRule nonDeterministicRule)
    {
        this.nonDeterministicRule = nonDeterministicRule;
    }
    public Rule() { }

    public void addScript(KontePluginScript script)
    {
        if (scripts == null)
            scripts = new ArrayList<KontePluginScript>();
        scripts.add(script);
    }

    public void setNonDeterministicRule(NonDeterministicRule nonDeterministicRule)
    {
        this.nonDeterministicRule = nonDeterministicRule;
    }

    public NonDeterministicRule getNonDeterministicRule()
    {
        return nonDeterministicRule;
    }
    

    public void addMacro(Constant macro)
    {
        macros.add(macro);
    }
    
    public void addPre(BooleanExpression expr)
    {
        pre.add(expr);
    }
    
    public void addPost(BooleanExpression expr)
    {
        post.add(expr);
    }

    public void addTransform(Transform rule)
    {
        transforms.add(rule);
    }
    
    public String getName() { return nonDeterministicRule.getName(); }

    public void setId(int id)
    {
        this.id = id;
    }
    public String toString()
    {
        return String.format("rule %s", nonDeterministicRule.getName());
    }
    public String toStringVerbose()
    {
        StringBuilder bd = new StringBuilder();
        bd.append(transforms);
        if (!pre.isEmpty()) bd.append(" pre").append(pre);
        if (!post.isEmpty()) bd.append(" post").append(post);
        return bd.toString();        
    }


    
}
