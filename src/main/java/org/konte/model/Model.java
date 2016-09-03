package org.konte.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.konte.parse.ParseException;
import org.konte.parse.CameraBuilder;
import org.konte.lang.Tokens.Constant;
import org.konte.lang.Language;
import org.konte.model.NonDeterministicRule.WeighedRule;
import org.konte.expression.Name;
import org.konte.expression.BooleanExpression;
import org.konte.expression.Expression;
import org.konte.expression.ExpressionFunction;
import org.konte.expression.Operator;
import org.konte.expression.Value;
import org.konte.generate.RandomFeed;
import org.konte.generate.ShapeReader;
import org.konte.image.Camera;
import org.konte.generate.RuleWriter;
import org.konte.generate.Runtime;
import org.konte.image.CanvasEffect;
import org.konte.lang.ShapeReaders;
import org.konte.model.PathRule.Placeholder;
import org.konte.model.Untransformable.Effect;

/**
 *
 * @author pvto
 */
public class Model {

    public static final Object RNDFEED_KEY = new Object();
    public RandomFeed getRandomFeed()
    {
        return (RandomFeed)globalvar.get(RNDFEED_KEY);
    }

    public LinkedHashMap<String, NonDeterministicRule> rules = new LinkedHashMap<>(30);
    public LinkedHashMap<String, Constant> constants = new LinkedHashMap<>(30);
    public LinkedHashMap<Integer, Object> objects = new LinkedHashMap<>();
    public LinkedHashMap<String, Definition> definitions = new LinkedHashMap<>(30);
    public String startshape = null;
    public Transform backgroundTransform = null;
    public Background bg = null;
    public ArrayList<Camera> cameras = new ArrayList<>();
    public GlobalLighting lighting;
    public ArrayList<ColorSpace> colorSpaces = new ArrayList<>();
    public ShapeReader shapeReader;
    public Map globalvar = new HashMap();
    // following tables are for fast access in generate phase
    public Rule[] indexedRules;
    public NonDeterministicRule[] indexedNd;
    public Transform[] indexedSt;     //
    float[] indexedConstants;
    public Definition[] indexedDefinitions;
    public ArrayList<Definition.NameMap> defMaps = new ArrayList<>();
    private ArrayList<Name> nameExpressions = new ArrayList<>();
    private int nextid = 0;
    // is set true in  initForGenerate()
    // ; set false in parse
    public boolean isGenerateContext = false;
    public boolean isPreEvaluated = false;
    public long maxShapes = 1000L * 1000L * 10L;
    public int pushStackSize = 16;
    public float minfeaturesize = 0.0001f;
    public int imgIndex, imguIndex, imgvIndex, imgrIndex, imggIndex, imgbIndex, imgaIndex;
    public static BitmapCache bitmapCache = new BitmapCache();
    public DrawingContext context;
    public Map<Float, List<CanvasEffect>> canvasEffects = new HashMap<>();

    private boolean drawLayerSeparately = false;
    private boolean hasMultipleLayers = false;
    
    public Model()
    {
        isGenerateContext = false;
        defCounter = 0;
    }

    public boolean isDrawLayersSeparately() {
        return drawLayerSeparately || canvasEffects.size() > 0;
    }

    
    public void addColorSpace(ColorSpace build)
    {
        this.colorSpaces.add(build);
    }

    public void addLight(Light light)
    {
        if (lighting == null)
        {
            lighting = new GlobalLighting();
        }
        lighting.addLight(light);
    }

    public Rule addRule(String name, double weigh, Rule r)
    {
        NonDeterministicRule nd = rules.get(name);
        if (nd == null)
        {
            nd = new NonDeterministicRule(name);
            rules.put(name, nd);
        }
        objects.put(nextid, r);
        r.setId(nextid++);
        r.setNonDeterministicRule(nd);
        nd.addRule(weigh, r);
        return r;
    }

    public void registerNameExpression(Name n)
    {
        nameExpressions.add(n);
    }

    int getNameExpressionId(String string)
    {
        for (Definition.NameMap nm : defMaps)
        {
            if (nm.name.equals(string))
            {
                return nm.id;
            }
        }
        return -1;
    }

    private void checkForConstantCyclicity(Iterable<Constant> constants) throws ParseException
    {
        for (Constant c : constants)
        {
            Expression cur = c.value;
            cyclCheck0(cur, new ArrayList<Name>());
        }
    }

