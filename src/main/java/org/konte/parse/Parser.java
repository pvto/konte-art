
package org.konte.parse;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import org.konte.model.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.konte.lang.Tokens.*;
import org.konte.lang.Tokenizer;
import org.konte.model.Untransformable;
import org.konte.misc.Readers;
import org.konte.expression.BooleanExpression;
import org.konte.expression.Expression;
import org.konte.expression.ExpressionFunction;
import org.konte.expression.Name;
import org.konte.expression.Value;
import org.konte.generate.Runtime;
import org.konte.generate.StreamingShapeReader;
import org.konte.image.Camera;
import org.konte.model.systems.DistanceMetric;
import org.konte.image.CanvasEffect;
import org.konte.imprt.SvgImport;
import org.konte.lang.CameraProperties;
import org.konte.lang.Language;
import org.konte.lang.Language.LanguageFunctor;
import org.konte.lang.LightModifier;
import org.konte.lang.ShapeReaders;
import org.konte.lang.Tokenizer.TokenizerString;
import org.konte.misc.PrefixStringMap;
import org.konte.model.PathRule.Placeholder;
import org.konte.plugin.KontePluginScript;
import org.konte.plugin.KonteScriptExtension;
import org.konte.ui.Ui;

/**<p>Parses a konte model from a list of token strings.
 *<p>Method parse creates a state machine for the task. Parse returns a new
 * model or throws a parse exception, if it encounters a syntax error.
 *
 * @author pvto
 */
public class Parser {

    private ArrayList<Expression> decodeHtmlRgb(String s)
    {
        ArrayList<Expression> lexprs = new ArrayList<Expression>();
        int intval = Integer.parseInt(s.substring(1, 7), 16);
        lexprs.add(new Value(((float)(intval >> 16))/255));
        lexprs.add(new Value(((float)(intval >> 8 & 0xFF))/255));
        lexprs.add(new Value(((float)(intval & 0xFF))/255));
        return lexprs;
    }

    private ArrayList<TokenizerString> stripQuotes(ArrayList<TokenizerString> included) throws ParseException
    {
        for(TokenizerString t : included)
        {
            String s = t.getString();
            if (s.charAt(0) == '"')
            {
                if (s.charAt(s.length() - 1) != '"')
                {
                    throw new ParseException("Unterminated character literal " + s, lineNr, caretPos);
                }
                s = s.substring(1, s.length() - 1);
                t.setString(s);
            }
        }
                    return included;
    }







    private enum ParsingContext {

        GRAMMAR,
        MODEL_CREATE,
        MODEL,
        INCLUDE,
        SYSTEM,
        STARTSHAPE,
        SHADING_CREATE,
        SHADING,
        SHADING_ADJUSTMENTS,
        BACKGROUND_CREATE,
        BACKGROUND,
        LIGHT_CREATE,
        LIGHT,
        LIGHT_ADJUSTMENTS,
        CAMERA_CREATE,
        CAMERA,
        FX,
        DEF_NAME,
        MACRO_NAME,
        MACRO_VALUE,
        PATH_CREATE,
        RULE_CREATE,
        PATH,
        RULE,
        RULE_PRE_CREATE,
        RULE_PRE,
        RULE_POST_CREATE,
        RULE_POST,
        RULE_TRANSFORM_CREATE,
        TRANSFORM_ADJUSTMENTS,
        TRANSFORM_DEFS,
        REPEAT_CREATE,
        REPEAT_ADJUSTMENTS,
        IF_CREATE,
        IF,
        BLOCK_CREATE,
        BLOCK}

    public static int getExpressionList(ArrayList<Tokenizer.TokenizerString> ttOrig, int startpos, ArrayList<Token> ret) throws ParseException
    {
        return getExpressionList(ttOrig, startpos, ret, 0);
    }
    public static int getExpressionList(ArrayList<Tokenizer.TokenizerString> ttOrig, int startpos, ArrayList<Token> ret, int isSemicolons) throws ParseException
    {
        int lbcount = 0;
        Token t = Language.tokenByName(ttOrig.get(startpos).getString());
        Token first = (t == null) ? new Token(ttOrig.get(startpos).getString()) : t;
        ret.clear();
        boolean hasNext = ttOrig.size() > startpos + 1;
        boolean hasNextAfter = ttOrig.size() > startpos + 2;
        Token u = !hasNext ? null : Language.tokenByName(ttOrig.get(startpos + 1).getString());

        if (isSemicolons != 1)
        {
            if (t == Language.left_bracket || t instanceof Function || t instanceof InnerExpressiveToken)
            {
            // skip to loop
            }
            else if (t == Language.subtract)
            {
                if (!hasNext)
                {
                    throw new ParseException("Orphaned negation ", lineNr, caretPos);
                }

                if (u == null || u == Language.left_bracket || u instanceof Function
                        || (u != null && InnerExpressiveToken.class.isAssignableFrom(u.getClass())))
                {
                // skip to loop
                }
                else
                {
                    throw new ParseException("Expression: expecting ( or number or name after -", lineNr, caretPos);
                }
            } else if (t == null || t instanceof Untransformable)
            {
                if (isSemicolons == 2 || !(u instanceof Operator))
                {
                    ret.add(first);
                    return startpos;
                }
            }
            else
            {
                throw new ParseException("Expression starts: expecting ( or - or function declaration, found " + t, lineNr, caretPos);
            }
        }
        int ii = startpos;
        boolean moveToNext = true;
        Token last = null;
        while (moveToNext)
        {
            int lbctmp = lbcount;
            String s = ttOrig.get(ii).getString();
            last = ii>startpos ?
                t : null;
            t = Language.tokenByName(s);
            if (t == null)
            {
                t = new Token(s);
            }
            if (t == Language.right_curl || t == Language.semicolon ||
                    (lbcount==0 && t == Language.comma))
                    {
                moveToNext = false;
                break;
            } else if (t == Language.left_curl && last == Language.multiply)
            {
                moveToNext = false;
                ret.remove(ret.size() - 1);
                ii--;
                break;
            } else if (t == Language.left_bracket)
            {
                if ( startpos < ii
                    && ! (last == Language.left_bracket || last instanceof Operator
                        || last instanceof Comparator
                        || last == Language.comma
                        || Function.class.isAssignableFrom(last.getClass())))
                        {
                    moveToNext = false;
                    break;
                }
                lbcount++;
            } else if (t == Language.right_bracket)
            {
                lbcount--;
                if (lbcount <= 0 && isSemicolons != 1)
                {
                    moveToNext = false;
                    if (isSemicolons == 0 && ttOrig.size() > ii + 1
                            && Language.tokenByName(ttOrig.get(ii + 1).getString()) instanceof Operator)
                            {
                        moveToNext = true;
                    }
                }
            } else if ( startpos < ii
                    && (last.getClass() == Constant.class || last.getClass() == Token.class 
                      || InnerExpressiveToken.class.isAssignableFrom(last.getClass()))
                    && ! (t instanceof Operator || t instanceof Comparator
                        || lbctmp > 0 && t == Language.comma))
                        {
                moveToNext = false;
                break;
            }
            if (moveToNext && ii>startpos &&
                    (last == null || (lbctmp == 0 && last==Language.right_bracket))
                    && !(t instanceof org.konte.lang.Tokens.Operator ||
                         t instanceof Comparator ||
                          t == Language.right_curl ||
                          t == Language.right_bracket ||
                          t == Language.comma ||
                          t == Language.semicolon))
                          {
                moveToNext = false;
                break;
            }
            ret.add(t);
            if (ii + 1 >= ttOrig.size())
            {
                moveToNext = false;
            }
            ii++;
        }
        if (lbcount > 0)
        {
            throw new ParseException("Missing  )  after\n" +
                    ret.stream().map(tok -> tok.name).reduce("", (ss,s)->ss+" "+s) + "\n", lineNr, caretPos);
        } else if (lbcount < 0)
        {
            throw new ParseException("Unexpected  ) \n " +
                    ret.stream().map(tok -> tok.name).reduce("", (ss,s)->ss+" "+s) + "\n", lineNr, caretPos);
        }
        else
        {
            if (t==Language.right_bracket &&
                    last instanceof org.konte.lang.Tokens.Operator)
                throw new ParseException("Expression terminates with " + last,
                        lineNr, caretPos);
        }
                Runtime.sysoutln(ret, -1);
        return ii - 1;
    }



