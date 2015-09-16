
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
import org.konte.lang.func.Prob;

/**Many of the language features are defined here.
 *
 * @author pvto
 */
public class Language {
    
    public static final double version = 1.0;
    /** output log level*/
    public static int output_verbose_filter = Integer.parseInt(System.getProperty("konte.verbosity", "8"));

    /** All named tokens in this language */
    public static final List<Token> tokens = new ArrayList<>();
    /** Parse/generate-time references to tokens for fast access*/
    public static final Map<String,Token> tokenReferences = new HashMap<>();
    /** defined output shapes for parse time and generate time PUSH/POP access */
    public static final Map<Integer,Untransformable> utref = new HashMap<>();


    /**Plugin extensions for inline scripting register here */
    public static final List<KonteScriptExtension> scriptExtensions = new ArrayList<>();
    
    public static final Token addToken(Token token)
    {
        tokens.add(token);
        return token;
    }
    /** <p>Constants can be added at parse time. These are global constants,
     * i.e. executed globally at any single phase of generation.
     * 
     * @return the added Constant
     */
    public static Constant addConstant(String name, float val)
    {
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
    public static Untransformable addUntransformable(Untransformable ut)
    {
        utref.put(ut.getId(), ut);
        addToken(ut);
        return ut;
    }
    public static Untransformable getUntransformable(int ind)
    {
        return utref.get(ind);
    }

    /* Each character in a {@link ControlToken} will be found on this list -
     * which then defines boundaries for valid names */
    public static BitSet controlCharacters = new BitSet(1024);
            
    
    public static void addControlCharacters(String name)
    {
        for (char c : name.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isLetter(c)) {
                controlCharacters.set((int)c);
            }
        }
    }
    
    /** Function-like construct that is used for building
     * objects (for instance shading points) in the model  */
    public static class LanguageFunctor extends Function
    {
        int argsCount = -1;
        public LanguageFunctor(String name) { super(name); }
        public LanguageFunctor(String name, int argsCount) {
            super(name);
            this.argsCount = argsCount;
        }
        @Override public int getArgsCount() { return argsCount; }
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
    public static final int MISC_model = 0;
    public static final String[][] miscKeywords = {
        {"pushstack", "maxshapes", "feature", "order", "streamrate"}
    };
    
    // enumerate keywords in the language
    
    public static final Token model = addToken(new Token("model"));
    public static final Token include = addToken(new Token("include"));
    public static final Token push_stack = addToken(new Token(miscKeywords[MISC_model][0]));
    public static final Token max_shapes = addToken(new Token(miscKeywords[MISC_model][1]));
    public static final Token feature_size = addToken(new Token(miscKeywords[MISC_model][2]));
    public static final Token order = addToken(new Token(miscKeywords[MISC_model][3]));
    public static final Token stream_rate = addToken(new Token(miscKeywords[MISC_model][4]));
    public static final Token startshape = addToken(new Token("startshape"));
    public static final Token background = addToken(new Token("background"));
    public static final Token light = addToken(new Token("light"));
    public static final Token fov = addToken(new InnerToken("fov", TransformModifier.fov.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) {
            return new TransformModifier.fov(e, t);
        }
    });  // other InnerTokens are introduced below
    public static final Token lookat = addToken(new LanguageFunctor("lookat", 3));
    public static final Token fx = addToken(new Token("fx"));
    public static final Token rule = addToken(new Token("rule"));
    public static final Token path = addToken(new Token("path"));
    public static final Token moveto = addToken(new LanguageFunctor("moveto", 3));
    public static final Token bend = addToken(new LanguageFunctor("bend", 3));
    public static final Token lineto = addToken(new LanguageFunctor("lineto", 3));
    public static final Token curveto = addToken(new LanguageFunctor("curveto", 3));
    public static final Token close = addToken(new Token("close"));
    public static final Token pre_context = addToken(new Token("pre"));
    public static final Token post_context = addToken(new Token("post"));
    public static final Token ifToken = addToken(new Token("if"));
//    public static final Token fail = addToken(new Token("fail"));
    public static final Token point = addToken(new LanguageFunctor("point") {
        @Override public boolean nArgsAllowed(int n) {
            return n > 0 && n < 4;
        }
    });
    //shading is InnerExpressiveToken (two-fold usage)

    public static final Token def = addToken(new Token("DEF"));
    public static final Token undef = addToken(new Token("UNDEF"));
    public static final Token macro = addToken(new Token("MACRO"));
    
