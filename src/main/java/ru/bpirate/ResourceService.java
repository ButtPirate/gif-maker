package ru.bpirate;

import ru.bpirate.exceptions.BackendException;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceService {
    public static void exportResource(String resourceName, String folderPath) throws BackendException {
        System.out.println("Unpacking resource <" + resourceName + ">...");
        InputStream stream = ResourceService.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
        OutputStream resStreamOut = null;
        String jarFolder;
        int readBytes;
        byte[] buffer = new byte[4096];
        try {
            resStreamOut = new FileOutputStream(folderPath + resourceName);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            stream.close();
            resStreamOut.close();
        } catch (Exception e) {
            throw new BackendException("Could not unpack resource <" + resourceName + ">", e);
        }
    }

}
