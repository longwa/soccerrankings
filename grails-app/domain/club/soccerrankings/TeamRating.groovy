package club.soccerrankings

/**
 * Store rating and power metrics for each team
 */
class TeamRating {
    Rating ratingType

    // Some numerical value which is sortable to produce a ranking
    BigDecimal value

    Date lastUpdated
    Date dateCreated

    static belongsTo = [
        team: Team
    ]
}
