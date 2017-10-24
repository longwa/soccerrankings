package club.soccerrankings.parser

import club.soccerrankings.Game
import club.soccerrankings.Source
import groovy.util.logging.Slf4j

/**
 * These URL's are the main sources for YSR data.
 *
 * xmlhttp.open("GET","/teamdetails.php?teamId="+teamId);
 * xmlhttp.open("GET","/gamestable.php?teamId="+teamId);
 * xmlhttp.open("GET","/teamsourcetable.php?teamId="+teamId);
 */
@Slf4j
class YouthSoccerRankingsScheduleParser implements ScheduleParser {
    List<Game> parseGames(Source source) {
        []
    }
}
