/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.parse;

import com.obj.Material;
import com.obj.WavefrontObject;
import com.threed.jpct.DeSerializer;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.TextureManager;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import org.json.JSONObject;
import org.parse4j.*;
import org.parse4j.callback.FindCallback;
import org.shop3d.http.Req_Base;
import org.shop3d.http.Req_DesignUpload;
import org.shop3d.http.Req_GetPrice;
import org.shop3d.util.SharedData;
import org.shop3d.util.TextureHandler;
import org.shop3d.util.Util;

/**
 *
 * @author zeus
 */
public class ParseOperation {

    public static void IniParse() {
        // Add your initialization code here
        Parse.initialize(SharedData.ApplicationId_,
                SharedData.RestId_);

    }

    // ----------- THESE ARE  TEMP VARIABLES USED FOR SORTING BASED ON TEXTURE NAMES
    static int jxx = 0;
             // list of returns of the compare method which will be used to manipulate
    // the another comparator according to the sorting of previous listA
    static ArrayList<Integer> sortingMethodReturns = new ArrayList<Integer>();

    // -----------------
    // order_count is automatically incremented by 1
    // order_item_count , no. of items in the order
    // order_item_type_count, no. of differnt types of items.
    // total_stripe_charge_amount
    // total_profit_amount
    // total_sculpteo_cost_amount
    public static void IncrementSaleOrderCount(final int order_item_count,
            final int order_item_type_count,
            final double total_stripe_charge_amount,
            final double total_profit_amount,
            final double total_sculpteo_cost_amount,
            final double total_cc_cost_amount
    ) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            Date date = new Date();
            cal.setTime(dateFormat.parse(dateFormat.format(date)));

            // Get that particular date object.
            ParseQuery<ParseObject> computeQuery = ParseQuery.getQuery("Sales_Order_Count");
            computeQuery.whereEqualTo("this_date", cal.getTime());