    private boolean cyclCheck0(Expression cur, List<Name> existing) throws ParseException
    {
        boolean res = true;
        if (cur instanceof Name)
        {
            Constant c = constants.get(((Name) cur).getName());
            if (c != null)
            {
                for (Name e : existing)
                {
                    if (e.getName().compareTo(c.name) == 0)
                    {
                        throw new ParseException("Cyclic MACRO expression: " + c);
                    }
                }
                existing.add((Name) cur);
                List<Name> tmp = new ArrayList<Name>(existing);
                res |= cyclCheck0(c.value, existing);
                existing = tmp;
            }
        }
        else
        {
            if (cur instanceof Operator)
            {
                Operator op = (Operator) cur;
                if (op.getLeading() != null)
                {
                    List<Name> tmp = new ArrayList<>(existing);
                    res = res | cyclCheck0(op.getLeading(), existing);
                    existing = tmp;
                }
                if (op.getTrailing() != null)
                {
                    List<Name> tmp = new ArrayList<>(existing);
                    res = res | cyclCheck0(op.getTrailing(), existing);
                    existing = tmp;
                }
            }
            else if (cur instanceof ExpressionFunction)
            {
                ExpressionFunction func = (ExpressionFunction) cur;
                for (Expression e : func.getArgs())
                {
                    List<Name> tmp = new ArrayList<>(existing);
                    res |= cyclCheck0(e, existing);
                    existing = tmp;
                }
            }
        }
        return res;
    }

    private void updateNameExpressionIds()
    {
        for (Name n : nameExpressions)
        {
            for (Definition.NameMap nm : defMaps)
            {
                if (nm.name.equals(n.getName()))
                {
                    n.setId(nm.id);
                    break;
                }
            }
        }
    }

    public void setShapeReader(ShapeReader sr) throws ParseException
    {
        if (shapeReader != null)
        {
            throw new ParseException("Will not initialize shapereader twice");
        }
        shapeReader = sr;
    }

    /**Adds or updates a constant (global name-->value mapping)
     *
     */
    public Constant addConstant(String name, Expression value, boolean isDef) throws ParseException
    {
        Constant c;
        if ((c = constants.get(name)) == null)
        {
            c = new Constant(name, value, isDef);
            objects.put(nextid, c);
            c.setId(nextid++);
            constants.put(name, c);
        }
        else
        {
            if (isGenerateContext)
            {
                c.value = value;
            }
            else
            {
                c.setValue(value);
                c.preEval(isDef);
//                return new Constant(name, value, isDef);
            }
        }
        return c;
    }

    public void setConstant(String name, Expression value) throws ParseException
    {
        Constant c;
        c = constants.get(name);
        if (c == null)
        {
            addConstant(name, value, false);
        }
        else
        {
//            if (c.isMacro)
//            {
            c.constVal = value.evaluate();
            c.value = value;
            c.isMacro = false;
//            } else
//                c.constVal = value.evaluate();
        }
    }
    static int defCounter = 0;

    public Definition addDefinition(Definition d)
    {
        d.id = defCounter++;
        definitions.put(d.name + "/" + d.id, d);
        return d;
    }

    private void preEvDefs(Transform st)
    {
        for (Definition d : st.defs) {
            indexedDefinitions[d.id] = d;
            Definition.NameMap dmap = new Definition.NameMap(-1, d.name);
            int ipos = -1;
            for (int i = 0; i < defMaps.size(); i++)
            {
                if (defMaps.get(i).name.compareTo(d.name) == 0)
                {
                    ipos = i;
                    break;
                }
            }
            if (ipos < 0)
            {
                defMaps.add(ipos = defMaps.size(), dmap);
            }
            dmap.id = ipos;
            d.setNameId(ipos);
//            Runtime.sysoutln(d + " " + d.definition, 0);
        }
        Collections.sort(st.defs, new Comparator<Definition>()
        {

            @Override public int compare(Definition o1, Definition o2)
            {
                return o1.nameId - o2.nameId;
            }
        });
    }

