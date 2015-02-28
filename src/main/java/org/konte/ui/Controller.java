package org.konte.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.konte.export.AbstractExporterBase;
import org.konte.export.AbstractExporterBase.Type;
import org.konte.generate.RandomFeed;
import org.konte.generate.RuleWriter;
import org.konte.generate.ShapeReader;
import org.konte.misc.CommandLine;
import org.konte.generate.ImageAPI;
import org.konte.generate.ImageAPI.RenderType;
import org.konte.generate.RenderTuple;
import org.konte.model.Model;
import org.konte.parse.ParseException;
import org.konte.generate.Runtime;
import org.konte.image.SeqRecorder;

public class Controller {

    ExecutorService executor = Executors.newCachedThreadPool();
    private RenderTuple rtuple;
    private int stopStrength = 0;
    private ShapeReader lastShapeReader;
    private Model lastModel;


    public enum Task {

        RENDER,
        EXPORT_SCENE,
        EXPORT,
        CANCEL
    }

    public static class TaskInfo {

        private Task task;
        private Object[] args;

        public TaskInfo(Task task, Object[] args)
        {
            this.task = task;
            this.args = args;
        }
    }



    public void addTask(final TaskInfo task)
    {

        Runnable r = new Runnable()
        {
            public void run()
            {

                if (task != null)
                {
                    switch (task.task)
                    {
                        case RENDER:
                            cancel(5);
                            try {
                                render((Integer) task.args[0], (Integer) task.args[1],
                                    (EditViewInterface) task.args[2], (Long) task.args[3], (RenderType)task.args[4],
                                    (task.args.length>5?(SeqRecorder)task.args[5]:null),
                                    (task.args.length>6?(boolean)task.args[6]:false));
                            }
                            catch(Exception ex)
                            {
                                Runtime.sysoutln("[render] " + ex.getMessage());
                                ex.printStackTrace();
                            }
                            break;
                        case CANCEL:
                            int strength = task.args.length > 0 ? (Integer)task.args[0] : 1;
                            cancel(strength);
                            break;
                        case EXPORT:
                            export((EditViewInterface) task.args[0], (String) task.args[1]);
                            break;
                        case EXPORT_SCENE:
                            exportScene(lastShapeReader, lastModel, (AbstractExporterBase.Type)task.args[0]);
                    }
                }

            }
         };
        Runtime.sysoutln("[konte] submit task " + task.task);
        executor.submit(r);
    }

    public Controller()
    {
    }

    private void cancel(int strength)
    {
        if (rtuple != null && rtuple.ruleWriter != null)
        {
            rtuple.shapeReader.finish(strength);
            rtuple.ruleWriter.finish();
        }
        stopStrength = strength;
    }

    private synchronized void render(Integer width, Integer height, EditViewInterface ev, Long maxtime, RenderType renderType, SeqRecorder seqReq, boolean enableSvgSceneExport)
    {
        stopStrength = 0;
        String grammar = ev.getScriptText();
        RandomFeed rndFeed = ev.getRandomFeed();
        
        long startTime = System.currentTimeMillis();
        
        ImageAPI iapi = new ImageAPI();
        iapi.setRenderType(renderType);
        iapi.setEnableSvgSceneExport(enableSvgSceneExport);
        
        try {
            rtuple = iapi.setImageSize(width,height).init(grammar, rndFeed);
            if (maxtime != -1)
            {
                new Thread(new Stopper(rtuple.shapeReader, rtuple.ruleWriter, maxtime)).start();
            }

        }
        catch (ParseException e)
        {
            String userMsg = e.getMessage() + " AT LINE " + e.getLineNr();
            Runtime.sysoutln(userMsg);
            System.out.println(" - " + e.getStackTrace()[0]);
            JOptionPane.showMessageDialog(null, userMsg);
            try {
                ev.getEditor().requestFocus();
                ev.setCaretPosition(e.getCaretPos());
            } catch(Exception ex) { ex.printStackTrace(); }
            return;
        }
        catch (Exception e)
        {
            Runtime.sysoutln(e.getMessage() + "");
            e.printStackTrace();
            return;
        }
        Runtime.sysoutln("----------", 20);
        Runtime.sysoutln(String.format("Rendering: %s trfms,%s rules;%s shads,%s lghts,%s fovs",
                rtuple.model.indexedSt.length,
                rtuple.model.indexedRules.length,
                rtuple.model.colorSpaces.size(),
                rtuple.model.lighting.getLights().size(),
                rtuple.model.cameras.size()
                ), 20);
        // start the generator
        if (renderType == RenderType.SEQUENCE && seqReq != null)
        {
            seqReq.setRenderTuple(rtuple);
            seqReq.start();
        }
        iapi.start();
        int ind = 0;
        lastShapeReader = rtuple.shapeReader;
        lastModel = rtuple.model;

        while (rtuple.shapeReader.state() != 0)
        {
            if (stopStrength >= 2)
            {
                ev.setDisplayImage(rtuple.canvas.getImage());
                ev.updateRenderInfo(rtuple.ruleWriter.getRandomFeed());
                return;
            }
            org.konte.misc.Func.sleep(10);
            if (ind % 40 == 0)
            {
                rtuple.ruleWriter.printStats();
            }
            if (ind++ % 10 == 0)
            {
                ev.setDisplayImage(rtuple.canvas.getImage());
            }
        }
        iapi.commit();
        ev.setDisplayImage(rtuple.canvas.getImage());
        Runtime.sysoutln(String.format(
                "Finished %s shapes in %s ms",
                rtuple.shapeReader.getShapeCount(), (System.currentTimeMillis() - startTime)), 20);
        ev.updateRenderInfo(rtuple.ruleWriter.getRandomFeed());
        rtuple.shapeReader.rewind();
        
    }

   


