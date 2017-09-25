package DataSources;

import Mongo.MongoApi;

import org.bson.conversions.Bson;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;
import static sun.plugin.javascript.navig.JSType.URL;

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

public class SportMonks extends DataSourceHelper{
    //String apiKey = "b06IxSJPMghoSFlfAV4T8QSPglp9m9bAj1OjrXx7JFKn4Z4yLGxbtlJe5EUU";
    String apiKey = "VNmyjpTdo8TkKEPbQTqjuok29ZRW9wOYvnzyitOdT4lpKiXtf9bxGHdPmhPs";

    String mongoDataBase = "soccer_sm";
    String premierLeagueID = "8";

    public void getSeasons(){
        getSportMonkFullRefresh("https://soccer.sportmonks.com/api/v2.0/seasons?api_token={your_api_key}","seasons");
    }

    public void getLeagues(){
        getSportMonkFullRefresh("https://soccer.sportmonks.com/api/v2.0/leagues?api_token={your_api_key}","leagues");
    }

    public void getRound(){
        String strURL = "https://soccer.sportmonks.com/api/v2.0/rounds/season/{id}?api_token={your_api_key}";
        strURL = strURL.replace("{id}", "6397");
        getSportMonkFullRefresh(strURL, "rounds");
    }

    public void getRoundFixtures(String startDate, String endDate){
        String strURL = "https://soccer.sportmonks.com/api/v2.0/fixtures/between/{from}/{to}?api_token={your_api_key}";
        strURL = strURL.replace("{from}", startDate);
        strURL = strURL.replace("{to}", endDate);
        getSportMonkInsert(strURL,"fixtures");
    }

    public void getTeams(String teamId){
        String strURL = "https://soccer.sportmonks.com/api/v2.0/teams/season/{id}?api_token={your_api_key}&include=stats";
        strURL = strURL.replace("{id}", teamId);
        getSportMonkInsert(strURL,"teams");
    }

    public void getDateFixtures(String matchDate){
        String strURL = "https://soccer.sportmonks.com/api/v2.0/fixtures/date/{date}?api_token={your_api_key}";
        strURL = strURL.replace("{date}", matchDate);
        getSportMonkInsert(strURL,"fixtures");
    }
    public void getCommentary(String matchID){
        String strURL = "https://soccer.sportmonks.com/api/v2.0/commentaries/fixture/{id}?api_token={your_api_key}";
        strURL = strURL.replace("{id}", matchID);
        getSportMonkInsert(strURL,"commentary");
    }

    private void getSportMonkFullRefresh(String inURL, String collection){
        String strURL = inURL;
        strURL = strURL.replace("{your_api_key}", this.apiKey);
        ArrayList<Document> seasonArrayDocs = new ArrayList<Document>();

        MongoApi seasons = new MongoApi(this.mongoDataBase, collection);
        try {
            JSONObject season = new JSONObject(getJsonString(strURL, 0));
            JSONArray seasonArray = season.getJSONArray("data");

            seasons.dropCollection(Boolean.FALSE);
            for (int i = 0; i < seasonArray.length(); i++){
                Document seasonDoc = Document.parse(seasonArray.get(i).toString());
                seasonArrayDocs.add(seasonDoc);
            }
            seasons.insertMany(seasonArrayDocs,Boolean.FALSE);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed at Sport Monks getSportMonkFullRefresh");
        } finally {
            seasons.closeConnection();
        }

    }
    private void getSportMonkInsert(String inURL, String collection){
        String strURL = inURL;
        strURL = strURL.replace("{your_api_key}", this.apiKey);
        ArrayList<Document> seasonArrayDocs = new ArrayList<Document>();

        MongoApi seasons = new MongoApi(this.mongoDataBase, collection);
        try {
            JSONObject season = new JSONObject(getJsonString(strURL, 0));
            JSONArray seasonArray = season.getJSONArray("data");

            for (int i = 0; i < seasonArray.length(); i++){
                Document seasonDoc = Document.parse(seasonArray.get(i).toString());
                seasonDoc.append("_id",seasonDoc.getInteger("id"));
                seasonArrayDocs.add(seasonDoc);
            }
            seasons.insertMany(seasonArrayDocs,Boolean.FALSE);
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Failed at Sport Monks getSportMonkInsert");
        } finally {
            seasons.closeConnection();
        }

    }
    private ArrayList<Document> getSportMonkInsert(String inURL){
        String strURL = inURL;
        strURL = strURL.replace("{your_api_key}", this.apiKey);
        ArrayList<Document> seasonArrayDocs = new ArrayList<Document>();
        try {
            JSONObject season = new JSONObject(getJsonString(strURL, 0));
            JSONArray seasonArray = season.getJSONArray("data");

            for (int i = 0; i < seasonArray.length(); i++){
                Document seasonDoc = Document.parse(seasonArray.get(i).toString());
                seasonArrayDocs.add(seasonDoc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed at Sport Monks getSportMonkInsert");
        }
        return seasonArrayDocs;
    }
}