    private static int lineNr;     // line number in source for parse exceptions
    private static int caretPos;   // caret position (running) in source, for parse exceptions
    
    private static final Pattern prefixPattern = Pattern.compile("^\\^(\\w+\\\\?)\\^(.*)");


    public Model parse(ArrayList<Tokenizer.TokenizerString> tokenStrings) throws ParseException
    {
        tokenStrings = stripQuotes(tokenStrings);
        
        Runtime.stateServer.clear();

        String workdir = System.getProperty("konte.workdir");

        Runtime.sysoutln("PARSING MODEL", 2);
        Model m = new Model();
        Name.model = m;
        m.bitmapCache.clearReferences(); // clear image reference hash from earlier parsings

        ArrayDeque<ParsingContext> contextStack =
                new ArrayDeque<ParsingContext>();
        ParsingContext curCtx = ParsingContext.GRAMMAR;
        Transform ltfm = null; // last transform (inside rule transform)
        Transform lrstfm = null;   // last repeat transform (inside repeat)
        RepeatStructure lrepeat = null;
        ArrayDeque<ConditionalStructure> conditionalStack = new ArrayDeque<ConditionalStructure>();
        Rule lastRule = null;
        NonDeterministicRule lndr = null;
        Expression lexpr = null;
        ArrayList<Expression> lexprs = new ArrayList<Expression>();
        ArrayList<Expression> tmpexps = new ArrayList<Expression>();
        ExpressionParser exprParser = new ExpressionParser();
        ArrayList<Token> exprL = new ArrayList<Token>();
        String lastName = null;
        Float lastValue = null;
        Float val = null;
        int pos = 0;
        Token lastInnerToken = null;
        Token tokenBefore = null;
        boolean wasNamed = false;
        boolean wasSet = false;
        boolean isSpecialContext = false;
        boolean isDef = false;
        CameraBuilder camBd = null;
        ColorSpaceBuilder colBd = new ColorSpaceBuilder();
        LightBuilder lightBd = new LightBuilder();
        GreyBoxSystem greyBoxSystem = null;
        String s = null;
        for (int i = 0; i < tokenStrings.size(); i++)
        {
            s = tokenStrings.get(i).getString();
            lineNr = tokenStrings.get(i).getLineNr();
            caretPos = tokenStrings.get(i).getCaretPos();

            if (s.startsWith(Language.comment_start.name))
            {
                s = s.substring(2);
                for(KonteScriptExtension ext : Language.scriptExtensions)
                {
                    if (ext.isScript(s))
                    {
                        try {
                            KontePluginScript script = ext.getInstance(s);
                            script.setModel(m);
                            switch(curCtx)
                            {
                                case GRAMMAR:
                                    script.execute();
                                    break;
                                case RULE:
                                    script.setRule(lastRule);
                                    lastRule.addScript(script);
                                    break;
                            }
                        }
                        catch(Exception ex)
                        {
                            throw new ParseException("plugin script failure: " + ex.getMessage(), lineNr, caretPos);
                        }
                        break;
                    }
                }
                continue;
            }
            if (s.startsWith(Language.comment.name))
            {
                continue;
            }
            Token t = null;
            t = Language.tokenByName(s);
            if (t == Language.comment_end)
            {
                continue;
            }
            try {
            switch (curCtx)
            {
                case GRAMMAR:
                    contextStack.push(curCtx);
                    if (t == Language.rule)
                    {
                        curCtx = ParsingContext.RULE_CREATE;
                    } else if (t == Language.path)
                    {
                        curCtx = ParsingContext.PATH_CREATE;
                    } else if (t == Language.def)
                    {
                        curCtx = ParsingContext.DEF_NAME;
                    } else if (t == Language.macro)
                    {
                        curCtx = ParsingContext.MACRO_NAME;
                    } else if (t == Language.background)
                    {
                        curCtx = ParsingContext.BACKGROUND_CREATE;
                    } else if (t == Language.light)
                    {
                        curCtx = ParsingContext.LIGHT_CREATE;
                    } else if (t == Language.startshape)
                    {
                        curCtx = ParsingContext.STARTSHAPE;
                    } else if (t == Language.fov)
                    {
                        curCtx = ParsingContext.CAMERA_CREATE;
                    } else if (t == Language.shading)
                    {
                        curCtx = ParsingContext.SHADING_CREATE;
                    } else if (t == Language.model)
                    {
                        curCtx = ParsingContext.MODEL_CREATE;
                    } else if (t == Language.include)
                    {
                        curCtx = ParsingContext.INCLUDE;
                    } else if (t == Language.system)
                    {
                        curCtx = ParsingContext.SYSTEM;
                    } else if (t == Language.fx)
                    {
                        curCtx = ParsingContext.FX;
                    }
                    else
                    {
                        if (t != null)
                            throw new ParseException(
                                "Misplaced token at root level: " + s, lineNr, caretPos);
                        curCtx = ParsingContext.RULE_CREATE;
                        i--;
                    }
                    break;
                case MODEL_CREATE:
                    if (t == Language.left_curl)
                    {
                        curCtx = Parser.ParsingContext.MODEL;
                        pos = 0;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token after 'model': " + s, lineNr, caretPos);
                    }
                    break;
                case MODEL:
                    if ((pos & 8) != 0)
                    {
                        if (m.shapeReader == null)
                            throw new ParseException("streamrate definition before order definition near " + s + ". Try 'order STREAM'.", lineNr, caretPos);
                        if (! (m.shapeReader instanceof StreamingShapeReader))
                        {
                            throw new ParseException("Streamrate directive is available for 'order STREAM' shapereader only ", lineNr, caretPos);
                        }
                        try {
                            i = getExpressionList(tokenStrings, i, exprL);
                            Expression streamRate = exprParser.parse(exprL, 0, m);
                            ((StreamingShapeReader)m.shapeReader).streamRate = streamRate;
                        }
                        catch(Exception ex)
                        {
                            throw new ParseException("Can not parse streamrate expression near ", lineNr, caretPos);
                        }
                        pos = 0;
                    } else if (t == Language.feature_size)
                    {
                        pos |= 1;
                    } else if (t == Language.max_shapes)
                    {
                        pos |= 2;
                    } else if (t == Language.push_stack)
                    {
                        pos |= 4;
                    } else if (t == Language.order)
                    {
                        pos = 0x100;
                    } else if (t == Language.stream_rate)
                    {
                        pos |= 8;
                    } else if (t == Language.right_curl)
                    {
                        curCtx = contextStack.pop();
                    } else if (t == null)
                    {
                        if (pos == 0)
                            throw new ParseException("Model - orphaned token " + s, lineNr, caretPos);
                        else if (pos >= 0x100)
                        {
                            if (pos == 0x100)
                            {
                                if (m.shapeReader != null)
                                    throw new ParseException("Repeated order definition near " + s, lineNr, caretPos);
                                m.shapeReader = ShapeReaders.getReader(s, m);
                            }
                            pos = 0;
                        }
                        else
                        {
                            Float v = Language.returnAsValue(s);
                            if (v == null)
                                throw new ParseException("Expecting plain value in model settings near " + s, lineNr, caretPos);
                            if ((pos & 1) != 0)
                            {
                                m.minfeaturesize = v;
                            }
                            if ((pos & 2) != 0)
                            {
                                m.maxShapes = (int)Math.floor((double)v);
                            }
                            if ((pos & 4) != 0)
                            {
                                m.pushStackSize = Math.max(0,(int)Math.floor((double)v));
                            }
                            pos = 0;
                        }
                    }
                    else
                    {
                        throw new ParseException(
                                "model - not expecting " + s +
                                "", lineNr, caretPos);
                    }
                    break;
                case BACKGROUND_CREATE:
                    if (t == Language.left_curl)
                    {
                        ltfm = new Transform("background", lineNr, caretPos);
                        m.backgroundTransform = ltfm;
                        curCtx = Parser.ParsingContext.BACKGROUND;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token after 'background': " + s, lineNr, caretPos);
                    }
                    break;
                case BACKGROUND:
                    if (t instanceof InnerToken)
                    {
                        if ((t == Language.R) || (t == Language.G) ||
                                (t == Language.B) || (t == Language.A) ||
                                (t == Language.H) || (t == Language.S) ||
                                (t == Language.L) || (t == Language.RGB))
                                {
                            lastInnerToken = t;
                            isSpecialContext = true;
                            lexprs.clear();
                            pos = 0;
                        }
                        else
                        {
                            throw new ParseException("background - not expecting " + s +
                                    ". Valid tokens are R G B A H S L RGB", lineNr, caretPos);
                        }
                    } else if (t == Language.right_curl)
                    {
                        lastInnerToken = null;
                        curCtx = contextStack.pop();
                        isSpecialContext = false;
                    }
                    else
                    {
                        if (!isSpecialContext)
                        {
                            throw new ParseException("background - expecting R G B A H S or L before " + s, lineNr, caretPos);
                        }
                        if (lastInnerToken == null)
                            throw new ParseException("Missing token in background declaration before " +s, lineNr, caretPos);
                        else if (lastInnerToken == Language.RGB && s.matches("#[0-9A-Fa-f]{6}"))
                        {
                            lexprs = decodeHtmlRgb(s);
                            ltfm.setShapeTransform(lastInnerToken, lexprs);
                            lastInnerToken = null;
                        }
                        else
                        {
                            i = getExpressionList(tokenStrings, i, exprL);
                            lexprs.add(lexpr = exprParser.parse(exprL, 0, m));
                            if (!((InnerToken)lastInnerToken).higherParamCountAllowed(++pos))
                            {
                                if (!((InnerToken)lastInnerToken).nParamsAllowed(pos))
                                    throw new ParseException("Wrong number of arguments to " + lastInnerToken, lineNr, caretPos);
                                ltfm.setShapeTransform(lastInnerToken, lexprs);
                            }
                        }
                    }

                    break;
                case INCLUDE:
                    if (t != null)
                    {
                        if (lastName != null)
                            throw new ParseException("Expecting image name, found " + t, lineNr, caretPos);
                        else
                            throw new ParseException("Expecting file/url, found " + t, lineNr, caretPos);
                    }                        
                    if (lastName == null)
                    {
                        lastName = s;
                        if (lastName.matches("(.*(jpg|jpeg|png|gif|JPG|PNG|GIF|csv|tsv|CSV|TSV|data)|/dev/null/?|null|nil|empty)"))
                        {
                            break;
                        }
                    }
                    else {
                        lastName += " " + s;
                    }
                    
                    if (lastName == null || lastName.isEmpty())
                        throw new ParseException("Include file not specified", lineNr, caretPos);
                    lastName = lastName.replaceFirst("^~", System.getProperty("user.home"));
                    Matcher imgMatcher = Pattern.compile(
                            "(.*(jpg|jpeg|png|gif|JPG|PNG|GIF))\\s*(\\w*)$").matcher(lastName);
                    Matcher csvMatcher = Pattern.compile(
                            "(.*(csv|tsv|CSV|TSV|data))\\s*(\\w*)$").matcher(lastName);
                    Matcher nilMatcher = Pattern.compile(
                            "(/dev/null/?|null|nil|empty)\\s*(\\w*)$").matcher(lastName);
                    if (imgMatcher.find())
                    {
                        try {
                            String refName = imgMatcher.group(3);
                            if (refName.length() == 0)
                                throw new ParseException("Syntax: include [bitmap-name] [ref-name]", lineNr, caretPos);
                            File fl = getFile(workdir, imgMatcher.group(1));
                            Image img = null;
                            if (fl.exists())
                            {
                                img = Model.bitmapCache.add(fl, refName);
                            }
                            else
                            {
                                img = Model.bitmapCache.add(new URL(imgMatcher.group(1)), refName);
                            }
                            if (img == null)
                                throw new ParseException("Image not found");
                            Runtime.sysoutln(String.format(
                                    "%s [%dx%d] included as %s",
                                    fl.getAbsolutePath(), img.getWidth(null),
                                    img.getHeight(null), refName), 5);
                            Model.bitmapCache.init();
                        }
                        catch(Exception ex)
                        {
                            throw new ParseException(String.format(
                                    "bitmap %s not loaded: %s", imgMatcher.group(1), ex.getMessage()),
                                    lineNr, caretPos);
                        }
                    }
                    else if (csvMatcher.find())
                    {
                        try {
                            DataTable dataTable;
                            String refName = csvMatcher.group(3);
                            if (refName.length() == 0)
                                throw new ParseException("Syntax: include [csv/tsv-name] [ref-name]", lineNr, caretPos);
                            File fl = getFile(workdir, csvMatcher.group(1));
                            if (fl.exists())
                            {
                                dataTable = DataTable.parse(new FileInputStream(fl));
                            }
                            else
                            {
                                dataTable = DataTable.parse(new URL(csvMatcher.group(1)).openStream());
                            }
                            Runtime.sysoutln(String.format(
                                    "%s [%d lines x %d columns] included as %s",
                                    fl.getAbsolutePath(), dataTable.data.size(),
                                    dataTable.headers.length, refName), 5);
                            int ind = m.otherDataIdSequence++;
                            m.dataTables.put(ind, dataTable);
                            m.constants.put(refName, new Constant(refName, new Value((float)ind), true));
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                            throw new ParseException(String.format(
                                    "csv/tsv %s not loaded: %s", csvMatcher.group(1), ex.getMessage()),
                                    lineNr, caretPos);
                        }
                    }
                    else if (nilMatcher.find())
                    {
                        DataTable dataTable = new DataTable();
                        String refName = nilMatcher.group(2);
                        int ind = m.otherDataIdSequence++;
                        m.dataTables.put(ind, dataTable);
                        m.constants.put(refName, new Constant(refName, new Value((float)ind), true));
                    }
                    else
                    {
                        StringBuilder tmp = null;
                        if (lastName.charAt(0) == '#') { // library from jar
                            lastName = lastName.substring(1);
                            String pth = "/org/konte/resources/lib/" + lastName;
                            try {
                                InputStream in = Ui.class.getResource(pth).openStream();
                                tmp = Readers.load(in, StandardCharsets.UTF_8);
                            } catch(Exception ex) {
                                throw new ParseException("Library " + lastName + " does not exist", lineNr, caretPos);
                            }

                        } else {
                            File fl = getFile(workdir, lastName);
                            if (fl.exists())
                            {
                                tmp = Readers.fillStringBuilder(fl);
                            }
                            else
                            {
                                tmp = Readers.load(new URL(lastName).openStream());
                            }
                        }
                        if (tmp != null && tmp.length() > 0)
                        {
                            ArrayList<Tokenizer.TokenizerString> included =
                                    Tokenizer.retrieveTokenStrings(tmp.toString());
                            included = stripQuotes(included);
                            if (included.size() > 0)
                            {
                                tokenStrings.addAll(i+1, included);
                                Runtime.sysoutln(lastName + " included", 5);
                            } else
                                Runtime.sysoutln("Included file is empty: " + lastName, 5);
                        }
                    }
                    lastName = null;
                    curCtx = contextStack.pop();
                    break;
                case SYSTEM:
                    if (lastName == null)
                    {
                        GreyBoxSystem.Names gn = GreyBoxSystem.Names.valueOf(s);
                        if (gn == null)
                            throw new ParseException("Expecting 'system <type>', found " + lastName
                                    + " instead. (Types are: " + GreyBoxSystem.Names.values(), lineNr, caretPos);
                        greyBoxSystem = gn.generator.newInstance();
                        lastName = "--xxx--";
                        break;
                    }
                    else if ("--xxx--".equals(lastName)) {
                        int ind = m.otherDataIdSequence++;
                        m.greyBoxSystems.put(ind, greyBoxSystem);
                        m.constants.put(s, new Constant(s, new Value((float)ind), true));
                        lastName = s;
                        break;
                    }
                    if (t == Language.init)
                    {
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        tmpexps.clear();
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        Object[] vals = new Object[func.getArgs().length + 1];
                        vals[vals.length - 1] = m;  // insert model as last argument

                        int it = 0;
                        for (Expression e : func.getArgs())
                        {
                            float f = 0f;
                            try { f = e.evaluate(); } catch(Exception ex) {}
                            if (f == 0f && org.konte.expression.Name.class.isAssignableFrom(e.getClass())) {
                                org.konte.expression.Name name = (org.konte.expression.Name)e;
                                vals[it++] = name.getName();
                            }
                            else {
                                vals[it++] = f;
                            }
                        }
                        greyBoxSystem.initialize(vals);
                        curCtx = contextStack.pop();
                        lastName = null;
                    }
                    else
                        throw new ParseException("Expecting init(...) after system " + lastName + ", but got " + s, lineNr, caretPos);
                    break;
                case FX:
                    if (lastValue == null)
                    {
                        if (t == Language.subtract)
                        {
                            //ok
                        }
                        else
                        {
                            try
                            {
                                lastValue = Float.parseFloat(s);
                                if (tokenBefore == Language.subtract)
                                {
                                    lastValue = -lastValue;
                                }
                                val = null;
                            }
                            catch (Exception e)
                            {
                                throw new ParseException("fx layer not specified, found: " + s, lineNr, caretPos);
                            }
                        }
                    }
                    else
                    {
                        if (isSpecialContext)
                        {
                            if (val != null)
                            {
                                throw new ParseException("fx [layer] *  must be followed by <rep-count> { . . . }", lineNr, caretPos);
                            }
                            try
                            {
                                val = Float.parseFloat(s);
                            }
                            catch (Exception e)
                            {
                                throw new ParseException("fx *  must be followed by <rep-count> { . . . }", lineNr, caretPos);
                            }
                            isSpecialContext = false;
                            lastName = null;
                        }
                        else if (t == Language.left_curl)
                        {
                            if (lastName != null)
                                throw new ParseException("fx: expecting { but found " + s, lineNr, caretPos);
                        }
                        else if (t == Language.right_curl)
                        {
                            if (lastName == null)
                                throw new ParseException("fx: no matrix was specified between { and }", lineNr, caretPos);
                            String[] ss = lastName.split(":");
                            int[][] mtrix = new int[ss.length][ss[0].length()];
                            for (int j = 0; j < mtrix.length; j++)
                            {
                                for (int k = 0; k < mtrix[0].length; k++)
                                {
                                    int bits = (ss[j].charAt(k) - '0') & 0xFF;
                                    if (bits > 15) bits = (bits - 7) & 0x0F;
                                    mtrix[j][k] = bits;
                                }
                            }
                            CanvasEffect e = new CanvasEffect(mtrix);
                            if (m.canvasEffects.get(lastValue) == null)
                                m.canvasEffects.put(lastValue, new ArrayList<CanvasEffect>());
                            if (val != null)
                                e.repeat = val.intValue();
                            m.canvasEffects.get(lastValue).add(e);
                            curCtx = contextStack.pop();
                            lastName = null;
                            lastValue = null;
                            val = null;
                        }
                        else if (t == Language.multiply)
                        {
                            isSpecialContext = true;
                        }
                        else {
                            if (lastName == null)
                                lastName = "";
                            else
                            {
                                lastName += ':';
                            }
                            if (lastName.length() > 0)
                            {
                                String cmp = lastName.substring(0, lastName.indexOf(':'));
                                if (s.length() < cmp.length())
                                    throw new ParseException("String index out of range: " + (s.length()+1), lineNr, caretPos);
                            }
                            lastName += s;
                        }
                    }
                    break;
                case LIGHT_CREATE:
                    if (t==Language.left_curl)
                    {
                        lightBd.clearPointData();
                        curCtx = Parser.ParsingContext.LIGHT;
                    } else if (lastName == null)
                    {
                        lastName = s;
                        if (Language.isName(s))
                        {
                            if (t != null)
                                throw new ParseException("Unexpected token for light name: " + s, lineNr, caretPos);
                            lightBd.name(s);
                        } else
                            throw new ParseException("'light' must be followed by a name or a  '{' - found " + s, lineNr, caretPos);
                    }
                    else
                    {
                        throw new ParseException("Repeated name definition for light: " + lastName + "," + s, lineNr, caretPos);
                    }
                    break;
                case LIGHT:
                    if (t == Language.right_curl)
                    {
                        try {
                            lightBd.point(tmpexps, lrstfm);
                            m.addLight(lightBd.build());
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                            throw new ParseException(ex.getMessage(), lineNr, caretPos);
                        }
                        lrstfm = null;
                        lastName = null;
                        tmpexps.clear();
                        curCtx = contextStack.pop();
                    } else if (t == Language.point)
                    {
                        if (tmpexps.size() > 0)
                        {
                            try {
                                lightBd.point(tmpexps, lrstfm);
                            }
                            catch(Exception ex)
                            {
                                throw new ParseException(ex.getMessage(), lineNr, caretPos);
                            }
                            lrstfm = null;
                        }
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        tmpexps.clear();
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        for (Expression e : func.getArgs())
                        {
                            tmpexps.add(e);
                        }
                        //colSp = ColorSpace.createColorSpace(lastName, lexprs);
                    } else if  (t == Language.s)
                    {
                        i = getExpressionList(tokenStrings, i+1, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                        lightBd.strength(lexpr);
                    } else if  (t == Language.diffuse)
                    {
                        i = getExpressionList(tokenStrings, i+1, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                        lightBd.strength(lexpr);
                    } else if  (t == Language.specular)
                    {
                        i = getExpressionList(tokenStrings, i+1, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                        lightBd.specular(lexpr);
                    } else if  (t == Language.A)
                    {
                        i = getExpressionList(tokenStrings, i+1, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                            lightBd.alpha(lexpr);
                    } else if (t == Language.left_curl)
                    {
                        contextStack.push(curCtx);
                        lrstfm = new Transform(lineNr, caretPos);
                        curCtx = ParsingContext.LIGHT_ADJUSTMENTS;
                    }
                    else
                    {
                        if (lightBd.getType() >= 0)
                        {
                            throw new ParseException("Light type was already declared, then received " + s + "", lineNr, caretPos);
                        }
                        boolean matched = false;
                        for (LightModifier mod: LightModifier.values())
                        {
                            if (s.compareTo(mod.name())==0)
                            {
                                switch(mod)
                                {
                                    case DEFAULT:
                                        lightBd.type(0);
                                        matched = true;
                                        break;
                                    case COMPLEMENTARY:
                                        lightBd.type(1);
                                        matched = true;
                                        break;
                                    case DARKNESS:
                                        lightBd.type(2);
                                        matched = true;
                                        break;
                                    case PHONG:
                                        lightBd.type(10);
                                        matched = true;
                                        break;
                                    case AMBIENT:
                                        lightBd.type(11);
                                        matched = true;
                                        break;
                                }
                            }
                        }
                        if (!matched)
                        {
                            throw new ParseException("Unknown light type: " + s + " – known types are "
                                    + Arrays.toString(LightModifier.values()), lineNr, caretPos);
                        }
                    }
                    break;
                case SHADING_CREATE:
                    if (lastName == null)
                    {
                        lastName = s;
                        if (Language.isName(s))
                        {
                            if (t != null)
                                throw new ParseException("Unexpected token for shading name: " + s, lineNr, caretPos);
                            colBd.name(s);
                        } else
                            throw new ParseException("'shading' must be followed by a name, found " + s, lineNr, caretPos);
                    } else if (t==Language.left_curl)
                    {
                        curCtx = Parser.ParsingContext.SHADING;
                    }
                    else
                    {
                        throw new ParseException("Repeated name definition for shading: " + lastName + "," + s, lineNr, caretPos);
                    }
                    break;
                case SHADING:
                    if (t == Language.right_curl)
                    {
                        try {
                            colBd.point(tmpexps, lrstfm);
                            m.addColorSpace(colBd.build());
                        }
                        catch(Exception ex)
                        {
                            throw new ParseException(ex.getMessage(), lineNr, caretPos);
                        }
                        lrstfm = null;
                        lastName = null;
                        tmpexps.clear();
                        curCtx = contextStack.pop();
                    } else if (t == Language.point)
                    {
                        if (tmpexps.size() > 0)
                        {
                            try {
                                colBd.point(tmpexps, lrstfm);
                            }
                            catch(Exception ex)
                            {
                                throw new ParseException(ex.getMessage(), lineNr, caretPos);
                            }
                            lrstfm = null;
                        }
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        tmpexps.clear();
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        for (Expression e : func.getArgs())
                        {
                            tmpexps.add(e);
                        }
                        //colSp = ColorSpace.createColorSpace(lastName, lexprs);
                    } else if  (t == Language.A)
                    {
                        i = getExpressionList(tokenStrings, i+1, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                        colBd.strength(lexpr);
                    } else if (t == Language.left_curl)
                    {
                        contextStack.push(curCtx);
                        lrstfm = new Transform(lineNr, caretPos);
                        curCtx = ParsingContext.SHADING_ADJUSTMENTS;
                    }
                    break;
                case STARTSHAPE:
                    if (t != null)
                    {
                        throw new ParseException(
                                "Misplaced token after startshape: " + s, lineNr, caretPos);
                    }
                    else
                    {
                        if (Language.returnAsValue(s) != null)
                        {
                            throw new ParseException(
                                    "Not expecting a numerical constant after startshape", lineNr, caretPos);
                        }
                        m.startshape = s;
                        curCtx = contextStack.pop();
                    }
                    break;
                case CAMERA_CREATE:
                    if (t == Language.left_curl)
                    {
                        curCtx = Parser.ParsingContext.CAMERA;
                        ltfm = new Transform("fov", lineNr, caretPos);
                        camBd = new CameraBuilder(ltfm);
                        if (lastName != null)
                        {
                            camBd.setName(lastName);
                            lastName = null;
                        }
                    }
                    else if (t == null)
                    {
                        if (lastName != null)
                            throw new ParseException("Duplicate camera name: " + s, lineNr, caretPos);
                        lastName = s;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token after camera: " + s, lineNr, caretPos);
                    }
                    break;
                case CAMERA:
                    if (t instanceof InnerToken)
                    {
                        if ((t == Language.x) || (t == Language.y) ||
                                (t == Language.z) || (t == Language.rx) ||
                                (t == Language.ry) || (t == Language.rz) ||
                                (t == Language.s))
                                {
                            lastInnerToken = t;
                        }
                        else
                        {
                            throw new ParseException("Not expecting " + s +
                                    " in camera declaration. Valid tokens are: x y z rx ry rz s", lineNr, caretPos);
                        }
                    } else if (t == Language.lookat) {
                        i = getExpressionList(tokenStrings, i, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                        camBd.lookat = (ExpressionFunction)lexpr;
                    } else if (t == Language.metric) {
                        lastName = tokenStrings.get(++i).getString();
                    } else if (t == Language.right_curl)
                    {
                        if (lastInnerToken != null)
                            throw new ParseException("Orphaned " + lastInnerToken + " in Camera declaration", lineNr, caretPos);
                        curCtx = contextStack.pop();
                        Camera cam = camBd.build();
                        m.cameras.add(cam);
                        m.addConstant(cam.getName(), new Value((float)m.cameras.indexOf(cam)), true);
                        if (lastName != null) {
                            Object arg = lastName;
                            try { arg = Float.parseFloat(lastName); } catch(Exception ex) {}
                            DistanceMetric metric = DistanceMetric.guess(arg);
                            cam.setDistanceMetric(metric);
                            lastName = null;
                        }
                        camBd = null;
                    }
                    else
                    {
                        boolean used = false;
                        for (CameraProperties prop : CameraProperties.values())
                            if (s.equalsIgnoreCase(prop.toString()))
                            {
                                if (lastInnerToken != null)
                                    throw new ParseException("Orphaned " + s + " follows " + lastInnerToken + " in Camera declaration", lineNr, caretPos);
                                camBd.addProperty(prop);
                                used = true;
                            }
                        if (!used)
                        {
                            i = getExpressionList(tokenStrings, i, exprL);
                            lexpr = exprParser.parse(exprL, 0, m);
                            if (lastInnerToken == null)
                            {
                                if (lexpr instanceof Name)
                                {
                                    if (camBd.getName() != null)
                                        throw new ParseException("Duplicate camera name " + s, lineNr, caretPos);
                                    camBd.setName(s);
                                }
                                else
                                {
                                    camBd.addExtra(lexpr);
                                }
                            }
                            else
                            {
                                lexprs.clear();
                                lexprs.add(lexpr);
                                ltfm.setShapeTransform(lastInnerToken, lexprs);
                                lastInnerToken = null;
                            }
                        }
//                        throw new ParseException(
//                                "Wrong token inside shape adjustment block: " + s);
                    }
                    break;
                case DEF_NAME:
                    isDef = true;
                case MACRO_NAME:
                    if (t == null)
                    {
                        if (Language.isName(s))
                        {
                            lastName = s;
                            curCtx = ParsingContext.MACRO_VALUE;
                        }
                        else
                        {
                            throw new ParseException(
                                    "Missing MACRO/DEF name near " + s, lineNr, caretPos);
                        }
                    }
                    else
                    {
                        throw new ParseException(
                                "Invalid token after MACRO/DEF: " + s, lineNr, caretPos);
                    }
                    break;
                case MACRO_VALUE:
                    i = getExpressionList(tokenStrings, i, exprL);
                    lexpr = exprParser.parse(exprL, 0, m);
                    val = null;
                    if (isDef)
                    {
                        if ((val = lexpr.evaluate()) == null)
                            throw new ParseException("Could not evaluate constant " + lastName);
                        lexpr = new Value(val);
                    }
                    Constant macro = m.addConstant(lastName, lexpr, isDef);
                    if (lastRule != null)
                        lastRule.addMacro(macro);
                    Runtime.sysoutln((isDef ?
                        "DEF: " + lastName + "=" + val :
                        "MACRO: " + lastName + "=" + lexpr)  , 0);
                    lastName = null;
                    isDef = false;
                    curCtx = contextStack.pop();
                    break;
                case PATH_CREATE:
                    isSpecialContext = true;
                case RULE_CREATE:
                    if (t == null)
                    {
                        if (Language.isName(s))
                        {
                            if (!wasNamed)
                            {
                                lastName = s;
                                lastValue = null;
                                lastRule = null;
                                lndr = null;
                                if (m.constants.get(s) != null)
                                {
                                    throw new ParseException(
                                            "Rule/path name reserved to a constant: " + s, lineNr, caretPos);
                                }
                                wasNamed = true;
                            }
                            else
                            {
                                throw new ParseException(
                                        "Invalid syntax: 'rule' or 'path' name must be followed by a positive weight or '{' " +
                                        "(near " + s + ")", lineNr, caretPos);
                            }
                        }
                        else
                        {
                            if (!wasNamed)
                            {
                                throw new ParseException("Missing rule name near " + s, lineNr, caretPos);
                            }
                            lastValue = Language.returnAsValue(s);
                            if (lastValue == null)
                            {
                                throw new ParseException(
                                        "Invalid syntax: 'rule' or 'path' name must be followed by a positive weight or '{' " +
                                        "(near " + s + ")", lineNr, caretPos);
                            }
                        }
                    } else if (t == Language.left_curl)
                    {
                        if (!wasNamed)
                        {
                            throw new ParseException("Missing rule or path name", lineNr, caretPos);
                        }
                        if (lastValue == null)
                        {
                            lastValue = 1.0f;
                        }
                        lrepeat = null;
                        Rule tmprule = isSpecialContext ? new PathRule() : new Rule();
                        lastRule = m.addRule(lastName, lastValue, tmprule);
                        lndr = lastRule.getNonDeterministicRule();
                        if (isSpecialContext)
                        {
                            curCtx = Parser.ParsingContext.PATH;
                            isSpecialContext = false;
                        }
                        else
                        {
                            curCtx = Parser.ParsingContext.RULE;
                        }
                        lastValue = null;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token after 'rule' or 'path': " + s, lineNr, caretPos);
                    }
                    break;
                case PATH:
                    if (t == null && s.trim().toLowerCase().startsWith("m "))
                    { // attempt svg import via konte paths
                        SvgImport svgi = new SvgImport();
                        StringBuilder path = new StringBuilder();
                        path.append("<root><path id=\"").append(lastRule.getName()).append("\"");
                        path.append(" d=\"").append(s).append("\" /></root>");
                        if (svgi.initDocument(new ByteArrayInputStream(path.toString().getBytes())) != null)
                        {
                            String kontePath = svgi.allPathsToScript("", new HashMap<String, String>());
                            kontePath = kontePath.substring(kontePath.lastIndexOf('{') + 1);
                            kontePath = kontePath.substring(0, kontePath.lastIndexOf('}'));
                            ArrayList<Tokenizer.TokenizerString> included =
                                    Tokenizer.retrieveTokenStrings(kontePath);
                            if (included.size() > 0)
                            {
                                tokenStrings.remove(i);
                                tokenStrings.addAll(i--, included);
                                Runtime.sysoutln(s + " included", 0);
                            } else
                                Runtime.sysoutln("Included svg script is empty: " + s, 5);
                            break;
                        }
                    }
                case RULE:
                    if (t == null || t instanceof Untransformable ||
                            t == Language.peek || t == Language.pop)
                            {
                        if (Language.isName(s))
                        {
                            if (lastRule instanceof PathRule)
                                throw new ParseException(String.format(
                                        "call ('%s') within 'path' is prohibited", s), lineNr, caretPos);
                            if (t instanceof Untransformable)
                            {
                                ltfm = new TerminatingShape(s, lineNr, caretPos);
                                ((TerminatingShape)ltfm).shape = (Untransformable)t;
                            }
                            else
                            {
                                ltfm = new Transform(s, lineNr, caretPos);
                            }
                            if (lrepeat != null)
                            {
                                lrepeat.repeatedTransform = ltfm;
                                lrepeat = null;
                            }
                            else
                            {
                                lastRule.addTransform(ltfm);
                            }
                            lastValue = null;
                            contextStack.push(curCtx);
                            curCtx = Parser.ParsingContext.RULE_TRANSFORM_CREATE;

                        }
                        else
                        {
                            lastValue = Language.returnAsValue(s);
                            if (lastValue != null)
                            {
                                lexpr = new Value(lastValue);
                            }
                            else
                            {
                                lexpr = Name.createExpressionFinalName(s);
                            }
                            contextStack.push(curCtx);
                            curCtx = Parser.ParsingContext.REPEAT_CREATE;
                            isSpecialContext = false;

                        }

                    } else if (t == Language.moveto && lastRule instanceof PathRule)
                    {
//                        if (((PathRule)lastRule).moveto != null)
//                            throw new ParseException("Not expecting 'moveto' again", lineNr, caretPos);
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        Placeholder ple = new Placeholder(PathRule.MOVE_TO, func.getArgs());
                        ((PathRule)lastRule).steps.add(ple);
                        ((PathRule)lastRule).closed = 0;
                    } else if (t == Language.bend && lastRule instanceof PathRule)
                    {
                        PathRule pr = (PathRule)lastRule;
//                        if (pr.closed > 0)
//                            throw new ParseException("Path already closed", lineNr, caretPos);
                        if (pr.steps.size() > 2
                                && pr.steps.get(pr.steps.size()-1).type == PathRule.BEND
                                && pr.steps.get(pr.steps.size()-2).type == PathRule.BEND)
                            throw new ParseException("More than two control points in path sequence", lineNr, caretPos);
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        Placeholder ple = new Placeholder(PathRule.BEND, func.getArgs());
                        pr.steps.add(ple);
                        pr.closed = 0;
                    } else if (t == Language.curveto && lastRule instanceof PathRule)
                    {
                        PathRule pr = (PathRule)lastRule;
//                        if (pr.closed > 0)
//                            throw new ParseException("Path already closed", lineNr, caretPos);
                        // add missing bezier control points if necessary
                        pr.checkBezierControls();
                        // add curveto point
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        Placeholder ple = new Placeholder(PathRule.CURVE_TO, func.getArgs());
                        pr.steps.add(ple);
                        pr.closed = 0;
                    } else if (t == Language.lineto && lastRule instanceof PathRule)
                    {
                        PathRule pr = (PathRule)lastRule;
                        i = getExpressionList(tokenStrings, i, exprL);
                        try {
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pex)
                        {
                            throw new ParseException(
                                pex.getMessage(), lineNr, caretPos);
                        }
                        ExpressionFunction func = (ExpressionFunction)lexpr;
                        pr.steps.add(new Placeholder(PathRule.LINE_TO, func.getArgs()));
                    } else if (t == Language.close && lastRule instanceof PathRule)
                    {
                        PathRule pr = (PathRule)lastRule;
                        if (pr.steps.get(pr.steps.size() - 1).type == PathRule.CLOSE)
                        {
                            throw new ParseException("Duplicat close in path", lineNr, caretPos);
                        }
                        pr.steps.add(new Placeholder(PathRule.CLOSE, null));
                        pr.closed = 1;
                    } else if (t instanceof LanguageFunctor)
                    {
                        throw new ParseException(String.format(
                                "Keyword '%s' prohibited in 'rule' block", s), lineNr, caretPos);
                    } else if (t == Language.subtract || t == Language.left_bracket ||
                                t instanceof Function)
                                {
                        i = getExpressionList(tokenStrings, i, exprL, 2);
                        lexpr = exprParser.parse(exprL, 0, m);
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.REPEAT_CREATE;
                        isSpecialContext = false;
                    } else if (t == Language.multiply) { // special case of repeat : apply transforms, call contained then once
                        lexpr = new Value(-1f);
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.REPEAT_CREATE;
                        isSpecialContext = true;
                    } else if (t == Language.pre_context)
                    {
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.RULE_PRE_CREATE;
                    } else if (t == Language.post_context)
                    {
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.RULE_POST_CREATE;
                    } else if (t == Language.def)
                    {
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.MACRO_NAME;
                    } else if (t == Language.macro)
                    {
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.MACRO_NAME;
                    } else if (t == Language.ifToken)
                    {
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.IF_CREATE;
                    } else if (t == Language.left_curl)
                    {
                        contextStack.push(curCtx);
                        isSpecialContext = true;
                        BlockStructure blockStructure = new BlockStructure(lineNr, caretPos);
                        conditionalStack.push(blockStructure);
                        curCtx = Parser.ParsingContext.BLOCK_CREATE;
                        i--; // curl is required by next state
                    } else if (t == Language.right_curl)
                    {
                        isSpecialContext = false;
                        wasNamed = false;   // clear our trail
                        lastName = null;
                        lastRule = null;
                        curCtx = contextStack.pop();
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token within " + lastRule + ": " + s, lineNr, caretPos);
                    }
                    break;
                case RULE_TRANSFORM_CREATE:
                    if (t == Language.left_curl)
                    {
                        curCtx = Parser.ParsingContext.TRANSFORM_ADJUSTMENTS;
                        lastValue = null;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token within " + lastRule + ": " + s, lineNr, caretPos);
                    }
                    break;
                case LIGHT_ADJUSTMENTS:
                case SHADING_ADJUSTMENTS:
                case TRANSFORM_ADJUSTMENTS:
                case REPEAT_ADJUSTMENTS:
                    if (t instanceof InnerToken
                            || t == Language.def || t == Language.undef
                            || t == Language.right_curl || t == Language.left_curl)
                            {
                        if (lastInnerToken == null && t instanceof InnerToken)
                        {
                            lastInnerToken = t;
                            pos = 0;
                            InnerToken it = (InnerToken)t;
                            if (it.nParamsAllowed(0) && !it.higherParamCountAllowed(0)) {
                                lexprs.clear();
                                if (curCtx==ParsingContext.TRANSFORM_ADJUSTMENTS)
                                    ltfm.setShapeTransform(lastInnerToken, lexprs);
                                else
                                    lrstfm.setShapeTransform(lastInnerToken, lexprs);
                            }
                        } else if (lastInnerToken != null && ((InnerToken)lastInnerToken).nParamsAllowed(pos)) {  // number of set arguments is (pos+1)-1 = pos
                            if (pos > 0) {
                                if (curCtx==ParsingContext.TRANSFORM_ADJUSTMENTS)
                                    ltfm.setShapeTransform(lastInnerToken, lexprs);
                                else
                                    lrstfm.setShapeTransform(lastInnerToken, lexprs);
                            }
                            lastInnerToken = (t instanceof InnerToken ? t : null);
                            pos = 0;
                        }
                        else if (t instanceof InnerToken)
                        {
                            throw new ParseException("Wrong number of arguments to " + lastInnerToken +
                                    " or missing () in " + lastRule, lineNr, caretPos);
                        }
                        if (t == Language.def || t == Language.undef) {
                            contextStack.push(curCtx);
                            isSpecialContext = false;
                            pos = (t == Language.undef ? 1 : 0);    // 0 denotes def
                            curCtx = Parser.ParsingContext.TRANSFORM_DEFS;
                        } else if (t == Language.left_curl)
                        {
                            isSpecialContext = true;
                            contextStack.push(curCtx);
                            pos = 0;
                            curCtx = Parser.ParsingContext.TRANSFORM_DEFS;
                            isSpecialContext = true;
                            wasSet = wasNamed = false;
                        } else if (t == Language.right_curl)
                        {
                            if (lastInnerToken != null)
                            {
                                throw new ParseException("Missing argument(s) to " + lastInnerToken
                                        + " in " + lastRule, lineNr, caretPos);
                            }
                            if (curCtx != ParsingContext.REPEAT_ADJUSTMENTS)
                                lrepeat = null;
                            curCtx = contextStack.pop();
                        }
                    }
                    else
                    {
                        if (lastInnerToken == null)
                        {
                            if (t==Language.left_bracket && i > 0 &&
                                    Language.tokenByName(tokenStrings.get(i-1).getString())==null)
                                throw new ParseException("Unknown function: " + tokenStrings.get(i-1).getString(), lineNr, caretPos);
                            throw new ParseException("token missing or unknown expression near " + s + " in " + lastRule, lineNr, caretPos);
                        }
                        String MM = "#[0-9A-Fa-f]";
                        if (lastInnerToken == Language.RGB && s.startsWith("#"))
                        {
                            boolean b8 = s.matches(MM + "{8}");
                            ArrayList<Expression> aexpr = null;
                            if (b8)
                            {
                                aexpr = new ArrayList<Expression>();
                                aexpr.add(new Value( -1f + 1f / 255f * (float)Integer.parseInt(s.substring(7), 16)));
                            }
                            if (!b8 && !s.matches(MM + "{6}"))
                            {
                                throw new ParseException("Give RGB color in form #09AFFF or #09AFFF80", lineNr, caretPos);
                            }
                            lexprs = decodeHtmlRgb(s);
                            if (curCtx==ParsingContext.TRANSFORM_ADJUSTMENTS)
                            {
                                ltfm.setShapeTransform(lastInnerToken, lexprs);
                                if (b8) { ltfm.setShapeTransform(Language.A, aexpr); }
                            }
                            else
                            {
                                lrstfm.setShapeTransform(lastInnerToken, lexprs);
                                if (b8) { lrstfm.setShapeTransform(Language.A, aexpr); }
                            }
                            lastInnerToken = null;
                            break;
                        }
                        try {
                            i = getExpressionList(tokenStrings, i, exprL);
                            if (lastInnerToken == Language.push && exprL.size() == 1) {
                                Token tmpToken = exprL.get(0);
                                Matcher matcher2 = prefixPattern.matcher(tmpToken.name);
                                if (matcher2.find()) {
                                    String prefixType = matcher2.group(1);
                                    String toPrefix = matcher2.group(2);
                                    int strait = 1;
                                    if ('\\' == prefixType.charAt(prefixType.length() - 1)) {
                                        strait = 0;
                                        prefixType = prefixType.substring(0, prefixType.length() - 1);
                                    }
                                    PrefixStringMap psmap = Language.prefixMaps.get(prefixType);
                                    if (psmap == null)
                                        throw new ParseException("Cannot find prefix mapping for ^" + prefixType + "^ (in " + tmpToken.name + ")", lineNr, caretPos);
                                    String chr = toPrefix.charAt(strait==0?0:toPrefix.length()-1) + "";
                                    String mapped = psmap.map(chr);
                                    exprL.set(0, new Token(mapped));
                                    if (toPrefix.length() > 1) {
                                        TokenizerString tts = tokenStrings.get(i);
                                        tokenStrings.set(i, 
                                                new TokenizerString('^'+matcher2.group(1)+'^'+toPrefix.substring(1-strait, toPrefix.length()-strait), 
                                                        tts.getLineNr(), tts.getCaretPos()));
                                        i--;
                                    }
                                }
                            }
                            lexpr = exprParser.parse(exprL, 0, m);
                        }
                        catch(ParseException pe)
                        {
                            throw new ParseException(pe.getMessage(), lineNr, caretPos);
                        }

                        switch (pos)
                        {
                            case 0:
                                lexprs.clear();
                            default:
                                if (!((InnerToken)lastInnerToken).higherParamCountAllowed(pos))
                                    throw new ParseException("Too many arguments to " + lastInnerToken + " in " + lastRule, lineNr, caretPos);
                                lexprs.add(lexpr);
                                if (!((InnerToken)lastInnerToken).higherParamCountAllowed(pos+1) &&
                                        ((InnerToken)lastInnerToken).nParamsAllowed(pos+1))
                                        {
                                    if (curCtx==ParsingContext.TRANSFORM_ADJUSTMENTS)
                                        ltfm.setShapeTransform(lastInnerToken, lexprs);
                                    else
                                        lrstfm.setShapeTransform(lastInnerToken, lexprs);
                                    lastInnerToken = null;
                                }
                                break;
                        }
                        pos++;
                    }
                    break;
                case TRANSFORM_DEFS:
                    if (!isSpecialContext && t != Language.left_curl)
                        throw new ParseException("DEF block should start with a  {", lineNr, caretPos);
                    if (wasNamed && wasSet && isSpecialContext)
                    {
                        i = getExpressionList(tokenStrings, i, exprL, Language.IS_SEMICOLON_SEPARATORS?1:0);
                        lexpr = exprParser.parse(exprL, 0, m);
                        boolean isUndef = pos == 1 ? true : false;
                        Definition d = null;
                        if (contextStack.peek()==ParsingContext.REPEAT_ADJUSTMENTS)
                        {
                            d = lrstfm.setDef(lastName, lexpr, isUndef);
                        } else
                            d = ltfm.setDef(lastName, lexpr, isUndef);
                        Runtime.sysoutln("DEF: " + d.toString() + "=" + d.definition, 0);
                        if (d.id == null)
                            m.addDefinition(d);
                        wasNamed = wasSet = false;

                    } else if (t == Language.left_curl)
                    {
                        if (isSpecialContext)
                            throw new ParseException("Duplicate  {  in DEFinition block", lineNr, caretPos);
                        isSpecialContext = true;
                        wasSet = wasNamed = false;
                    } else if (t == Language.semicolon || t == Language.comma)
                    {
                        wasSet = wasNamed = false;
                    } else if (t == Language.right_curl)
                    {
                        isSpecialContext = false;
                        lastName = null;
                        curCtx = contextStack.pop();
                    } else if (t == Language.equals)
                    {
                        wasSet = true;
                    } else if (t == null || !(t instanceof InnerToken))
                    {
                        if (!wasNamed)
                        {
                            if (wasSet) throw new ParseException("Orphaned  =  near " + s, lineNr, caretPos);
                            lastName = s;
                            wasNamed = true;
                        } else
                            throw new ParseException("Missing = in DEFinition block near " + s, lineNr, caretPos);
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token within DEF block: " + s, lineNr, caretPos);
                    }
                    break;
                case RULE_PRE_CREATE:
                case RULE_POST_CREATE:
                    if (t == Language.left_curl)
                    {
                        switch(curCtx)
                        {
                            case RULE_PRE_CREATE: curCtx = Parser.ParsingContext.RULE_PRE; break;
                            case RULE_POST_CREATE: curCtx = Parser.ParsingContext.RULE_POST; break;
                        }
                        lastValue = null;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token after pre or post condition: " + s, lineNr, caretPos);
                    }
                    break;
                case RULE_PRE:
                case RULE_POST:
                    if (t == Language.semicolon || t == Language.comma)
                    {
                        isSpecialContext = false;
                    } else if (t instanceof Comparator)
                    {
                        throw new ParseException("Misplaced comparator " + lexpr + " in " +
                                curCtx + " near " + s, lineNr, caretPos);
                    } else if (t == Language.right_curl)
                    {
                        curCtx = contextStack.pop();
                    }
                    else
                    {
                        i = getExpressionList(tokenStrings, i, exprL, Language.IS_SEMICOLON_SEPARATORS?1:0);
                        lexpr = exprParser.parse(exprL, 0, m);
                        switch (curCtx)
                        {
                            case RULE_PRE: lastRule.addPre((BooleanExpression) lexpr); break;
                            case RULE_POST: lastRule.addPost((BooleanExpression) lexpr); break;
                        }
                    }
                    break;
                case REPEAT_CREATE:
                    if (t == Language.left_curl)
                    {
                        if (!isSpecialContext)
                        {
                            throw new ParseException("Missing * from repeat block near " + s, lineNr, caretPos);
                        }
                        RepeatStructure r = new RepeatStructure(lineNr, caretPos);
                        r.repeats = lexpr;
                        if (lrepeat != null)
                        {
                            lrepeat.repeatedTransform = r;
                        } else if (conditionalStack.size() > 0)
                        {
                            conditionalStack.peek().onCondition.add(r);
                        }
                        else
                        {
                            lastRule.addTransform(r);
                        }
                        lrepeat = r;    // ready for transform iteration
                        lrstfm = new Transform(lineNr, caretPos);  // will be loaded in repeat block
                        lrepeat.repeatTransform = lrstfm;
                        curCtx = Parser.ParsingContext.REPEAT_ADJUSTMENTS;
                    } else if (t == Language.multiply)
                    {
                        if (isSpecialContext)
                            throw new ParseException("Duplicate *", lineNr, caretPos);
                        isSpecialContext = true;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token within repeat block: " + s, lineNr, caretPos);
                    }
                    break;
                case BLOCK_CREATE:
                case IF_CREATE:
                    if (t == Language.left_bracket)
                    {
                        if (isSpecialContext)
                            throw new ParseException(
                                    "Expecting ')' - found '" + t + "' after conditional", lineNr, caretPos);
                        isSpecialContext = true;
                        i = getExpressionList(tokenStrings, i, exprL, 0);
                        lexpr = exprParser.parse(exprL, 0, m);

                        ConditionalStructure cond = new ConditionalStructure(lineNr, caretPos);
                        if (!(lexpr instanceof BooleanExpression))
                        {
                            lexpr = new BooleanExpression.Ne(lexpr, new Value(0f));

                        }
                        cond.conditional = (BooleanExpression)lexpr;
                        conditionalStack.push(cond);
                    }
                    else if(t == Language.right_bracket)
                    {
                        throw new ParseException("Orphaned ')' after 'if'", lineNr, caretPos);
                    }
                    else if(t == Language.left_curl)
                    {
                        ConditionalStructure cond = conditionalStack.peek();
                        if (cond == null || !isSpecialContext)
                            throw new ParseException("Requiring a conditional after if, found " + s, lineNr, caretPos);
                        if (lastRule instanceof PathRule)
                        {
                            throw new ParseException("block structuring prohibited within 'path'", lineNr, caretPos);
                        }
                        if (lrepeat != null)
                        {
                            lrepeat.repeatedTransform = cond;
                        } else
                        if (conditionalStack.size() > 1)
                        {
                            Iterator it = conditionalStack.iterator();
                            it.next();
                            ((ConditionalStructure)it.next()).onCondition.add(cond);
                        }  else
                            lastRule.addTransform(cond);
                        lrepeat = null; // repeats always inside if, not the other way round
                        curCtx = Parser.ParsingContext.IF;
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token within block definition: " + s, lineNr, caretPos);
                    }
                    break;
                case IF:
                    if (t == null || t instanceof Untransformable ||
                            t == Language.peek || t == Language.pop)
                            {
                        if (Language.isName(s))
                        {
                            if (t instanceof Untransformable)
                            {
                                ltfm = new TerminatingShape(s, lineNr, caretPos);
                                ((TerminatingShape)ltfm).shape = (Untransformable)t;
                            }
                            else
                            {
                                ltfm = new Transform(s, lineNr, caretPos);
                            }
                            if (lrepeat != null)
                                lrepeat.repeatedTransform = ltfm;
                            else
                                conditionalStack.peek().onCondition.add(ltfm);
                            lastValue = null;
                            contextStack.push(curCtx);
                            curCtx = Parser.ParsingContext.RULE_TRANSFORM_CREATE;

                        }
                        else
                        {
                            lastValue = Language.returnAsValue(s);
                            if (lastValue != null)
                            {
                                lexpr = new Value(lastValue);
                            }
                            else
                            {
                                lexpr = Name.createExpressionFinalName(s);
                            }
                            contextStack.push(curCtx);
                            curCtx = Parser.ParsingContext.REPEAT_CREATE;
                            isSpecialContext = false;

                        }
                    } else if (t instanceof LanguageFunctor)
                    {
                        throw new ParseException(String.format(
                                "Keyword '%s' prohibited in 'if' block", s), lineNr, caretPos);
                    } else if (t == Language.subtract || t == Language.left_bracket ||
                                t instanceof Function)
                                {
                        i = getExpressionList(tokenStrings, i, exprL);
                        lexpr = exprParser.parse(exprL, 0, m);
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.REPEAT_CREATE;
                        isSpecialContext = false;
                    } else if (t == Language.multiply) {
                        lexpr = new Value(-1f);
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.REPEAT_CREATE;
                        isSpecialContext = true;
                    } else if (t == Language.ifToken)
                    {
                        contextStack.push(curCtx);
                        curCtx = Parser.ParsingContext.IF_CREATE;
                        isSpecialContext = false;
                    } else if (t == Language.left_curl)
                    {
                        contextStack.push(curCtx);
                        BlockStructure blockStructure = new BlockStructure(lineNr, caretPos);
                        conditionalStack.push(blockStructure);
                        isSpecialContext = true;
                        curCtx = Parser.ParsingContext.BLOCK_CREATE;
                        i--;    // next state requires {
                    } else if (t == Language.right_curl)
                    {
                        isSpecialContext = false;
                        conditionalStack.pop();
                        lrepeat = null;
                        curCtx = contextStack.pop();
                    }
                    else
                    {
                        throw new ParseException(
                                "Misplaced token within  " + lastRule + ": " + s, lineNr, caretPos);
                    }
                    break;
                default:
                    throw new ParseException("Parsing broken", lineNr, caretPos);
            }
            }
            catch(ParseException ex )
            {
                ex.printStackTrace();
                if (ex.getLineNr() == 0 && ex.getCaretPos() == 0) {
                    ex.setLineAndCaret(lineNr, caretPos);
                }
                throw ex; //new ParseException(ex.getMessage(),lineNr,caretPos);
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                throw new ParseException(ex.getMessage(), lineNr, caretPos);
            }
            tokenBefore = t;

        }
        if (curCtx != ParsingContext.GRAMMAR)
            throw new ParseException("Unterminated grammar after " + s, lineNr, caretPos);
        //Runtime.sysoutln(m, -1);
        Runtime.sysoutln(" ok",3);
        return m;
    }


    private File getFile(String workdir, String lastName)
    {
        File fl = (new File(lastName));
        if (workdir != null && !fl.getAbsolutePath().equals(lastName))
        {
            int lind = workdir.indexOf("/");
            int wind = workdir.indexOf("\\");
            if (lind >= 0)
            {
                lastName = lastName.replace("\\", "/");
            }
            if (wind >= 0)
            {
                lastName = lastName.replace("/", "\\\\");
            }
            lastName = workdir + lastName;
            fl = new File(lastName);
        }
        return fl;
    }

}
