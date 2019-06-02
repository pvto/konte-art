package org.konte.model.systems;

import org.konte.misc.Vector3;
import org.konte.model.GreyBoxSystem;
import org.konte.model.Model;

import java.util.*;

public class TriangulationSystem implements GreyBoxSystem {

    ArrayList<Vector3> points = new ArrayList<>();
    float[][] distances;
    ArrayList<int[]> triangles;
    private ArrayList<Seg> hull;

    private Model model;

    DistanceMetric metric = DistanceMetric.DistanceMetrics.EUCLIDEAN.dm;

    @Override
    public GreyBoxSystem newInstance() { return new TriangulationSystem(); }

    private static class A3FDist implements Comparator<float[]> {
        @Override
        public int compare(float[] a, float[] b) {
            if (a[3] == b[3]) return 0;
            if (a[3] > b[3]) return 1;
            return -1;
        }
    }
    private static class SegSort implements Comparator<Seg> {
        @Override
        public int compare(Seg a, Seg b) {
            int amin = Math.min(a.a, a.b);
            int bmin = Math.min(b.a, b.b);
            if (amin < bmin) return -1;
            if (amin > bmin) return 1;
            int amax = Math.max(a.a, a.b);
            int bmax = Math.max(b.a, b.b);
            if (amax < bmax) return -1;
            if (amax > bmax) return 1;
            return 0;
        }
    }

    private static Comparator<float[]> a4f = new A3FDist();
    private static Comparator<Seg> segSorter = new SegSort();

    private static float coef(Vector3 a, Vector3 b)
    {
        if (a.x == b.x) return 1e7f;
        return (b.y - a.y) / (b.x - a.x);
    }
    private static float coef(float[] a, float[] b)
    {
        if (a[0] == b[0]) return 1e7f;
        return (b[1] - a[1]) / (b[0] - a[0]);
    }

    private class Seg {
        int a, b;
        int triangle;
        int seg;
        double ang;
        Seg(int a, int b, int triangle, int seg)
        {
            this.a = a;  this.b = b;
            this.triangle = triangle;
            this.seg = seg;
            this.ang = angle(distances[a], distances[b]);
        }
    }
    private void addSegments(ArrayList<Seg> hull, int[] triangle, int ind)
    {
        hull.add(new Seg(triangle[0], triangle[1], ind, 0));
        hull.add(new Seg(triangle[1], triangle[2], ind, 1));
        hull.add(new Seg(triangle[2], triangle[0], ind, 2));
    }

    private void addPointTri(HashMap<Integer, List<Integer>> ptris, int point, int tri)
    {
        List<Integer> tris = ptris.get(point);
        if (tris == null) {
            tris = new ArrayList<Integer>();
            ptris.put(point, tris);
        }
        tris.add(tri);
    }