    public static final Token comment = addToken(new Context("//"));
    public static final Token comment_start = addToken(new Context("/*"));
    public static final Token comment_end = addToken(new Context("*/"));
    public static final Token left_curl = addToken(new Context("{"));
    public static final Token right_curl = addToken(new Context("}"));
    public static final Token left_bracket = addToken(new Context("("));
    public static final Token right_bracket = addToken(new Context(")"));
    public static final Token left_squarebracket = addToken(new Context("["));
    public static final Token right_squarebracket = addToken(new Context("]"));
    public static final Token hyphen = addToken(new Context("\""));
    
    public static final Token comma = addToken(new ControlToken(","));
    public static final Token semicolon = addToken(new ControlToken(";"));

    
    public static final Token and = addToken(new Comparator("&&"));
    public static final Token or = addToken(new Comparator("||"));
    public static final Token equals = addToken(new Comparator("="));
    public static final Token ne = addToken(new Comparator("!="));
    public static final Token lt = addToken(new Comparator("<"));
    public static final Token lte = addToken(new Comparator("<="));    
    public static final Token gt = addToken(new Comparator(">"));
    public static final Token gte = addToken(new Comparator(">="));    
    public static final Token add = addToken(new Operator("+"));
    public static final Token subtract = addToken(new Operator("-"));
    public static final Token multiply = addToken(new Operator("*"));
    public static final Token divide = addToken(new Operator("/"));
    public static final Token modulo = addToken(new Operator("%"));
    public static final Token pow_op = addToken(new Operator("**"));

    public static final Token inc = addToken(new Func.EInc("inc", null));
    public static final Token sin = addToken(new Mathm.ESin("sin"));
    public static final Token cos = addToken(new Mathm.ECos("cos"));
    public static final Token tan = addToken(new Mathm.ETan("tan"));
    public static final Token asin = addToken(new Mathm.EAsin("asin"));
    public static final Token acos = addToken(new Mathm.EAcos("acos"));
    public static final Token atan = addToken(new Mathm.EAtan("atan"));
    public static final Token abs = addToken(new Mathm.EAbs("abs"));
    public static final Token sqrt = addToken(new Mathm.ESqrt("sqrt"));
    public static final Token log = addToken(new Mathm.ELog("log"));
    public static final Token log10 = addToken(new Mathm.ELog10("log10"));
    public static final Token pow = addToken(new Mathm.EPow("pow"));
    public static final Token rnd = addToken(new Mathm.ERandom("rnd"));
    public static final Token irnd = addToken(new Mathm.EIRand("irnd"));
    public static final Token rndf = addToken(new Func.ERndf("rndf", null));
    public static final Token irndf = addToken(new Func.EIrndf("irndf", null));
    public static final Token round = addToken(new Mathm.ERound("round"));
    public static final Token floor = addToken(new Mathm.EFloor("floor"));
    public static final Token mean = addToken(new Mathm.EMean("mean"));
    public static final Token lininterp = addToken(new Mathm.ELin("lirp"));
    public static final Token saw = addToken(new Mathm.ESawWave("saw"));
    public static final Token squareWave = addToken(new Mathm.ESquareWave("square"));
    public static final Token hipas = addToken(new Mathm.EHipass("hipas"));
    public static final Token lowpas = addToken(new Mathm.ELowpass("lopas"));
    public static final Token max = addToken(new Mathm.EMax("max"));
    public static final Token min = addToken(new Mathm.EMin("min"));
    public static final Token mandelb = addToken(new Fractal.EMandelbrot("mandelbrot"));
    public static final Token julia = addToken(new Fractal.EJulia("julia"));
    public static final Token binm = addToken(new Prob.EBinm("binm"));
    public static final Token binmcuml = addToken(new Prob.EBincuml("binmc"));
    public static final Token binmRndf = addToken(new Func.ERndfbin("brndf", null));
    public static final Token binmRnd = addToken(new Prob.ERndbin("brnd"));
    public static final Token hypg = addToken(new Prob.EHypg("hypg"));
    public static final Token hypgcuml = addToken(new Prob.EHypgcuml("hypgc"));
    public static final Token hypgRndf = addToken(new Func.ERndfhypg("hypgrndf", null));
    public static final Token hypgRnd = addToken(new Prob.ERndhypg("hypgrnd"));
    public static final Token negbinm = addToken(new Prob.ENegbinm("negbinm"));

