package ru.bpirate;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * Created by Stef6 on 03/15/2018.
 */
public class FileDAO {
    private String runningPath;
    private File folder;
    private List<File> targetImages;

    //Class methods

    /**
     * Returns list of images that were found in the folder
     * Attempts to sort them like in the explorer
     *
     * @return list of files
     * @throws Exception - throws exception if no files were found
     */
    private List<File> listImages() throws Exception {

        FilenameFilter FILENAME_FILTER = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.contains(".jpg") || lowercaseName.contains(".png") || lowercaseName.contains(".jpeg");
            }
        };

        File[] files = this.getFolder().listFiles(FILENAME_FILTER);
        if (files.length != 0) {
            List<File> list = Arrays.asList(files);
            Collections.sort(list, new Comparator<File>() {
                private final Comparator<String> NATURAL_COMPARATOR = new WindowsExplorerStringComparator();

                public int compare(File o1, File o2) {
                    return NATURAL_COMPARATOR.compare(removeExtension(o1.getName()), removeExtension(o2.getName()));
                }
            });
            return list;
        } else {
            throw new Exception("No image files were found in folder <" + this.getRunningPath() + ">!");
        }
    }

    //Inner classes
    class WindowsExplorerStringComparator implements Comparator<String> {
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

    //Constructors
    public FileDAO() {
        try {
            this.setRunningPath(new File(ResourceDAO.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/'));
        } catch (URISyntaxException e) {
            System.out.println("Can't get full path to running .jar!");
            e.printStackTrace();
        }

        this.setFolder(new File(this.getRunningPath()));

        try {
            this.setTargetImages(this.listImages());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //GET- and SET-methods
    public String getRunningPath() {
        return runningPath;
    }

    public void setRunningPath(String runningPath) {
        this.runningPath = runningPath;
    }

    public List<File> getTargetImages() {
        return targetImages;
    }

    public void setTargetImages(List<File> targetImages) {
        this.targetImages = targetImages;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }
}