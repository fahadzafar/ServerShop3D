/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shop3d.server.main;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Fahad
 */
public class ServerMain {
    
    
    
    public static void start(){
        
        
        ServerOperations.ClearAllQueueExecutioners();
        
        // Threading logic that runs infinitely
        //ServerConfig.Cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool( 1);
        
        
         // Start the server logger
        try {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    ServerOperations.LogServerData();
                    ServerConfig.TaskCompletedCount = 0;

                    ServerConfig.TaskIdleCount = 0;

                   
                }
            }, 0, ServerConfig.LogServerPerformanceInterval);
        } catch (Exception er) {

        }

        
        
          // Counter that launches the get job and execute queue. 
        int i = 0;
        while (true) {
            long startTime = System.nanoTime();

            Runnable worker = new WorkerThread_FulfillOrder(i);
            executor.execute(worker);
            try {
                Thread.sleep(ServerConfig.WaitBeforeGetNextWork);
                i++;
                if (i == Integer.MAX_VALUE) {
                    i = 0;
                    // Henche executioner is the process id that executed a task.
                }

                long endTime = System.nanoTime();
                long duration = (endTime - startTime);
                // System.out.println(SharedData.baseId+ " Launch Start-End. TTE=" + (duration / 1000000000) + " secs"
        //);

            } catch (Exception er) {
//                ParseOperation.UploadError(er.getMessage() + "--- i= " + i, "yt_ExtractServer");
            }
        }
        
        
    }
    
}
