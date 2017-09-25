import DataFix.DataFixSM;
import DataSources.*;
import DataSources.Semantic.EventMatch;

/**
 * Created by scapista on 8/27/17.
 * +
 * + Priority:
 * + 1. build failing
 * + 2. function not working
 * + 3. nice to have but would optimize
 * + 4. nice to have no benefit
 * + 5. future stateR
 */

//arsenal team identifier zymx5xdh4knl5dwbcfv3kszge9d8brnw

public class main_class {
    public static void main (String args[]){
//        SportsRadarDelta delta = new SportsRadarDelta();
//        delta.updateTeamMatchDetails();
//
        SportMonksDelta sportMonks = new SportMonksDelta();
       sportMonks.updateTeamMatchDetails();

        EventMatch events = new EventMatch();
        events.createEventsSemCollection();

        //sportMonks.getSeasons();
        //sportMonks.getLeagues();
        //sportMonks.getRound();
        //sportMonks.getMatchDayFixture();

        //sportMonks.getMatchDays();

//          DataFix.DataFixSM datafix = new DataFixSM();
//        datafix.getAllDocuments();
//        datafix.createFixturesBackup();


        //SportsRadar getSR = new SportsRadar();

        //getSR.getRounds();
        //getSR.getMatchSummary(match_id);
        //getSR.getMatchEvents(match_id);

        //SportsOpenDataMongo getopendata = new SportsOpenDataMongo(strLeague,strTeam,strSeason,strApiKey,strRound);
        //getopendata.getTeamMatches(strLeague, strTeam, strSeason, strApiKey);

        //getopendata.getSeasonStandings();
        //getopendata.getSeasonTeams();
        //getopendata.getSeasonRound();


    }



}
