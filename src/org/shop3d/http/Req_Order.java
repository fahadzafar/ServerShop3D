/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.shop3d.http;

import com.android.internal.util.Protocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsslutils.sslcontext.PKIXSSLContextFactory;
import org.parse4j.ParseObject;
import org.shop3d.util.CustomHttpClient;
import org.shop3d.util.MySSLSocketFactory;
import org.shop3d.util.ServerCallbackWrappingTrustManager;
import org.shop3d.util.ServerCallbackWrappingTrustManager.CheckServerTrustedCallback;
import org.shop3d.util.SharedData;
import org.shop3d.util.TrustAllCertificates;
import org.shop3d.util.Util;
import org.shop3d.util.WebClientDevWrapper;

public class Req_Order extends Req_Base {

//-----------------------
    public Req_Order(Req_Base.RunMode iRunMode) {
        super(iRunMode, "api/store/3D/order/");

    }

    public static String Call(String URL, List<NameValuePair> postParameters) {
        BufferedReader in = null;
        DefaultHttpClient httpClient = null;
        StringBuffer sb = new StringBuffer();
        try {

            SSLContext sslContext = SSLContext.getInstance("SSL");
            try {
// set up a TrustManager that trusts everything
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        // System.out.println("getAcceptedIssuers =============");
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs,
                            String authType) {
                        //   System.out.println("checkClientTrusted =============");
                    }

                    public void checkServerTrusted(X509Certificate[] certs,
                            String authType) {
                        // System.out.println("checkServerTrusted =============");
                    }
                }}, new SecureRandom());

                SSLSocketFactory sf = new SSLSocketFactory(sslContext);
                Scheme httpsScheme = new Scheme("https", 443, sf);
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(httpsScheme);

// apache HttpClient version >4.2 should use BasicClientConnectionManager
                ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);
                httpClient = new DefaultHttpClient(cm);
            } catch (Exception er) {
            }

            // httpClient =               CustomHttpClient.getHttpClient();
            HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false); //making 3G network works*
            HttpPost request = new HttpPost(URL);
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(formEntity);

            HttpResponse response = httpClient.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);

                String[] tokens = line.split(" ");
                if (tokens.length > 1) {
                    System.out.println(" ERROR: " + line);
                    return null;
                } else if (tokens.length == 1) {
                    // System.out.println("NEW ORDER ID: " + line);
                    return line;
                } else {
                    // System.out.println("NOTHING RETURNED - ORDER ");
                    return null;
                }

            }
            in.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            int y = 0;
        }
        return sb.toString();
    }

    public String SendPostRequest(ParseObject shipAdd, ParseObject shipRates, ArrayList<String> items, String sNormExpress) {

            // ---
        String result = "";

        // test();
        String country = shipAdd.getString("country");
        String[] countrySplitNameAndCode = country.split(",");
        country = countrySplitNameAndCode[countrySplitNameAndCode.length - 1];

        URLPost = "https://www.sculpteo.com/en/api/store/3D/order/";

        try {
            /*
             channel = channel.open();
             boolean isConnected = channel.connect(new InetSocketAddress(uri.getHost(), port));
             */

            List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            postParameters.add(new BasicNameValuePair("login", SharedData.Sculpteo_Username));
            postParameters.add(new BasicNameValuePair("password", SharedData.Sculpteo_Password));

            
            
            // ORDER FAKEEEEEEEEEEEEEEEEEEEEEE
            if (SharedData.SCULPTEO_FAKE_ORDER) {
                postParameters.add(new BasicNameValuePair("fake", "1"));
            }

            postParameters.add(new BasicNameValuePair("theme", "3dshop"));

            postParameters.add(new BasicNameValuePair("ship_addressee", shipAdd.getString("address_name")));
            postParameters.add(new BasicNameValuePair("ship_street1", shipAdd.getString("address_line_1")));
            postParameters.add(new BasicNameValuePair("ship_street2", shipAdd.getString("address_line_2")));
            postParameters.add(new BasicNameValuePair("ship_city", shipAdd.getString("city")));
            postParameters.add(new BasicNameValuePair("ship_state", shipAdd.getString("state")));
            postParameters.add(new BasicNameValuePair("ship_postal_code", shipAdd.getString("zipcode")));
            postParameters.add(new BasicNameValuePair("ship_country", country));

            // Regular or express type shipping.
            if (sNormExpress.equals("default") == false) {
                postParameters.add(new BasicNameValuePair("ship_method", sNormExpress));
            }

            for (int i = 0; i < items.size(); i++) {
                postParameters.add(new BasicNameValuePair("item[" + i + "]", items.get(i)));
            }
            result = Call(URLPost, postParameters);
// QfGQC4YQ:4:0.8697:in:color_plastic
// YLXNMztE
            /*
             String resultx = webb
             .post(URLPost)
             .param("login", SharedData.Sculpteo_Username)
             .param("password", SharedData.Sculpteo_Password)
             .param("fake", "1")
                    
             .param("ship_addressee", shipAdd.getString("name"))
             .param("ship_street1", shipAdd.getString("address_line_1"))
             .param("ship_street2", shipAdd.getString("address_line_2"))
             .param("ship_city", shipAdd.getString("city"))
             .param("ship_state", shipAdd.getString("state"))
             .param("ship_postal_code", shipAdd.getString("zipcode"))
             .param("ship_country", country)
             // .param("comment", "this is a fake record, do not print")
                    
             .param("item[" + 0 + "]", items.get(0))
                     
             .asString().getResponseMessage();
             System.out.println(resultx);*/
        } catch (Exception er) {
            System.out.println(er.getMessage());
        }
        //.param("ship_method",shipRates.getString("code")) 
                /*
         .asJsonObject()
         .getBody();
         
         for (int i = 0; i < items.size(); i++) {
         webb
         .post(URLPost).param("item[" + i + "]", items.get(i));
         }
         result = webb.post(URLPost).asJsonObject().getBody();
         */
        return result;
    }
}
