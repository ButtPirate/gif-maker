package ru.bpirate;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Stef6 on 03/22/2018.
 */
public class ResourceDAO {
    @Autowired
    private FileDAO fileDAO;

    /**
     * Copies resource from withing the running .jar to the same folder.
     *
     * @param resourceName - Resource name has to start with "/" when calling, e.g. exportResource("/folder-in-resource-folder/test-file.file")
     * @return
     * @throws Exception
     */
    public void exportResource(String resourceName) {
        System.out.println("Unpacking resource <" + resourceName + ">...");
        InputStream stream = ResourceDAO.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
        OutputStream resStreamOut = null;
        String jarFolder;
        int readBytes;
        byte[] buffer = new byte[4096];
        jarFolder = fileDAO.getRunningPath();
        try {
            resStreamOut = new FileOutputStream(jarFolder + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            stream.close();
            resStreamOut.close();
        } catch (Exception e) {
            System.out.println("Unpacking resource <" + resourceName + "> failed!");
            e.printStackTrace();
        }
    }
}
