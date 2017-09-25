package DataSources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import Mongo.MongoApi;
import org.bson.conversions.Bson;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.Doc;

/**
 * Created by scapista on 9/3/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class SportsRadar extends DataSourceHelper{
    String apiKey = "3e9v32f84446fu2bhz4h7437";
    int waitTime = 1500;

    public void getSchedule(String tournamentKey){
        String strUrl = "https://api.sportradar.us/soccer-t3/eu/en/tournaments/{tournament}/schedule.json?api_key={your_api_key}";
        strUrl = strUrl.replace("{your_api_key}", apiKey);
        strUrl = strUrl.replace("{tournament}", tournamentKey);
        String jsonString = getJsonString(strUrl, this.waitTime);
        MongoApi tournament = new MongoApi("soccer", "tournament");
        try {
            JSONObject sport_events = new JSONObject(jsonString);
            JSONArray jsonArrayResponse = sport_events.getJSONArray("sport_events");
            ArrayList<Document> tournamentDocs = new ArrayList<Document>();
            Bson filter = null;
            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                Document sport_event = Document.parse(jsonArrayResponse.get(i).toString());
                filter = new Document("tournament.id",tournamentKey);
                sport_event = sport_event
                        .append("_id", sport_event.getString("id"))
                        .append("insert_dt", new java.util.Date() );
                tournamentDocs.add(sport_event);
            }
            tournament.deleteMany(filter,Boolean.FALSE);
            tournament.insertMany(tournamentDocs,Boolean.FALSE);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            tournament.closeConnection();
        }
    }
    public void getMatchSummary(String matchId){
        String strUrl = "https://api.sportradar.us/soccer-xt3/eu/en/matches/{match_id}/summary.json?api_key={your_api_key}";
        strUrl = strUrl.replace("{your_api_key}", apiKey);
        strUrl = strUrl.replace("{match_id}", matchId);
        try {
            String jsonString = getJsonString(strUrl, this.waitTime);
            Document matchSummary = Document.parse(jsonString);
            Document matchEvents = new Document ("_id", matchId)
                    .append("sport_event",matchSummary.get("sport_event"))
                    .append("sport_event_conditions",matchSummary.get("sport_event_conditions"))
                    .append("sport_event_status", matchSummary.get("sport_event_status"))
                    .append("statistics",matchSummary.get("statistics"));
            MongoApi tournament = new MongoApi("soccer", "match_summaries");
            tournament.insertOne(matchEvents, Boolean.TRUE);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void getMatchEvents(String matchId){
        String strUrl = "https://api.sportradar.us/soccer-xt3/eu/en/matches/{match_id}/timeline.json?api_key={your_api_key}";
        strUrl = strUrl.replace("{your_api_key}", apiKey);
        strUrl = strUrl.replace("{match_id}", matchId);
        try {
            String jsonString = getJsonString(strUrl, this.waitTime);
            Document tmpDoc      = Document.parse(jsonString);

            Document matchEvents = new Document ("_id", matchId)
                    .append("sport_event",tmpDoc.get("sport_event"))
                    .append("sport_event_status",tmpDoc.get("sport_event_status"))
                    .append("coverage_info", tmpDoc.get("coverage_info"))
                    .append("timeline",tmpDoc.get("timeline"));

            MongoApi tournament = new MongoApi("soccer", "match_events");
            tournament.insertOne(matchEvents, Boolean.TRUE);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
