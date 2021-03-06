/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.http;

import com.goebl.david.Webb;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.NameValuePair;

/**
 *
 * @author Fahad
 */
public class Req_Base {

    RunMode CurrentRunMode;
    String URLBaseSculpteo;
    String URLPost;
    String WebAPIDIR;
    private final String USER_AGENT = "Shop3D";
    private final String SANDBOX_ID = "I1Pjyb6Rtkc%3D";
    private final String UserLogin = "";
    private final String UserPassword = "";

    Webb webb;

    public Req_Base(RunMode iRunMode, String iWebApiDir) {
        
        webb = Webb.create();  
        WebAPIDIR = iWebApiDir;
        // Set the correct URL for TEST or Production
        if (iRunMode == RunMode.PRODUCTION) {
            URLBaseSculpteo = "http://www.sculpteo.com/en/";
            URLPost = URLBaseSculpteo + WebAPIDIR;
        } else {
            URLBaseSculpteo = "http://sandbox.sculpteo.com/en/";
            URLPost = URLBaseSculpteo + WebAPIDIR + "?sandbox="+SANDBOX_ID;
        }
    }

    /*
    public JSONArray SendPostRequest() {
        JSONArray result = webb
                .get(URLPost)
                .retry(1, false) // at most one retry, don't do exponential backoff
                .asJsonArray()
                .getBody();

        String data = result.toString();
        return result;
    }
*/
    // Used to figure out if running in the sandbox environment or production
    public enum RunMode {

        TEST,
        PRODUCTION
    }

}
