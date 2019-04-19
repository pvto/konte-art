package org.konte.model.systems;

/**
 *
 * @author Paavo Toivanen https://github.com/pvto
 */
public interface GradientFunction {

    float gradient(float u0, float u1, float u);





    public static class LinearGradient implements GradientFunction {
        public float gradient(float u0, float u1, float u)
        {
            return u0 + (u1 - u0) * u; } }

    public static class PowGradient implements GradientFunction {
        public final double exponent;
        public PowGradient(double exponent) { this.exponent = exponent; }
        public float gradient(float u0, float u1, float u)
        {
            return u0 + (u1 - u0) * (float) Math.pow(u, exponent);
        }
    }

    public static class SmoothstepGradient implements GradientFunction {
        public float gradient(float u0, float u1, float u)
        {
            return u0 + (u1 - u0) * u*u*u*(u * (u*6 - 15) + 10); } }

    public static class SinGradient implements GradientFunction {
        public float gradient(float u0, float u1, float u)
        {
            return u0 + (u1 - u0) * (float)Math.sin(u * Math.PI / 2.0); }
        }


    public enum Gradients {
        LINEAR(new LinearGradient()),
        SQUARE(new PowGradient(2.0)),
        CUBIC(new PowGradient(3.0)),
        QUADRATIC(new PowGradient(4.0)),
        SMOOTHSTEP(new SmoothstepGradient()),
        SIN(new SinGradient())
        ;

        public final GradientFunction gf;
        private Gradients(GradientFunction f) { this.gf = f; }
    }

}
