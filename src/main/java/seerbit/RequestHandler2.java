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

public class RequestHandler2 implements HttpHandler {

    // A singleton class that stores the transactions
    private Storage st = null;
    String payload = "";
    int resp = 0;

    @Override
    public void handle(HttpExchange he) {

        System.out.println("Serving the request");

        st = Storage.getStorage();

        try {

            // Serve for GET requests only
            if (he.getRequestMethod().equalsIgnoreCase("GET")) {

                System.out.println("RequestHandler:handle:GET:IN");

                try {

                    //Retrieves the statistics for the past 30 seconds
                    payload = st.getStatistics();

                    byte[] data = payload.getBytes();
                    int contentLength = data.length;

                    // REQUEST Headers
                    he.getResponseHeaders().set("Content-Type", "application/json");
                    he.sendResponseHeaders(200, contentLength);

                    // RESPONSE Body
                    OutputStream os = he.getResponseBody();

                    os.write(data);

                    he.close();

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {

            System.out.println("RequestHandler2:handle:" + ex);
        }
    }

}