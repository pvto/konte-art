package org.konte.misc;

import java.util.Observable;
import org.konte.generate.ImageAPI;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.konte.lang.help.Help;
import org.konte.parse.ParseException;

public class CommandLine {

    public static void main(String[] args) throws ParseException, IllegalArgumentException, IllegalAccessException, IOException, Exception
    {

        if (args.length == 0)
        {
            printHelp();
            return;
        }
        int width = 1024;
        int height = 800;
        String filename = "grammar.c3dg";
        String destfile = null;
        String VARIATION = null;
        boolean doSave = true;

        Object[][] params = new Object[][]
        {
            {"^-s(\\d+):(\\d+)", new String[]{"\\d+"}},
            {"^-f.*", null},
            {"^-d.*", null},
            {"^-no$", null},
            {"^--\\w+", new String[]{"\\w+"}},
            {"^-help.*", new String[]{"[^:]+"}}
        };
        ArrayList<ArrayList<Pattern>> patterns = new ArrayList<ArrayList<Pattern>>();

        for (int i = 0; i < args.length; i++)
        {
            String s = args[i];
            if (i == 0)
            {
                for (int j = 0; j < params.length; j++)
                {
                    ArrayList<Pattern> p = new ArrayList<Pattern>();
                    patterns.add(p);
                    p.add(Pattern.compile((String) params[j][0]));
                    if (params[j][1] != null)
                    {
                        for (int k = 0; k < ((String[]) params[j][1]).length; k++)
                        {
                            p.add(Pattern.compile(((String[]) params[j][1])[k]));
                        }
                    }
                }
            }
            for (int j = 0; j < params.length; j++)
            {

                ArrayList<Pattern> p = patterns.get(j);
                ArrayList<String> results = new ArrayList<String>();
                boolean matched = true;
                for (int k = 0; k < p.size() && matched; k++)
                {
                    Matcher m = p.get(k).matcher(s);
                    switch (k)
                    {
                        case 0:
                            if (!m.matches())
                            {
                                matched = false;
                            }
                            break;
                        default:
                            while (m.find())
                            {
                                results.add(s.substring(m.start(), m.end()));
                            }
                    }
                }
                if (matched)
                {
                    switch (j)
                    {
                        case 0:
                            width = Integer.parseInt(results.get(0));
                            height = Integer.parseInt(results.get(1));
                            break;
                        case 1:
                            filename = s.substring(2);
                            break;
                        case 2:
                            destfile = s.substring(2);
                            break;
                        case 3:
                            doSave = false;
                            break;
                        case 4:
                            VARIATION = results.get(0);
                            break;
                        case 5:
                            boolean doHtml = s.startsWith("helph") ? true : false;
                            if (results.size() > 1)
                            {
                                System.out.println(Help.help(results.get(1), doHtml));
                            }
                            else
                                System.out.println(Help.help("topics", doHtml));
                            System.exit(0);
                        default:
                    }
                }
            }
        }
        if (destfile == null)
        {
            destfile = makeExportFileName(VARIATION,filename);
        }
        if (!destfile.endsWith(".png")) destfile += ".png";
        Long startt = System.currentTimeMillis();
        BufferedImage img = ImageAPI.createImage(
                Readers.fillStringBuilder(new File(filename)).toString(),
                VARIATION == null ? (VARIATION="AAA") : VARIATION, width, height);

        org.konte.generate.Runtime.stateServer.setListener(new Observer() {

            public void update(Observable o, Object o1)
            {
                System.out.println(o1.toString());
            }
        });
        if (!doSave)
        {
            JFrame frame = new JFrame();
            frame.setSize(width, height);
            JLabel panel = new JLabel();

            ImageIcon output = new ImageIcon(img);
            panel.setIcon(output);
            frame.add(panel);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            System.out.println("finish");
            curp(startt);
            return;
        }

        writeImage(destfile,img);
        curp(startt);
        System.exit(0);
    }
    public static void writeImage(String destfile, BufferedImage image)
            throws FileNotFoundException, IOException
    {
        int destType = destfile.toLowerCase().endsWith("jpg") ? 1 : 0;
//        System.out.println("Writing " + destfile);
        File file = new File(destfile);
        switch (destType)
        {
            case 1:
                ImageIO.write(image, "JPG", file);
                break;
            case 0:
            default:
                ImageIO.write(image, "PNG", file);
                break;
        }
        System.out.println("Written: " + file);

    }


    private static void curp(long startt)
    {
        System.out.println("Done (" + (System.currentTimeMillis()-startt) + " ms)");
    }

    private static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

    public static String makeExportFileName(String rnd, String destfile)
    {
        String fof = f.format(new Date(System.currentTimeMillis()));
        int ind2 = destfile.lastIndexOf(".");
        String suffix = "", bulkPart = destfile;
        if (ind2 > 0) {
            suffix = destfile.substring(ind2);
            bulkPart = destfile.substring(0, ind2);
        }
        return bulkPart + "-" + rnd + "-" + fof + suffix;
    }

    private static void printHelp()
    {
        System.out.println(
                "CommandLine -f[filename=grammar.c3dg] -d[imagefile=from filename and modifiers] ");
        System.out.println("\t\t-s[width=1024]:[height=800] --[variation=random] -no");
        System.out.println("\timagefile (optional) can end with .png or .jpg, but jpg export is crappy");
        System.out.println("\tvariation is a code for the random seed, from A to ZZZZZZZ");
        System.out.println("\t-no outputs to a window instead of file");
    }
}
