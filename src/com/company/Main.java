package com.company;

import com.sun.org.apache.regexp.internal.RE;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    //public static final String PATHTOFOLDER = "U:\\_downloads\\_edit\\temp";
    public static String PATHTOFOLDER = "";
    public static final String[] palettes = {"full","diff"};
    public static final String[] dithers = {
            "none",
            "bayer:bayer_scale=1",
            "bayer:bayer_scale=2",
            "bayer:bayer_scale=3",
            "bayer:bayer_scale=4",
            "bayer:bayer_scale=5",
            "floyd_steinberg",
            "sierra2",
            "sierra2_4a" };

    public static String fullCommand = "";

    public static FilenameFilter textFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            String lowercaseName = name.toLowerCase();
            if (!lowercaseName.contains(".exe")) {
                return true;
            } else {
                return false;
            }
        }
    };


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI().getPath());
        String jarDir = jarFile.getParentFile().getPath();

        PATHTOFOLDER = jarDir;

        System.out.println(PATHTOFOLDER);

        File folder = new File(PATHTOFOLDER);
        File[] listOfFiles = folder.listFiles(textFilter);
        String firstFileName = listOfFiles[0].getName();
        String format = firstFileName.substring(firstFileName.length()-3);

        RenameFiles(listOfFiles, format);
        listOfFiles = folder.listFiles(textFilter);

        fullCommand += "&& U: ";
        fullCommand += "&& cd \""+PATHTOFOLDER+"\" ";

        if (!CheckSizes(listOfFiles)) { ResizeFiles(listOfFiles,format); }
        listOfFiles = folder.listFiles();

        MakePalettes(format);
        MakeGifs(format);
        fullCommand += "&& del palette*.* ";

        fullCommand += "&& type nul > "+firstFileName.substring(0,firstFileName.length()-4)+".txt ";

        System.out.println(fullCommand);
        SendCmd(fullCommand);

        for (File currentFile:listOfFiles) {
            currentFile.delete();
        }


    }

    public static void SendCmd(String command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "ECHO Шалом! "+command);
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(),"866"));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) { break; }
            System.out.println(line);
        }


    }

    //find smallest resolution in a list
    public static Dimension FindSmalestFile(File[] listOfFiles) throws IOException {
        Dimension resolution = new Dimension();
        long minRes=0;

        for (File x:listOfFiles) {
            BufferedImage bimg = ImageIO.read(x);
            long temp = bimg.getWidth()*bimg.getHeight();
            if (temp<minRes || minRes == 0) {
                minRes = temp;
                resolution.setSize(bimg.getWidth(),bimg.getHeight());
            }
        }

        return resolution;
    }

    //check if all the files have the same resolution
    public static boolean CheckSizes(File[] listOfFiles) throws IOException {
        BufferedImage firstImage = ImageIO.read(listOfFiles[0]);
        long width=firstImage.getWidth(), height = firstImage.getHeight();

        for (File x:listOfFiles) {
            BufferedImage image = ImageIO.read(x);
            if (image.getWidth() != width || image.getHeight() != height) {
                return false;
            }

        }
        return true;



    }

    public static void RenameFiles(File[] listOfFiles, String format) {
        int index = 1;

        for (File x:listOfFiles) {
            File newFilename = new File(PATHTOFOLDER+"\\"+String.format("%03d",index)+"."+format);
            x.renameTo(newFilename);
            index++;
        }

    }

    public static void ResizeFiles(File[] listOfFiles, String format) throws IOException, InterruptedException {
        Dimension minRes = FindSmalestFile(listOfFiles);
        fullCommand += "&& for %f in (%03d."+format+") do ffmpeg -i %f -vf \"scale="+(int) minRes.getWidth()+":"+(int) minRes.getHeight()+", format=rgba\" %f -y ";
    }

    public static void MakePalettes(String format) throws IOException, InterruptedException {
        for (String currentPalette:palettes) {
            fullCommand += "&& ffmpeg -f image2 -i %03d."+format+" -lavfi \"palettegen=stats_mode="+currentPalette+"\" palette_"+currentPalette+".png ";
        }

    }

    public static void MakeGifs(String format) {
        int i = 1;
        for (String currentPalette:palettes){
            for (String currentDither:dithers){
                fullCommand += "&& ffmpeg -f image2 -r 0.5 -i %03d."+format+" -i palette_"+currentPalette+".png -lavfi \"paletteuse=dither="+currentDither+"\" -y output_"+currentPalette+"_"+currentDither.substring(0,4)+i+".gif ";
                i++;

            }
        }

    }


}
