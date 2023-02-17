package seerbit;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import com.sun.net.httpserver.*;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLContext;

public class SeerbitChallenge {

    //this method starts the REST API server
    //creates both HTTP and HTTPS listeners

    public static void main(String[] args) {

        try {

            System.out.println("Starting:");

            // Bind http to port 5645
            InetSocketAddress inetAddress = new InetSocketAddress(5645);

            HttpServer httpServer = HttpServer.create(inetAddress, 0);

            // Adding '/test' context
            httpServer.createContext("/transactions", new RequestHandler());
            httpServer.createContext("/statistics", new RequestHandler2());

            // Start the server
            httpServer.start();

            // Bind https to port 5646
            InetSocketAddress inetAddress2 = new InetSocketAddress(5646);

            //initialize the HTTPS server
            HttpsServer HTTPS_Server = HttpsServer.create(inetAddress2, 0);
            SSLContext SSL_Context = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] Password = "password".toCharArray();
            KeyStore Key_Store = KeyStore.getInstance("JKS");
            FileInputStream Input_Stream = new FileInputStream("httpskey.jks");
            Key_Store.load(Input_Stream, Password);

            // setup the key manager factory
            KeyManagerFactory Key_Manager = KeyManagerFactory.getInstance("SunX509");
            Key_Manager.init(Key_Store, Password);

            // setup the trust manager factory
            TrustManagerFactory Trust_Manager = TrustManagerFactory.getInstance("SunX509");
            Trust_Manager.init(Key_Store);

            // setup the HTTPS context and parameters
            SSL_Context.init(Key_Manager.getKeyManagers(), Trust_Manager.getTrustManagers(), null);
            HTTPS_Server.setHttpsConfigurator(new HttpsConfigurator(SSL_Context) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext SSL_Context = getSSLContext();
                        SSLEngine SSL_Engine = SSL_Context.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(SSL_Engine.getEnabledCipherSuites());
                        params.setProtocols(SSL_Engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters SSL_Parameters = SSL_Context.getSupportedSSLParameters();
                        params.setSSLParameters(SSL_Parameters);

                        System.out.println("The HTTPS server is connected");

                    } catch (Exception ex) {

                        System.out.println("Failed to create the HTTPS port");
                    }
                }
            });

            HTTPS_Server.createContext("/transactions", new RequestHandler());
            HTTPS_Server.createContext("/statistics", new RequestHandler2());
            HTTPS_Server.setExecutor(null); // creates a default executor
            HTTPS_Server.start();

        } catch (Exception ex) {

            System.out.println("SeerbitChallenge:" + ex);
        }
    }

}