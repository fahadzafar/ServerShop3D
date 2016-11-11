/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONArray;
import org.json.JSONObject;
import org.shop3d.util.MultipartUtility;
import org.shop3d.util.SharedData;

/**
 *
 * @author Fahad
 */
public class Req_DesignUpload extends Req_Base {

    public Req_DesignUpload(RunMode iRunMode) {
        super(iRunMode, "upload_design/a/3D/");

    }

    public JSONObject SendPostRequest(String filename, String filenameWithPath, String SculpteoName, String SculpteoDescription, String listId, String scale
    , String modelRot) {
        
        
        
        // ---
        
         /*
     *  fix for
     *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
     *       sun.security.validator.ValidatorException:
     *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
     *               unable to find valid certification path to requested target
     */
    TrustManager[] trustAllCerts = new TrustManager[] {
       new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

          public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

       }
    };
   try {
    SSLContext sc = SSLContext.getInstance("SSL");
     
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    // Create all-trusting host name verifier
    HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
    };
    // Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    
    
    } catch (Exception ex) {
            Logger.getLogger(Req_DesignUpload.class.getName()).log(Level.SEVERE, null, ex);
        }
    /*
     * end of the fix
     */

        // -----------------
        JSONObject result = null;
        try {

            Path ZipPath = Paths.get(filenameWithPath);
            byte[] ZipData = Files.readAllBytes(ZipPath);
            ZipData = Base64.encodeBase64(ZipData);
            String stringData = new String(ZipData);

            String sendRot = "0,0,0";
            if (modelRot != null) {
                sendRot = modelRot;
            }
             // Ovverride since we need to upload using https, not http
            // as Req_Base only creates the default URLPost with http.
            URLPost = "https://www.sculpteo.com/en/" + WebAPIDIR;

            result = webb
                    .post(URLPost)
                    .header("X-Requested-With", "XMLHttpRequest")
              
                    .param("name", SculpteoName)
                    .param("description", SculpteoDescription)
                    .param("designer", SharedData.Sculpteo_Username)
                    .param("password", SharedData.Sculpteo_Password)
                    .param("list", listId)
                    .param("file", stringData)
                    .param("filename", filename + ".zip")
                    .param("unit", SharedData.Sculpteo_Units)
                   // .param("sizes", scale)
                    .param("rotation",modelRot)
                    .param("materials", SharedData.Sculpteo_PrintMaterial)
                    .asJsonObject()
                    .getBody();

   
        } catch (IOException ex) {
            System.err.println(ex);
        }

        return result;
    }
}
