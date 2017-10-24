package club.soccerrankings.parser

import club.soccerrankings.Source
import club.soccerrankings.SourceType
import groovy.util.logging.Slf4j
import groovyx.net.http.ChainedHttpConfig
import groovyx.net.http.FromServer
import groovyx.net.http.HttpBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Find or create sources for YSR. For YSR, each team is effectively a source since the game history
 * is behind the Team Details instead of the reverse normally.
 */
@Slf4j
class YouthSoccerRankingsSourceParser implements SourceParser {
    final static String BASE_URL = "https://youthsoccerrankings.us/"

    @Override
    List<Source> findSources() {
        List<Map> teams = findAllTeams()
        teams.collect {
            Source source = Source.findOrCreateByUrl(it.url as String)
            source.name = "Youth Soccer Rankings (${it.id})"
            source.sourceType = SourceType.YSR
            source.save()
        }
    }

    private List<Map> findAllTeams(String region = 'KY', String age = '13', String gender = 'Boys') {
        def builder = HttpBuilder.configure { request.uri = BASE_URL }

        // Get the first batch of 100
        List<Map> teams = fetchTeams(builder, region, age, gender, '', '')

        // Something may have gone wrong
        if (!teams) {
            log.info("No team information returned for initial load")
            return []
        }

        String lastTeamId = null
        String lastRank = null

        // Loop and get the rest of them in 100 block increments (limited by the site)
        while (teams.last().id != lastTeamId) {
            lastTeamId = teams.last().id
            lastRank = teams.last().rank

            // Fetch the next chunk
            teams.addAll(fetchTeams(builder, region, age, gender, lastTeamId, lastRank))
        }

        teams
    }

    /**
     * For this source we need to find the teams first and then get the schedule from the team details.
     *
     * @param builder the builder instance
     * @param region Region1 or National or state abbreviation like KY
     * @param age 13, 14, etc.
     * @param gender Boys or Girls
     * @param lastTeamId the last id from the previous block of 100 loaded
     * @param lastRank the last rank from the previous block of 100 loaded
     * @return
     */
    private List<Map> fetchTeams(HttpBuilder builder, String region, String age, String gender, String lastTeamId, String lastRank) {
        Document doc = builder.get(Document) {
            request.uri.path = "/rankingstable.php"
            request.uri.query = [region: region, age: age, gender: gender, name: '', showUnranked: 'true', lastTeamId: lastTeamId, lastRank: lastRank]

            log.info("Fetching team chunk: {}, {}", request.uri.path, request.uri.query)

            response.parser('text/html') { ChainedHttpConfig cfg, FromServer fs ->
                String text = fs.inputStream.text
                if (!text.contains("<table")) {
                    text = "<table>${text}</table>"
                }
                Jsoup.parseBodyFragment(text)
            }
        }
        if (!doc) {
            return []
        }

        doc.select(".teamRow").collect {
            [id: it.attr("id"), rank: it.attr("data-rank"), url: it.select("a").attr("href")]
        }
    }
}