    private class Stopper implements Runnable {

        ShapeReader bsr;
        RuleWriter rw;
        long maxtime;

        private Stopper(ShapeReader bsr, RuleWriter rw, long maxtime)
        {
            this.bsr = bsr;
            this.rw = rw;
            this.maxtime = maxtime;
        }

        public void run()
        {
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() < start + maxtime / 10 
                    || bsr.getAddedCount() < 1000)
                    {
                org.konte.misc.Func.sleep(20);
            }
            if (bsr.state() != 0)
            {
                rw.finish();
                bsr.finish(5);
            }
        }
    }

    private void exportScene(ShapeReader lastShapeReader, Model lastModel, Type type)
    {
        if (lastShapeReader == null)
        {
            Runtime.sysoutln("A scene must be rendered before export");
            return;
        }
        AbstractExporterBase ee = AbstractExporterBase.createExporter(lastShapeReader, lastModel, type);
        
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
            type.name() + " scene files", type.getFileNameExtension()));
        File expF;
        if (fc.showSaveDialog(null) != 0 ||
                (expF = fc.getSelectedFile()) == null) { // not ok
            return;
        }
        if (!expF.getName().endsWith("." + type.getFileNameExtension()))
            expF = new File(expF.getAbsolutePath() + "." + type.getFileNameExtension());
        try {
            Runtime.sysoutln("Exporting scene");
            ee.export(expF);
            Runtime.sysoutln("Written: " + expF);
        }
        catch (FileNotFoundException ex)
        {
            Runtime.sysoutln("Can't open for saving: " + expF);
        }
        catch (IOException ex)
        {
            Runtime.sysoutln("Can't write to: " + expF);
        }
        
    }    
    
    private void export(EditViewInterface ev, String path)
    {
        if (ev.getDisplayImage() == null)
        {
            Runtime.sysoutln("No image");
            return;
        }
        if (ev.suggestExportFileName() == null)
        {
            makeOpenExportDialog();
            Runtime.sysoutln("Still unmappable scene file - rerender required");
            return;
        }

        String expF = path;
        if (!expF.matches(".+(jpg|JPG|png|PNG)$"))
                expF += ev.suggestExportFileName();
        
        try {
            Runtime.sysoutln("Exporting");
            CommandLine.writeImage(expF, ev.getDisplayImage());
            Runtime.sysoutln("Written: " + expF);
        }
        catch (FileNotFoundException ex)
        {
            Runtime.sysoutln("Can't open for saving: " + expF);
        }
        catch (IOException ex)
        {
            Runtime.sysoutln("Can't write to: " + expF);
        }

    }

    public void makeOpenExportDialog()
    {
        System.out.println("not implemented");
    }
    


    
    
}
