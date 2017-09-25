package DataSources.Semantic;

import Mongo.MongoApi;
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.InsertRequest;
import com.mongodb.client.FindIterable;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.bson.Document;
import org.bson.codecs.IntegerCodec;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.sort;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static sun.audio.AudioPlayer.player;


/**
 * Created by scapista on 9/15/17.
 * +
 * + This class is meant to build the soccer_sm.events_sem collection
 * + by mergering both commentary and event information
 * +
 */

public class EventMatch {
    String homeTeam = null, awayTeam = null;

    public void createEventsSemCollection(){
        associateMatchEvents();
        MongoApi getAssociation = new MongoApi("soccer_sem","match_association");
        for (Document match_association : getAssociation.getMongoDocumentList(Boolean.FALSE)){
            getEventsbyMatchId(match_association);
        }

    }
    public void associateMatchEvents(){
        MongoApi getEvents = new MongoApi("soccer","match_events");
        MongoApi getFixtures = new MongoApi("soccer_sm","premier_league_fixtures");
        ArrayList<Document> associateMatchColl = new ArrayList<>();
        try {
            for( Document doc : getFixtures.getMongoDocumentList(eq("league_id",8),
                    and(    eq("localteam_id",1)
                            ,eq("visitorteam_id",1)
                    )
                    ,Boolean.FALSE)) {
                System.out.println(doc);
                if(((Document)doc.get("visitorteam_id")).containsKey("name")
                        && ((Document)doc.get("localteam_id")).containsKey("name")) {
                    Integer matchId = null;
                    try {
                        matchId = doc.getInteger("_id");
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    doc = getEvents.getMongoDocumentList(Boolean.FALSE,
                            and(regex("sport_event.competitors.name",
                                    ((Document) doc.get("localteam_id")).getString("name").split(" ")[0], "i"),
                                    regex("sport_event.competitors.name",
                                    ((Document) doc.get("visitorteam_id")).getString("name").split(" ")[0], "i")
                            )
                    ).first();
                    if (doc != null && !doc.isEmpty()) {
                        associateMatchColl.add(new Document("_id", matchId)
                                .append("sports_radar_id", doc.getString("_id")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getEvents.closeConnection();
            getFixtures.closeConnection();
        }
        MongoApi putAssociation = new MongoApi("soccer_sem","match_association");
        putAssociation.dropCollection(Boolean.FALSE);
        putAssociation.insertMany(associateMatchColl,Boolean.TRUE);
    }
    public void getEventsbyMatchId(Document match_association){
        MongoApi getEvents = new MongoApi("soccer","match_events");
        MongoApi getCommentary = new MongoApi("soccer_sm","commentary");
        ArrayList<Document> eventList = new ArrayList<>();
        try{
            for(Document timelineDoc :  getEvents.getMongoDocumentList(
                    eq("_id",match_association.getString("sports_radar_id")),
                    new Document("timeline",1).append("sport_event",1),
                    Boolean.FALSE)
                    .sort(new Document("timeline.date",1))) {

                String matchId = timelineDoc.getString("_id");
                setCompetitors((Document) timelineDoc.get("sport_event"));

                for (Document eventDoc : (ArrayList<Document>) timelineDoc.get("timeline")) {
                    if (eventDoc.containsKey("match_time") && eventDoc.containsKey("type")) {
                        int multipleCommentary = 1;

                        for ( Document commetaryDoc : getCommentary.getMongoDocumentList(Boolean.FALSE,
                                and(    eq( "fixture_id", match_association.getInteger("_id") ),
                                        getMinuteFilter(eventDoc,Boolean.FALSE),
                                        getCommentPattern(eventDoc)))) {
                            eventDoc.append("commentary" + multipleCommentary, commetaryDoc.getString("comment"))
                                    .append("fixture_id",commetaryDoc.get("fixture_id"))
                                    .append("order",commetaryDoc.get("order"));
                            multipleCommentary++;
                        }
                    }
                    eventList.add(eventDoc
                            .append("match_id",matchId)
                            .append("insert_dt",new java.util.Date())
                    );

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getEvents.closeConnection();
            getCommentary.closeConnection();
        }
        if (!eventList.isEmpty()) {
            MongoApi pushEvents = new MongoApi("soccer_sem", "events_commentary");
            pushEvents.dropCollection(Boolean.FALSE);
            pushEvents.insertMany(eventList, Boolean.TRUE);
        } else {
            System.out.println("empty event List");
        }
    }
    private void setCompetitors (Document sporting_event){
        ArrayList<Document> competitorList = (ArrayList<Document>)sporting_event.get("competitors");

        //System.out.println(competitorList.get(0).getString("qualifier"));
        for (Document competitorDoc : competitorList){
            try {
                if (competitorDoc.getString("qualifier").equals("away"))
                    this.awayTeam = competitorDoc.getString("name").split("\\s+")[0];
                else if (competitorDoc.getString("qualifier").equals("home"))
                    this.homeTeam = competitorDoc.getString("name").split("\\s+")[0];
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("home vs away setCompetitors has failed");
            }
        }
        System.out.println("away -->" + this.awayTeam);
        System.out.println("home -->" + this.homeTeam);
    }

    private Bson getMinuteFilter (Document matchDoc, Boolean strictMatch){
        Bson pattern = null;
        int matchTime;
        if (matchDoc.containsKey("match_time")) {
            matchTime = matchDoc.getInteger("match_time");
            if (strictMatch)
                pattern = eq("minute", matchTime);
            else
                pattern = in("minute", Arrays.asList(matchTime - 1, matchTime, matchTime + 1));
            if((matchTime == 90 || matchTime == 45) && matchDoc.containsValue("stoppage_time")){
                String stoppage_time = matchDoc.getString("stoppage_time");
                if ( stoppage_time != null && !stoppage_time.isEmpty())
                    pattern = and(pattern, eq("extra_minute",Integer.parseInt(stoppage_time)));
            }
        }
        else pattern = new Document("minute",0);
        return pattern;
    }
    private Bson getCommentPattern (Document eventDoc){
        String pattern = "~", commentExpression;
        Bson queryFilter = null;

        if (eventDoc.containsKey("player")) {
            Document tmpDoc  = (Document) eventDoc.get("player");
            commentExpression = tmpDoc.getString("name").split(",")[0];
            queryFilter = new BasicDBObject("$text",
                    new BasicDBObject("$search", commentExpression));
        }

        switch(eventDoc.getString("type")){
            case "free_kick":
                pattern = "free kick|^Foul";
                if (eventDoc.getString("team").equals("away"))
                    pattern = this.awayTeam + ".*" + pattern + ".*" + this.homeTeam;
                else
                    pattern = this.homeTeam + ".*" + pattern + ".*" + this.awayTeam;
                break;
            case "score_change":
                pattern = "^Goal";
                break;
            case "offside":
                pattern = "^Offside";
                break;
            case "shot_off_target":
                break;
            case "shot_on_target":
                pattern = "^Attempt saved";
                break;
            case "corner_kick":
                pattern = "^Corner";
                break;
            case "yellow_card":
                pattern = "yellow card";
                if (eventDoc.getString("team").equals("away"))
                    pattern = this.awayTeam + ".*" + pattern;
                else
                    pattern = this.homeTeam + ".*" + pattern;
                break;
            default:
                pattern = "~";
                break;
        }
        System.out.println(eventDoc.getString("type") + "->" + pattern);
        if (queryFilter != null)
            return and(regex("comment",pattern), queryFilter);
        else
            return regex("comment",pattern);


    }
}
