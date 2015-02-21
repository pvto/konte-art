
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
            for (int j = 0; j < m[0].length; j++)
            {
                copy[i*m[0].length + j] = m[i][j];
            }

        }
    }
}
