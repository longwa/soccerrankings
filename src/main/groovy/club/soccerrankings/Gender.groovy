package club.soccerrankings

enum Gender {
    BOYS("Boys"), GIRLS("Girls")

    final String description

    Gender(String description) {
        this.description = description
    }

    @Override
    String toString() {
        return description
    }

    static Gender forString(String str) {
        if (!str) {
            return BOYS
        }

        String toUpper = str.toUpperCase()
        if (toUpper.contains("BOYS")) {
            return BOYS
        }
        if (toUpper.contains("GIRLS")) {
            return GIRLS
        }

        str?.toUpperCase()?.contains("G") ? GIRLS : BOYS
    }
}