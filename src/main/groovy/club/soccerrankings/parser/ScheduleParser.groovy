package club.soccerrankings.parser

import club.soccerrankings.Game
import club.soccerrankings.Source

/**
 * Implementors are capable of taking a source which goes to a page containing games and returning
 * game objects for that source.
 */
trait ScheduleParser {
    /**
     * @return games for this source, some may have already been processed and saved
     */
    abstract List<Game> parseGames(Source source)
}