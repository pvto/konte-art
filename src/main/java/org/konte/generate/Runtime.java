package org.konte.generate;

import org.konte.lang.Language;

public class Runtime {

    /** Generate time info to GUI etc.
     *
     */
    public static StateServer stateServer = new StateServer();

    public static void sysoutln(Object str)
    {
        if (stateServer.addState(str.toString()))
        {
            System.out.println(str);
        }
    }

    public static void sysoutln(Object str, int prodlev)
    {
        if (prodlev >= Language.output_verbose_filter)
            
            if (stateServer.addState(str.toString()))
            {
                System.out.println(str);
            }
    }
    
    public static void sysout(Object str, int prodlev)
    {
        if (prodlev >= Language.output_verbose_filter)
            if (stateServer.addState(str.toString()))
            {
                System.out.print(str);
            }
    }
}