    public static final Token img_red = addToken(new Img.EImgRed("imgred", null));
    public static final Token img_green = addToken(new Img.EImgGreen("imggreen", null));
    public static final Token img_blue = addToken(new Img.EImgBlue("imgblue", null));
    public static final Token img_alpha = addToken(new Img.EImgAlpha("imgalpha", null));
    public static final Token img_width = addToken(new Img.EImgWidth("imgwidth", null));
    public static final Token img_height = addToken(new Img.EImgHeight("imgheight", null));

    public static final Token PI =     addConstant("PI",       (float)Math.PI);
    public static final Token E =      addConstant("E",        (float)Math.E);

    
    public static Token SQUARE;
    public static Token SPHERE;
    public static Token CONE;
    public static Token PIPE;
    public static Token TRIANGLE;
    public static Token RTRIANGLE;
    public static Token HEXAGON;
    public static Token BOX;
    public static Token CIRCLE;
    public static Token RSQU;
    public static Token CSQU;
    public static Token MESH;
    
    public static Token BLUR_SQUARE;
    public static Token BLUR_TRIANGLE;
    public static Token BLUR_HEXAGON;
    public static Token BLUR_32GON;
   
//    public static final Token period = addToken(new Operator("."));
    
    // following tokens are both parsed and recognized as local expressions at generate time
    public static final Token depth = addToken(new InnerExpressiveToken("d", TransformModifier.d.class, 1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.d(e, t); }
    });  // depth takes one and one only parameter inside transforms
    public static final Token x = addToken(new InnerAffineExpressiveToken("x",TransformModifier.x.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.x(e, t); }
    });
    public static final Token y = addToken(new InnerAffineExpressiveToken("y",TransformModifier.y.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.y(e, t); }
    });
    public static final Token z = addToken(new InnerAffineExpressiveToken("z",TransformModifier.z.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.z(e, t); }
    });
    public static final Token s = addToken( new InnerAffineExpressiveToken("s",TransformModifier.s.class,1,3) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.s(e, t); }
    });  // takes one parameter (uniform scale) or 3 (x,y,z scaled all by distinct factors)
    public static final Token sx = addToken(new InnerAffineExpressiveToken("sx",TransformModifier.sx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.sx(e, t); }
    });
    public static final Token sy = addToken(new InnerAffineExpressiveToken("sy",TransformModifier.sy.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.sy(e, t); }
    });
    public static final Token sz = addToken(new InnerAffineExpressiveToken("sz",TransformModifier.sz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.sz(e, t); }
    });
    public static final Token rx = addToken(new InnerAffineExpressiveToken("rx",TransformModifier.rx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.rx(e, t); }
    });
    public static final Token ry = addToken(new InnerAffineExpressiveToken("ry",TransformModifier.ry.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.ry(e, t); }
    });
    public static final Token rz = addToken(new InnerAffineExpressiveToken("rz",TransformModifier.rz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.rz(e, t); }
    });
    public static final Token R = addToken(new InnerExpressiveToken("R",TransformModifier.R.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.R(e, t); }
    });
    public static final Token G = addToken(new InnerExpressiveToken("G",TransformModifier.G.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.G(e, t); }
    });
    public static final Token B = addToken(new InnerExpressiveToken("B",TransformModifier.B.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.B(e, t); }
    });
    public static final Token H = addToken(new InnerExpressiveToken("H",TransformModifier.H.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.H(e, t); }
    });
    public static final Token S = addToken(new InnerExpressiveToken("S",TransformModifier.Sat.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.Sat(e, t); }
    });
    public static final Token L = addToken(new InnerExpressiveToken("L",TransformModifier.L.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.L(e, t); }
    });
    public static final Token A = addToken(new InnerExpressiveToken("A",TransformModifier.A.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.A(e, t); }
    });
    public static final Token shading = addToken(new InnerExpressiveToken("shading",TransformModifier.shading.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.shading(e, t); }
    });
    public static final Token col0 = addToken(new InnerExpressiveToken("col0",TransformModifier.col0.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.col0(e, t); }
    });
    public static final Token layer = addToken(  new InnerExpressiveToken("layer",TransformModifier.layer.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.layer(e, t); }
    });
    // following tokens are parsed but not recognized as local expressions at generate time
    public static final Token skewx = addToken(new InnerAffineToken("skewx",TransformModifier.skewx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.skewx(e, t); }
    });
    public static final Token skewy = addToken(new InnerAffineToken("skewy",TransformModifier.skewy.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.skewy(e, t); }
    });
    public static final Token skewz = addToken(new InnerAffineToken("skewz",TransformModifier.skewz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.skewz(e, t); }
    });
    public static final Token flipx = addToken(new InnerAffineToken("flipx",TransformModifier.flipx.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.flipx(e, t); }
    });
    public static final Token flipy = addToken(new InnerAffineToken("flipy",TransformModifier.flipy.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.flipy(e, t); }
    });
    public static final Token flipz = addToken(new InnerAffineToken("flipz",TransformModifier.flipz.class,1) {
        @Override public TransformModifier newInstance(Expression e, Token t) { return new TransformModifier.flipz(e, t); }
    });
    public static final Token skew = addToken( new InnerAffineToken("skew",TransformModifier.skew.class,3) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.skew(e, t); }
    });  // arbitrary skew along each axis
    public static final Token RGB = addToken(    new InnerToken("RGB",TransformModifier.RGB.class,1,3) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.RGB(e, t); }
    });  // 1 uniform transition, 3 set RGB
    public static final Token HSLA = addToken(   new InnerToken("HSLA",TransformModifier.HSLA.class,4) {
        @Override public TransformModifier newInstance(List<Expression> e, Token t) { return new TransformModifier.HSLA(e, t); }
    });  // HSLA transition
    // PUSH and PEEK belong best here, though PUSH is an InnerToken, and PEEK resides on a step higher level
    // note that PUSH has special treatment in PointTransform.setShapeTransform and other places.
    public static final Token push = addToken(new InnerToken("PUSH",-1) { }); // -1 denotes any number of params...
    public static final Token peek = addToken(new Token("PEEK"));
    public static final Token pop = addToken(new Token("POP"));
    // bitmap reference related
