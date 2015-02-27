
package org.konte.lang;

import java.util.Map;
import org.konte.expression.Expression;
import java.util.BitSet;
import org.konte.plugin.KonteScriptExtension;
import org.konte.lang.func.Mathm;
import org.konte.model.Untransformable;
import org.konte.model.UtConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.konte.expression.Value;
import org.konte.lang.func.Fractal;
import org.konte.lang.func.Func;
import org.konte.lang.func.Img;
import org.konte.model.TransformModifier;
import org.konte.plugin.PluginLoader;
import static org.konte.lang.Tokens.*;

/**Many of the language features are defined here.
 *
 * @author pvto
 */
public class Language {
    
    public static final double version = 0.9;
    /** output log level*/
    public static int output_verbose_filter = 0;

    /** All named tokens in this language */
    public static final List<Token> tokens = new ArrayList<Token>();
    /** Parse/generate-time references to tokens for fast access*/
    public static Map<String,Token> tokenReferences;
    /** defined output shapes for parse time and generate time PUSH/POP access */
    public static final Map<Integer,Untransformable> utref = new
            HashMap<Integer,Untransformable>();


    /**Plugin extensions for inline scripting register here */
    public static final List<KonteScriptExtension> scriptExtensions =
            new ArrayList<KonteScriptExtension>();
    
    public static Token addToken(Token token) {
        tokens.add(token);
        return token;
    }
    /** <p>Constants can be added at parse time. These are global constants,
     * i.e. executed globally at any single phase of generation.
     * 
     * @param constant constant name-value mapping
     * @return the added Constant
     */
    public static Constant addConstant(String name, float val) {
        Constant constant = null;
        try {
            constant = new Constant(name, new Value(val), true);
            addToken(constant);
        }
        catch(Exception ex) 
        {

        }
        return constant;
    }
    public static Untransformable addUntransformable(Untransformable ut) {
        utref.put(ut.getId(), ut);
        addToken(ut);
        return ut;
    }
    public static Untransformable getUntransformable(int ind) {
        return utref.get(ind);
    }

    /* Each character in a {@link ControlToken} will be found on this list -
     * which then defines boundaries for valid names */
    public static BitSet controlCharacters = new BitSet(1024);
            
    
    public static void addControlCharacters(String name) {
        for (char c : name.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isLetter(c)) {
                controlCharacters.set((int)c);
            }
        }
    }
    
    /** Function-like construct that is used for building
     * objects (for instance shading points) in the model  */
    public static class LanguageFunctor extends Function {
        int argsCount = -1;
        public LanguageFunctor(String name) { super(name); }
        public LanguageFunctor(String name, int argsCount) {
            super(name);
            this.argsCount = argsCount;
        }
        public int getArgsCount() { return argsCount; }
        @Override
        public float value(float... args) throws Exception 
        {
            return 0f;
        }
    }
    
    
    
    
    
    /** whether ; is used between boolean expressions -- equivalent to inclusive or*/
    public static boolean IS_SEMICOLON_SEPARATORS = true;
    
    
    // expression priorities
    public static final int OR_PRIORITY = 1;
    public static final int AND_PRIORITY = 2;
    public static final int COMPARISON_PRIORITY = 3;
    public static final int ADD_PRIORITY = 4;
    public static final int MULTIPLY_PRIORITY = 5;
    public static final int POWER_PRIORITY = 6;
    public static final int NEGATION_PRIORITY = 7;
    public static final int FUNCTION_PRIORITY = 8;
    public static final int VALUE_PRIORITY = 9;
    public static final int BRACKET_PRIORITY = 10;
    

    // misc keywords: (type-index,[list])
    public static int MISC_model = 0;
    public static String[][] miscKeywords = new String[][] {
        new String[] {
            "pushstack", "maxshapes", "feature", "order", "streamrate"
        }

    };
    
    // enumerate keywords in the language
    
    public static Token model = addToken(new Token("model"));
    public static Token include = addToken(new Token("include"));
    public static Token push_stack = addToken(new Token(miscKeywords[MISC_model][0]));
    public static Token max_shapes = addToken(new Token(miscKeywords[MISC_model][1]));
    public static Token feature_size = addToken(new Token(miscKeywords[MISC_model][2]));
    public static Token order = addToken(new Token(miscKeywords[MISC_model][3]));
    public static Token stream_rate = addToken(new Token(miscKeywords[MISC_model][4]));
    public static Token startshape = addToken(new Token("startshape"));
    public static Token background = addToken(new Token("background"));
    public static Token light = addToken(new Token("light"));
    public static Token fov = addToken(new InnerToken("fov", TransformModifier.fov.class,1) {
        public TransformModifier newInstance(Expression e, Token t) {
            return new TransformModifier.fov(e, t);
        }
    });  // other InnerTokens are introduced below
    public static Token fx = addToken(new Token("fx"));
    public static Token rule = addToken(new Token("rule"));
    public static Token path = addToken(new Token("path"));
    public static Token moveto = addToken(new LanguageFunctor("moveto", 3));
    public static Token bend = addToken(new LanguageFunctor("bend", 3));
    public static Token lineto = addToken(new LanguageFunctor("lineto", 3));
    public static Token curveto = addToken(new LanguageFunctor("curveto", 3));
    public static Token close = addToken(new Token("close"));
    public static Token pre_context = addToken(new Token("pre"));
    public static Token post_context = addToken(new Token("post"));
    public static Token ifToken = addToken(new Token("if"));
