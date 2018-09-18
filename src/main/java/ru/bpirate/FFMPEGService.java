package ru.bpirate;

import org.apache.commons.io.FilenameUtils;
import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.bpirate.DitherFilter.*;

public class FFMPEGService {
    /**
     * Resize all images to one resolution.
     *
     * @param listOfFiles
     * @param smallestDimension smallest dimension, will be used as output resolution.
     * @throws FileException
     * @throws BackendException
     */
    public static void resizeFilesToOneSize(List<File> listOfFiles, Dimension smallestDimension) throws FileException, BackendException {
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
            builder.append("-i \"" + x.getName() + "\" "); //input is the file
            builder.append("-q:v 1 "); //quality scale - the best
            builder.append("-vf \"scale=" + (int) smallestDimension.getWidth() + ":" + (int) smallestDimension.getHeight() + ":flags=lanczos" + ", format=rgba\" "); //resize filter
            builder.append("\"" + x.getName() + "\" -y"); //replace original image with scaled
            CMDService.sendCmd(builder.toString(), Main.pathToParentFolder);
        }

    }

    /**
     * Create palette files.
     * @param targetImages
     * @return
     * @throws BackendException
     */
    public static List<File> createPalette(List<File> targetImages) throws BackendException {
        List<String> palettes = new ArrayList<>();
        String[] allPalettes = {"diff", "full"};
        String[] bestPalettes = {"full"};

        if (Main.args_fullFilters) {
            palettes = Arrays.asList(allPalettes);
        } else {
            palettes = Arrays.asList(bestPalettes);
        }

        for (String currentPalette : palettes) { //for each mode
            StringBuilder builder = new StringBuilder();
            builder.append("ffmpeg -f image2 "); //call FFMPEG

            for (File x : targetImages) { //all target images as input
                builder.append("-i \"" + x.getName() + "\" ");
            }

            builder.append("-lavfi \"palettegen=stats_mode=" + currentPalette + "\" palette_" + currentPalette + ".png"); //current palette generation mode

            CMDService.sendCmd(builder.toString(), Main.pathToParentFolder); //send CMD command
        }

        File[] paletteFiles = new File(Main.pathToParentFolder).listFiles((dir, name) -> { //list all generated palette files
            String lowercaseName = name.toLowerCase();
            return lowercaseName.contains("palette");
        });

        return Arrays.asList(paletteFiles);


    }

    /**
     * Create .gif files
     * @param targetImages
     * @param palettes
     * @param delay
     * @return
     * @throws BackendException
     */
    public static List<File> createGifs(List<File> targetImages, List<File> palettes, String delay) throws BackendException {
        List<DitherFilter> ditherFilters;
        DitherFilter[] allDitherFilters = {NONE, BAYER1, BAYER2, BAYER5, FLOYDSTEINBERG, SIERRA, SIERRA4A};
        DitherFilter[] bestDitherFilters = {NONE, BAYER5};
        if (Main.args_fullFilters) {
            ditherFilters = Arrays.asList(allDitherFilters);
        } else {
            ditherFilters = Arrays.asList(bestDitherFilters);
        }

        List<File> result = new ArrayList<>();

        FileService.formatImages(targetImages);

        for (File palette : palettes) { //for each palette
            for (DitherFilter ditherFilter : ditherFilters) { //for each dither mode
                StringBuilder builder = new StringBuilder();
                builder.append("ffmpeg -f image2 "); //call FFMPEG
                builder.append("-r " + delay + " "); //images per second

                builder.append("-i %03d").append(".jpg").append(" "); //input files

                builder.append("-i "+palette.getName() + " "); //palette input
                builder.append("-lavfi \"paletteuse=dither="+ditherFilter.getFullFilter()+"\" "); //specify paletteuse dither filter
                String outputFilename = generateFilename(palette, ditherFilter);
                builder.append("-y ");
                builder.append("\"" + outputFilename + "\"");

                CMDService.sendCmd(builder.toString(), Main.pathToParentFolder);

                File createdGif = new File(Main.pathToParentFolder + outputFilename);
                result.add(createdGif);

            }

        }

        return result;
    }

    /**
     * Generate unique output filename based on combination of current palette and dither filter.
     * @param palette
     * @param filter
     * @return
     */
    private static String generateFilename(File palette, DitherFilter filter) {
        StringBuilder builder = new StringBuilder();
        builder.append("gif-maker_");
        builder.append(FilenameUtils.getBaseName(palette.getName()));
        builder.append("_");
        builder.append(filter.getShortName());
        builder.append(".gif");

        return builder.toString();

    }

    /**
     * If any of the files in list are bigger than 1920 wide - scale them down.
     * If resulting image is still higher than 1080 - scale them again, by height.
     * @param listOfFiles
     * @throws FileException
     * @throws BackendException
     */
    public static void resizeBigFiles(List<File> listOfFiles) throws FileException, BackendException {
        for (File x : listOfFiles) {
            BufferedImage image = null;
            try {
                image = ImageIO.read(x);
            } catch (IOException e) {
                throw new FileException("Could not read file!", e);
            }

            if (image.getWidth() > 1920) {
                StringBuilder builder = new StringBuilder();
                builder.append("ffmpeg "); //ffmpeg call
                builder.append("-i \"" + x.getName() + "\" "); //input is the file
                builder.append("-q:v 1 "); //quality scale - the best
                builder.append("-vf \"scale=1920:-1:flags=lanczos" + ", format=rgba\" "); //resize filter
                builder.append("\"" + x.getName() + "\" -y"); //replace original image with scaled
                CMDService.sendCmd(builder.toString(), Main.pathToParentFolder);

                x = new File(x.getName());
                try {
                    image = ImageIO.read(x);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (image.getHeight() > 1080) {
                StringBuilder builder = new StringBuilder();
                builder.append("ffmpeg "); //ffmpeg call
                builder.append("-i \"" + x.getName() + "\" "); //input is the file
                builder.append("-q:v 1 "); //quality scale - the best
                builder.append("-vf \"scale=-1:1080:flags=lanczos" + ", format=rgba\" "); //resize filter
                builder.append("\"" + x.getName() + "\" -y"); //replace original image with scaled
                CMDService.sendCmd(builder.toString(), Main.pathToParentFolder);
            }
        }

    }

}
