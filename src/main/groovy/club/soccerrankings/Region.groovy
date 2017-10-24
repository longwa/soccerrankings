package club.soccerrankings

enum Region {
    R1("Region 1"),
    R2("Region 2"),
    R3("Region 3"),
    R4("Region 4")

    final String description

    Region(String description) {
        this.description = description
    }

    @Override
    String toString() {
        description
    }

    static Region valueOfDescription(String description) {
        values().find { it.description == description }
    }
}