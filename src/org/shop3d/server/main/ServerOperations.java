/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.server.main;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.parse4j.ParseException;
import org.parse4j.ParseObject;
import org.parse4j.ParseQuery;
import org.shop3d.util.SharedData;
import org.shop3d.util.Util;

/**
 *
 * @author Fahad
 */
public class ServerOperations {

    public static List<ParseObject> GetOrderItems(ParseObject orderMain) {
        List<ParseObject> doMe = null;
        try {
            ParseQuery<ParseObject> requests = new ParseQuery("Order_Item");
            requests.whereEqualTo("order_id", orderMain);
            //  requests.include("model_id");
            //  requests.include("usermade_model_id");

            doMe = requests.find();
        } catch (Exception er) {
            System.out.println("Exception: " + er.getMessage());
        }
        return doMe;
    }

    public static List<ParseObject> GetWork(String workerId) {
        try {
            // Get the top requets that have no executioner.
            ParseQuery<ParseObject> requests = new ParseQuery("Order");
            requests.orderByDescending("-createdAt");
            //requests.orderByAscending("createdAt");
            // requests.addAscendingOrder("createdAt");

            requests.whereEqualTo("status", 1);

            requests.limit(ServerConfig.WorkBatchCount);
            List<ParseObject> doMe = new ArrayList<ParseObject>();
            doMe = requests.find();

            if (doMe != null) {
                // Set all the requets you got to have the executioner as yourself.
                for (int i = 0; i < doMe.size(); i++) {
                    doMe.get(i).put("executioner", workerId);
                    doMe.get(i).save();
                }

                // Now fetch work that has your name as executioner.
                ParseQuery<ParseObject> getMyWork = new ParseQuery("Order");

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

    //----------------
    // status = 1 : "PAID"
    // status = 2 : "Placed to Sculpteo"
    // status = 10 : "Cancelled"
    //---------------------------------------------------
    public static void ClearAllQueueExecutioners() {
        ParseQuery<ParseObject> requests = new ParseQuery("Order");
        requests.whereEqualTo("status", 1);
        List<ParseObject> doMe = new ArrayList<ParseObject>();
        try {
            doMe = requests.find();
            if (doMe != null) {
                for (int i = 0; i < doMe.size(); i++) {
                    doMe.get(i).put("executioner", "none");
                    doMe.get(i).save();
                }
                System.out.println("All orders with status=1, set executioner=none");
                WorkerThread_FulfillOrder.LogWrite.Write(Util.GetDateTime() + ": ServerStarted: " + SharedData.baseIdx + " -All orders with status=1, set executioner=none");
            } else {
                System.out.println("No orders found with status=1");
                 WorkerThread_FulfillOrder.LogWrite.Write(Util.GetDateTime() + ": ServerStarted: " + SharedData.baseIdx + " -No orders found with status=1");

            }
        } catch (ParseException ex) {
            System.out.println("Error in clearallQueneExecutioner: " + ex.getMessage());
        }

    }

    public static void UploadError(String exceptionMessage, String className) {
        /*
         ParseObject completedErrorEntry = new ParseObject("Errors");
         completedErrorEntry.put("executioner", SharedData.baseIdx);
         completedErrorEntry.put("error_class_name", className);
         completedErrorEntry.put("exception_message", exceptionMessage);
         completedErrorEntry.saveInBackground();*/
    }

    public static void LogServerData() {/*
         ParseObject completedErrorEntry = new ParseObject("Logs");
         float total = ServerConfig.TaskCompletedCount + ServerConfig.TaskIdleCount;
         boolean totalIsZero = false;
         if (total == 0) {
         total = 1;
         totalIsZero = true;
         }
         completedErrorEntry.put("executioner", SharedData.baseIdx);
         completedErrorEntry.put("idle", ServerConfig.TaskIdleCount / total * 100);
         completedErrorEntry.put("completed", ServerConfig.TaskCompletedCount / total * 100);

         if (totalIsZero) {
         total = 0;
         }
         completedErrorEntry.put("total", total);

         completedErrorEntry.saveInBackground();*/

    }
}
