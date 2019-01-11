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

    public enum DistanceMetrics {
        EUCLIDEAN(new EuclideanDistance()),
        MANHATTAN(new ManhattanDistance()),
        CHEBYSHEV(new ChebyshevDistance()),
        COSINE(new CosineDistance())
        ;

        public final DistanceMetric dm;
        private DistanceMetrics(DistanceMetric m) { this.dm = m; }
    }

}
