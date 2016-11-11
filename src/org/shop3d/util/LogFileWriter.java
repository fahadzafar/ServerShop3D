/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Fahad
 */
public class LogFileWriter {

    String baseLogDir = SharedData.LogPathDir;
    String filename = "";

    public void Write(String data) {
        // Check to see if today's folder exists
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //get current date time with Date()
        Date date = new Date();

        String todaysDateHour = dateFormat.format(date);
        File logFolder = new File(baseLogDir + todaysDateHour);
        if (logFolder.exists() == false) {
            logFolder.mkdir();
        }

        // Now check to see if the file exists in the folder per hour.
        dateFormat = new SimpleDateFormat("HH");
        String todaysFileName = dateFormat.format(date);

        File logFile = new File(baseLogDir + todaysDateHour + "//" + todaysFileName + ".txt");
        if (logFile.exists() == false) {
            try {
                logFile.createNewFile();
            } catch (Exception er) {

            }
        }

        // ---------- Now just write the data.
        try {
            data += "\n";
            Files.write(Paths.get(baseLogDir + todaysDateHour + "//" + todaysFileName + ".txt"), data.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            //exception handling left as an exercise for the reader
        }

    }
}
