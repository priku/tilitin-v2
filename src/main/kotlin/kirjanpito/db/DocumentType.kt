package kirjanpito.db

/**
 * Sisältää tositelajin tiedot.
 * 
 * Kotlin data class - korvaa DocumentType.java
 * 
 * @author Tommi Helineva (alkuperäinen Java)
 * @author Kotlin migration by Claude
 */
data class DocumentTypeData(
    var id: Int = 0,
    var number: Int = 0,
    var name: String = "",
    var numberStart: Int = 0,
    var numberEnd: Int = 0
) : Comparable<DocumentTypeData> {
    
    /**
     * Vertaa tositelajeja numeron perusteella.
     */
    override fun compareTo(other: DocumentTypeData): Int = number - other.number
    
    /**
     * Tarkistaa onko annettu tositenumero tämän tositelajin alueella.
     * 
     * @param docNumber tositenumero
     * @return true jos numero on alueella [numberStart, numberEnd]
     */
    fun isInRange(docNumber: Int): Boolean = docNumber in numberStart..numberEnd
    
    /**
     * Palauttaa seuraavan vapaan tositenumeron.
     * 
     * @param currentMax nykyinen suurin käytetty numero
     * @return seuraava numero tai numberStart jos currentMax < numberStart
     */
    fun nextNumber(currentMax: Int): Int = maxOf(numberStart, currentMax + 1)
}
