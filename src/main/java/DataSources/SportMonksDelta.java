package DataSources;

import DataFix.DataFixSM;
import Mongo.MongoApi;
import com.mongodb.client.MongoCursor;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import static Utilities.Utilities.getDaysBetweenDates;
import static Utilities.Utilities.isBeforeToday;
import static com.mongodb.client.model.Filters.*;
import static com.sun.jmx.snmp.ThreadContext.contains;

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

public class SportMonksDelta extends SportMonks{
    ArrayList<String> matches = new ArrayList<String>();

    public void updateTeamMatchDetails(){
        //getTeams("6397");
        getMatchDays();
        getMatchCommentary();
    }
    public void getMatchCommentary(){
        MongoApi fixtures = new MongoApi(super.mongoDataBase, "premier_league_fixtures");
        try{
            Bson filter = and(eq("league_id",8), nin("_id",getCommentaryIds()));

            for (Document doc : fixtures.getMongoDocumentList(Boolean.FALSE,filter)){
                try {
                    getCommentary(String.valueOf(doc.getInteger("_id")));
                    try {
                        fixtures.findReplace(
                                eq("_id", doc.getInteger("_id")),
                                doc.append("commentaries", "complete"),
                                Boolean.FALSE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            fixtures.closeConnection();
        }
    }

    private MongoCursor<String> getCommentaryIds(){
        MongoApi commentary = new MongoApi(super.mongoDataBase, "commentary");
        return commentary.getDatabaseDistinctString("fixture_id",Boolean.TRUE);
    }

    public void getMatchDays(){
        MongoApi rounds = new MongoApi(super.mongoDataBase, "rounds");
        try {
            Bson filter = ne("status","pulled");

            for (Document doc : rounds.getMongoDocumentList(Boolean.FALSE, filter)) {
               String startMatchDate = doc.getString("start");
               String endMatchDate = doc.getString("end");

               for(String matchDate : getDaysBetweenDates(startMatchDate,endMatchDate)){
                   if (isBeforeToday(matchDate)) {
                       getDateFixtures(matchDate);
                       try {
                           rounds.findReplace(eq("_id",doc.getObjectId("_id"))
                                   , doc.append("status", "pulled")
                                   , Boolean.FALSE);
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                       DataFixSM datafix = new DataFixSM();
                       datafix.createFixturesBackup();
                   }
               }
           }
       } catch (Exception e){
            e.printStackTrace();
       } finally {
            rounds.closeConnection();
       }
    }
    private void getTeamMatches(){
        MongoApi match = new Mongo.MongoApi(this.mongoDataBase, "fixtures");
        //Bson filter = new Document("league_id",this.premierLeagueID);
        Bson projection = new Document ("id", 1);
        try {
            for (Document doc : match.getMongoDocumentList( projection, Boolean.FALSE)) {
                System.out.print(doc);
                String matchID = doc.getString("id");
                if (!this.matches.contains(matchID))
                    this.matches.add(matchID);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            match.closeConnection();
        }
    }

    private void printList(){
        System.out.println("----------------");
        for (String str : this.matches){
            System.out.println(str);
        }
        System.out.println("----------------");
//        for (String str : this.removedMatches){
//            System.out.println(str);
//        }
//        System.out.println("----------------");
//        for (String str : this.teams){
//            System.out.println(str);
//        }
//        System.out.println("----------------");
    }
}
