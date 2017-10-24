package club.soccerrankings.parser

import club.soccerrankings.AgeGroup
import club.soccerrankings.Gender
import club.soccerrankings.Region
import club.soccerrankings.Source
import club.soccerrankings.State
import club.soccerrankings.Team
import club.soccerrankings.TeamSource
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
class YouthSoccerRankingsTeamParser implements TeamParser {
    /**
     * @return create or find teams for this source
     */
    @Override
    List<TeamSource> loadTeams(Source source) {
        List<TeamSource> results = []

        List<Map> teams = findAllTeams(source)
        teams.each {
            // See if we already have a TeamSource mapping
            TeamSource teamSource = TeamSource.findBySourceAndTeamSourceId(source, it.id as String)
            if (!teamSource) {
                teamSource = new TeamSource(source: source, teamSourceId: it.id)

                // Find the team
                Team team = lookupTeam(source, teamSource.teamSourceId)
                if (team) {
                    teamSource.url = it.url
                    team.addToSources(teamSource)
                    results << teamSource
                }
            }
            else {
                results << teamSource
            }
        }

        results
    }

    private Team lookupTeam(Source source, String teamId) {
        Map teamDetail = fetchTeamInformation(source, teamId)

        // In case something went wrong, we just report that we can't find anything
        if (!teamDetail?.name) {
            return null
        }

        String searchName = teamDetail.name
        Integer searchYear = teamDetail.ageYear
        Gender searchGender = teamDetail.gender

        // Try to find an existing team
        Team team = Team.fuzzyFind(searchName, searchYear, searchGender)
        if (team) {
            return team
        }

        // Create a new team
        new Team(
            name: Team.sanitizeName(searchName, searchYear, searchGender),
            year: searchYear,
            region: teamDetail.region,
            state: teamDetail.state,
            coachName: teamDetail.coach,
            managerName: teamDetail.manager
        )
    }

    private Map fetchTeamInformation(Source source, String teamId) {
        Document doc = HttpBuilder.configure { request.uri = "${source.url}/teamdetails.php?teamId=${teamId}" }.get() as Document

        // Team name
        Map result = [:]
        result.name = doc.select("h1").text()

        // Age and gender are on a single line for some weird reason
        String ageAndGender = doc.select("h2").first().text()
        result.ageYear = AgeGroup.forString(ageAndGender).year
        result.gender = Gender.forString(ageAndGender)

        // Pull region and state
        // Ex. National:<br><br>Region 2: <br><br>State (KY): <br><br>Score:
        String regionAndState = doc.select("h2 tr td").text()

        def stateMatcher = regionAndState =~ /State \(([A-Z]{2})\)/
        if (stateMatcher?.groupCount() == 1) {
            result.state = State.valueOfAbbreviation(stateMatcher[0][1])
        }

        def regionMatcher = regionAndState =~ /(Region [1234])/
        if (regionMatcher?.groupCount() == 1) {
            result.region = Region.valueOfDescription(regionMatcher[0][1])
        }

        // Try to parse the coach/manager field
        String coachAndManager = doc.select("h3").first().text()?.toUpperCase()
        if (coachAndManager.contains("COACH") && coachAndManager.contains("MANAGER")) {
            def coachMatcher = (doc.select("h3").first().text() =~ /Coach:\s(.+)\sManager:\s(.+)/)
            if (coachMatcher?.groupCount() == 2) {
                result.coach = coachMatcher[0][1]
                result.manager = coachMatcher[0][2]
            }
        }
        else if (coachAndManager.contains("COACH")) {
            def coachMatcher = (doc.select("h3").first().text() =~ /Coach:\s(\S+)/)
            if (coachMatcher?.groupCount() == 1) {
                result.coach = coachMatcher[0][1]
            }
        }

        result
    }

    private List<Map> findAllTeams(Source source, String region = 'KY', String age = '13', String gender = 'Boys') {
        def builder = HttpBuilder.configure { request.uri = source.url }

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