    @Override
    public void initialize(Object[] args)
    {
        if (args.length > 0) {
            model = (Model)args[args.length - 1];
        }
        if (args.length > 1) {
            this.metric = DistanceMetric.guess(args[0]);
        }
        if (points.size() == 0)
            return;


        // delaunay triangulation using sweep-hull algorithm
        // http://www.s-hull.org/paper/s_hull.pdf

        // select hull center a
        int first = (int) (model.getRandomFeed().get() * points.size());
        Vector3 a = points.get(first);

        // order by distance from center, accept closest point b as another tri vertex
        distances = new float[points.size()][];
        distances[0] = new float[]{a.x, a.y, a.z, 0f};
        for(int i = 0, j = 1; i < points.size(); i++)
        {
            if (i == first) continue;
            Vector3 v = points.get(i);
            float dist = metric.distance(a, v);
            distances[j] = new float[]{v.x, v.y, v.z, dist};
            j++;
        }
        Arrays.sort(distances, a4f);
        Vector3 b = new Vector3(distances[1]);

        // find smallest circum-circle, accept third point c
        Vector3 circum = null;
        int ind = 2;
        for(int i = 2; i < distances.length; i++)
        {
            Vector3 tmp = circumCircle(a, b, new Vector3(distances[i]));
            if (tmp != null)
            {
                tmp.z = metric.distance(a, tmp);
                if (circum == null || tmp.z < circum.z) {
                    circum = tmp;
                    ind = i;
                }
            }
        }
        float[] cd = distances[ind];
        distances[ind] = distances[2];
        distances[2] = cd;

        // order a,b,c by right hand rule
        double anga = Math.atan2(a.y - circum.y, a.x - circum.x);
        double angb = Math.atan2(b.y - circum.y, b.x - circum.x);
        double angc = Math.atan2(cd[1] - circum.y, cd[0] - circum.x);
        double ab = rhdist(anga, angb);
        double ac = rhdist(anga, angc);
        if (ac < ab) {
            distances[2] = distances[1];
            distances[1] = cd;
        }

        // resort by |x-circum|
        for(int i = 3; i < distances.length; i++)
            distances[i][3] = metric.distance(circum, new Vector3(distances[i]));
        distances[0][3] = -3; // keep first three points in first slots, no matter triangle shape
        distances[1][3] = -2;
        distances[2][3] = -1;
        Arrays.sort(distances, a4f);

        // create convex hull iteratively with initial triangulation
        ArrayList<int[]> triangles = new ArrayList<>();
        ArrayList<Seg[]> sharedSegments = new ArrayList<>();
        triangles.add(new int[]{0,1,2});
        ArrayList<Seg> hull = new ArrayList<>();
        addSegments(hull, triangles.get(0), 0);
        HashMap<Integer, List<Integer>> pointTriangle = new HashMap<>();
        addPointTri(pointTriangle, 0, 0);
        addPointTri(pointTriangle, 1, 0);
        addPointTri(pointTriangle, 2, 0);
        for(int i = 3; i < distances.length; i++)
        {
            float[] p = distances[i];
            int end = hull.size();
            List<Seg> toHull = new ArrayList<>();
            for(int h = 0; h < end; h++)
            {
                Seg s = hull.get(h);
                double angh = s.ang;
                float[] p0 = distances[s.a];
                anga = angle(p, p0);
                //angb = ang(p, distances[s.b]);
                double rhda = rhdist(anga, angh);
                if (rhda-1e-7 <= Math.PI || rhda == Math.PI*2d)
                    continue;
                hull.remove(h--);
                end--;
                ind = triangles.size();
                triangles.add(new int[]{s.a, i, s.b});
                Seg ha = new Seg(s.a, i, ind, 0);
                Seg hb = new Seg(i, s.b, ind, 1);
                toHull.add(ha);
                toHull.add(hb);
                addPointTri(pointTriangle, s.a, ind);
                addPointTri(pointTriangle, s.b, ind);
                addPointTri(pointTriangle, i, ind);
            }
            toHull.sort(segSorter);
            for(int h = 0; h < toHull.size(); h++)
            {
                Seg s = toHull.get(h);
                boolean isShared = h > 0 && segSorter.compare(toHull.get(h-1), s) == 0
                    || h < toHull.size()-1 && segSorter.compare(s, toHull.get(h+1)) == 0;
                if (!isShared)
                    hull.add(s);
            }
        }


        // "flip" adjacent triangles if their shared side will be shortened... does this conform to proper delaunay, probably not
        flipAdjacent(triangles, pointTriangle);

        this.triangles = triangles;
        this.hull = hull;
    }

    private void removePointTriangle(HashMap<Integer, List<Integer>> pointTriangle, int point, int tri)
    {
        List<Integer> tris = pointTriangle.get(point);
        int ind = tris.indexOf(tri);
        if (ind >= 0)
            tris.remove(ind);
    }
    private void addPointTriangle(HashMap<Integer, List<Integer>> pointTriangle, int point, int tri)
    {
        List<Integer> tris = pointTriangle.get(point);
        int ind = tris.indexOf(tri);
        if (ind < 0)
            tris.add(-ind, tri);
    }
    private void flip(int[] tri, int a, int b) {
        int tmp = tri[a];
        tri[a] = tri[b];
        tri[b] = tmp;
    }
    private void flipAdjacent(ArrayList<int[]> triangles, HashMap<Integer, List<Integer>> pointTriangle)
    {
        int exFlips = -1;
        int exChange = 0;
        int staticChange = 0;
        int flips = 0;
        for(int z = 0; z < 24 && exFlips != flips && staticChange < 4; z++) {
            exFlips = flips;
            for (int p = (int) (model.randomFeed.get()*distances.length), zzi = 0; zzi < distances.length; p=(p+1)%distances.length, zzi++) {
                List<Integer> tris = pointTriangle.get(p);
                if (tris == null) continue;
                //if (z == 0)
                tris.sort((Integer t0, Integer t1) -> t0 - t1);
                for (int i = 0; i < tris.size() - 1; i++) {
                    int t = tris.get(i);
                    int[] tri = triangles.get(t);
                    for (int j = i + 1; j < tris.size(); j++) {
                        int pib = tri[0] == p ? 0 : (tri[1] == p ? 1 : 2);
                        int u = tris.get(j);
                        int[] trj = triangles.get(u);

                        int pjb = trj[0] == p ? 0 : (trj[1] == p ? 1 : 2);
                        if (trj[pjb] != p)
                            continue;
                        int pic = (pib + 1) % 3;  // bcd -> acd
                        int pid = (pib + 2) % 3;
                        int pja = (pjb + 1) % 3;
                        int pjc = (pjb + 2) % 3;  // cba -> cab
                        if (tri[pic] == trj[pjc]) {}
                        else if (tri[pic] == trj[pja]) { flip(trj, pjc, pja); }
                        else if (tri[pid] == trj[pjc]){ flip(tri, pic, pid); }
                        else if (tri[pid] == trj[pja]) { flip(tri, pic, pid); flip(trj, pjc, pja); }
                        else { continue; }
                        int b, c, d, a;
                        b = p;
                        c = tri[pic];
                        d = tri[pid];
                        a = trj[pja];
                        float[] b_ = distances[b];
                        float[] c_ = distances[c];
                        float[] d_ = distances[d];
                        float[] a_ = distances[a];
                        double angcd = angle(c_, d_);
                        double angca = angle(c_, a_);
                        double angcb = angle(c_, b_);
                        if (!testConvexTri(angcd, angcb, angca))
                            continue;
                        double angba = angle(b_, a_);
                        double angbd = angle(b_, d_);
                        if (!testConvexTri(angba, complementAngle(angcb), angbd))
                            continue;
                        // length check: only if ad is shorter than bc, switch
                        if (metric.distance(new Vector3(a_), new Vector3(d_))
                                >= metric.distance(new Vector3(b_), new Vector3(c_)))
                            continue;
                        //   d      d
                        //  bc ->  ac
                        //  a      b
                        removePointTriangle(pointTriangle, tri[pib], t);
                        removePointTriangle(pointTriangle, trj[pjc], u);
                        tri[pib] = trj[pja];
                        trj[pjc] = tri[pid];
                        addPointTriangle(pointTriangle, tri[pib], t);
                        addPointTriangle(pointTriangle, trj[pjc], u);
                        flips++;
                        break;
                    }
                }
            }
            int change = flips-exFlips;
            if (exChange == change)
                staticChange++;
            org.konte.generate.Runtime.sysoutln("flips=" + flips + "/" + exFlips + " " + (flips-exFlips), 10);
            exChange = change;
        }
    }

