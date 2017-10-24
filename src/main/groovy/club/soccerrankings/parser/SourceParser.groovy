package club.soccerrankings.parser

import club.soccerrankings.Source

trait SourceParser {
    /**
     * @return list of sources
     */
    abstract List<Source> findSources()
}