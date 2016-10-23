package org.konte.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import org.konte.generate.RenderTuple;
import org.konte.misc.CommandLine;


public class SeqRecorder {
    private String prefix = "seq";
    private int index = 0;
    private int maxState = 0;
    private int finishFlag = 0;
    private int flags = 3;
    private long frequency = 1000/40;   // this is not working wholly correctly yet
    private RenderTuple rtuple;
    private Timer timer;

    public SeqRecorder(String prefix)
    {
        this.prefix = prefix;
    }
    
    public void start()
    {
        this.index = 0;
        maxState = 0;
        finishFlag = 0;
        timer = new Timer();
        schedTask(timer, frequency);
    }
    
    private void schedTask(final Timer timer, final long delay)
    {
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                String filename = genNextFilename();
                synchronized(rtuple.ruleWriter.lock1)
                {
                    long start = System.currentTimeMillis();
                    int state = rtuple.shapeReader.state();
                    maxState = Math.max(state, maxState);
                    try
                    {
                        if (maxState > 0)
                        {
                            switch(state)
                            {
                                case 1:
                                    if ((flags & 1) == 1)
                                    {
                                        BufferedImage img = rtuple.canvas.getImage();
                                        if (img != null)
                                            CommandLine.writeImage(filename, img);
                                    }
                                    break;
                                case 0:
                                case 3:
                                    if ((flags & 2) == 2)
                                    {
                                        BufferedImage img = rtuple.canvas.getImage();
                                        if (img != null)
                                            CommandLine.writeImage(filename, img);
                                    }
                                    break;
                            };
                        }
                        if (maxState == 0 || state != 0 || finishFlag++<1)
                            schedTask(timer, Math.max(1,SeqRecorder.this.frequency - (System.currentTimeMillis() - start)));
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    index++;
                } // sync(ruleWriter)
            }
        };
        timer.schedule(task, delay);
    }

    private String genNextFilename()
    {
        return String.format("%s%04d.png",prefix, index);
    }

    public void setFrequency(long frequency)
    {
        this.frequency = frequency;
    }

    public long getFrequency()
    {
        return frequency;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public RenderTuple getRenderTuple()
    {
        return rtuple;
    }

    public void setRenderTuple(RenderTuple rtuple)
    {
        this.rtuple = rtuple;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    public int getFlags()
    {
        return flags;
    }

    
}
