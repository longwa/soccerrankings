package club.soccerrankings

import java.time.LocalDate

class Game {
    Team homeTeam
    Team awayTeam

    // The home and away scores respectively
    Integer homeScore
    Integer awayScore

    // The date this game occurred
    LocalDate date

    // Some kind of identifier from the source that identifies this game (usually some kind of uuid or id value)
    String gameId

    // Tracking
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        source: Source
    ]

    static constraints = {
        homeScore nullable: true, min: 0
        awayScore nullable: true, min: 0
    }
}