    /*

                | 3/4pi
                |
                |
                .
       2pi-----.a--------.b  0



           pi   b.--------a
                          .
                          |
                          |
                          |  1/2pi
     */

    public static double angle(float[] a, float[] b)
    {
        return Math.atan2(b[1] - a[1], b[0] - a[0]);
    }

    public static double complementAngle(double a)
    {
        double c = Math.PI + a;
        if (c > Math.PI * 2d)
            c = c % (Math.PI * 2d);
        return c;
    }
    public static double rhdist(double a, double b) {
        if (b < a) {
            return b + 2.0*Math.PI - a;
        }
        double ret = b - a;
        if (ret > Math.PI * 2d)
            ret -= Math.PI * 2d;
        return ret;
    }

    public static double angdist(double a, double b)
    {
        if (a >= 0 && b < 0 && a >= Math.PI / 2.0) {
            return b + 2.0*Math.PI - a;
        }
        else if (b >= 0 && a < 0 && b >= Math.PI / 2.0) {
            return a + 2.0*Math.PI - a;
        }
        return b - a;
    }

    public static boolean angleRhBetween(double a, double between, double b)
    {
        return rhdist(a, between) < rhdist(a, b);
    }

    public static boolean testConvexTri(double a, double b, double c)
    {
        double angledist = rhdist(a, c);
        boolean bsplits = angleRhBetween(a, b, c);
        if (!bsplits) angledist = Math.PI * 2d - angledist;
        return angledist < Math.PI;
    }


    public static Vector3 circumCircle(Vector3 a, Vector3 b, Vector3 c) {
        float a1 = b.y == a.y ? 0f : -(b.x-a.x) / (b.y-a.y);
        float a2 = c.y == a.y ? 0f : -(c.x-a.x) / (c.y-a.y);
        if (a1 == a2)
            return null;
        float abx = (a.x + b.x) / 2f;
        float aby = (a.y + b.y) / 2f;
        float acx = (a.x + c.x) / 2f;
        float acy = (a.y + c.y) / 2f;
        float cx = (acy - aby + abx*a1 - acx*a2) / (a1 - a2);
        float cy = a1*cx + aby - a1*abx;
        //float r2 = (cx - abx)*(cx - abx) + (cy - aby)*(cy - aby);
        return new Vector3(cx, cy, 0f);
    }

    /*
y = ax + b
a = (y2-y1) / (x2-x1)
b = y1 - a*x1

a1 * (x - x1) + y1 = a2 * (x - x2) + y2
x * (a1 - a2) = a2*-x2 + y2 - y1 + a1*x1
x = (y2 - y1 + x1*a1 - x2*a2) / (a1 - a2)
  = cx
  find cy by subst
     */
    @Override
    public void evaluate(float[] args) {
        initialize(new Object[]{});
    }

    @Override
    public float read(float[] args) {
        int tri = (int)args[1];
        if (tri == -1)
            return triangles.size();
        if (tri >= triangles.size()) tri = triangles.size()-1;
        int vertex = (int)args[2];
        int xy = (int)args[3];
        return distances[triangles.get(tri)[vertex]][xy];
    }

    @Override
    public void write(float[] args) {
        points.add(new Vector3(args[1], args[2], args.length>3?args[3]: 0));
    }
}
