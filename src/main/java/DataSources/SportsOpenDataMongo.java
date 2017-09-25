package DataSources;

import Exceptions.NoMoreApiCallsException;
import Mongo.MongoApi;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.bson.conversions.Bson;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.currentDate;
import static com.mongodb.client.model.Updates.set;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by scapista on 8/31/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class SportsOpenDataMongo{

    private String league = null, team = null, season = null, apiKey = null, round = null;
    private List<Document> lstDBObjectList = new ArrayList<Document>();
    public SportsOpenDataMongo (String inLeague, String inTeam, String inSeason, String inApiKey, String inRound){
        //super(inDatabase,inCollection);
        this.league = inLeague;
        this.team   = inTeam;
        this.season = inSeason;
        this.apiKey = inApiKey;
        this.round  = inRound;
    }
    public void getSeasonStandings(){
        MongoApi standings = new MongoApi("soccer", "standings");
        try {
            String strMashapeGet = "https://sportsop-soccer-sports-open-data-v1.p.mashape.com/v1/leagues/{league_slug}/seasons/{season_slug}/standings";
            strMashapeGet = strMashapeGet.replace("{league_slug}", this.league);
            strMashapeGet = strMashapeGet.replace("{season_slug}", this.season);

            JSONObject jsonObjectResponse = callJsonResponse(strMashapeGet, this.apiKey);
            JSONArray jsonArrayResponse = jsonObjectResponse.getJSONArray("standings");

            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                Document tmpDoc = Document.parse(jsonArrayResponse.getJSONObject(i).toString());
                tmpDoc = tmpDoc
                        .append("league", this.league)
                        .append("season_year", this.season)
                        .append("insert_dt", new Date());

                Bson filter = new Document("league", this.league)
                        .append("season_year", this.season)
                        .append("team", tmpDoc.get("team"));
                Bson update = combine(
                        set("league", tmpDoc.get("league"))
                        ,set("away", tmpDoc.get("away"))
                        ,set("overall", tmpDoc.get("overall"))
                        ,set("home", tmpDoc.get("home"))
                        ,set("position", tmpDoc.get("position"))
                        ,currentDate("lastModified"));
                System.out.println(tmpDoc.get("team"));
                //System.out.println(update.toString());
                standings.upsertOne(filter, update, Boolean.FALSE);
                //lstDBObjectList.add(tmpDoc);
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getSeasonStandings failed");
        }
        finally {
            standings.closeConnection();
        }


    }
    public void getSeasonTeams(){
        MongoApi teams = new MongoApi("soccer", "teams");
        try {
            String strMashapeGet = "https://sportsop-soccer-sports-open-data-v1.p.mashape.com/v1/leagues/{league_slug}/seasons/{season_slug}/teams";
            strMashapeGet = strMashapeGet.replace("{league_slug}", this.league);
            strMashapeGet = strMashapeGet.replace("{season_slug}", this.season);

            JSONObject jsonObjectResponse = callJsonResponse(strMashapeGet, this.apiKey);
            JSONArray jsonArrayResponse = jsonObjectResponse.getJSONArray("teams");

            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                Document tmpDoc = Document.parse(jsonArrayResponse.getJSONObject(i).toString());
                tmpDoc = tmpDoc
                        .append("league", this.league)
                        .append("season_year", this.season)
                        .append("insert_dt", new Date());
                lstDBObjectList.add(tmpDoc);
            }
            teams.insertMany(lstDBObjectList,Boolean.TRUE);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void getSeasonRound(){
        /*
        1. need stadium that the game was played in
        2. need refferee that reffed the match
        3.
         */
        MongoApi teams = new MongoApi("soccer", "seasonRounds");
        try {
            String strMashapeGet = "https://sportsop-soccer-sports-open-data-v1.p.mashape.com/v1/leagues/{league_slug}/seasons/{season_slug}/rounds/{round_slug}";
            strMashapeGet = strMashapeGet.replace("{league_slug}", this.league);
            strMashapeGet = strMashapeGet.replace("{season_slug}", this.season);
            strMashapeGet = strMashapeGet.replace("{round_slug}" , this.round );

            JSONObject jsonObjectResponse = callJsonResponse(strMashapeGet, this.apiKey);
            JSONArray jsonArrayResponse = jsonObjectResponse.getJSONArray("rounds");

            for (int i = 0; i < jsonArrayResponse.length(); i++) {
                Document tmpDoc = Document.parse(jsonArrayResponse.getJSONObject(i).toString());
                tmpDoc = tmpDoc
                        .append("league", this.league)
                        .append("season_year", this.season)
                        .append("insert_dt", new Date());
                lstDBObjectList.add(tmpDoc);
            }
            teams.insertMany(lstDBObjectList,Boolean.TRUE);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public JSONObject callJsonResponse(String strMashapeGet, String apiKey){
        HttpResponse<JsonNode> response = null;
        try {
            MongoApi mongo_connection = new MongoApi("soccer", "apiCalls");
            Bson filter = new Document("_id", "SportsOpenData");
            Bson projection = new Document("Calls", 1)
                    .append("lastModified",1);

            if (getSportsOpenDataAPICalls(filter, projection, Boolean.FALSE, mongo_connection)) {

                response = Unirest.get(strMashapeGet)
                        .header("X-Mashape-Key", apiKey)
                        .header("Accept", "application/json")
                        .asJson();

                setApiCalls(Integer.parseInt(
                        response.getHeaders()
                                .get("X-RateLimit-requests-Remaining")
                                .toString()
                                .replaceAll("[^\\d.]", ""))
                        , filter
                        , Boolean.TRUE
                        , mongo_connection);
            }
            else throw new NoMoreApiCallsException();
        }
        catch(NoMoreApiCallsException ex){
            System.out.println("No more API calls before limit");
        }
        catch (Exception e) {
            //System.out.println(response.getStatus());
            //System.out.println("headers -> " + response.getHeaders().toString());
            //System.out.println("body -> " + response.getBody().toString());
            System.out.println(e.toString());
            System.out.println("callJsonResponse failed");
        }
        return response.getBody().getObject().getJSONObject("data");
    }

    public void setRound(String round){
        this.round = round;
    }

    private boolean getSportsOpenDataAPICalls (Bson filter, Bson projection, Boolean Close, MongoApi mongo_connection){
        int apiCallCnt = 0;
        Date currentDate = new Date();
        Date lastCallDt = null;
        try {
            Document doc = mongo_connection.getMongoDocument(filter, projection, Close);
            apiCallCnt = doc.getInteger("Calls");
            lastCallDt = doc.getDate("lastModified");

            System.out.println(apiCallCnt + " API Calls Left");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("getSportsOpenDataAPICalls failed");
        }
        return apiCallCnt > 0 || Utilities.Utilities.isSameDay(lastCallDt, currentDate);
    }

    private void setApiCalls(Integer intCalls, Bson filter,  Boolean Close, MongoApi mongo_connection){
        Bson update = combine(set("Calls", intCalls),currentDate("lastModified"));
        mongo_connection.upsertOne( filter, update, Close);
    }



}
