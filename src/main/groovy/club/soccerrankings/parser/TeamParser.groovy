package club.soccerrankings.parser

import club.soccerrankings.Source
import club.soccerrankings.TeamSource

/**
 * Given a source, the team parser will find and associate existing teams or create new teams for that source
 */
trait TeamParser {
    /**
     * @return list of team source pairings
     */
    abstract List<TeamSource> loadTeams(Source source)
}