    private void preEvSt(Transform st, Rule r) throws ParseException
    {
        NonDeterministicRule ndr;
        if (st == null)
        {
            throw new ParseException("Missing (null) shape transform in " + r);
        }
        st.model = this;
        for (TransformModifier tm : st.acqExps)
        {
            if (tm.token == Language.layer)
            {
                hasMultipleLayers = true;
            }
        }
        preEvDefs(st);
        if (st instanceof TerminatingShape)
        {
            if (((TerminatingShape)st).shape instanceof Effect)
            {
                this.drawLayerSeparately = true;
            }
            //
        }
        else if (st instanceof RepeatStructure)
        {
            RepeatStructure rt = (RepeatStructure) st;
            if (rt.repeatTransform != null)
            {
                rt.repeatTransform.model = this;
                rt.repeatTransform.initialize(this);
                preEvDefs(rt.repeatTransform);
            }
            if (rt.repeatedTransform != null)
            {
                preEvSt(rt.repeatedTransform, r);
            }
        }
        else if (st instanceof ConditionalStructure)
        {
            ConditionalStructure c = (ConditionalStructure) st;
            try
            {
                Boolean constval = c.conditional.bevaluate();
                // exceptional that expression evaluates compile time
                c.conditional = new BooleanExpression.Dummy(constval);  // replace by dummy boolean
            }
            catch (Exception ex)
            {
            }
            for (Transform t : c.onCondition)
            {
                preEvSt(t, r);
            }
        }
        else
        {
            if (!(Language.pop.compareTo(st.ruleName) == 0)
                    && !(Language.peek.compareTo(st.ruleName) == 0))
            {
                if ((ndr = rules.get(st.ruleName)) == null
                        && !(st instanceof RepeatStructure))
                {
                    throw new ParseException("Could not find rule for shape transform " + st, st.lineNr, st.caretPos);
                }
                st.indexedNd = ndr.id;
            }
        }


        st.initialize(this);
    }

    private void preEvPrePost(Rule r) throws ParseException
    {
        for (int i = 0; i < r.pre.size() + r.post.size(); i++)
        {
            BooleanExpression be = (i >= r.pre.size() ? r.post.get(i - r.pre.size()) : r.pre.get(i));
            try
            {
                boolean constval = be.bevaluate();
                // exceptional that expression evaluates compile time
                be = new BooleanExpression.Dummy(constval);  // replace by dummy boolean
                if (i >= r.pre.size())
                {
                    r.post.set(i - r.pre.size(), be);
                }
                else
                {
                    r.pre.set(i, be);
                }
            }
            catch (NullPointerException ex)
            { // expected that it didn't (contains dynamic reference)
            }
        }
    }

    private int preEvExpressions(Expression[] arr) throws ParseException
    {
        int evaluated = 1;
        for (int i = 0; i < arr.length; i++)
        {
            Expression xpr = arr[i];
            try
            {
                Float evl = xpr.evaluate();
                if (evl == null)
                {
                    evaluated = 0;
                }
                else
                {
                    arr[i] = new Value(evl);
                }
            }
            catch (Exception e)
            {
//                e.printStackTrace();
                evaluated = 0;
            }
        }
        return evaluated;
    }

