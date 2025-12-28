package kirjanpito.db

import java.math.BigDecimal

/**
 * Sisältää viennin tiedot.
 * 
 * Kotlin data class - korvaa Entry.java
 * 
 * @author Tommi Helineva (alkuperäinen Java)
 * @author Kotlin migration by Claude
 */
data class EntryData(
    var id: Int = 0,
    var documentId: Int = 0,
    var accountId: Int = 0,
    var debit: Boolean = false,
    var amount: BigDecimal? = null,
    var description: String? = null,
    var rowNumber: Int = 0,
    var flags: Int = 0
) {
    /**
     * Palauttaa yksittäisen lipun arvon.
     * 
     * @param index lipun indeksi (0-31)
     * @return true jos lippu on asetettu
     */
    fun getFlag(index: Int): Boolean = (flags and (1 shl index)) > 0
    
    /**
     * Asettaa yksittäisen lipun arvon.
     * 
     * @param index lipun indeksi (0-31)
     * @param value lipun arvo
     */
    fun setFlag(index: Int, value: Boolean) {
        flags = if (value) {
            flags or (1 shl index)
        } else {
            flags and (1 shl index).inv()
        }
    }
    
    /**
     * Luo oliosta kopion.
     * Kotlin data class tarjoaa copy() automaattisesti,
     * mutta tämä säilyttää Java-yhteensopivuuden.
     * 
     * @return kopio
     */
    fun duplicate(): EntryData = copy()
    
    /**
     * Palauttaa summan etumerkin huomioiden (debit = positiivinen).
     * 
     * @return summa etumerkillä
     */
    fun signedAmount(): BigDecimal {
        val amt = amount ?: BigDecimal.ZERO
        return if (debit) amt else amt.negate()
    }
    
    /**
     * Tarkistaa onko vienti tyhjä (ei summaa tai summa on nolla).
     */
    fun isEmpty(): Boolean = amount == null || amount == BigDecimal.ZERO
    
    /**
     * Tarkistaa onko vienti validi (on summa ja tili).
     */
    fun isValid(): Boolean = accountId > 0 && amount != null && amount != BigDecimal.ZERO
}
