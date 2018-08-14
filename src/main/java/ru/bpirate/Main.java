package ru.bpirate;

import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    static String pathToParentFolder; //in windows format, e.g. C:\Users\Bpirate\Desktop\folder-with-images
    static List<File> targetImages;

    public static void main(String[] args) throws BackendException, FileException {
        //Looking up path to running .jar
        pathToParentFolder = FileService.findParentFolder();

        //Listing images
        targetImages = FileService.listImages(pathToParentFolder);

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

        List<File> createdGifs = FFMPEGService.createGifs(targetImages, palettes, "0.5", pathToParentFolder);

        List<File> listedFiles = Arrays.asList(new File(pathToParentFolder).listFiles(((dir, name) -> !name.contains("gif-maker"))));
        listedFiles.removeAll(createdGifs);
        listedFiles.remove(new File("backup"));

        for (File fileToDelete : listedFiles) {
            fileToDelete.delete();
        }

    }

}
