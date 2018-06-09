package ru.bpirate;

import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import java.awt.*;
import java.io.File;
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
            FFMPEGService.resizeFiles(targetImages, smallestDimension, pathToParentFolder);
        }

        //generate palette files
        List<File> palettes = FFMPEGService.createPalette(targetImages, pathToParentFolder);

    }

}