//    public static Token fail = addToken(new Token("fail"));
    public static Token point = addToken(new LanguageFunctor("point") {
        @Override
        public boolean nArgsAllowed(int n) {
            if (n > 0 && n < 4)
                return true;
            return false;
        }
    });
    //shading is InnerExpressiveToken (two-fold usage)

    public static Token def = addToken(new Token("DEF"));
    public static Token undef = addToken(new Token("UNDEF"));
    public static Token macro = addToken(new Token("MACRO"));
    
    public static Token comment = addToken(new Context("//"));
    public static Token comment_start = addToken(new Context("/*"));
    public static Token comment_end = addToken(new Context("*/"));
    public static Token left_curl = addToken(new Context("{"));
    public static Token right_curl = addToken(new Context("}"));
    public static Token left_bracket = addToken(new Context("("));
    public static Token right_bracket = addToken(new Context(")"));
    public static Token left_squarebracket = addToken(new Context("["));
    public static Token right_squarebracket = addToken(new Context("]"));
    public static Token hyphen = addToken(new Context("\""));
    
    public static Token comma = addToken(new ControlToken(","));
    public static Token semicolon = addToken(new ControlToken(";"));

    
    public static Token and = addToken(new Comparator("&&"));
    public static Token or = addToken(new Comparator("||"));
    public static Token equals = addToken(new Comparator("="));
    public static Token ne = addToken(new Comparator("!="));
    public static Token lt = addToken(new Comparator("<"));
    public static Token lte = addToken(new Comparator("<="));    
    public static Token gt = addToken(new Comparator(">"));
    public static Token gte = addToken(new Comparator(">="));    
    public static Token add = addToken(new Operator("+"));
    public static Token subtract = addToken(new Operator("-"));
    public static Token multiply = addToken(new Operator("*"));
    public static Token divide = addToken(new Operator("/"));
    public static Token modulo = addToken(new Operator("%"));
    public static Token pow_op = addToken(new Operator("**"));

    public static Token inc = addToken(new Func.EInc("inc", null));
    public static Token sin = addToken(new Mathm.ESin("sin"));
    public static Token cos = addToken(new Mathm.ECos("cos"));
    public static Token tan = addToken(new Mathm.ETan("tan"));
    public static Token asin = addToken(new Mathm.EAsin("asin"));
    public static Token acos = addToken(new Mathm.EAcos("acos"));
    public static Token atan = addToken(new Mathm.EAtan("atan"));
    public static Token abs = addToken(new Mathm.EAbs("abs"));
    public static Token sqrt = addToken(new Mathm.ESqrt("sqrt"));
    public static Token log = addToken(new Mathm.ELog("log"));
    public static Token log10 = addToken(new Mathm.ELog10("log"));
    public static Token pow = addToken(new Mathm.EPow("pow"));
    public static Token rnd = addToken(new Mathm.ERandom("rnd"));
    public static Token irnd = addToken(new Mathm.EIRand("irnd"));
    public static Token round = addToken(new Mathm.ERound("round"));
    public static Token floor = addToken(new Mathm.EFloor("floor"));
    public static Token mean = addToken(new Mathm.EMean("mean"));
    public static Token lininterp = addToken(new Mathm.ELin("lirp"));
    public static Token saw = addToken(new Mathm.ESawWave("saw"));
    public static Token squareWave = addToken(new Mathm.ESquareWave("square"));
    public static Token hipas = addToken(new Mathm.EHipass("hipas"));
    public static Token lowpas = addToken(new Mathm.ELowpass("lopas"));
    public static Token max = addToken(new Mathm.EMax("max"));
    public static Token min = addToken(new Mathm.EMin("min"));
    public static Token mandelb = addToken(new Fractal.EMandelbrot("mandelbrot"));
    public static Token julia = addToken(new Fractal.EJulia("julia"));
    public static Token img_red = addToken(new Img.EImgRed("imgred", null));
    public static Token img_green = addToken(new Img.EImgGreen("imggreen", null));
    public static Token img_blue = addToken(new Img.EImgBlue("imgblue", null));
    public static Token img_alpha = addToken(new Img.EImgAlpha("imgalpha", null));
    public static Token img_width = addToken(new Img.EImgWidth("imgwidth", null));
    public static Token img_height = addToken(new Img.EImgHeight("imgheight", null));

    public static Token PI =     addConstant("PI",       (float)Math.PI);
    public static Token E =      addConstant("E",        (float)Math.E);

    
    public static Token SQUARE;
    public static Token SPHERE;
    public static Token CONE;
    public static Token PIPE;
    public static Token TRIANGLE;
    public static Token RTRIANGLE;
    public static Token BOX;
    public static Token CIRCLE;
    public static Token RSQU;
    public static Token CSQU;
    public static Token MESH;
   
