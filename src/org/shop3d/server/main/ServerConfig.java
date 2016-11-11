/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shop3d.server.main;

/**
 *
 * @author Fahad
 */
public class ServerConfig {
      // These values are set through the settings.
    public static int Cores= 1;
    public static int WaitBeforeGetNextWork = 15000;  // in millisecs
    public static int WorkBatchCount = 1;
    public static int WaitAfterWorkComplete = 15000;
    
    // Time after which the server logs work.
    public static int LogServerPerformanceInterval = 3600000;
    public static int LogServerGetSettingsInterval = 3600000;
    
 
    public static float TaskIdleCount = 0;
    public static float TaskCompletedCount = 0;
   
}
