package club.soccerrankings

/**
 * Effectively a join table between teams and sources. This is needed to keep additional team-specific source data,
 * such as identifiers or name variations.
 */
class TeamSource {
    String teamName
    String teamSourceId

    // Specific full url for this team
    String url

    // Tracking
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        team: Team,
        source: Source
    ]

    static constraints = {
        url nullable: true, url: true
    }
}