//    public static Token period = addToken(new Operator("."));
    
    // following tokens are both parsed and recognized as local expressions at generate time
    public static Token depth = addToken(new InnerExpressiveToken("d", TransformModifier.d.class, 1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.d(e, t); }
    });  // depth takes one and one only parameter inside transforms
    public static Token x = addToken(new InnerAffineExpressiveToken("x",TransformModifier.x.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.x(e, t); }
    });
    public static Token y = addToken(new InnerAffineExpressiveToken("y",TransformModifier.y.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.y(e, t); }
    });
    public static Token z = addToken(new InnerAffineExpressiveToken("z",TransformModifier.z.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.z(e, t); }
    });
    public static Token s = addToken( new InnerAffineExpressiveToken("s",TransformModifier.s.class,1,3) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.s(e, t); }
    });  // takes one parameter (uniform scale) or 3 (x,y,z scaled all by distinct factors)
    public static Token sx = addToken(new InnerAffineExpressiveToken("sx",TransformModifier.sx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.sx(e, t); }
    });
    public static Token sy = addToken(new InnerAffineExpressiveToken("sy",TransformModifier.sy.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.sy(e, t); }
    });
    public static Token sz = addToken(new InnerAffineExpressiveToken("sz",TransformModifier.sz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.sz(e, t); }
    });
    public static Token rx = addToken(new InnerAffineExpressiveToken("rx",TransformModifier.rx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.rx(e, t); }
    });
    public static Token ry = addToken(new InnerAffineExpressiveToken("ry",TransformModifier.ry.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.ry(e, t); }
    });
    public static Token rz = addToken(new InnerAffineExpressiveToken("rz",TransformModifier.rz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.rz(e, t); }
    });
    public static Token R = addToken(new InnerExpressiveToken("R",TransformModifier.R.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.R(e, t); }
    });
    public static Token G = addToken(new InnerExpressiveToken("G",TransformModifier.G.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.G(e, t); }
    });
    public static Token B = addToken(new InnerExpressiveToken("B",TransformModifier.B.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.B(e, t); }
    });
    public static Token H = addToken(new InnerExpressiveToken("H",TransformModifier.H.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.H(e, t); }
    });
    public static Token S = addToken(new InnerExpressiveToken("S",TransformModifier.Sat.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.Sat(e, t); }
    });
    public static Token L = addToken(new InnerExpressiveToken("L",TransformModifier.L.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.L(e, t); }
    });
    public static Token A = addToken(new InnerExpressiveToken("A",TransformModifier.A.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.A(e, t); }
    });
    public static Token shading = addToken(new InnerExpressiveToken("shading",TransformModifier.shading.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.shading(e, t); }
    });
    public static Token col0 = addToken(new InnerExpressiveToken("col0",TransformModifier.col0.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.col0(e, t); }
    });
    public static Token layer = addToken(  new InnerExpressiveToken("layer",TransformModifier.layer.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.layer(e, t); }
    });
    // following tokens are parsed but not recognized as local expressions at generate time
    public static Token skewx = addToken(new InnerAffineToken("skewx",TransformModifier.skewx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.skewx(e, t); }
    });
    public static Token skewy = addToken(new InnerAffineToken("skewy",TransformModifier.skewy.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.skewy(e, t); }
    });
    public static Token skewz = addToken(new InnerAffineToken("skewz",TransformModifier.skewz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.skewz(e, t); }
    });
    public static Token flipx = addToken(new InnerAffineToken("flipx",TransformModifier.flipx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.flipx(e, t); }
    });
    public static Token flipy = addToken(new InnerAffineToken("flipy",TransformModifier.flipy.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.flipy(e, t); }
    });
    public static Token flipz = addToken(new InnerAffineToken("flipz",TransformModifier.flipz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.flipz(e, t); }
    });
    public static Token skew = addToken( new InnerAffineToken("skew",TransformModifier.skew.class,3) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.skew(e, t); }
    });  // arbitrary skew along each axis
    public static Token RGB = addToken(    new InnerToken("RGB",TransformModifier.RGB.class,1,3) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.RGB(e, t); }
    });  // 1 uniform transition, 3 set RGB
    public static Token HSLA = addToken(   new InnerToken("HSLA",TransformModifier.HSLA.class,4) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.HSLA(e, t); }
    });  // HSLA transition
    // PUSH and PEEK belong best here, though PUSH is an InnerToken, and PEEK resides on a step higher level
    // note that PUSH has special treatment in PointTransform.setShapeTransform and other places.
    public static Token push = addToken(new InnerToken("PUSH",-1) { }); // -1 denotes any number of params...
    public static Token peek = addToken(new Token("PEEK"));
    public static Token pop = addToken(new Token("POP"));
    // bitmap reference related
