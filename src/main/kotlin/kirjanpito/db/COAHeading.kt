package kirjanpito.db

/**
 * Sisältää tilikartan väliotsikon tiedot.
 * 
 * Kotlin data class - korvaa COAHeading.java
 * 
 * @author Tommi Helineva (alkuperäinen Java)
 * @author Kotlin migration by Claude
 */
data class COAHeadingData(
    var id: Int = 0,
    var number: String = "",
    var text: String = "",
    var level: Int = 0
) : Comparable<COAHeadingData> {
    
    /**
     * Vertaa tämän otsikon numeroa toisen otsikon numeroon.
     * Jos numerot ovat samat, vertaa tasoja.
     * 
     * @return pienempi kuin 0, jos tämän otsikon numero aakkosjärjestyksessä
     * aikaisemmin; suurempi kuin 0, jos tämän otsikon numero on aakkosjärjestyksessä
     * myöhemmin
     */
    override fun compareTo(other: COAHeadingData): Int {
        val result = number.compareTo(other.number)
        return if (result == 0) level - other.level else result
    }
    
    /**
     * Palauttaa sisennetyn tekstin tason mukaan.
     * 
     * @param indentString sisennysmerkkijono (oletus: 2 välilyöntiä)
     * @return sisennetty teksti
     */
    fun indentedText(indentString: String = "  "): String {
        return indentString.repeat(level) + text
    }
}
