package org.konte.lang.help;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import org.konte.lang.AffineTransform;
import static org.konte.lang.Tokens.*;
import org.konte.lang.Language;

/**Language help facility. Uses help.properties.
 *
 * @author pvto
 */
public class Help {

    private static final HashMap<String,String> mp = new HashMap<String,String>(100);
    
    private Help() { }
    
    public static void addReference(String keyword, String explanation)
    {
        mp.put(keyword, explanation);
    }
    
    static
    {
        Properties props = new Properties();
        try
        {
            props.load(Help.class.getResourceAsStream("help.properties"));
        }
        catch(Exception e)
        {
            System.out.println("Help properties file not found");
        }
        for (Entry e : props.entrySet())
        {
            mp.put((String)e.getKey(), (String)e.getValue());
        }
    }
    
    private static String[][] keywords = {
        {"ALL","all"},
        {"INTRO","introduction","intro","topics","help"},
        {"TOKENS","tokens"},
        {"CONTEXTS","contexts","parsing"},
        {"INNER","inner","inner tokens","transform","transforms"},
        {"INNER_O","color","colors","rgb","shadings","other"},
        {"INNER_EXP","backreference","expressions"},
        {"AFFINE","affine","affine transforms","spatial","space"},
        {"CONTROLS","control","operator","operators"},
        {"COMPARATORS","comparator","comparators"},
        {"FUNCTIONS","function","functions"}
        
    };
    public enum Topic {
        ALL,INTRO, MODEL, TOKENS, CONTEXTS, INNER, AFFINE, INNER_O, INNER_EXP, CONTROLS, COMPARATORS, FUNCTIONS;
        private Topic() { }
        private Class getClassTyp()
        {
            switch(this)
            {
                case CONTEXTS: return Context.class;
                case INNER: return InnerToken.class;
                case AFFINE: return AffineTransform.class;
                case INNER_EXP: return InnerExpressiveToken.class;
                case CONTROLS: return ControlToken.class;
                case COMPARATORS: return Comparator.class;
                case FUNCTIONS: return Function.class;
                default:
                    return null;
            }
        }
        private static Topic getTopic(String keyword)
        {
            for(String[] ss : keywords)
            {
                try
                {
                    Topic t = Topic.valueOf(Topic.class, ss[0]);
                    for (int i=1 ; i < ss.length; i++) 
                        if (ss[i].equals(keyword))
                            return t;
                }
                catch(Exception ex)
                {
                    
                }
            }
            return null;
        }
    }

    private static int greyCounter = 0;
    public static String getExplanation(String keyword)
    {
        String s = null;
        Token t = Language.tokenByName(keyword);
        if (t!=null)
        {
            if (((s = mp.get(t.name)) == null))
                for (String s2 : t.aliases) 
                    if ((s = mp.get(s2)) != null) break;
        }
        if (s==null)
        {
            s = mp.get(keyword);
        }
        Topic top = Topic.getTopic(keyword);
        if (top != null)
        {
            if (s == null)
            {
                s = helpTopic(top);
            }
            if (s.contains("<tr>"))
            {
                return String.format("</table><table  class='konteHelp' >%s</table><table class='konteHelp'>\n", s);
            }
            return String.format("</table><table  class='konteHelp' ><tr><td>%s</td></tr></table><table class='konteHelp'>\n",s);
        }
        if (s==null)
        {
            s = String.format("<tr><td>No help found on topic \"%s\"</td></tr>",keyword);
        }
        else
        {
            s = String.format("<tr %s><td class='konte_tbl_first'>\n<a name='%s'><b><code>%s</code></b></a>\n</td>\n<td>\n<p>%s</td></tr>\n",
                    greyCounter++ %2 == 0 ? "style='background-color:#dddddd;'" : "",
                    (t != null) ? t.name : keyword,
                    (t != null) ? t.toHelpString() : keyword,
                    s);
        }
        return s;
    }
    private static String om(String str, boolean omitXml)
    {
        if (!omitXml)
        {
            for(Token t: Language.tokens)
            {
                if (t instanceof Operator ||
                        t instanceof Context ||
                        t instanceof ControlToken)
                    continue;

                str = str.replaceAll("[\\s>]("+t.name+")[\\s,\\.]",
                        String.format(" <a href='#%s'>%s</a> ", t.name, "$1"));
                for (String name : t.aliases)
                {
                    str = str.replaceAll("[\\s>]("+name+")\\s",
                        String.format(" <a href='#%s'>%s</a> ", t.name, "$1"));

                }
            }
            str = str.replaceAll("Usage", "<span class='HL'>Usage</span>");
            return str;
        }
        return str.replaceAll("<[^>]*>", "");
    }
        
    
    public static String help(String keyword, boolean omitXml)
    {
        return om(new StringBuilder().append("\n\t").
                append("<table class='konteHelp'>").
                append(getExplanation(keyword)).
                append("</table>").
                append("\n").toString(), 
                omitXml)
                ;
    }
    private static String help(Token t)
    {
         return help(t.name, false);
    }

    private static String getExplTop(Topic topic)
    {
        switch(topic)
        {
            case ALL:
            case INTRO:
                return "";
            case MODEL:
            case COMPARATORS:
            case FUNCTIONS:
                return topic.toString();
            case AFFINE:
                return "SPATIAL TRANSFORM TOKENS";
            case INNER_O:
                return "MISCELLANEOUS TRANSFORM TOKENS (colors, iteration control, layers, etc.)";
            default:
                return "";

        }
    }

    public static String helpTopic(Topic topic)
    {
        StringBuilder bd = new StringBuilder();
        String exp = getExplTop(topic);
        if (exp.length() > 0)
            bd.append("</table><table><tr><td><H4 class='konteHelpH4'>-- "+exp+":</H4></td></tr></table>\n<table class='konteHelp'>\n\n");
        switch(topic)
        {
            case ALL:
                bd.append(helpTopic(topic.INTRO));
                bd.append(helpTopic(topic.MODEL));
                bd.append(helpTopic(topic.AFFINE));
                bd.append(helpTopic(topic.INNER_O));
                bd.append(helpTopic(topic.CONTROLS));
                bd.append(helpTopic(topic.COMPARATORS));
                bd.append(helpTopic(topic.FUNCTIONS));
                break;
            case INTRO: 
            case MODEL:
                bd.append(getExplanation(topic.toString().toLowerCase()));
                break;
            case TOKENS:
                for (Token t : Language.tokens)
                {
                    if (!(t instanceof Function || t instanceof InnerToken || t instanceof Comparator))
                        bd.append(getExplanation(t.name));
                }
                break;
            case INNER_O:
                for (Token t : Language.tokens)
                {
                    if (t instanceof InnerToken && !(t instanceof AffineTransform))
                        bd.append(getExplanation(t.name));
                }                
                break;
            case CONTEXTS:
            case INNER:
            case AFFINE:
            case INNER_EXP:
            case CONTROLS:
            case COMPARATORS:
            case FUNCTIONS:
                for (Token t : Language.tokens)
                {
                    if (topic.getClassTyp().isInstance(t))
                        bd.append(getExplanation(t.name));
                }
                break;                
                
        }
        return bd.toString();
    }
    
    public static void main(String[] args)
    {
        System.out.println(Help.help("all",false));
        System.out.println(Help.help("sz",false));
        System.out.println(Help.help("affine",true));
        System.out.println(Help.help("backreference",false));
        System.out.println(Help.help("all",true));
    }

}
