package DataSources;

import Mongo.MongoApi;
import org.bson.conversions.Bson;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.sun.tools.corba.se.idl.InterfaceState.Public;
import static com.sun.tools.doclint.Entity.and;
import static com.sun.tools.doclint.Entity.delta;
import static com.sun.tools.doclint.Entity.or;

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

public class SportsRadarDelta {
    private ArrayList<String> matches = new ArrayList<String>();
    private ArrayList<String> removedMatches = new ArrayList<String>();
    private ArrayList<String> teams = new ArrayList<String>();

    public SportsRadarDelta(){
    }

    public void updateTeamMatchDetails(){
        //ArrayList<String> teams = new ArrayList();
        //teams.add("Arsenal FC");

        SportsRadar SR = new SportsRadar();
        SR.getSchedule("sr:tournament:17");

        getOpponentTeams("Arsenal FC");

        for (String team : teams) {
            getTeamMatches(team);
            getTeamMatchExcludeIds(team, "soccer", "match_summaries");
            getTeamMatchExcludeIds(team, "soccer", "match_events");
        }
        printList();

        SportsRadar sportsRadar = new SportsRadar();
        for (String matchID : this.matches){
            sportsRadar.getMatchSummary(matchID);
            sportsRadar.getMatchEvents(matchID);
        }
    }
    private void getOpponentTeams(String team){
        MongoApi match = new Mongo.MongoApi("soccer", "tournament");
        Bson filter = new Document ("competitors.name",team).append("status", "closed");
        Bson projection = new Document ("competitors.name", 1);
        try {
            for (Document doc : match.getMongoDocumentList(filter, projection, Boolean.FALSE)) {
                ArrayList<Document> competitors = (ArrayList<Document>) doc.get("competitors");
                for (Document teamNames : competitors){
                    String teamname = teamNames.get("name").toString();
                    if (!teams.contains(teamname))
                        this.teams.add(teamname);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            match.closeConnection();
        }
    }

    private void getTeamMatches(String team){
        MongoApi match = new Mongo.MongoApi("soccer", "tournament");
        Bson filter = new Document ("competitors.name",team).append("status", "closed");
        Bson projection = new Document ("id", 1);
        try {
            for (Document doc : match.getMongoDocumentList(filter, projection, Boolean.FALSE)) {
                String matchID = doc.get("id").toString();
                if (!this.matches.contains(matchID))
                    this.matches.add(matchID);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            match.closeConnection();
        }
    }
    private void getTeamMatchExcludeIds(String team, String database, String collection){
        MongoApi summaries = new Mongo.MongoApi(database, collection);
       Bson projection = new Document ("_id", 1);
        try {
            for (Document doc : summaries.getMongoDocumentList(projection, Boolean.FALSE)) {
                String matchID = doc.get("_id").toString();
                if (this.matches.contains(matchID)) {
                    this.matches.remove(matchID);
                    if (!this.removedMatches.contains(matchID))
                        this.removedMatches.add(matchID);
                    System.out.println( "removed -->" + matchID );
                } else if (!this.removedMatches.contains(matchID)) {
                    this.matches.add(matchID);
                    System.out.println( "added -->" + matchID);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("failed in getTeamMatchSummaries");
        } finally {
            summaries.closeConnection();
        }
    }
    private void printList(){
        System.out.println("----------------");
        for (String str : this.matches){
            System.out.println(str);
        }
//        System.out.println("----------------");
//        for (String str : this.removedMatches){
//            System.out.println(str);
//        }
        System.out.println("----------------");
        for (String str : this.teams){
            System.out.println(str);
        }
        System.out.println("----------------");
    }
}
