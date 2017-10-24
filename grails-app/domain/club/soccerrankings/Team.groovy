package club.soccerrankings

import groovy.util.logging.Slf4j
import me.xdrop.fuzzywuzzy.FuzzySearch

@Slf4j
class Team {
    String name
    Integer year
    Region region
    State state
    Gender gender

    String coachName
    String managerName

    // Logo
    byte[] logoImage
    String logoContentType

    // Tracking
    Date dateCreated
    Date lastUpdated

    static hasMany = [
        sources: TeamSource,
        homeGames: Game,
        awayGames: Game,
    ]

    static mappedBy = [
        homeGames: 'homeTeam', awayGames: 'awayTeam'
    ]

    static constraints = {
        coachName nullable: true
        managerName nullable: true
        name maxSize: 128
        logoImage nullable: true
        logoContentType nullable: true
    }

    static mapping = {
        logoImage column: 'logo_image', sqlType: 'bytea'
    }

    static Team fuzzyFind(String searchName, Integer searchYear, Gender searchGender) {
        searchName = sanitizeName(searchName, searchYear, searchGender)

        // Simple search based on the cleaned name
        Team team = findByNameAndYearAndGender(searchName, searchYear, searchGender)
        if (team) {
            return team
        }

        int bestMatch = 0

        // Look through all of them and fuzzy match
        List<Team> candidates = findAllByYearAndGender(searchYear, searchGender)
        candidates.each {
            int match = FuzzySearch.ratio(searchName, it.name)
            if (match > bestMatch) {
                team = it
                bestMatch = match
            }
        }

        log.info("Matched team {} from search name {}, ratio {}", team, searchName, bestMatch)

        bestMatch > 75 ? team : null
    }

    static String sanitizeName(String searchName, Integer searchYear, Gender searchGender) {
        searchName = searchName.toUpperCase()

        // Remove Boys or Girls
        searchName = searchName.replaceAll(searchGender.description.toUpperCase(), '')

        // Remove the year (both 2005 and 05)
        searchName = searchName.replaceAll("$searchYear", '').replaceAll("$searchYear".substring(2, 4), '')

        // Remove any age group classifications
        searchName = searchName.replaceAll(/B?U?\d{2}B?U?/, '')
        searchName
    }
}
