package club.soccerrankings

enum SourceType {
    GOTSOCCER("GotSoccer"),
    AFFINITY("Affinity"),
    TOURNEYCENTRAL("TourneyCentral"),
    SINC("SoccerInCollege"),
    YSR("YouthSoccerRankings"),

    final String description

    SourceType(String description) {
        this.description = description
    }

    @Override
    String toString() {
        description
    }
}