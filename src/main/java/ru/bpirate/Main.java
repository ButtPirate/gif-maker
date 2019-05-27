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
    /**
     * In windows format, e.g. "C:\Users\Bpirate\Desktop\folder-with-images"
     */
    static String pathToParentFolder;

    static List<File> targetImages;

    /**
     * Will either be copied to clipboard (if multiple variations of .gif are created),
     * or will be used as an output filename.
     */
    public static String firstFilename;

    /**
     * If true will use all possible combinations of palette modes and dither filters.
     * If false, will use only best ones.
     * Toggle using "--all-filters" or "-A" in args.
     */
    public static boolean args_fullFilters = false;

    /**
     * Images per second.
     * For example, "0.5" would mean change of picture every 2 seconds.
     * Toggle using "-D [number]" in args, e.g. "-D 1", "-D 0.5"
     */
    public static String args_delay = "0.5";

    /**
     * Ignore image format differences.
     *
     * By default, if one of the source images has different file format, an exception will be risen.
     * This can be overridden by passing "--ignore-format" or "-I" in args.
     *
     * The exception is thrown as a warning to user, as real format of images is ignored in conversion process,
     * and all files are converted to .jpg anyway.
     */
    public static boolean args_ignoreFileFormat = false ;

    public static void main(String[] args) throws BackendException, FileException {
        // Parse passed args.
        parseArgs(args);

        // Looking up path to running .jar.
        pathToParentFolder = FileService.findParentFolder();

        // Listing all images in folder.
        targetImages = FileService.listImages(pathToParentFolder);

        firstFilename = FilenameUtils.getBaseName(targetImages.get(0).getName());

        // Check if all images have same extension.
        if (!args_ignoreFileFormat) {
            if (!FileService.checkExtension(targetImages)) {
                throw new FileException("Not all images have same format!", new Exception());
            }
        }

        // Backup.
        FileService.backupFiles(pathToParentFolder, targetImages);

        // Unpack FFMPEG.
        ResourceService.exportResource("/ffmpeg.exe", pathToParentFolder);

        // Check if all files have same resolution. Scale them to smallest one if they are not.
        if (!FileService.checkSizes(targetImages)) {
            System.out.println("Resizing images!");
            Dimension smallestDimension = FileService.findLowestDimension(targetImages);
            FFMPEGService.resizeFilesToOneSize(targetImages, smallestDimension);
            targetImages = FileService.listImages(pathToParentFolder);
        }

        // Check if there is no images bigger than 1920:1080. Scale them down if there are.
        if (!FileService.checkImagesForBigFiles(targetImages)) {
            FFMPEGService.resizeBigFiles(targetImages);
            targetImages = FileService.listImages(pathToParentFolder);
        }

        // Generate palette files.
        List<File> palettes = FFMPEGService.createPalette(targetImages);

        // Create .gifs!
        List<File> createdGifs = FFMPEGService.createGifs(targetImages, palettes, args_delay);

        // Deleting all temp files.
        List<File> listedFiles = Arrays.asList(new File(pathToParentFolder).listFiles(((dir, name) -> !name.contains("gif-maker"))));
        listedFiles.removeAll(createdGifs);
        listedFiles.remove(new File("backup"));
        for (File fileToDelete : listedFiles) {
            fileToDelete.delete();
        }

        // Paste first filename to clipboard.
        setClipboardContent(firstFilename);

    }

    /**
     * Parse passed args params.
     * On any exception default values will be used.
     *
     * @param args
     */
    private static void parseArgs(String... args) {
        try {
            List<String> listArgs = Arrays.asList(args);

            if (listArgs.contains("--all-filters") || listArgs.contains("-A")) {
                args_fullFilters = true;
            }

            if (listArgs.contains("--ignore-format") || listArgs.contains("-I")) {
                args_ignoreFileFormat = true;
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

    /**
     * Set system clipboard contents to the name of the first file for convenient renaming later.
     *
     * @param content
     */
    private static void setClipboardContent(String content) {
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection stringSelection = new StringSelection(content);
        clpbrd.setContents(stringSelection, null);
    }

}
