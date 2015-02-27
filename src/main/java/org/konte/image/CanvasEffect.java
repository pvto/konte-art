
package org.konte.image;

public class CanvasEffect {

    public int[][] matrix;
    public int[] copy;
    public int repeat = 1;

    public CanvasEffect(int[][] m)
    {
        matrix = m;
        copy = new int[m.length * m[0].length];
        for (int i = 0; i < m.length; i++)
        {
            System.arraycopy(m[i], 0, copy, i*m[0].length, m[0].length);
        }
    }
}
