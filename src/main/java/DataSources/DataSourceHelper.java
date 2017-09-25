package DataSources;

import Mongo.MongoApi;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import static Utilities.Utilities.isBeforeToday;

/**
 * Created by scapista on 9/11/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class DataSourceHelper {

    public String getJsonString (String strUrl, int waitTime){
        StringBuilder jsonBuilder = new StringBuilder();
        try {
            URL url = new URL(strUrl);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            for (String line; (line = reader.readLine()) != null; ) {
                jsonBuilder.append(line.toString());
            }
            Thread.sleep(waitTime);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(strUrl);
            System.out.println("failed in getJsonString");
        }
        return jsonBuilder.toString();
    }
}
