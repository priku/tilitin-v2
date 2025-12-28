package kirjanpito.db

import java.util.Date

/**
 * Sisältää tilikauden tiedot.
 * 
 * Kotlin data class - korvaa Period.java
 * 
 * @author Tommi Helineva (alkuperäinen Java)
 * @author Kotlin migration by Claude
 */
data class PeriodData(
    var id: Int = 0,
    var startDate: Date? = null,
    var endDate: Date? = null,
    var locked: Boolean = false
) {
    /**
     * Tarkistaa onko annettu päivämäärä tilikauden sisällä.
     * 
     * @param date tarkistettava päivämäärä
     * @return true jos päivämäärä on tilikauden sisällä
     */
    fun containsDate(date: Date): Boolean {
        val start = startDate ?: return false
        val end = endDate ?: return false
        return !date.before(start) && !date.after(end)
    }
    
    /**
     * Palauttaa tilikauden keston päivinä.
     * 
     * @return päivien määrä tai null jos päivämäärät puuttuvat
     */
    fun durationInDays(): Long? {
        val start = startDate ?: return null
        val end = endDate ?: return null
        val diffInMillis = end.time - start.time
        return diffInMillis / (1000 * 60 * 60 * 24)
    }
}
