/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.server.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import org.parse4j.ParseException;
import org.parse4j.ParseFile;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;
import org.parse4j.callback.FindCallback;
import org.shop3d.http.Req_Base;
import org.shop3d.http.Req_DesignUpload;
import org.shop3d.http.Req_GetPrice;
import org.shop3d.http.Req_Order;
import static org.shop3d.parse.ParseOperation.IncrementSaleOrderCount;
import org.shop3d.util.FileUtils;
import org.shop3d.util.HtmlCostExtracter;
import org.shop3d.util.LogFileWriter;
import org.shop3d.util.SharedData;
import org.shop3d.util.Util;

public class WorkerThread_FulfillOrder implements Runnable {

    private String myCompleteId;
    public static LogFileWriter LogWrite = new LogFileWriter();

    public WorkerThread_FulfillOrder(int threadNo) {
        this.myCompleteId = threadNo + "_" + SharedData.baseIdx;

    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        processCommand();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // System.out.println(myCompleteId + "      Work processing Start-End. TTE=" + (duration / 1000000000) + " secs"
        // );
    }

    public void AddModelSoldCount(final ParseObject mid, final int increment) {

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Sales_Model_Count");
        query.whereEqualTo("model_id", mid);

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> list, ParseException parseException) {
                boolean DoNewObj = false;

                if (list == null) {
                    DoNewObj = true;
                }
                if (list != null) {
                    if (list.size() == 0) {
                        DoNewObj = true;
                    } else {
                        list.get(0).increment("count", increment);
                        list.get(0).saveInBackground();
                        DoNewObj = false;
                    }
                }

                if (DoNewObj) {
                    ParseObject newObj = ParseObject.create("Sales_Model_Count");
                    newObj.put("model_id", mid);
                    newObj.put("count", increment);

                    newObj.saveInBackground();

                }

            }
        });

    }

    private void processCommand() {
        try {
            List<ParseObject> queueItem = ServerOperations.GetWork(myCompleteId);
            String fetchTime = Util.GetDateTime();

            if (queueItem != null) {
                if (queueItem.size() > 0) {
                    ServerConfig.TaskCompletedCount++;
                }

                for (int i = 0; i < queueItem.size(); i++) {
                    String screenMessage = fetchTime + ": Fetched order ObjectID:" + queueItem.get(i).getObjectId()
                            + ", charge_amount:" + queueItem.get(i).getDouble("charge_amount");
                    //  + ", min_profit:" + Util.FormatDouble(queueItem.get(i).getDouble("min_profit"), 2);
                    System.out.println(screenMessage);
                    LogWrite.Write(screenMessage);
                    //------------------------------------
                    // This code runs for each mainorder, once.
                    //------------------------------------
                    // Get the shipping for this order
                    // ------------- Get the damn shipping
                    ParseObject shipping_address = ExtraPOLoader.GetShipping(queueItem.get(i));
                    ParseObject shipping_rate = ExtraPOLoader.GetShippingRate(queueItem.get(i));
                    //---------------

                    // Get the order items for this order.
                    List<ParseObject> allOrderItems = ServerOperations.GetOrderItems(queueItem.get(i));
                    ArrayList<String> sculpteoOrderItems = new ArrayList<String>();
                    for (int j = 0; j < allOrderItems.size(); j++) {
                        ParseObject orderItem = allOrderItems.get(j);
                        // Load the usermade model or the other model.
                        ParseObject model = ExtraPOLoader.GetModel(allOrderItems.get(j));
                        ParseObject model_usermade = ExtraPOLoader.GetModelUserMade(allOrderItems.get(j));
                        // <uuid>:<qty>:<scale>:<unit>:<material>
                        if (model == null) {
                            // Usermade object
                            ParseObject originalModel = ExtraPOLoader.GetModelFromUsermade(model_usermade);

                            boolean shouldBeUploaded = false;

                            if (model_usermade.getString("new_uuid") == null) {
                                shouldBeUploaded = true;
                            } else {
                                if (model_usermade.getString("new_uuid").length() == 0) {
                                    shouldBeUploaded = true;
                                }
                            }
                            // Check if the model is already created and has a UUID.
                            if (shouldBeUploaded) {
                                // -------------------------------------------------------------------
                                // Ordering a my model that must be uploaded.
                                //--------------------------------------------------------------------
                                // The longest route.
                                try {
                                    ParseFile mtlFile = (ParseFile) originalModel.get("default_mtl_file");
                                    ParseFile objFile = (ParseFile) originalModel.get("default_obj_file");

                                    byte[] compressedData = mtlFile.getData();
                                    byte[] mtlData = Util.decompress(compressedData);
                                    InputStream is = new ByteArrayInputStream(mtlData);
                                    BufferedReader bfReader = new BufferedReader(new InputStreamReader(is));

                                    compressedData = objFile.getData();
                                    byte[] ObjData = Util.decompress(compressedData);
                                    InputStream isOBJ = new ByteArrayInputStream(ObjData);
                                    BufferedReader bfReaderObj = new BufferedReader(new InputStreamReader(isOBJ));

                                    String orderFolderDir = queueItem.get(i).getObjectId() + "\\";

                                    try {
                                        boolean success = (new File(SharedData.OrderWritePathDir + orderFolderDir)).mkdirs();
                                    } catch (Exception er) {
                                        System.out.println("Cannot craete folder");
                                        return;
                                    }

                                    File newMTLfile = new File(SharedData.OrderWritePathDir + orderFolderDir + originalModel.getString("original_filename") + ".mtl");
                                    File newOBJfile = new File(SharedData.OrderWritePathDir + orderFolderDir + originalModel.getString("original_filename") + ".obj");
                                    String scales = originalModel.getString("scales").replace("*", ",");
                                    String mtlNames = originalModel.getString("mtl_flat_name_original");
                                    String mtlFlatColors = model_usermade.getString("mtl_color");
                                    String temp = "";

                                    // Add the material and new colors to the hashmap.
                                    HashMap<String, String> matWithColor = new HashMap<String, String>();
                                    String[] names = mtlNames.split("\\*");
                                    String[] cols = mtlFlatColors.split("\\*");

                                    // Add the new color  for the mtl names.
                                    for (int k = 0; k < names.length; k++) {
                                        matWithColor.put("X_" + names[k], cols[k]);
                                    }

                                    // ------- write the obj file
                                    FileWriter fwObj = new FileWriter(newOBJfile.getAbsoluteFile());
                                    BufferedWriter bwObj = new BufferedWriter(fwObj);
                                    while ((temp = bfReaderObj.readLine()) != null) {
                                        bwObj.write(temp + "\n");
                                    }
                                    bwObj.close();
                                    // ----------------------

                                    FileWriter fw = new FileWriter(newMTLfile.getAbsoluteFile());
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    newMTLfile.createNewFile();
                                    while ((temp = bfReader.readLine()) != null) {
                                        //System.out.println(temp);

                                        if (temp.contains("newmtl")) {
                                            String[] mtlLine = temp.split(" ");
                                            if (matWithColor.containsKey(mtlLine[mtlLine.length - 1])) {
                                                String replaceCol = matWithColor.get(mtlLine[mtlLine.length - 1]);
                                                bw.write(temp + "\n");

                                                // Now find the correct kd -------------------------                                          
                                                while ((temp = bfReader.readLine()) != null) {
                                                    if (temp.contains("Kd")) {
                                                        String[] RGB = replaceCol.split(":");
                                                        bw.write("Kd " + RGB[0] + " " + RGB[1] + " " + RGB[2] + "\n");
                                                        break;
                                                    } else {
                                                        bw.write(temp + "\n");
                                                    }

                                                }
                                                // --------------------------------------------------
                                            } else {
                                                bw.write(temp + "\n");
                                            }

                                        } else {
                                            bw.write(temp + "\n");
                                        }

                                    } // while
                                    bw.close();

                                    // Make the zip file
                                    // Create the zip file.
                                    String[] allModelFiles = {
                                        SharedData.OrderWritePathDir + orderFolderDir + originalModel.getString("original_filename") + ".obj",
                                        SharedData.OrderWritePathDir + orderFolderDir + originalModel.getString("original_filename") + ".mtl"};
                                    String zipFileName = "OrderID_" + queueItem.get(i).getObjectId() + "_ModelID_" + model_usermade.getObjectId();
                                    //  originalModel.getString("original_filename")
                                    Util.ZipAndSave(allModelFiles, SharedData.OrderWritePathDir + orderFolderDir + zipFileName + ".zip");

                                    // NOW UPLOAD THE REAL MODEL 
                                    JSONObject modelUpload = null;
                                    String[] allScales = scales.split(",");
                                    Req_DesignUpload uploadModel = new Req_DesignUpload(Req_Base.RunMode.PRODUCTION);

                                    String errormsg = "";
                                    boolean uploadError = false;
                                    ParseObject error = new ParseObject("Errors");
                                    for (int tryy = 0; tryy < 3; tryy++) {
                                        try {
                                            modelUpload = uploadModel.SendPostRequest(
                                                    originalModel.getString("original_filename"),
                                                    SharedData.OrderWritePathDir + orderFolderDir + zipFileName + ".zip",
                                                    originalModel.getString("title"),
                                                    originalModel.getString("description"),
                                                    SharedData.LIST_PRODUCT,
                                                    scales,
                                                    originalModel.getString("s_rotation"));

                                            // Make sure pricing is ready.
                                            Req_GetPrice priceMeModel = new Req_GetPrice(Req_Base.RunMode.PRODUCTION);
                                            JSONObject priceResult = priceMeModel.SendPostRequest(modelUpload.getString("uuid"),
                                                    allScales[allOrderItems.get(j).getInt("scale_index")]);

                                            model_usermade.put("new_uuid", modelUpload.getString("uuid"));
                                            model_usermade.save();
                                            
                                            // If successful, break this trying to upload loop.
                                            uploadError = false;
                                            tryy = 500;
                                        } catch (Exception er) {
                                            // DID NOT FIND THE UUID, REPORT ERROR AND END IF NEXT TRY FAILS.
                                            uploadError = true;
                                            error.put("order_id", queueItem.get(i).getObjectId());
                                            error.put("item_id", allOrderItems.get(j).getObjectId());
                                            error.put("original_model", originalModel.getObjectId());
                                            error.put("original_model_title", originalModel.get("title"));
                                            error.put("exception_message", er.getMessage());
                                            // queueItem.get(i).put("status", 10);
                                            // queueItem.get(i).save();
                                            // return;
                                        }
                                        
                                    } // end of tryy.

                                    if (uploadError) {
                                        error.save();
                                        queueItem.get(i).put("status", 10);
                                        queueItem.get(i).save();
                                        return;
                                    }

                                    // Make the order item to sculpteo entry.
                                    String full = modelUpload.getString("uuid") + ":"
                                            + orderItem.getInt("quantity") + ":"
                                            + allScales[allOrderItems.get(j).getInt("scale_index")] + ":"
                                            + SharedData.Sculpteo_Units + ":"
                                            + SharedData.Sculpteo_PrintMaterial;
                                    sculpteoOrderItems.add(full);

                                    // Delete the model directory.
                                    FileUtils.deleteRecursive(new File(SharedData.OrderWritePathDir + orderFolderDir));

                                } catch (Exception er) {

                                    System.out.println(er.getMessage());
                                }
                                // ------------------------- END LONGEST ROUTE
                            } else {
                                    // -------------------------------------------------------------------
                                // Ordering a my model that is already on Sculpteo.
                                //-------------------------------------------------------------------

                                // The model has already been stored and now its being re-ordered.
                                String scales = originalModel.getString("scales").replace("*", ",");
                                int scaleInde = orderItem.getInt("scale_index");
                                String[] allScales = scales.split(",");
                                String full = model_usermade.getString("new_uuid") + ":"
                                        + orderItem.getInt("quantity") + ":"
                                        + allScales[scaleInde] + ":"
                                        + SharedData.Sculpteo_Units + ":"
                                        + SharedData.Sculpteo_PrintMaterial;
                                sculpteoOrderItems.add(full);
                            }

                            // If a usermade model is slod, increment its original model's count.
                            ParseObject objCount = ParseObject.createWithoutData("Model", model_usermade.getParseObject("original_model_id").getObjectId());
                            AddModelSoldCount(objCount, orderItem.getInt("quantity"));
                            // Read the material RGBs.
                        } else {
                                // -------------------------------------------------------------------
                            // Ordering the original model
                            //--------------------------------------------------------------------

                            // A default model is sold, increment its count.
                            AddModelSoldCount(model, orderItem.getInt("quantity"));

                            // It is a typical object.
                            int scaleIndex = orderItem.getInt("scale_index");
                            String[] objScales = model.getString("scales").split("\\*");

                            String full = model.getString("uuid") + ":"
                                    + orderItem.getInt("quantity") + ":"
                                    + objScales[scaleIndex] + ":"
                                    + SharedData.Sculpteo_Units + ":"
                                    + SharedData.Sculpteo_PrintMaterial;
                            sculpteoOrderItems.add(full);
                            // --------------------------------------------------------------------------
                        }
                    }
                    try {
                        // Now that the Order has been created. Post it to sculpteo.
                        Req_Order placeOrder = new Req_Order(Req_Base.RunMode.PRODUCTION);

                        // Get all the countries,
                        //     List<String> allCountries = ExtraPOLoader.GetEuropeanCountries(); //------------ COUNTRY TESET - 1
                        //      for (int ix = 0; ix < allCountries.size(); ix++) { //------------ COUNTRY TESET - 2
                        //          String counCode = allCountries.get(ix); //------------ COUNTRY TESET - 3
                        //          shipping_address.put("country", counCode); //------------ COUNTRY TESET - 4
                        String scOrderID = placeOrder.SendPostRequest(shipping_address, shipping_rate, sculpteoOrderItems, queueItem.get(i).getString("ship_type_code"));

                        if (scOrderID != null) {
                            queueItem.get(i).put("s_order_id", scOrderID);
                            queueItem.get(i).put("status", 2);

                            // Extract the actual tax, shipping and item cost for the order from the sulpteo web page.
                            HtmlCostExtracter sculpteCost = new HtmlCostExtracter(scOrderID);
                            String sC = sculpteCost.GetShippingCost();
                            String sT = sculpteCost.GetTax();
                            String sTotal = sculpteCost.GetTotal();

                            double dSculpShip = Double.parseDouble(sC);
                            double dSculpTax = Double.parseDouble(sT);
                            double dSculpTotal = Double.parseDouble(sTotal);
                            double allItemPrintCost = Util.FormatDouble(dSculpTotal - dSculpTax - dSculpShip, 2);

                            String actual = "Actual---- Total_Cost= " + sTotal
                                    + ",         All-Item-Print-Cost= " + allItemPrintCost
                                    + ", Shipping= " + sC
                                    + ", Tax= " + sT;

                            queueItem.get(i).put("actual_total_cost", dSculpTotal);
                            queueItem.get(i).put("actual_tax", dSculpTax);
                            queueItem.get(i).put("actual_shipping", dSculpShip);

                            double chargedAmount = queueItem.get(i).getDouble("charge_amount");
                            double ccCost = Util.FormatDouble(2.7 / 100 * chargedAmount + 0.30, 2);
                            //double actualProfit = Util.FormatDouble(chargedAmount - ccCost - (dSculpShip + dSculpTax + dSculpTotal), 2);
                            double actualProfit = Util.FormatDouble(chargedAmount - dSculpTotal - ccCost, 2);

                            queueItem.get(i).put("actual_profit", actualProfit);

                            String screenCompletionMsg = Util.GetDateTime() + ": Completed Order with sculpteo_id:" + scOrderID
                                    + "   (StripeChargeAmount - SculpteoCost - CCCost) " + chargedAmount + " - " + dSculpTotal + " - "
                                    + ccCost + " = " + actualProfit + "(Actual Profit)";
                            if (actualProfit < 0) {
                                String shame = "*************** A L A R M : LOSS INCURRED *******************************************";
                                LogWrite.Write(shame);
                                System.out.println(shame);
                            }
                            System.out.println(screenCompletionMsg);
                            LogWrite.Write(screenCompletionMsg);
                            LogWrite.Write(actual);
                            LogWrite.Write("-------------------------------------");

                            try {
                                queueItem.get(i).save();
                                IncrementSaleOrderCount(queueItem.get(i).getInt("total_item_count"),
                                        queueItem.get(i).getInt("total_item_type_count"),
                                        chargedAmount,
                                        actualProfit,
                                        dSculpTotal,
                                        ccCost
                                );
                            } catch (Exception er) {

                            }

                        } else {
                            System.out.println("  -------------- Order completion Failed !! -------------------");
                            LogWrite.Write(Util.GetDateTime() + ":  ORDER COMPLETION FAILED OrderID:" + queueItem.get(i).getObjectId());
                        }
                        //  } //------------ COUNTRY TESET - 5
                    } catch (Exception er) {
                        System.out.println("  -------------- Order completion Failed !! -------------------");
                        queueItem.get(i).put("executioner", "none");
                        queueItem.get(i).put("status", 10);
                        LogWrite.Write(Util.GetDateTime() + ": status=10, ORDER COMPLETION FAILED OrderID:" + queueItem.get(i).getObjectId());
                        LogWrite.Write(Util.GetDateTime() + ": Reason:" + er.getMessage());
                        // Add to the parse order cancelled

                        ParseObject cancelledOrder = new ParseObject("Cancelled_Order");
                        cancelledOrder.put("order_id", queueItem.get(i));
                        cancelledOrder.put("error_message", er.getMessage());
                        try {
                            cancelledOrder.save();
                        } catch (Exception erp) {
                            System.out.println(" Error in Category count " + erp.getMessage());
                        }

                        try {
                            queueItem.get(i).save();
                        } catch (Exception ex) {

                        }
                    }
                    /*
                     String vidId = queueItem.get(i).getString("videoId");
                     String title = queueItem.get(i).getString("title");

                     // Perform the work -----------------
                     if (vidId == null || vidId.equals("")) {
                     return;
                     }

                     try {

                     Process p = Runtime.getRuntime().exec(SharedData.BaseCommandExtractAudio + vidId + ".mp3"
                     + " https://www.youtube.com/watch?v=" + vidId, null, new File(SharedData.RootStoragePath));
                     // 10 min vid = qYILKnaVPgw
                     // 3.5 mins = d3bUg8wsgVE
                     p.waitFor();

                     // Now upload the file to Parse.
                     Path path = Paths.get(SharedData.RootStoragePath + vidId + ".mp3");
                     ParseOperation.UploadFileAndDeleteQueueEntry(queueItem.get(i), Files.readAllBytes(path));
                     String det = path.toString();
                     File storedFile = new File(SharedData.RootStoragePath + vidId + ".mp3");
                     System.out.println("Finished:" + title + "\n \t \t, state, idle:" + SharedData.TaskIdleCount +
                     ", completed:" + SharedData.TaskCompletedCount);
                     storedFile.delete();
                     } catch (Exception ex) {
                     ServerOperations.UploadError(ex.getMessage() + "--- i= " + i + " + , queueItem.size= " + queueItem.size(), "WorkerThread (queueItem loop)");

                     }
                     */
                    // ------------------------
                } // for each retrieved item.
            } else {
                ServerConfig.TaskIdleCount++;
            }

            Thread.sleep(ServerConfig.WaitAfterWorkComplete);

        } catch (InterruptedException e) {
            ServerOperations.UploadError(e.getMessage(), "WorkerThread (processCommand())");

        }
    }

    @Override
    public String toString() {
        return this.myCompleteId;
    }
}
