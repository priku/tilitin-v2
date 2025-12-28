package kirjanpito.db

import java.util.Date

/**
 * Sisältää tositteen tiedot.
 * 
 * Kotlin data class - korvaa Document.java
 * 
 * @author Tommi Helineva (alkuperäinen Java)
 * @author Kotlin migration by Claude
 */
data class DocumentData(
    var id: Int = 0,
    var number: Int = 0,
    var periodId: Int = 0,
    var date: Date? = null
) {
    /**
     * Kopioi tositteen tiedot toiseen olioon.
     * Säilyttää yhteensopivuuden Java-koodin kanssa.
     *
     * @param target kohde
     */
    fun copyTo(target: DocumentData) {
        target.id = id
        target.number = number
        target.periodId = periodId
        target.date = date
    }
    
    /**
     * Luo kopion tästä dokumentista.
     * Kotlin data class tarjoaa copy() automaattisesti,
     * mutta tämä on eksplisiittinen versio.
     */
    fun duplicate(): DocumentData = copy()
}
