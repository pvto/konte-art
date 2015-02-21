/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.konte.misc;

/**
 *
 * @author Paavo
 */
public class Misc {
    
    public static String replicate(String s, int n) {
        StringBuilder bd = new StringBuilder();
        for(int i=  0; i < n; i++)
            bd.append(s);
        return bd.toString();
    }
}
