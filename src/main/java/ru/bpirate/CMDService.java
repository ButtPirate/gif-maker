package ru.bpirate;

import ru.bpirate.exceptions.BackendException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CMDService {
    public static void sendCmd(String command, String runningPath) throws BackendException {
        String driveLetter = runningPath.substring(0, 1);
        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "" //launch CMD
                + driveLetter + ": " //change to correct drive
                + "&& CD " + runningPath + " " //CD to running path
                + "&& " + command); //send command
        builder.redirectErrorStream(true);
        try {
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
        } catch (Exception e) {
            throw new BackendException("Could not execute CMD command <" + command + ">!", e);

        }

    }
}
