package DataFix;

import Mongo.MongoApi;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.ws.client.BindingProviderProperties;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import javax.print.attribute.IntegerSyntax;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.type;

/**
 * Created by scapista on 9/4/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future state
 */

public class DataFixSM {
    public void getAllDocuments(){
        MongoApi match = new Mongo.MongoApi("soccer", "match_summaries");
        ArrayList<Document> fnlDocs = new ArrayList<Document>();
        ArrayList<String> fnlKey = new ArrayList<String>();

        for (Document doc : match.getMongoDocumentList(Boolean.FALSE)){
            System.out.print(doc.toString());

            Document sport_event        = (Document) doc.get("sport_event");
            Document sport_event_conditions = (Document) doc.get("sport_event_conditions");
            Document sport_event_status      = (Document) doc.get("sport_event_status");
            Document statistics=  (Document) doc.get("statistics");

            String matchId = sport_event.getString("id");

            if (!fnlKey.contains(matchId)){
                fnlKey.add(matchId);
                Document fnlDoc =
                        new Document("_id", matchId )
                                .append("sport_event_status", sport_event_conditions)
                                .append("coverage_info", sport_event_status)
                                .append("timeline", statistics);
                fnlDocs.add(fnlDoc);
            }
        }
        match.setDatabaseCollection("soccer", "match_summaries_bkp");
        match.insertMany(fnlDocs,Boolean.TRUE);
    }
    public void createFixturesBackup(){
        MongoApi match = new Mongo.MongoApi("soccer_sm", "premier_league_fixtures");
        ArrayList<Document> fnlDocs = new ArrayList<Document>();
        ArrayList<Integer> fnlKey = new ArrayList<Integer>();

        for (Document doc : match.getMongoDocumentList(Boolean.FALSE,type("commentaries","bool"))){
            //System.out.print(doc.toString());

            MongoApi teams = new Mongo.MongoApi("soccer_sm", "teams");

            Boolean commentaries     =  doc.getBoolean("commentaries");
            Boolean winning_odds_calculated =  doc.getBoolean("winning_odds_calculated");
            Boolean deleted =   doc.getBoolean("deleted");

            Integer stage_id      =  doc.getInteger("stage_id");
            Integer season_id =  doc.getInteger("season_id");
            Integer round_id =  doc.getInteger("round_id");
            Integer aggregate_id = doc.getInteger("aggregate_id");
            Integer referee_id      = doc.getInteger("referee_id");
            Integer league_id =  doc.getInteger("league_id");
            Integer venue_id      = doc.getInteger("venue_id");
            Integer attendance =  doc.getInteger("attendance");

            Document scores = (Document) doc.get("scores");
            Document time =  (Document) doc.get("time");
            Document weather_report      = (Document) doc.get("weather_report");
            Document standings =  (Document) doc.get("standings");
            Document coaches      = (Document) doc.get("coaches");
            Document localteam_id      = teams.getMongoDocument(eq("id",doc.getInteger("localteam_id")),
                    new Document("name",1).append("id",1).append("_id",0),Boolean.FALSE);
            Document visitorteam_id = teams.getMongoDocument(eq("id",doc.getInteger("visitorteam_id")),
                    new Document("name",1).append("id",1).append("_id",0),Boolean.FALSE);

            teams.closeConnection();
            Integer matchId = doc.getInteger("id");

            if (!fnlKey.contains(matchId)){
                fnlKey.add(matchId);
                Document fnlDoc =
                        new Document("_id", matchId )
                        .append("stage_id", stage_id)
                        .append("season_id",season_id)
                        .append("round_id",round_id)
                        .append("aggregate_id",aggregate_id)
                        .append("referee_id",referee_id)
                        .append("winning_odds_calculated",winning_odds_calculated)
                        .append("deleted",deleted)
                        .append("league_id",league_id)
                        .append("venue_id",venue_id)
                        .append("commentaries", commentaries)
                        .append("scores", scores)
                        .append("time",time)
                        .append("weather_report",weather_report)
                        .append("standings",standings)
                        .append("coaches",coaches)
                        .append("attendance",attendance)
                        .append("visitorteam_id",visitorteam_id)
                        .append("localteam_id",localteam_id)
                        .append("insert_dt",new java.util.Date())
                        ;
                fnlDocs.add(fnlDoc);
            }
        }
        match.setDatabaseCollection("soccer_sm", "fixtures_bkp");
        match.insertMany(fnlDocs,Boolean.TRUE);
    }
}