            computeQuery.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> list, ParseException parseException) {
                    if (list != null) {
                        list.get(0).increment("order_count");
                        list.get(0).increment("order_item_count", order_item_count);
                        list.get(0).increment("order_item_type_count", order_item_type_count);
                        list.get(0).increment("total_stripe_charge_amount", total_stripe_charge_amount);
                        list.get(0).increment("total_profit_amount", total_profit_amount);
                        list.get(0).increment("total_sculpteo_cost_amount", total_sculpteo_cost_amount);
                        list.get(0).increment("total_cc_cost_amount", total_cc_cost_amount);
                        list.get(0).saveInBackground();
                    }
                }
            });
        } catch (Exception er) {

        }

    }

    public static void CreateSalesCountRecord() {

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse("2016-01-01"));

            for (int i = 0; i < 365; i++) {

                ParseObject data = new ParseObject("Sales_Order_Count");
                data.put("this_date", cal.getTime());
                data.put("order_count", 0);
                data.put("order_item_count", 0);
                data.put("order_item_type_count", 0);
                data.put("total_stripe_charge_amount", 0);
                data.put("total_profit_amount", 0);
                data.put("total_sculpteo_cost_amount", 0);
                data.put("total_cc_cost_amount", 0);
                cal.add(Calendar.DATE, 1);
                data.save();
            }

            /*
             // CHECK
             IncrementSaleOrderCount(5);
             Calendar cal2 = Calendar.getInstance();
             cal2.setTime(dateFormat.parse("2016-01-15"));

             ParseQuery<ParseObject> computeQuery = ParseQuery.getQuery("Sales_Order_Count");
             computeQuery.whereEqualTo("this_date", cal2.getTime());
             List<ParseObject> allObj = null;
             allObj = computeQuery.find();
             if (allObj != null) {
             System.out.println("Size : " +allObj.size() + " , count = " + allObj.get(0).getInt("count"));
             }*/
        } catch (Exception er) {
            System.out.println(" Error in Category count " + er.getMessage());
        }

    }

    public static void ClearAll(String classname) {
        List<ParseObject> allObj = null;
        ParseQuery<ParseObject> computeQuery = ParseQuery.getQuery(classname);
        try {
            allObj = computeQuery.find();

            if (allObj != null) {
                // for each category count the number of models in there.
                for (int i = 0; i < allObj.size(); i++) {
                    allObj.get(i).delete();
                }
            }

        } catch (Exception er) {
            System.out.println(" Error in Category count " + er.getMessage());
        }

    }

    public static ParseFile UploadParseFile(byte[] data, boolean compress) {
        try {
            if (compress) {
                data = Util.compress(data);
            }
            ParseFile UplodedFile = new ParseFile(SharedData.Parse_UploadFilename, data);
            UplodedFile.save();
            return UplodedFile;
        } catch (Exception er) {
            System.out.println(" Parse file upload exception");
            return null;
        }
    }

    public static void UpdateCategoryCount() {

        // Get all the categories
        List<ParseObject> allCategories = null;
        ParseQuery<ParseObject> computeQuery = ParseQuery.getQuery("Category");
        try {
            allCategories = computeQuery.find();

            // for each category count the number of models in there.
            for (int i = 0; i < allCategories.size(); i++) {
                ParseQuery<ParseObject> perCategory = ParseQuery.getQuery("Model");
                perCategory.whereEqualTo("category_id", allCategories.get(i));

                int count = perCategory.count();
                allCategories.get(i).put("model_count", count);
                allCategories.get(i).save();
            }

        } catch (Exception er) {
            System.out.println(" Error in Category count " + er.getMessage());
        }

    }

    public static void UploadModel(String iModelDir) {
        String filename = "";

        try {

            File[] files = new File(iModelDir).listFiles();

            for (File file : files) {
                if (file.isFile()) {
                    if (file.getName().contains(".obj")) {
                        String[] tokens = file.getName().split(".obj");
                        filename = tokens[0];
                    }
                }
            }

            // This is the object we are trying to fill up. 
            ParseObject completedDBEntry = new ParseObject("Model");

            // Add ratings
            completedDBEntry.put("rating", Util.FormatDouble(Math.random() + 4, 1));

            // 1. Read the info.txt file
            // Reads: title, description, unit, sizes, category, total_icons,
            // changeable_materials
            HashMap<String, String> modelInfo = new HashMap<String, String>();
            try {
                File file = new File(iModelDir + "info.txt");
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] tokens = line.split(":");

                    // Helps not throw the out of bounds exception when
                    // there are tailing new lines at the end of info.txt
                    if (tokens.length >= 2) {
                        modelInfo.put(tokens[0], tokens[1]);
                    }
                }
                fileReader.close();
            } catch (Exception er) {
                er.printStackTrace();
            }

            String sculpteoRot = null;
            if (modelInfo.containsKey("s_rotation")) {
                sculpteoRot = modelInfo.get("s_rotation");
                completedDBEntry.put("s_rotation", sculpteoRot);
            }

            // Set the model type.
            if (modelInfo.containsKey("model_type")) {
                completedDBEntry.put("model_type", modelInfo.get("model_type"));
            } else {
                completedDBEntry.put("model_type", "0");
            }

            // Common parameters.
            String objFilePath = iModelDir + filename + ".obj";
            String mtlFilePath = iModelDir + filename + ".mtl";
            Path OBJpath = Paths.get(objFilePath);
            Path MTLpath = Paths.get(mtlFilePath);

            // 2. Make object 3D file, Serialize and Compress.
            // This is needed so it download and displays on the mobile quickly.
            // NOTE: UploadParseFile will compress it before uploading.
            File objFile = new File(objFilePath);
            File mtlFile = new File(mtlFilePath);

            InputStream objInpStream = new FileInputStream(objFile);
            InputStream mtlInpStream = new FileInputStream(mtlFile);

            Logger.setLogLevel(Logger.LL_ONLY_ERRORS);
            Object3D thing = null;
            try {
                thing = Object3D.mergeAll(Loader.loadOBJ(objInpStream, mtlInpStream, 20));
                thing.build();

            } catch (Exception er) {
                System.out.println("Cannot parse obj file: " + objFilePath);
                System.out.println(er.getMessage());
                System.out.println(" ***  EXITING THIS MODEL UPLOAD");
                return;
            }

            TextureManager tm = TextureManager.getInstance();
            TextureHandler th = new TextureHandler();
            th.init(tm);

            // byte[] serialziedObjMtlData = Util.convertToBytes(thing);
            ByteArrayOutputStream outST = new ByteArrayOutputStream();
            DeSerializer obj = new DeSerializer();

            obj.serialize(thing, outST, false);
            byte[] SerializedData = outST.toByteArray();

            completedDBEntry.put("serialized_obj_mtl", UploadParseFile(SerializedData, true));

            // 3. Make a .zip file from the (obj + mtl) and upload to Sculpteo and get UUID.
            // This is needed for the entry to parse.
            // Create the zip file.
            String[] allModelFiles = th.GetAllImageFilePaths(iModelDir, objFilePath, mtlFilePath);
            // String[] allModelFiles = {objFilePath, mtlFilePath};

            completedDBEntry.put("original_filename", filename);

            // If the model has a high res obj file, we need to ssave that 
            // as zip, since that gets uploaded with sculpteo.
            /*if (modelInfo.get("has_highres").equals("false") == false) {
             // Means there is a high res versiion available.
             String highResName = modelInfo.get("has_highres");
             objFilePath = iModelDir + highResName + ".obj";
             mtlFilePath = iModelDir + highResName + ".mtl";

             completedDBEntry.put("original_filename_highres", highResName);
             OBJpath = Paths.get(objFilePath);
             MTLpath = Paths.get(mtlFilePath);

             allModelFiles[0] = objFilePath;
             allModelFiles[1] = mtlFilePath;
             Util.ZipAndSave(allModelFiles, iModelDir + filename + ".zip");

             // Now reset everything since those paths are used later
             objFilePath = iModelDir + filename + ".obj";
             mtlFilePath = iModelDir + filename + ".mtl";
             OBJpath = Paths.get(objFilePath);
             MTLpath = Paths.get(mtlFilePath);
             allModelFiles[0] = objFilePath;
             allModelFiles[1] = mtlFilePath;
             completedDBEntry.put("has_highres", true);

            
            
            
            
            
            
            
             } else */
            {
                // There is no high-res model. 
                Util.ZipAndSave(allModelFiles, iModelDir + filename + ".zip");
                completedDBEntry.put("has_highres", false);

                // Now save all the textures in a zip file
                if (th.HasTextures()) {
                    String[] allImgTexFiles = th.GetOnlyImageFilePaths(iModelDir);
                    Util.ZipAndSave(allImgTexFiles, iModelDir + "img_tex_only" + ".zip");
                    byte[] AllImgTexData = Files.readAllBytes(Paths.get(iModelDir + "img_tex_only" + ".zip"));

                    completedDBEntry.put("all_img_tex_zip", UploadParseFile(AllImgTexData, true));
                    completedDBEntry.put("has_textures", true);
                } else {
                    completedDBEntry.put("has_textures", false);
                }
            }

            // 9. Read the materials
            WavefrontObject mtlObj = new WavefrontObject(iModelDir + filename + ".obj");
            Hashtable<String, Material> myMat = mtlObj.getMaterials();

            // This array contains the names of the materials turned into textures
            // which are changable.
            ArrayList<String> JPCTTexNames = new ArrayList<String>();

            String mtlNames = "";
            String mtlRGB = "";
            String mtlOrder = "";
            int counter = 0;
            Enumeration e = myMat.keys();
            while (e.hasMoreElements()) {
                String actualMtlName = e.nextElement().toString();

                if (actualMtlName.contains("X_")) {
                    Material thisMat = myMat.get(actualMtlName);
                    String RGB = Util.FormatDouble(thisMat.getKd().getX(), 2) + ":"
                            + Util.FormatDouble(thisMat.getKd().getY(), 2) + ":"
                            + Util.FormatDouble(thisMat.getKd().getZ(), 2);

                    int iR = (int) (thisMat.getKd().getX() * 255);
                    int iG = (int) (thisMat.getKd().getY() * 255);
                    int iB = (int) (thisMat.getKd().getZ() * 255);

                    String texName = "__obj-Color:" + iR + "/" + iG + "/" + iB;
                    if (JPCTTexNames.contains(texName)) {
                        // Marker start 
                        continue;
                    }
                    JPCTTexNames.add(texName);

                    String[] tokens = actualMtlName.split("_");
                    if (mtlNames.equals("")) {
                        mtlNames = tokens[tokens.length - 1];
                        mtlRGB = RGB;
                        mtlOrder = counter + "";
                    } else {
                        mtlNames += "*" + tokens[tokens.length - 1];
                        mtlRGB += "*" + RGB;
                        mtlOrder += "*" + counter;
                    }
                    counter++;

                }
            } // End of all hashtable items

            //-------------------------
            // SORT THE NAMES AND COLORS ALPHABETICALLY
            //--------------------------------
            ArrayList<String> XColors = new ArrayList(Arrays.asList(mtlRGB.split("\\*")));
            ArrayList<String> XNames = new ArrayList(Arrays.asList(mtlNames.split("\\*")));

            ///// FOR VERTICES TEST
           //  if (true) {
            //     return;
           // }
         
            //int jxx = 0;
            // list of returns of the compare method which will be used to manipulate
            // the another comparator according to the sorting of previous listA
            //ArrayList<Integer> sortingMethodReturns = new ArrayList<Integer>();
            sortingMethodReturns.clear();
       
            
            // Sort the materials based on their names.
            Collections.sort(XNames, new Comparator<String>() {

                @Override
                public int compare(String lhs, String rhs) {
                    // TODO Auto-generated method stub
                    int returning = lhs.compareTo(rhs);
                    sortingMethodReturns.add(returning);
                    return returning;
                }

            });

            // Sort the colors accordingly.
            jxx = 0;
            Collections.sort(XColors, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                // TODO Auto-generated method stub

                // comparator method will sort the second list also according to
                    // the changes made with list a
                    int returning = sortingMethodReturns.get(jxx);
                    jxx++;
                    return returning;
                }
            });

            // Sort the JPCT texture names accordingly.
            jxx = 0;
            Collections.sort(JPCTTexNames, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                // TODO Auto-generated method stub

                // comparator method will sort the second list also according to
                    // the changes made with list a
                    int returning = sortingMethodReturns.get(jxx);
                    jxx++;
                    return returning;
                }
            });
            
            mtlNames = Util.MakeOneString(XNames, "*");
             mtlRGB = Util.MakeOneString(XColors, "*");
            //-----------------------
            // END SORT
            //-----------------------------------

            // Marker end
            String mtlJPCTTexNames = Util.MakeOneString(JPCTTexNames, "*");

            // Stores the original names of the materials as in the .mtl file.
            completedDBEntry.put("mtl_flat_name_original", mtlNames);
            // Stores the material names as displayed on the screen.
            completedDBEntry.put("mtl_flat_name_display", mtlNames);
            // The names of the textures in the texture manager of JPCT for flat changeable colors.
            completedDBEntry.put("mtl_flat_name_texture", mtlJPCTTexNames);

            // The original order of the materials as in the .mtl file.
            completedDBEntry.put("mtl_flat_order_display", mtlOrder);
            // The order of the materials in the .mtl file.
            completedDBEntry.put("mtl_flat_order_original", mtlOrder);

            // Raw RGB values of the material.
            completedDBEntry.put("mtl_flat_colors", mtlRGB);

            // The total number of changable flat material.
            completedDBEntry.put("mtl_flat_count", JPCTTexNames.size());

            completedDBEntry.put("priority", 5);
            completedDBEntry.put("visible", false);

            // ------------- THIS IS A USELESS UPLOAD TO FIND THE SIZE DIMS OF THE OBJECT......
            Req_DesignUpload uploadModel = new Req_DesignUpload(Req_Base.RunMode.PRODUCTION);
            JSONObject modelUpload = uploadModel.SendPostRequest(filename, iModelDir + filename + ".zip",
                    modelInfo.get("title"), modelInfo.get("description"), SharedData.LIST_CHECK_SIZE, "1", sculpteoRot);
            // -------------------------------------------

            double dimX = modelUpload.getDouble("dimx");
            double dimY = modelUpload.getDouble("dimy");
            double dimZ = modelUpload.getDouble("dimz");

            // Now you just got the default x , y ,z, calculate all 10  form 1 inch height to 15 inch heights
            // Hence calculate the scales, since only the scales are sent for price calculation.
            String materialMaxDims = "10:15:8";
            String[] matDimMax = materialMaxDims.split(":");
            ArrayList<String> allowedScales = new ArrayList<String>();

            double maxX = Double.parseDouble(matDimMax[0]) - 0.25;
            double maxY = Double.parseDouble(matDimMax[1]) - 0.25;
            double maxZ = Double.parseDouble(matDimMax[2]) - 0.25;//- 0.25;

            float startingHeight = Float.parseFloat(modelInfo.get("starting_size").toString());
            float incrementHeight = Float.parseFloat(modelInfo.get("increments").toString());

            // Starting height for scaling is 2 inches, no model can be ordered smaller than 2 inches.
            for (float i = startingHeight; i <= maxZ; i += incrementHeight) {
                // This is the needed scale for a 1 - 15 inch height.
                double scale = i / dimZ;

                // Now we have to check if it lies within all the other bounds
                if (((scale * dimX) <= maxX)
                        && ((scale * dimY) <= maxY)
                        && ((scale * dimZ) < maxZ)) {

                    allowedScales.add(Util.FormatDouble(scale, 9) + "");
                }
            }

            // NOW UPLOAD THE REAL MODEL JSONObject 
            modelUpload = uploadModel.SendPostRequest(filename, iModelDir + filename + ".zip",
                    modelInfo.get("title"), modelInfo.get("description"),
                    SharedData.LIST_PRODUCT, Util.MakeOneString(allowedScales, ","),
                    sculpteoRot);

            // -------------------------------------------------------------------------------
            // Output: modelUpload = 
            // modelUpload = (org.json.JSONObject) {"unit":"in","design_secret":"2dc3aaa2241127ae10e43f7af9abe841e02b3b5c71245bc335f64b6fcb965dde",
            // "parts":4,"name":"Android Figurine","scale":1,"uuid":"cuwx3Jqi","dimz":1.9615079164505,"dimy":3.5689821243286204,"slug":
            // "android-figurine-13","dimx":3.01826810836792}
            // 3. Calculate price for all sizes.
            // Get prices per scale sizes.
            // Strings that go with the object to Parse
            //  String uploadSculpteoPrices = "";
            //  String uploadSellingPrices = "";
            String uploadDims = "";
            //  String displayPrice = "";

            if (allowedScales.size() <= 0) {
                System.out.print("THERE ARE NO SIZES AVAILABLE");
                return;
            }
            completedDBEntry.put("uuid", modelUpload.getString("uuid"));

            for (int i = 0; i < allowedScales.size(); i++) {

                // Only get the price of the smallest size, so you can sort them according to price.
                if (i == 0) {
                    Req_GetPrice priceMeModel = new Req_GetPrice(Req_Base.RunMode.PRODUCTION);
                    JSONObject priceResult = priceMeModel.SendPostRequest(modelUpload.getString("uuid"), allowedScales.get(i));
                    double sculpteoPricePerScale = priceResult.getJSONObject("body").getJSONObject("price").getDouble("unit_price_raw");
                    //   double sellingPricePerScale = sculpteoPricePerScale * m + sculpteoPricePerScale;

                    sculpteoPricePerScale = Util.FormatDouble(sculpteoPricePerScale, 2);
                    //   sellingPricePerScale = Util.FormatDouble(sellingPricePerScale, 2);
                    completedDBEntry.put("smallest_size_price", sculpteoPricePerScale);
                    //  completedDBEntry.put("smallest_size_selling_price", sellingPricePerScale);
                }
                // Calculate the dimensions at this scale.
                double scale = Double.parseDouble(allowedScales.get(i));
                String dimPerScale = Util.FormatDouble(dimX * scale, 2) + " x " + Util.FormatDouble(dimY * scale, 2) + " x "
                        + Util.FormatDouble(dimZ * scale, 2) + " " + SharedData.Sculpteo_Units;

                // Make is printable string format.
                if (uploadDims.equals("")) {
                    //uploadSculpteoPrices = sculpteoPricePerScale + "";
                    // uploadSellingPrices = sellingPricePerScale + "";
                    uploadDims = dimPerScale;
                    //displayPrice = Math.ceil(sellingPricePerScale) + "";
                } else {
                    // uploadSculpteoPrices = (uploadSculpteoPrices + "*" + sculpteoPricePerScale);
                    //  uploadSellingPrices = uploadSellingPrices + "*" + sellingPricePerScale;
                    uploadDims = uploadDims + "*" + dimPerScale;
                    // displayPrice = displayPrice + "*" + Math.ceil(sellingPricePerScale);

                }

            }

            //    completedDBEntry.put("display_price", displayPrice);
            //   completedDBEntry.put("sale_price", uploadSellingPrices);
            //  completedDBEntry.put("sculpteo_price", uploadSculpteoPrices);
            completedDBEntry.put("on_sale", 0);
            completedDBEntry.put("scales", Util.MakeOneString(allowedScales, "*"));
            completedDBEntry.put("percentage_profit", SharedData.Sculpteo_ProfitPercentage);
            completedDBEntry.put("dims", uploadDims);
            completedDBEntry.put("dim_scale_1", Util.FormatDouble(dimX, 2) + " x " + Util.FormatDouble(dimY, 2) + " x " + Util.FormatDouble(dimZ, 2));

            // Output: priceResult =
            // result = (org.json.JSONObject) {"body":{"volume":421.6209739452435,"delivery":{"shipped_delta":4,"receipt_date":"2015-11-13T17:00:00",
            // "receipt_timer":"30 hours"},"material":"color_plastic","success":true,"price":{"total_cost":"$347.28",
            // "unit_price_without_discount":"$347.28","unit_price_round":"$347","total_cost_without_discount_raw":"347.28",
            // "unit_price_without_discount_raw":"347.28","profit_raw":"31.29","has_tax":false,"total_cost_without_discount":"$347.28",
            // "discount":0,"total_cost_raw":"347.28","unit_price":"$347.28","unit_price_raw":"347.28"},"scale":38.099999999999994,
            // "currency":"USD"},"error":{"description":"no error","id":0}}
            // 4. Upload obj and mtl as Parse Files.
            // This is needed to get a price quote and size etc. from parse.
            byte[] ObjData = Files.readAllBytes(OBJpath);
            byte[] MtlData = Files.readAllBytes(MTLpath);

            completedDBEntry.put("default_obj_file", UploadParseFile(ObjData, true));
            completedDBEntry.put("default_mtl_file", UploadParseFile(MtlData, true));

            // 6. Upload all Icons as files.
            // These icons are the still images for the model.
            int totalIcons = Integer.parseInt(modelInfo.get("total_icons"));
            for (int i = 0; i < totalIcons; i++) {
                String iconDirPath = iModelDir + "image_" + i + SharedData.ImageExt;
                Path iconPath = Paths.get(iconDirPath);
                completedDBEntry.put("icon_" + i, UploadParseFile(Files.readAllBytes(iconPath), false));
            }
            completedDBEntry.put("total_icons", totalIcons);

            // 7. Convert category to category id.
            List<ParseObject> answer = null;
            ParseQuery<ParseObject> computeQuery = ParseQuery.getQuery("Category");
            computeQuery.whereEqualTo("name", modelInfo.get("category"));

            try {
                answer = computeQuery.find();
            } catch (Exception er) {
                System.out.println(" Error getting category:" + modelInfo.get("category") + " --- " + er.getMessage());
            }
            // Only 1 object should be there for this category name.
            completedDBEntry.put("category_id", answer.get(0));

            // 8. Complete filling all other info inside the Parse object.
            completedDBEntry.put("title", modelInfo.get("title"));
            completedDBEntry.put("description", modelInfo.get("description"));
            completedDBEntry.put("average_rating", 0);
            completedDBEntry.put("total_rating_count", 0);

            // Create a searchable entry.
            String searchTxt = modelInfo.get("title").toLowerCase() + " " + modelInfo.get("description").toLowerCase() + " " + modelInfo.get("category").toLowerCase();
            searchTxt = searchTxt.replaceAll(",.", " ");
            completedDBEntry.put("search_text", searchTxt);
            /*
             // Confirming that hte Texture names are correctly generated
             //__obj-Color:75/115/8
             //__obj-Color:75/115/8
             // Get the names of the tetures
             
             for (int i = 0; i < JPCTTexNames.size(); i++) {
             Texture tex = tm.getTexture(JPCTTexNames.get(i));
             int y = 0;
             }

             */
            // 10. Save the object, and thank God.
            completedDBEntry.save();
            // If successful, add to the model count.
            answer.get(0).increment("model_count");
            answer.get(0).save();
            System.out.println(" \n\n\n--------------------------  COMPLETED: " + filename);
            System.out.println(" In:" + iModelDir);
            System.out.println(" -------------------------- ");

        } catch (Exception er) {
            System.out.println(" \n\n\nFAILED --------------------------:  " + filename);
            System.out.println(" In:" + iModelDir);
            System.out.println(" -------------------------- ");
            System.out.println("Exception: " + er.getMessage());
        }

        int y = 0;
    }
    /*
     public static void ClearAllQueueExecutioners() {
     ParseQuery<ParseObject> requests = new ParseQuery("Queue");
     requests.whereNotEqualTo("executioner", "none");
     List<ParseObject> doMe = new ArrayList<ParseObject>();
     try {
     doMe = requests.find();
     if (doMe != null) {
     for (int i = 0; i < doMe.size(); i++) {
     doMe.get(i).put("executioner", "none");
     doMe.get(i).saveInBackground();
     }
     System.out.println("All Queue items set to executioner=none");
     } else {
     System.out.println("No executioners found to be be NOT none");

     }
     } catch (ParseException ex) {
     Logger.getLogger(ParseOperation.class.getName()).log(Level.SEVERE, null, ex);

     }

     }

     public static List<ParseObject> GetWork(String workerId) {
     try {
     // Get the top requets that have no executioner.
     ParseQuery<ParseObject> requests = new ParseQuery("Queue");
     requests.orderByDescending("priority,-createdAt");
     //requests.orderByAscending("createdAt");
     // requests.addAscendingOrder("createdAt");

     requests.whereEqualTo("executioner", "none");
     requests.limit(SharedData.WorkBatchCount);
     List<ParseObject> doMe = new ArrayList<ParseObject>();
     doMe = requests.find();

     if (doMe != null) {
     // Set all the requets you got to have the executioner as yourself.
     for (int i = 0; i < doMe.size(); i++) {
     System.out.println(workerId + ", got:" + doMe.get(0).getString("videoId"));
     doMe.get(i).put("executioner", workerId);
     doMe.get(i).save();
     }

     // Now fetch work that has your name as executioner.
     ParseQuery<ParseObject> getMyWork = new ParseQuery("Queue");
     getMyWork.orderByDescending("priority");
     getMyWork.whereEqualTo("executioner", workerId);
     List<ParseObject> myTasks = getMyWork.find();
     return myTasks;

     } else {
     // System.out.println(workerId + ", No work found");
     Thread.sleep(5000);
     }
     } catch (Exception er) {
     System.out.println("Exception: " + er.getMessage());
     }

     return null;
     }

     public static void UploadFileAndDeleteQueueEntry(ParseObject qEntry, byte[] data) {
     try {
     ParseObject completedDBEntry = new ParseObject("Completed");
     completedDBEntry.put("videoId", qEntry.getString("videoId"));
     completedDBEntry.put("executioner", qEntry.getString("executioner"));
     completedDBEntry.put("title", qEntry.getString("title"));
     completedDBEntry.put("duration", qEntry.getString("duration"));
     completedDBEntry.put("priority", qEntry.getInt("priority"));
     completedDBEntry.put("userId", qEntry.getParseObject("userId"));

     ParseFile music = new ParseFile("abc.mp3", data);
     music.save();
     completedDBEntry.put("storedFile", music);
     completedDBEntry.put("status", "1");
     completedDBEntry.save();

     qEntry.delete();
     } catch (Exception er) {
     System.out.println("Exception: " + er.getMessage());
     }

     }

     public static void UploadServerSettings() {
     try {
     ParseObject completedDBEntry = new ParseObject("ServerSettings");
     completedDBEntry.put("executioner", SharedData.baseId);
     completedDBEntry.put("cores", SharedData.Cores);
     completedDBEntry.put("workBatchCount", SharedData.WorkBatchCount);
     completedDBEntry.put("waitAfterWorkCompletion", SharedData.WaitAfterWorkComplete);
     completedDBEntry.put("waitBeforeGetNextWork", SharedData.WaitBeforeGetNextWork);
     completedDBEntry.put("logServerGetSettingsInterval", SharedData.LogServerGetSettingsInterval);
     completedDBEntry.put("logServerPerformanceInterval", SharedData.LogServerPerformanceInterval);

     completedDBEntry.saveInBackground();

     } catch (Exception er) {
     System.out.println("Exception: " + er.getMessage());
     }
     }

     public static void UploadError(String exceptionMessage, String className) {
     ParseObject completedErrorEntry = new ParseObject("Errors");
     completedErrorEntry.put("executioner", SharedData.baseId);
     completedErrorEntry.put("errorClassName", className);
     completedErrorEntry.put("exceptionMessage", exceptionMessage);
     completedErrorEntry.saveInBackground();
     }

     public static void LogServerData() {
     ParseObject completedErrorEntry = new ParseObject("Logs");
     float total = SharedData.TaskCompletedCount + SharedData.TaskIdleCount;
     boolean totalIsZero = false;
     if (total == 0) {
     total = 1;
     totalIsZero = true;
     }
     completedErrorEntry.put("executioner", SharedData.baseId);
     completedErrorEntry.put("idle", SharedData.TaskIdleCount / total * 100);
     completedErrorEntry.put("completed", SharedData.TaskCompletedCount / total * 100);

     if (totalIsZero) {
     total = 0;
     }
     completedErrorEntry.put("total", total);

     completedErrorEntry.saveInBackground();
     }

     public static void GetServerSettings() {
     try {
     ParseQuery<ParseObject> requests = new ParseQuery("ServerSettings");
     requests.limit(1);
     requests.whereEqualTo("executioner", SharedData.baseId);
     List<ParseObject> doMe = new ArrayList<ParseObject>();

     doMe = requests.find();

     if (doMe != null) {
     if (doMe.size() > 0) {
     ParseObject Settings = doMe.get(0);
     SharedData.WaitBeforeGetNextWork = Settings.getInt("waitBeforeGetNextWork");
     SharedData.WorkBatchCount = Settings.getInt("workBatchCount");
     SharedData.WaitAfterWorkComplete = Settings.getInt("waitAfterWorkCompletion");;

     SharedData.LogServerPerformanceInterval = Settings.getInt("logServerPerformanceInterval");

     SharedData.LogServerGetSettingsInterval = Settings.getInt("logServerGetSettingsInterval");

     }
     }
     } catch (ParseException ex) {
     Logger.getLogger(ParseOperation.class.getName()).log(Level.SEVERE, null, ex);
     }

     }
     */
}
