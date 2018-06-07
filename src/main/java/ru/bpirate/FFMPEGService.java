package ru.bpirate;

import ru.bpirate.exceptions.BackendException;
import ru.bpirate.exceptions.FileException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
}