    public void initForGenerate() throws IllegalArgumentException, IllegalAccessException, ParseException
    {
        if (rules.size() == 0)
        {
            throw new ParseException("No rules are defined.");
        }
        if (shapeReader == null)
        {
            shapeReader = ShapeReaders.getReader("foo", this);
        }
        if (bg == null)
        {
            bg = new Background();
        }
        if (backgroundTransform == null)
        {
            backgroundTransform = new Transform("background", 0,0);
        }
        bg.initFromTransform(backgroundTransform);
        if (cameras.size() == 0)
        {
            cameras.add(new CameraBuilder().setDefault().build());
        }
        if (lighting == null)
        {
            lighting = new GlobalLighting()
            {

                @Override
                public void lightObject(DrawingContext shape)
                {
                }
            };
        }
        bitmapCache.init();
        RuleWriter tmpwr = null;
        try
        {
            tmpwr = new RuleWriter(this);
            tmpwr.setAsLocalConstantSource();
        }
        catch (IOException e)
        {
            throw new ParseException("Model: init failed: " + e.getMessage());
        }

        //count rules
        int count = 0;
        for (NonDeterministicRule nd : rules.values())
        {
            count += nd.getRules().size();
        }
        //create arrays for fast dynamic access
        indexedRules = new Rule[count];
        indexedNd = new NonDeterministicRule[rules.size()];
        indexedConstants = new float[constants.size()];
        indexedDefinitions = new Definition[defCounter];

        // copy ndrule, rules, and transforms to arrays for fast indexing        
        int ndpos = 0, rulepos = 0, stpos = 0;
        for (NonDeterministicRule nd : rules.values())
        {
            indexedNd[ndpos] = nd;
            nd.id = ndpos;
            for (WeighedRule wr : nd.getRules())
            {
                for (Transform st : wr.rule.transforms)
                {
                    while (st instanceof RepeatStructure)
                    {
                        RepeatStructure rt = (RepeatStructure) st;
                        rt.index = stpos;
                        rt.repeatTransform.index = stpos++;
                        st = ((RepeatStructure) st).repeatedTransform;

                    }
                    st.index = stpos++;
                }
                indexedRules[rulepos] = wr.rule;
                wr.rule.id = rulepos;
                rulepos++;
            }
            ndpos++;
        }

        if (this.startshape == null)
        {
            startshape = indexedNd[0].getName();
        }

        checkForConstantCyclicity(this.constants.values());

        //shape transforms to array for fast access
        indexedSt = new Transform[Math.max(0, stpos - 1)];
        // map shape transforms to rules
        isGenerateContext = true;
        for (NonDeterministicRule nd : rules.values())
        {
            for (WeighedRule wr : nd.getRules())
            {
                Rule r = wr.rule;
                checkForConstantCyclicity(r.macros);
                // preevaluate pre and post context rules
                preEvPrePost(r);
                // preevaluate shape transforms
                for (Transform st : r.transforms)
                {
                    preEvSt(st, r);
                }
                if (r instanceof PathRule)
                {
                    PathRule pr = (PathRule) r;
                    int evaluated = 1;
//                    evaluated &= preEvExpressions(pr.moveto);
                    for (Placeholder ph : pr.steps)
                    {
                        if (ph.type == PathRule.CLOSE)
                        {
                        }
                        else
                        {
                            evaluated &= preEvExpressions(ph.data);
                        }
                    }
                    if (evaluated != 0)
                    {
                        pr.closed = 2;
                        Untransformable ut = pr.createUntransformable(PathRule.REMOVE_CLOSING);
                        ut.id = pr.id;
                        ut.name = pr.getName();
                        Language.addUntransformable(ut);
                    }
                }
            }
        }
        imgIndex = this.getNameExpressionId("img");
        imguIndex = this.getNameExpressionId("u");
        imgvIndex = this.getNameExpressionId("v");
        imgrIndex = this.getNameExpressionId(Language.imgr.name);
        imggIndex = this.getNameExpressionId(Language.imgg.name);
        imgbIndex = this.getNameExpressionId(Language.imgb.name);
        imgaIndex = this.getNameExpressionId(Language.imga.name);
        if (rules.get(startshape) == null)
        {
            throw new ParseException(String.format(
                    "rule %s referenced by startshape directive not found", startshape));
        }
        updateNameExpressionIds();
        for(Light li : this.lighting.getLights())
        {
            if (li instanceof PhongLight)
            {
                PhongLight pli = (PhongLight)li;
                pli.ambientId = this.getNameExpressionId("ambient");
                pli.diffuseId = this.getNameExpressionId("diffuse");
                pli.specularId = this.getNameExpressionId("specular");
            }
            else if (li instanceof AmbientLight)
            {
                ((AmbientLight)li).ambientId = this.getNameExpressionId("ambient");
            }
        }
        Runtime.sysoutln(this.toShortString(), 5);
        isPreEvaluated = true;
    }

    public String toShortString()
    {
        StringBuilder bd = new StringBuilder();
        bd.append("MODEL:\n");
        bd.append("\t maxshapes ").append(maxShapes).append("\n");
        bd.append("\t pushstack size ").append(pushStackSize).append("\n");
        bd.append("\t min feature size ").append(minfeaturesize).append("\n");
        bd.append(" Startshape: ").append(startshape);
        return bd.toString();
    }

    @Override
    public String toString()
    {
        StringBuilder bd = new StringBuilder();
        bd.append(toShortString()).append("\n");
        bd.append("Rules: ").append(rules).append("\n");
        bd.append(backgroundTransform).append("\n");
        bd.append("Cameras: ").append(cameras).append("\n");
        return bd.toString();
    }

}
