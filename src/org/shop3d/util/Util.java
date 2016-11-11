/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.util;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.text.DateFormat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author zeus
 */
public class Util {

    public static String IntlCountryCompareToken = "Other";
    public static String EuropeCompareToken = "Rest of Europe";

    public static boolean IsShippingCountryEuropeOrOther(String countryName) {
        if (countryName.equals(IntlCountryCompareToken)
                || countryName.equals(EuropeCompareToken)) {
            return true;
        }
        return false;
    }

    public static double FormatDouble(double input, int digits) {
        String start = ".";
        for (int i = 0; i < digits; i++) {
            start += "0";
        }

        DecimalFormat df = new DecimalFormat(start);
        return Double.valueOf(df.format(input));
    }

    public static byte[] convertToBytes(Object object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(object);
            return bos.toByteArray();
        } catch (Exception er) {
            return null;
        }
    }

    public static Object convertFromBytes(byte[] bytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (Exception er) {
            return null;
        }
    }

    public static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index  
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        // LOG.debug("Original: " + data.length / 1024 + " Kb");  
        // LOG.debug("Compressed: " + output.length / 1024 + " Kb");  
        return output;
    }

    public static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        byte[] output = outputStream.toByteArray();
        // LOG.debug("Original: " + data.length);  
        // LOG.debug("Compressed: " + output.length);  
        return output;
    }

    public static void ZipAndSave(String[] srcFiles, String outputZipName) {
        String zipFile = outputZipName;

        // String[] srcFiles = { "C:/srcfile1.txt", "C:/srcfile2.txt", "C:/srcfile3.txt"};
        try {

            // create byte buffer
            byte[] buffer = new byte[1024];

            FileOutputStream fos = new FileOutputStream(zipFile);

            ZipOutputStream zos = new ZipOutputStream(fos);

            for (int i = 0; i < srcFiles.length; i++) {

                File srcFile = new File(srcFiles[i]);

                FileInputStream fis = new FileInputStream(srcFile);

                String a = srcFile.getCanonicalPath();
                String b = srcFile.getPath();
                String c = srcFile.getAbsolutePath();
                String d = srcFile.getParent();
                String extraDir = "";
                if (srcFile.getAbsolutePath().contains("Maps")) {
                    extraDir = "Maps\\";
                }
                // begin writing a new ZIP entry, positions the stream to the start of the entry data
                zos.putNextEntry(new ZipEntry(extraDir + srcFile.getName()));

                int length;

                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }

                zos.closeEntry();

                // close the InputStream
                fis.close();

            }

            // close the ZipOutputStream
            zos.close();

        } catch (IOException ioe) {
            System.out.println("Error creating zip file: " + ioe);
        }
    }

    public static String GetMyIp() {

        URL url = null;
        BufferedReader in = null;
        String ipAddress = "";
        try {
            url = new URL("http://bot.whatismyipaddress.com");
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            ipAddress = in.readLine().trim();
            /* IF not connected to internet, then
             * the above code will return one empty
             * String, we can check it's length and
             * if length is not greater than zero, 
             * then we can go for LAN IP or Local IP
             * or PRIVATE IP
             */
            if (!(ipAddress.length() > 0)) {
                try {
                    InetAddress ip = InetAddress.getLocalHost();
                    System.out.println((ip.getHostAddress()).trim());
                    ipAddress = (ip.getHostAddress()).trim();
                } catch (Exception exp) {
                    ipAddress = "ERROR";
                }
            }
        } catch (Exception ex) {
            // This try will give the Private IP of the Host.
            try {
                InetAddress ip = InetAddress.getLocalHost();
                System.out.println((ip.getHostAddress()).trim());
                ipAddress = (ip.getHostAddress()).trim();
            } catch (Exception exp) {
                ipAddress = "ERROR";
            }
            //ex.printStackTrace();
        }
        System.out.println("IP Address: " + ipAddress);
        return ipAddress;
    }

    public static String ToSignificantFiguresString(String n, int toDigits) {
        int diff = toDigits - n.length();
        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                n = "0" + n;
            }
        }
        return n;
    }

    public static String MakeOneString(ArrayList<String> data, String divider) {
        String output = "";
        for (int i = 0; i < data.size(); i++) {
            if (output.equals("")) {
                output = data.get(i);
            } else {
                output += (divider + data.get(i));
            }
        }
        return output;
    }

    public static String GetDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //get current date time with Date()
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String GetSuperUniqueId() {
        String compName = "bro";
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME")) {
            compName = env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            compName = env.get("HOSTNAME");
        } else {
            compName = "Unknown_Computer";
        }

        String id = GetMyIp();
        Random rand = new Random();

        int n = rand.nextInt(1000000) + 1;
        String finalOutput = compName + "_" + id + "_" + ToSignificantFiguresString(n + "", 8);
        System.out.println("My super unique ID: " + finalOutput);
        return finalOutput;

    }
}
