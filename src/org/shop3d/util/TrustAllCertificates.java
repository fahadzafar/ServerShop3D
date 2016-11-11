/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shop3d.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author Fahad
 */
  public class TrustAllCertificates implements X509TrustManager, HostnameVerifier {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

        /**
         * Installs a new {@link TrustAllCertificates} as trust manager and
         * hostname verifier.
         */
        public static void install() {
            try {
                TrustAllCertificates trustAll = new TrustAllCertificates();

                // Install the all-trusting trust manager
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null,
                        new TrustManager[]{trustAll},
                        new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(trustAll);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed setting up all thrusting certificate manager.", e);
            } catch (KeyManagementException e) {
                throw new RuntimeException("Failed setting up all thrusting certificate manager.", e);
            }
        }
    }
