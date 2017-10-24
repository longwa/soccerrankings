package club.soccerrankings

import java.util.regex.Matcher

enum AgeGroup {
    U10(2008),
    U11(2007),
    U12(2006),
    U13(2005),
    U14(2004),
    U15(2003),
    U16(2002),
    U17(2001),
    U18(2000),
    U19(1999),
    UNK(1980)

    final Integer year

    AgeGroup(Integer year) {
        this.year = year
    }

    /**
     * @return return the age group identifier for this birth year
     */
    static AgeGroup forYear(Integer birthYear) {
        values().find { it.year == birthYear }
    }

    /**
     * Take a string with an age group number and return this enum
     * @param ageString
     * @return
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    static AgeGroup forString(String ageString) {
        Matcher m = (ageString =~ /(\d\d)/)
        if (m?.groupCount() == 1) {
            return valueOf("U${m[0][1]}")
        }

        UNK
    }
}