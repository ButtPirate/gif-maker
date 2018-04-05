package ru.bpirate;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Stef6 on 04/04/2018.
 */
public class CmdDAO {
    @Autowired
    private FileDAO fileDAO;

    //Class methods

    /**
     * Sends command to CMD
     * Changes working directory of CMD to running path of .jar before executing command
     *
     * @param command - command to send
     * @throws IOException
     * @throws InterruptedException
     */
    public void sendCmd(String command) throws IOException, InterruptedException {
        String driveLetter = this.getFileDAO().getRunningPath().substring(0, 1);
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "" //launch CMD
                + driveLetter + ": " //change to correct drive
                + "&& CD \"" + this.getFileDAO().getRunningPath() + " " //CD to running path
                + "&& " + command); //send command
        builder.redirectErrorStream(true);
        Process p = builder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), "866"));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }

    }

    //GET- and SET-methods

    public FileDAO getFileDAO() {
        return fileDAO;
    }

    public void setFileDAO(FileDAO fileDAO) {
        this.fileDAO = fileDAO;
    }
}
