package org.konte.generate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.konte.expression.Name;
import org.konte.generate.RuleWriter.Tmper;
import org.konte.image.DefaultCanvas;
import org.konte.lang.Tokenizer;
import org.konte.model.Model;
import org.konte.parse.ParseException;
import org.konte.parse.Parser;


public class ImageAPI {

    private RenderTuple rtuple;
    private int imgWidth = 512,  imgHeight = 512;
    private static MemoryWatcher memoryWatcher;
    private RenderType renderType = RenderType.IMAGE;

    public void setRenderType(RenderType renderType) {
        this.renderType = renderType;
    }



    public enum RenderType {

        IMAGE,SVG,SEQUENCE}

    
    public ImageAPI setImageSize(int width, int height) {
        imgWidth = width;
        imgHeight = height;
        return this;
    }




    public RenderTuple init(String grammar, RandomFeed rndFeed)
            throws ParseException, IOException, Exception {
        Name.gene = null;
        Model model = new Parser().parse(
                Tokenizer.retrieveTokenStrings(
                new StringBuilder(grammar)));
        return init(model, rndFeed);
    }
    
    public RenderTuple init(Model model, RandomFeed rndFeed)
            throws ParseException, IOException, Exception {
        rtuple = new RenderTuple();
        rtuple.model = model;
        if (!rtuple.model.isGenerateContext) {
            rtuple.model.initForGenerate();
        }
        switch(renderType) {
            case IMAGE:
            case SEQUENCE:
                rtuple.canvas = new DefaultCanvas(rtuple.model, rtuple.model.bg, rtuple.model.lighting);
                break;
            case SVG:
                rtuple.canvas = null; // new SVGExportCanvas(rtuple.model.bg,rtuple.model.lighting);
                break;
        }
        
        rtuple.canvas.init(imgWidth, imgHeight);
        rtuple.shapeReader = rtuple.model.shapeReader;
        rtuple.shapeReader.setCanvas(rtuple.canvas);
        rtuple.shapeReader.rewind();
        
        rtuple.ruleWriter = new RuleWriter(rtuple.model);
        rtuple.ruleWriter.setAsLocalConstantSource();
        rtuple.ruleWriter.setRandomFeed(rndFeed);
        rtuple.ruleWriter.init(rtuple.shapeReader);

        return rtuple;
    }

    public void start() {
        if (memoryWatcher==null)
            (memoryWatcher = new MemoryWatcher()).start();
        Thread tmt2 = new Thread(rtuple.shapeReader);
        tmt2.setDaemon(true);
        tmt2.start();
        while(rtuple.shapeReader.state() == 0) try { Thread.sleep(1); } catch(Exception ex) { };
        Thread tmt = new Thread(new Tmper(rtuple.ruleWriter));
        tmt.setDaemon(true);
        tmt.start();
        memoryWatcher.signalRender();
    }
    
    public void waitFor() {
        while (rtuple.shapeReader.state() != 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
        }  
        commit();
    }
    
    public void commit() {
        memoryWatcher.signalRenderEnd();
    }
    
    public static BufferedImage createImage(String grammar, String randomKey, int width, int height) 
            throws ParseException, IOException, Exception {
        ImageAPI iapi = new ImageAPI().setImageSize(width, height);
        RandomFeed rnd = new RandomFeed(randomKey);
        iapi.init(grammar, rnd);
        iapi.start();
        iapi.waitFor();
        return iapi.rtuple.canvas.getImage();
    }
}
