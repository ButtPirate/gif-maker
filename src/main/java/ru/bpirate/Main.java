package ru.bpirate;

import org.apache.commons.io.FilenameUtils;
import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    static String pathToParentFolder; //in windows format, e.g. C:\Users\Bpirate\Desktop\folder-with-images
    static List<File> targetImages;
    public static boolean args_fullFilters = false;
    public static String args_delay = "0.5";

    public static void main(String[] args) throws BackendException, FileException {
        parseArgs(args);

        //Looking up path to running .jar
        pathToParentFolder = FileService.findParentFolder();

        //Listing images
        targetImages = FileService.listImages(pathToParentFolder);

        String firstFilename = FilenameUtils.getBaseName(targetImages.get(0).getName());

        //Checking if all images have same extension
        if (!FileService.checkExtension(targetImages)) {
            throw new FileException("Not all images have same format!", new Exception());
        }

        //Backup
        FileService.backupFiles(pathToParentFolder, targetImages);

        //Unpack FFMPEG
        ResourceService.exportResource("/ffmpeg.exe", pathToParentFolder);

        //Check if all files have same resolution
        if (!FileService.checkSizes(targetImages)) {
            System.out.println("Resizing images!");
            Dimension smallestDimension = FileService.findLowestDimension(targetImages);
            FFMPEGService.resizeFilesToOneSize(targetImages, smallestDimension, pathToParentFolder);
            targetImages = FileService.listImages(pathToParentFolder);
        }

        if (!FileService.checkImagesForBigFiles(targetImages)) {
            FFMPEGService.resizeBigFiles(targetImages, pathToParentFolder);
            targetImages = FileService.listImages(pathToParentFolder);
        }

        //generate palette files
        List<File> palettes = FFMPEGService.createPalette(targetImages, pathToParentFolder);

        List<File> createdGifs = FFMPEGService.createGifs(targetImages, palettes, args_delay, pathToParentFolder);

        List<File> listedFiles = Arrays.asList(new File(pathToParentFolder).listFiles(((dir, name) -> !name.contains("gif-maker"))));
        listedFiles.removeAll(createdGifs);
        listedFiles.remove(new File("backup"));

        for (File fileToDelete : listedFiles) {
            fileToDelete.delete();
        }

        setClipboardContent(firstFilename);

    }

    private static void parseArgs(String... args) {
        try {
            List<String> listArgs = Arrays.asList(args);

            if (listArgs.contains("--all-filters") || listArgs.contains("-A")) {
                args_fullFilters = true;
            }

            if (listArgs.contains("-D")) {
                int indexOfOption = listArgs.indexOf("-D");
                args_delay = listArgs.get(++indexOfOption);
            }

        } catch (Exception e) {
            System.out.println("Caught error trying to parse command line args! Using defaults...");
            System.out.println(e.toString());
            args_fullFilters = false;
            args_delay = "0.5";
        }

    }

    private static void setClipboardContent(String content) {
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(content);
        clpbrd.setContents(stringSelection, null);
    }

}
