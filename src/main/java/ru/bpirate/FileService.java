package ru.bpirate;

import org.apache.commons.io.FileUtils;
import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class FileService {

    static String findParentFolder() throws BackendException {
        try {
            return new File(FileService.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
        } catch (URISyntaxException e) {
            throw new BackendException("Could not get path to running .jar! ", e);
        }

    }

    static List<File> listImages(String path) throws FileException {

        FilenameFilter FILENAME_FILTER = (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            return lowercaseName.contains(".jpg") || lowercaseName.contains(".png") || lowercaseName.contains(".jpeg");
        };
        File[] files = new File(path).listFiles(FILENAME_FILTER);
        if (files.length == 0) {
            throw new FileException("No image files were found in folder <" + path + ">!", new Exception());
        }

        List<File> list = Arrays.asList(files);

        Collections.sort(list, new Comparator<File>() {
            private final Comparator<String> NATURAL_COMPARATOR = new WindowsExplorerStringComparator();

            public int compare(File o1, File o2) {
                return NATURAL_COMPARATOR.compare(removeExtension(o1.getName()), removeExtension(o2.getName()));
            }
        });
        return list;
    }

    public static boolean checkExtension(List<File> list) {
        String firstExtension = getExtension(list.get(0).getName());
        for (File file : list) {
            if (!getExtension(file.getName()).equals(firstExtension)) {
                return false;
            }
        }
        return true;
    }

    static class WindowsExplorerStringComparator implements Comparator<String> {
        private String str1, str2;
        private int pos1, pos2, len1, len2;

        public int compare(String s1, String s2) {
            str1 = s1;
            str2 = s2;
            len1 = str1.length();
            len2 = str2.length();
            pos1 = pos2 = 0;

            int result = 0;
            while (result == 0 && pos1 < len1 && pos2 < len2) {
                char ch1 = str1.charAt(pos1);
                char ch2 = str2.charAt(pos2);

                if (Character.isDigit(ch1)) {
                    result = Character.isDigit(ch2) ? compareNumbers() : -1;
                } else if (Character.isLetter(ch1)) {
                    result = Character.isLetter(ch2) ? compareOther(true) : 1;
                } else {
                    result = Character.isDigit(ch2) ? 1
                            : Character.isLetter(ch2) ? -1
                            : compareOther(false);
                }

                pos1++;
                pos2++;
            }

            return result == 0 ? len1 - len2 : result;
        }

        private int compareNumbers() {
            int end1 = pos1 + 1;
            while (end1 < len1 && Character.isDigit(str1.charAt(end1))) {
                end1++;
            }
            int fullLen1 = end1 - pos1;
            while (pos1 < end1 && str1.charAt(pos1) == '0') {
                pos1++;
            }

            int end2 = pos2 + 1;
            while (end2 < len2 && Character.isDigit(str2.charAt(end2))) {
                end2++;
            }
            int fullLen2 = end2 - pos2;
            while (pos2 < end2 && str2.charAt(pos2) == '0') {
                pos2++;
            }

            int delta = (end1 - pos1) - (end2 - pos2);
            if (delta != 0) {
                return delta;
            }

            while (pos1 < end1 && pos2 < end2) {
                delta = str1.charAt(pos1++) - str2.charAt(pos2++);
                if (delta != 0) {
                    return delta;
                }
            }

            pos1--;
            pos2--;

            return fullLen2 - fullLen1;
        }

        private int compareOther(boolean isLetters) {
            char ch1 = str1.charAt(pos1);
            char ch2 = str2.charAt(pos2);

            if (ch1 == ch2) {
                return 0;
            }

            if (isLetters) {
                ch1 = Character.toUpperCase(ch1);
                ch2 = Character.toUpperCase(ch2);
                if (ch1 != ch2) {
                    ch1 = Character.toLowerCase(ch1);
                    ch2 = Character.toLowerCase(ch2);
                }
            }

            return ch1 - ch2;
        }
    }

    public static void backupFiles(String runningPath, List<File> images) throws BackendException {
        File backupFolder = new File(runningPath + "\\backup");
        backupFolder.mkdir();
        for (File x : images) {
            try {
                FileUtils.copyFileToDirectory(x, backupFolder);
            } catch (IOException e) {
                throw new BackendException("Something went wrong during backup", e);
            }
        }
    }

    public static boolean checkSizes(List<File> images) throws BackendException {
        BufferedImage firstImage;
        try {
            firstImage = ImageIO.read(images.get(0));
        } catch (IOException e) {
            throw new BackendException("Could not read image!", e);
        }
        long width = firstImage.getWidth();
        long height = firstImage.getHeight();

        for (File x : images) {
            BufferedImage image;
            try {
                image = ImageIO.read(x);
            } catch (IOException e) {
                throw new BackendException("Could not read image!", e);
            }
            if (image.getWidth() != width || image.getHeight() != height) {
                return false;
            }
        }

        return true;
    }

    public static Dimension findLowestDimension(List<File> listOfFiles) throws BackendException {
        Dimension resolution = new Dimension();
        long minRes = 0;

        for (File x : listOfFiles) {
            BufferedImage bimg = null;
            try {
                bimg = ImageIO.read(x);
            } catch (IOException e) {
                throw new BackendException("Could not read image!", e);
            }
            long temp = bimg.getWidth() * bimg.getHeight();
            if (temp < minRes || minRes == 0) {
                minRes = temp;
                resolution.setSize(bimg.getWidth(), bimg.getHeight());
            }
        }

        return resolution;
    }

    public static void formatImages(List<File> targetImages) throws BackendException {
        for (int i = 0; i < targetImages.size(); i++) {
            try {
                BufferedImage bufferedImage = ImageIO.read(targetImages.get(i));
                BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                        bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                ImageIO.write(newBufferedImage, "jpg", new File(String.format("%03d"+".jpg", i)));
            } catch (IOException e) {
                throw new BackendException("Could not convert images to .jpg!", e);
            }

        }
    }

    public static boolean checkImagesForBigFiles(List<File> images) throws BackendException {
        for (File x : images) {
            BufferedImage image;
            try {
                image = ImageIO.read(x);
            } catch (IOException e) {
                throw new BackendException("Could not read image!", e);
            }
            if (image.getWidth() > 1980 || image.getHeight() > 1080) {
                return false;
            }
        }

        return true;

    }

}
