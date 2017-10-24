package club.soccerrankings.parser

import club.soccerrankings.Source
import club.soccerrankings.Team

/**
 * Parse team information from a source and either return an existing team or a new team
 */
trait TeamDetailParser {
    /**
     * @param source the source
     * @param teamUrl the url for this source that loads the team information page
     * @param teamId source specific identifier for this team
     * @return
     */
    abstract Team findOrCreateTeam(Source source, String teamUrl, String teamId)
}