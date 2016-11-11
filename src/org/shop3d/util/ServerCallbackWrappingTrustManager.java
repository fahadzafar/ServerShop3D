/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.shop3d.util;

/**
 *
 * @author Fahad
 */

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.UUID;

import javax.net.ssl.X509TrustManager;

import org.jsslutils.sslcontext.X509TrustManagerWrapper;
public class ServerCallbackWrappingTrustManager implements X509TrustManager {
    private final X509TrustManager trustManager;
    private final KeyStore localTrustStore;
    private final CheckServerTrustedCallback callback;

    /**
     * Creates a new instance from an existing X509TrustManager.
     * 
     * @param trustManager
     *            X509TrustManager to wrap.
     * @param callback
     *            {@link CheckServerTrustedCallback} from the user-interface.
     * @param localTrustStore
     *            {@link KeyStore} (loaded) to use as a trust store; use its
     *            store method to save it.
     */
    public ServerCallbackWrappingTrustManager(X509TrustManager trustManager,
            CheckServerTrustedCallback callback, KeyStore localTrustStore) {
        this.trustManager = trustManager;
        this.localTrustStore = localTrustStore;
        this.callback = callback;
    }

    /**
     * Creates a new instance from an existing X509TrustManager.
     * 
     * @param trustManager
     *            X509TrustManager to wrap.
     * @param callback
     *            {@link CheckServerTrustedCallback} from the user-interface.
     * @param localTrustStore
     *            {@link KeyStore} to use as a trust store.
     * @param saveLocalTrustStore
     *            Set to true to save the keystore, otherwise, it will only be
     *            kept in memory.
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public ServerCallbackWrappingTrustManager(X509TrustManager trustManager,
            CheckServerTrustedCallback callback) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        this(trustManager, callback, KeyStore.getInstance(KeyStore
                .getDefaultType()));
        this.localTrustStore.load(null);
    }

    /**
     * Checks that the client is trusted; in this case, it delegates this check
     * to the trust manager it wraps
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        this.trustManager.checkClientTrusted(chain, authType);
    }

    /**
     * Checks that the server is trusted; in this case, it accepts anything.
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        try {
            this.trustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
            try {
                boolean certTrusted = false;
                Enumeration<String> aliases = this.localTrustStore.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = aliases.nextElement();
                    Certificate cert = this.localTrustStore
                            .getCertificate(alias);
                    if (chain[0].equals(cert)) {
                        certTrusted = true;
                        break;
                    }
                }
                if (certTrusted
                        || this.callback.checkServerTrusted(chain, authType)) {
                    this.localTrustStore.setCertificateEntry(UUID.randomUUID()
                            .toString(), chain[0]);
                } else {
                    throw e;
                }
            } catch (KeyStoreException kse) {
                throw new CertificateException(kse);
            }
        }
    }

    /**
     * Returns the accepted issuers; in this case, it's an empty array.
     */
    public X509Certificate[] getAcceptedIssuers() {
        return this.trustManager.getAcceptedIssuers();
    }

    /**
     * Wrapper factory class that wraps existing X509TrustManagers into
     * X509TrustManagers that trust any clients.
     * 
     * @author Bruno Harbulot.
     */
    public static class Wrapper implements X509TrustManagerWrapper {
        private final CheckServerTrustedCallback callback;
        private final KeyStore localTrustStore;

        public Wrapper(CheckServerTrustedCallback callback,
                KeyStore localTrustStore) {
            super();
            this.callback = callback;
            this.localTrustStore = localTrustStore;
        }

        /**
         * Builds an X509TrustManager from another X509TrustManager.
         * 
         * @param trustManager
         *            original X509TrustManager.
         * @return wrapped X509TrustManager.
         */
        public X509TrustManager wrapTrustManager(X509TrustManager trustManager) {
            if (localTrustStore != null) {
                return new ServerCallbackWrappingTrustManager(
                        (X509TrustManager) trustManager, callback,
                        localTrustStore);
            } else {
                try {
                    return new ServerCallbackWrappingTrustManager(
                            (X509TrustManager) trustManager, callback);
                } catch (KeyStoreException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (CertificateException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static interface CheckServerTrustedCallback {
        public boolean checkServerTrusted(X509Certificate[] chain,
                String authType);
    }
}