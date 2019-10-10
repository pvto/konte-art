package org.konte.model.systems;

import org.konte.misc.Vector3;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public interface DistanceMetric {

    float distance(Vector3 a, Vector3 b);





    public static class EuclideanDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            float dx = a.x - b.x;
            float dy = a.y - b.y;
            float dz = a.z - b.z;
            return (float)Math.sqrt(dx*dx + dy*dy + dz*dz); } }

    public static class ManhattanDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            float dx = (float)Math.abs(a.x - b.x);
            float dy = (float)Math.abs(a.y - b.y);
            float dz = (float)Math.abs(a.z - b.z);
            return dx + dy + dz; } }

    public static class CanberraDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            float dx = (float)(Math.abs(a.x - b.x) / (Math.abs(a.x) + Math.abs(b.x)));
            float dy = (float)(Math.abs(a.y - b.y) / (Math.abs(a.y) + Math.abs(b.y)));
            float dz = (float)(Math.abs(a.z - b.z) / (Math.abs(a.z) + Math.abs(b.z)));
            return dx + dy + dz; } }

    public static class ChebyshevDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            double dx = Math.abs(a.x - b.x);
            double dy = Math.abs(a.y - b.y);
            double dz = Math.abs(a.z - b.z);
            return (float)Math.max(dx, Math.max(dy, dz)); } }

    public static class PNorm implements DistanceMetric {
        private double exp, cexp;
        public PNorm(double exp) { this.exp = exp; this.cexp = 1.0 / exp; }

        public float distance(Vector3 a, Vector3 b)
        {
            double dx = Math.abs(a.x - b.x);
            double dy = Math.abs(a.y - b.y);
            double dz = Math.abs(a.z - b.z);
            return (float)Math.pow(Math.pow(dx, exp) + Math.pow(dy, exp) + Math.pow(dz, exp), cexp); } }

    public static class CosineDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            double dot = a.x * b.x + a.y * b.y + a.z * b.z;
            return (float) ( dot / Math.sqrt(a.length() * b.length()) ); } }

    public static class EuclSquaredDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            float dx = a.x - b.x;
            float dy = a.y - b.y;
            float dz = a.z - b.z;
            return dx*dx + dy*dy + dz*dz; } }

    public static class MeanAbsoluteErrorDistance implements DistanceMetric {
        public float distance(Vector3 a, Vector3 b)
        {
            float dx = a.x - b.x;
            float dy = a.y - b.y;
            float dz = a.z - b.z;
            return (float) (Math.abs(dx) + Math.abs(dy) + Math.abs(dz)) / 3.0f; } }

    public static class ModularHammingDistance implements DistanceMetric {
        public float modulo;
        public ModularHammingDistance(double modulo) {
            this.modulo = (float) modulo;
        }
        private int modSim(float a, float b, float modulo) {
            return Math.abs(a - b) < modulo ? 1 : 0;
        }
        public float distance(Vector3 a, Vector3 b)
        {
            return (float)
                (modSim(a.x, b.x, modulo) + modSim(a.y, b.y, modulo) + modSim(a.z, b.z, modulo));
        } }



    public enum DistanceMetrics {
        EUCLIDEAN(new EuclideanDistance()),
        EUCLIDEAN2(new EuclSquaredDistance()),
        MANHATTAN(new ManhattanDistance()),
        CANBERRA(new CanberraDistance()),
        CHEBYSHEV(new ChebyshevDistance()),
        COSINE(new CosineDistance()),
        MAE(new MeanAbsoluteErrorDistance()),
        MODHAMMING(new ModularHammingDistance(1e-6))
        ;

        public final DistanceMetric dm;
        private DistanceMetrics(DistanceMetric m) { this.dm = m; }
    }

    public static DistanceMetric guess(Object metric)
    {
        if (metric instanceof Float) {
            return new DistanceMetric.PNorm((Float)metric);
        }
        String name = metric.toString();
        if (name.startsWith(DistanceMetrics.MODHAMMING.name())) {
            try {
                String rest = name.substring(DistanceMetrics.MODHAMMING.name().length()).trim();
                double modulo = Double.parseDouble(rest);
                return new DistanceMetric.ModularHammingDistance(modulo);
            } catch(Exception e) {
                // fallback to valueof below
            }

        }
        return DistanceMetric.DistanceMetrics.valueOf(name).dm;
    }
}
