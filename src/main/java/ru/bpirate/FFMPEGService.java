package ru.bpirate;

import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FFMPEGService {
    public static void resizeFiles(List<File> listOfFiles, Dimension smallestDimension, String runningPath) throws FileException, BackendException {
        for (File x : listOfFiles) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(x);
            } catch (IOException e) {
                throw new FileException("Could not read file!", e);
            }

            if (image.getWidth() == smallestDimension.getWidth() && image.getHeight() == smallestDimension.getHeight()) {
                continue;
            }

            StringBuilder builder = new StringBuilder();
            builder.append("ffmpeg "); //ffmpeg call
            builder.append("-i " + x.getName() + " "); //input is the file
            builder.append("-q:v 1 "); //quality scale - the best
            builder.append("-vf \"scale=" + (int) smallestDimension.getWidth() + ":" + (int) smallestDimension.getHeight() + ":flags=lanczos" + ", format=rgba\" "); //resize filter
            builder.append(x.getName() + " -y"); //replace original image with scaled
            CMDService.sendCmd(builder.toString(), runningPath);
        }

    }

    public static List<File> createPalette(List<File> targetImages, String runningPath) throws BackendException {
        String[] palettes = {"diff", "full"}; //two different modes of palette generation

        for (String currentPalette : palettes) { //for each mode
            StringBuilder builder = new StringBuilder();
            builder.append("ffmpeg -f image2 "); //call FFMPEG

            for (File x : targetImages) { //all target images as input
                builder.append("-i " + x.getName() + " ");
            }

            builder.append("-lavfi \"palettegen=stats_mode=" + currentPalette + "\" palette_" + currentPalette + ".png"); //current palette generation mode

            CMDService.sendCmd(builder.toString(), runningPath); //send CMD commm=and
        }

        File[] paletteFiles = new File(runningPath).listFiles((dir, name) -> { //list all generated palette files
            String lowercaseName = name.toLowerCase();
            return lowercaseName.contains("palette");
        });

        return Arrays.asList(paletteFiles);


    }

}
