package org.konte.model;

import java.util.ArrayList;
import org.konte.generate.RandomFeed;

/**
 *
 * @author pvto
 */
public class NonDeterministicRule {

    private ArrayList<NonDeterministicRule.WeighedRule> rules = new ArrayList<WeighedRule>();
    private double total = 0;
    private String name;
    public int id;

    public ArrayList<NonDeterministicRule.WeighedRule> getRules()
    {
        return rules;
    }
    


    public NonDeterministicRule(String name)
    {
        this.name = name;
    }

    
    public static class WeighedRule {

        double weigh = 1;
        Rule rule;

        public WeighedRule(double weigh, Rule rule)
        {
            this.weigh = weigh;
            this.rule = rule;
        }
        
        public String toString()
        {
            StringBuilder bd = new StringBuilder();
            bd.append(String.format("%.2f", weigh)).append("=");
            bd.append(rule);
            return bd.toString();        
        }
    }

    public void addRule(double weigh, Rule rule)
    {
        if (weigh > 0)
        {
            rules.add(new WeighedRule(weigh, rule));
            recalculateTotalWeigh();
        }
    }

    private void recalculateTotalWeigh()
    {
        total = 0;
        for (WeighedRule r : rules)
        {
            total += r.weigh;
        }
    }

    public String getName()
    {
        return name;
    }
    
    public Rule randomRule(RandomFeed f)
    {
        if (rules.size()==1)
            return rules.get(0).rule;
        double d = f.get()*this.total;
        double c = 0;
        WeighedRule w = null;
        for (int i = 0; c < d; c += w.weigh)
        {
            w = rules.get(i++);
        }
        return w.rule;
    }
    
    public String toString()
    {
        StringBuilder bd = new StringBuilder();
        bd.append(name).append("[").append(String.format("%.2f",total)).append("]-").append(id).append(": ");
        bd.append(rules);
        return bd.toString();        
    }
}
