package Utilities;

import java.time.LocalDate;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static sun.rmi.transport.TransportConstants.Return;

/**
 * Created by scapista on 9/1/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class Utilities {
    public static boolean isSameDay (Date start_date, Date end_date){

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(start_date);
        cal2.setTime(end_date);

        return  cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    public static boolean isBeforeToday(String date){
        LocalDate today = LocalDate.now();
        LocalDate localDate = LocalDate.parse(date);

        return localDate.isBefore(today);
    }
    public static ArrayList<String> getDaysBetweenDates(String startdate, String enddate)
    {
        LocalDate start = LocalDate.parse(startdate);
        LocalDate end = LocalDate.parse(enddate);
        ArrayList<String> totalDates = new ArrayList<String>();
        while (!start.isAfter(end)) {
            totalDates.add(start.toString());
            start = start.plusDays(1);
        }
        return totalDates;
    }
    private static String defaultString (String str, String defaultValue){
        if (str == null)
            return defaultValue;
        else
            return str;
    }
}