//    public static final Token img = addToken(new Token("img"));
//    public static final Token imgu = addToken(new Token("u"));
//    public static final Token imgv = addToken(new Token("v"));
    public static final Token imgr = addToken(new Token("imgr"));
    public static final Token imgg = addToken(new Token("imgg"));
    public static final Token imgb = addToken(new Token("imgb"));
    public static final Token imga = addToken(new Token("imga"));
    
    public static final Token diffuse = addToken(new Token("diffuse"));
    public static final Token specular = addToken(new Token("specular"));
//    public static final Token imgu = addToken(new InnerToken("u",1));
//    public static final Token imgv = addToken(new InnerToken("v",1));

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

        R.addAlias("red");
        G.addAlias("green");
        B.addAlias("blue");
        H.addAlias("hue");
        S.addAlias("sat");
        L.addAlias("lightness");
        A.addAlias("alpha");
        
        layer.addAlias("lr");
        
        and.addAlias("and");
        or.addAlias("or");
        
        rndf.addAlias("random");

        // load plugins
        PluginLoader.main(null);    // this will run the static block...        
        
        // instantiate references for parse/generate time quick access
        int counter = 0;
        for (Token t : tokens)
        {
            counter++;
            counter += t.aliases.size();
        }

        for (Token t : tokens)
        {
            tokenReferences.put(t.name, t);
            for (String alias : t.aliases)
            {
                tokenReferences.put(alias, t);
            }
        }

    }





    public static boolean isControlChar(char c)
    {
        return Language.controlCharacters.get((int)c);
    }

    public static boolean isNameChar(char c)
    {
        return Character.isDigit(c) || Character.isLetter(c) ||
                c == '_' || c == '.';
    }


    public static Float returnAsValue(String s)
    {
        if (s.length() == 0)
            return null;
        for(int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != '.' && !Character.isDigit(ch))
                return null;
        }
        if (s.charAt(0)=='.')
            s = "0" + s;
        return Float.parseFloat(s);
    }
    
    public static final Token tokenByName(String s)
    {
        return tokenReferences.get(s);
    }
    
    public static boolean isName(String s)
    {
        boolean hasChar = false;
        for(char c: s.toCharArray())
            if (Character.isLetter(c))
                hasChar = true;
            else if (!isNameChar(c))
                return false;
        return hasChar;
    }

    private static char[] curIdName = {'A', 'A', 'A', 'A'};

    public static String nextIdName()
    {
        String name = String.copyValueOf(curIdName);
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
        return name;
    }
    
    public static List<Untransformable> untransformables()
    {
        return utb.getUntransformables();
    }
    

}
