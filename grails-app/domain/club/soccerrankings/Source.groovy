package club.soccerrankings

import java.time.LocalDate

/**
 * Represents a high level source such as GotSoccer, Soccer In College, etc.
 */
class Source {
    String name
    String url
    String sourceId
    SourceType sourceType

    // We will only scrape this source after or during these dates
    LocalDate startDate
    LocalDate endDate

    // The last time this source was checked
    LocalDate lastChecked

    // Tracking
    Date dateCreated
    Date lastUpdated

    static hasMany = [
        teams: TeamSource
    ]

    static constraints = {
        name maxSize: 128
        url url: true
        lastChecked nullable: true
    }

    static mapping = {
        url index: 'idx_source_url', unique: true
    }
}