//    public static Token img = addToken(new Token("img"));
//    public static Token imgu = addToken(new Token("u"));
//    public static Token imgv = addToken(new Token("v"));
    public static Token imgr = addToken(new Token("imgr"));
    public static Token imgg = addToken(new Token("imgg"));
    public static Token imgb = addToken(new Token("imgb"));
    public static Token imga = addToken(new Token("imga"));
//    public static Token imgu = addToken(new InnerToken("u",1));
//    public static Token imgv = addToken(new InnerToken("v",1));

    private static int nextId = 0;
    private static UtBuilder utb = UtBuilder.getUtBuilder();
    static {
        
        new UtConstants(utb).run();
        // aliases of keywords
        
        rule.addAlias("RULE");
        background.addAlias("bg");
        fov.addAlias("camera");

        def.addAlias("DEFINE");


        depth.addAlias("depth");
        s.addAlias("size");
        s.addAlias("scale");
        sx.addAlias("scalex");
        sy.addAlias("scaley");
        sz.addAlias("scalez");

        rx.addAlias("rotx");
        ry.addAlias("roty");
        rz.addAlias("rotz");
//        flip.addAlias("f");
        R.addAlias("red");
        G.addAlias("green");
        B.addAlias("blue");
        H.addAlias("hue");
        S.addAlias("sat");
        L.addAlias("lightness");
        A.addAlias("alpha");
        
        and.addAlias("and");
        or.addAlias("or");

        // load plugins
        PluginLoader.main(null);    // this will run the static block...        
        
        // instantiate references for parse/generate time quick access
        int counter = 0;
        for (Token t : tokens) {
            counter++;
            counter += t.aliases.size();
        }
        tokenReferences = new HashMap<String, Token>();
        for (Token t : tokens) {
            tokenReferences.put(t.name, t);
            for (String s : t.aliases) {
                tokenReferences.put(s, t);
            }
        }

    }





    public static boolean isControlChar(char c) {
        return Language.controlCharacters.get((int)c);
    }

    public static boolean isNameChar(char c) {
        return Character.isDigit(c) || Character.isLetter(c) ||
                c == '_' || c == '.';
    }


    public static Float returnAsValue(String s) {
        for(int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != '.' && !Character.isDigit(ch))
                return null;
        }
        float d = 0;
        if (s.charAt(0)=='.')
            s = "0" + s;
        d = Float.parseFloat(s);
        return d;
    }
    
    public static Token tokenByName(String s) {
        return tokenReferences.get(s);
    }
    
    public static boolean isName(String s) {
        boolean hasChar = false;
        for(char c: s.toCharArray())
            if (Character.isLetter(c))
                hasChar = true;
            else if (!isNameChar(c))
                return false;
        if (hasChar) return true;
        return false;
    }

    private static char[] curIdName = {'A', 'A', 'A', 'A'};

    public static String nextIdName() {
        String s = String.copyValueOf(curIdName);
        for (int i = 3; i > 0; i--) {
            curIdName[i]++;
            if (curIdName[i] > 'Z') {
                curIdName[i] = 'A';
            }
            else
            {
                break;
            }
        }
        return s;
    }
    
    public static List<Untransformable> untransformables() {
        return utb.getUntransformables();
    }
    

}
