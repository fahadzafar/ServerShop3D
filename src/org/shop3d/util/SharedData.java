/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.util;

/**
 *
 * @author zeus
 */
public class SharedData {

 
    public static boolean SCULPTEO_FAKE_ORDER = false;

    
    public static boolean SERVER_MODE = false;

    
    // public static String OrderWritePathDir = "C:\\Users\\Fahad\\Documents\\NetBeansProjects\\Shop3DAdming\\OrderZipWriter\\";
    // public static String LogPathDir = "C:\\Users\\Fahad\\Documents\\NetBeansProjects\\Shop3DAdming\\LogFiles\\";
    public static String OrderWritePathDir = "C:\\Users\\Fahad\\Dropbox\\Shop3DServerCode\\Shop3DAdming\\OrderZipWriter\\";
    public static String LogPathDir = "C:\\Users\\Fahad\\Dropbox\\Shop3DServerCode\\Shop3DAdming\\LogFiles\\";

    // Parse IDs for  Shop3D project.
    public static String ApplicationId_ =  "USEYOUROWN" ; 
    public static String RestId_ = "USEYOUROWN";
    // ----------------------------

    public static String baseIdx = Util.GetSuperUniqueId();

    // Reading icons are of the following format.
    public static String ImageExt = ".jpg";
    public static String Parse_UploadFilename = "some_file";

    // SCULPTEO Information.
    public static String Sculpteo_Username = "USEYOUROWN";
    public static String Sculpteo_Password = "USEYOUROWN";
    public static String Sculpteo_PrintMaterial = "color_plastic";
    public static String Sculpteo_Units = "in";
    public static String Sculpteo_Currency = "USD";

    public static float Sculpteo_ProfitPercentage = 0.40f;

    // This should be at the end of the obj file which is the higher
    // resolution file that will be sent to print.
    // The one on the parse server will still be the low rs file.
    // 5000 faces.
    public static String KEYWORD_HighRes = "_highres";

    public static boolean TESTING_UPLOAD = false;
    public static int TESTING_COUNT = 0;

    public static String LIST_CHECK_SIZE = "eMLoRKvQPB";
    public static String LIST_PRODUCT = "7u7cieogiA";

    public static void FixFolderPaths() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) {
            OrderWritePathDir = "C:\\Users\\Fahad\\Dropbox\\Shop3DServerCode\\Shop3DAdming\\OrderZipWriter\\";
            LogPathDir = "C:\\Users\\Fahad\\Dropbox\\Shop3DServerCode\\Shop3DAdming\\LogFiles\\";

        } else {
            // It is Ubuntu.
            SharedData.SERVER_MODE = true;
            OrderWritePathDir = "/home/zeus/Dropbox/Shop3DServerCode/Shop3DAdming/OrderZipWriter/";
            LogPathDir = "/home/zeus/Dropbox/Shop3DServerCode/Shop3DAdming/LogFiles/";
        }
    }

}
