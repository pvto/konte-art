package org.konte.model.systems;

import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;
import org.konte.misc.Vector3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public class WorlinNoise2DSystem implements GreyBoxSystem {

    private Model model;
    private DistanceMetric metric = new DistanceMetric.EuclideanDistance();
    private float[][] points;
    private int[] boxPointers;
    private int rows;
    private float rowStep;
    private List tmp = new ArrayList();

    private static Comparator byCol3 = new Comparator() {
        @Override public int compare(Object a, Object b)
        {
            float[] A = (float[])a;
            float[] B = (float[])b;
            if (A[2] > B[2]) return 1;
            if (A[2] == B[2]) return 0;
            return -1;
        }
    };

    private void tmpAddPoints(int pointer)
    {
        int imax = pointer == boxPointers.length - 1 ? points.length : boxPointers[pointer + 1];
        for(int i = boxPointers[pointer]; i < imax; i++)
            tmp.add(points[i]);
    }

    private void tmpComputeDistances(Vector3 A, int offset)
    {
        for(int i = offset; i < tmp.size(); i++)
        {
            float[] b_ = (float[]) tmp.get(i);
            Vector3 B = new Vector3(b_[0], b_[1], 0f);
            b_[2] = metric.distance(A, B);
        }
    }

    /** returns worlin  */
    public float worlin(float x, float y, float nth)
    {
        Vector3 A = new Vector3(x, y, 0f);
        int col = (int)Math.floor(x * rows);
        int row = (int)Math.floor(y * rows);
        int pointer = row * rows + col;
        tmpAddPoints(pointer);
        tmpComputeDistances(A, 0);
        java.util.Collections.sort(tmp, byCol3);
        int place = (int) nth - 1;
        int origLen = tmp.size();
        float best = place < origLen ? ((float[])tmp.get(place))[2] : 1f;
        float lby = row * rowStep;
        float uby = lby + rowStep;
        float lbx = col * rowStep;
        float ubx = lbx + rowStep;

        if (row > 0 && best > y - lby) tmpAddPoints(pointer - rows);
        if (row < rows - 1 && best > uby - y) tmpAddPoints(pointer + rows);
        if (col > 0 && best > x - lbx) tmpAddPoints(pointer - 1);
        if (col < rows - 1 && best > ubx - x) tmpAddPoints(pointer + 1);
        if (row > 0 && col > 0 & best > metric.distance(A, new Vector3(lbx, lby, 0f))) tmpAddPoints(pointer - 1 - rows);
        if (row > 0 && col < rows - 1 & best > metric.distance(A, new Vector3(ubx, lby, 0f))) tmpAddPoints(pointer + 1 - rows);
        if (row < rows - 1 && col > 0 & best > metric.distance(A, new Vector3(lbx, uby, 0f))) tmpAddPoints(pointer - 1 + rows);
        if (row < rows - 1 && col < rows - 1 & best > metric.distance(A, new Vector3(ubx, uby, 0f))) tmpAddPoints(pointer + 1 + rows);
        if (tmp.size() > origLen) {
            tmpComputeDistances(A, origLen);
            java.util.Collections.sort(tmp, byCol3);
        }
        float ret = ((float[])tmp.get(place))[2];
        tmp.clear();
        return ret;
    }

    @Override
    public GreyBoxSystem newInstance() {
        return new WorlinNoise2DSystem();
    }

    @Override
    public void initialize(Object[] args)
    {
        String usage = "usage:  Worlin2 <n-of-points> <distance-metric := {EUCLIDEAN,MANHATTAN,CHEBYSHEV,<p-norm-exponent>}>";
        if (args.length != 2 && args.length != 3) {
            throw new RuntimeException(usage);
        }
        model = (Model) args[args.length - 1];
        if (args.length > 2) {
            Object metric = args[1];
            if (metric instanceof Float) {
                this.metric = new DistanceMetric.PNorm((Float)metric);
            } else {
                this.metric = DistanceMetric.DistanceMetrics.valueOf(metric.toString()).dm;
            }
        }
        int n = 0;
        try {
            n = ((Float)args[0]).intValue();
        } catch (Exception ex) {
            throw new RuntimeException("Worlin2 creation failed. " + usage);
        }
        points = new float[n][3];

    }

    @Override
    public void evaluate(float[] args)
    {
        Object[] lists = new Object[rows * rows];
        for(int i = 0; i < lists.length; i++)
            lists[i] = new ArrayList();
        for(int i = 0; i < points.length; i++)
        {
            int row = (int)Math.floor(points[i][1] * rows);
            int col = (int)Math.floor(points[i][0] * rows);
            int pointer = row * rows + col;
            ((List)lists[pointer]).add(points[i]);
        }
        int boxpind = 0;
        for(int i = 0; i < lists.length; i++)
        {
            List list = (List)lists[i];
            boxPointers[i] = boxpind;
            for(int j = 0; j < list.size(); j++)
                points[boxpind++] = (float[])list.get(j);
        }
    }

    @Override
    public float read(float[] args)
    {
        initInternal();
        float x = normalize(args[1]);
        float y = normalize(args[2]);
        float nth = args[3] % points.length;
        return worlin(x, y, nth);
    }

    private float normalize(float x) {
        x = x % 1f;
        if (x < 0f) {
            x = x + 1f;
        }
        return x;
    }

    @Override
    public void write(float[] args) {
        initInternal();
        points[(int)args[2]] = new float[]{ normalize(args[0]), normalize(args[1]), 0f };
        evaluate(new float[]{});
    }


    private boolean initialized = false;
    private void initInternal()
    {
        if (initialized)
            return;
        initialized = true;

        int n = points.length;
        for(int i = 0; i < n; i++)
        {
            float x = (float)model.getRandomFeed().get();
            float y = (float)model.getRandomFeed().get();
            points[i] = new float[]{ x, y, 0f };
        }
        int boxn = n / 5;
        this.rows = 1;
        while(rows * rows < boxn) rows++;
        rows = Math.max(1, rows);
        this.rows = rows;
        this.rowStep = 1f / (float)rows;
        boxPointers = new int[rows * rows];
        evaluate(new float[]{});
    }
}
