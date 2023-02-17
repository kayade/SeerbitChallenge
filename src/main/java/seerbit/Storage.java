package seerbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Storage {

    private static volatile Storage st = null;
    //the container for the transactions
    private Map<BigDecimal, LocalDateTime> trans = null;

    private Storage() {

        //Prevent form the reflection api.
        if (st != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        //initializes the container. This container allows for thread safe concurrent access to all the transactions
        trans = new ConcurrentHashMap<>();
    }

    //creates or retrieves a single Storage object
    public static Storage getStorage() {

        //Double check locking pattern
        if (st == null) { //Check for the first time

            synchronized (Storage.class) {   //Check for the second time.

                //if there is no instance available... create new one
                if (st == null) {

                    st = new Storage();
                }
            }
        }

        return st;
    }

    //method that stores transactions
    public int storeTransaction(String input) {

        int res = 0;

        ObjectMapper objectMapper = new ObjectMapper();

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral("T")
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .appendPattern("z")
                .toFormatter();

        try {

            JsonNode actualObj = objectMapper.readTree(input);

            BigDecimal amt = new BigDecimal("0.0");
            LocalDateTime lDt = LocalDateTime.now();

            String amount = actualObj.at("/amount").asText();
            String timestamp = actualObj.at("/timestamp").asText();

            if (amount == null || timestamp == null) {

                res = 400;
                return res;

            } else if (amount.isEmpty() || timestamp.isEmpty()) {

                res = 400;
                return res;
            }

            try {

                amt = new BigDecimal(amount);
                lDt = LocalDateTime.parse(timestamp, formatter);

            } catch (Exception ex2) {

                System.out.println("Exception:" + ex2);

                res = 422;
                return res;
            }

            res = 201;

            if (LocalDateTime.now().isBefore(lDt)) {

                res = 422;

            } else if (LocalDateTime.now().minusSeconds(31).isAfter(lDt)) {

                res = 204;
            }

            if (res == 201 || res == 204) {

                this.trans.put(amt, lDt);
            }

        } catch (JsonProcessingException ex) {

            System.out.println("JSON:" + ex);

            res = 400;
            return res;

        } catch (Exception e) {

            System.out.println("Exception:" + e);

            res = 400;
            return res;
        }

        return res;
    }

    //method that calculates the sum of all the transactions within a 30 second window
    public BigDecimal getSum() {

        BigDecimal res = new BigDecimal("0.0");

        BigDecimal amt = new BigDecimal("0.0");
        LocalDateTime lDt = LocalDateTime.now();

        for (Map.Entry<BigDecimal, LocalDateTime> entry : trans.entrySet()) {

            System.out.println(entry.getKey() + "/" + entry.getValue());

            amt = entry.getKey();
            lDt = entry.getValue();

            if (LocalDateTime.now().minusSeconds(31).isBefore(lDt)) {

                res = res.add(amt);
            }
        }

        return res;
    }

    //method that calculates the avaerage of all the transactions within a 30 second window
    public BigDecimal getAverage() {

        BigDecimal res = new BigDecimal("0.0");
        int av = 0;

        BigDecimal amt = new BigDecimal("0.0");
        LocalDateTime lDt = LocalDateTime.now();

        for (Map.Entry<BigDecimal, LocalDateTime> entry : trans.entrySet()) {

            amt = entry.getKey();
            lDt = entry.getValue();

            if (LocalDateTime.now().minusSeconds(31).isBefore(lDt)) {

                res = res.add(amt);
                av++;
            }
        }

        if (av > 0) {

            BigDecimal av2 = new BigDecimal(av);

            res = res.divide(av2);
        }

        return res;
    }

    //method that returns the highest transaction within a 30 second window
    public BigDecimal getMax() {

        BigDecimal res = new BigDecimal("0.0");

        BigDecimal amt = new BigDecimal("0.0");
        LocalDateTime lDt = LocalDateTime.now();

        for (Map.Entry<BigDecimal, LocalDateTime> entry : trans.entrySet()) {

            amt = entry.getKey();
            lDt = entry.getValue();

            if (LocalDateTime.now().minusSeconds(31).isBefore(lDt)) {

                if (amt.compareTo(res) > 0) {
                    res = amt;
                }
            }
        }

        return res;
    }

    //method that returns the lowest transaction within a 30 second window
    public BigDecimal getMin() {

        BigDecimal res = new BigDecimal("0.0");
        int r = 0;

        BigDecimal amt = new BigDecimal("0.0");
        LocalDateTime lDt = LocalDateTime.now();

        for (Map.Entry<BigDecimal, LocalDateTime> entry : trans.entrySet()) {

            amt = entry.getKey();
            lDt = entry.getValue();

            if (LocalDateTime.now().minusSeconds(31).isBefore(lDt)) {

                if(r == 0){

                    res = amt;
                    r++;
                }

                if (res.compareTo(amt) > 0) {
                    res = amt;
                }
            }
        }

        return res;
    }

    //method that returns the count of all transaction within a 30 second window
    public long getCount() {

        long ct = 0;

        LocalDateTime lDt = LocalDateTime.now();

        for (Map.Entry<BigDecimal, LocalDateTime> entry : trans.entrySet()) {

            lDt = entry.getValue();

            if (LocalDateTime.now().minusSeconds(31).isBefore(lDt)) {

                ct++;
            }
        }

        return ct;
    }

    //method that returns statistics based on the transactions that happened in the last 30 second window
    public String getStatistics() {

        String res = "";

        BigDecimal sum = this.getSum();
        BigDecimal avg = this.getAverage();
        BigDecimal max = this.getMax();
        BigDecimal min = this.getMin();
        long count = this.getCount();

        res = "{\"sum\":\"" + String.valueOf(sum)
                + "\", \"avg\":\"" + String.valueOf(avg)
                + "\", \"max\":\"" + String.valueOf(max)
                + "\", \"min\":\"" + String.valueOf(min)
                + "\", \"count\":" + String.valueOf(count) + "}";

        return res;
    }

    //method that deletes all transactions in storage
    public void deleteTransactions() {

        trans = new ConcurrentHashMap<>();
    }

}