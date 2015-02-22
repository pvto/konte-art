package org.konte.lang.func;

import static org.konte.lang.Tokens.*;

/**Pre-defined floating point functions.
 *
 * {@see org.konte.plugin.PluginLoader} for loading dynamic functions.
 *
 * @author pvto
 */
public class Mathm {

    public static class ESin extends Function1 {

        public ESin(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.sin(val[0]);
        }
    }

    public static class ECos extends Function1 {

        public ECos(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.cos(val[0]);
        }
    }

    public static class ETan extends Function1 {

        public ETan(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.tan(val[0]);
        }
    }

    public static class EAsin extends Function1 {

        public EAsin(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.asin(val[0]);
        }
    }

    public static class EAcos extends Function1 {

        public EAcos(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.acos(val[0]);
        }
    }

    public static class EAtan extends Function1 {

        public EAtan(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.atan(val[0]);
        }
    }

    public static class EAbs extends Function1 {

        public EAbs(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return Math.abs(val[0]);
        }
    }

    public static class ESqrt extends Function1 {

        public ESqrt(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.sqrt(val[0]);
        }
    }

    public static class ELog extends Function1 {

        public ELog(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.log(val[0]);
        }
    }

    public static class EPow extends Function2 {

        public EPow(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.pow(val[0], val[1]);
        }
    }

    public static class ERandom extends Function0 {

        public ERandom(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.random();
        }
    }

    public static class EIRand extends Function1 {

        public EIRand(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.round(Math.random() * val[0]);
        }
    }

    public static class EFloor extends Function1 {

        public EFloor(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.floor(val[0]);
        }
    }

    public static class EMean extends FunctionN_ {

        public EMean(String name) {
            super(name);
        }

        @Override
        public float value(float... args) {
            float sum = 0;
            for (float f : args) {
                sum += f;
            }
            return sum / args.length;
        }
    }

    public static class EMax extends Function2 {

        public EMax(String name) {
            super(name);
        }

        @Override
        public float value(float... args) {
            return Math.max(args[0], args[1]);
        }
    }

    public static class EMin extends Function2 {

        public EMin(String name) {
            super(name);
        }

        @Override
        public float value(float... args) {
            return Math.min(args[0], args[1]);
        }
    }

    public static class ERound extends Function1 {

        public ERound(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            return (float) Math.round(val[0]);
        }
    }

    public static class ESawWave extends Function1 {

        public ESawWave(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            float tmp = (val[0] < -0.5f ? -val[0] + 0.5f : val[0] + 0.5f) % 1f - 0.5f;
            if (Math.abs(val[0] + 0.5f) % 2f >= 1f) {
                tmp = -tmp;
            }
            return tmp;
        }
    }

    public static class ESquareWave extends Function1 {

        public ESquareWave(String name) {
            super(name);
        }

        @Override
        public float value(float... val) {
            if (val[0] % 2 < 1) {
                return 0.5f;
            }
            return -0.5f;
        }
    }

    public static class EHipass extends Function2 {
        public EHipass(String name) {
            super(name);
        }
        @Override
        public float value(float... val) {
            if (val[0] >= val[1])
                return val[0];
            return 0f;
        }
    }

    public static class ELowpass extends Function2 {
        public ELowpass(String name) {
            super(name);
        }
        @Override
        public float value(float... val) {
            if (val[0] <= val[1])
                return val[0];
            return 0f;
        }
    }
}
