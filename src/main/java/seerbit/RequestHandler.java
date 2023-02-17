package seerbit;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements HttpHandler {

    // A singleton class that stores the transactions
    private Storage st = null;
    String payload = "";
    int resp = 0;

    @Override
    public void handle(HttpExchange he) {

        System.out.println("Serving the request");

        st = Storage.getStorage();

        try {

            // Serve for POST requests only
            if (he.getRequestMethod().equalsIgnoreCase("POST")) {

                System.out.println("RequestHandler:handle:POST:IN");

                try {

                    // REQUEST Headers
                    Headers requestHeaders = he.getRequestHeaders();
                    Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();

                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));

                    // REQUEST Body
                    InputStream is = he.getRequestBody();

                    byte[] data = new byte[contentLength];
                    int length = is.read(data);

                    payload = new String(data);

                    // This stores the transaction
                    resp = st.storeTransaction(payload);

                    // RESPONSE Headers
                    Headers responseHeaders = he.getResponseHeaders();

                    // Send RESPONSE Headers
                    he.sendResponseHeaders(resp, 0);

                    he.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Serve for DELETE requests only
            if (he.getRequestMethod().equalsIgnoreCase("DELETE")) {

                System.out.println("RequestHandler:handle:DELETE:IN");

                try {

                    st.deleteTransactions();

                    // REQUEST Headers
                    he.sendResponseHeaders(204, 0);

                    he.close();

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {

            System.out.println("RequestHandler:handle:" + ex);
        }
    }

}
