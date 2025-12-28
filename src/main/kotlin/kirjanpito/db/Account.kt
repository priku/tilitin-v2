package kirjanpito.db

import java.math.BigDecimal

/**
 * Sisältää tilin tiedot.
 * 
 * Kotlin data class - korvaa Account.java
 * 
 * @author Tommi Helineva (alkuperäinen Java)
 * @author Kotlin migration by Claude
 */
data class AccountData(
    var id: Int = 0,
    var number: String = "",
    var name: String = "",
    var type: Int = TYPE_ASSET,
    var vatCode: Int = 0,
    var vatRate: BigDecimal = BigDecimal.ZERO,
    var vatAccount1Id: Int = 0,
    var vatAccount2Id: Int = 0,
    var flags: Int = 0
) : Comparable<AccountData> {
    
    companion object {
        /** Vastaavaa */
        const val TYPE_ASSET = 0
        
        /** Vastattavaa */
        const val TYPE_LIABILITY = 1
        
        /** Oma pääoma */
        const val TYPE_EQUITY = 2
        
        /** Tulot */
        const val TYPE_REVENUE = 3
        
        /** Menot */
        const val TYPE_EXPENSE = 4
        
        /** Edellisten tilikausien voitto */
        const val TYPE_PROFIT_PREV = 5
        
        /** Tilikauden voitto */
        const val TYPE_PROFIT = 6
        
        /**
         * Palauttaa tilityypin nimen.
         */
        fun typeName(type: Int): String = when (type) {
            TYPE_ASSET -> "Vastaavaa"
            TYPE_LIABILITY -> "Vastattavaa"
            TYPE_EQUITY -> "Oma pääoma"
            TYPE_REVENUE -> "Tulot"
            TYPE_EXPENSE -> "Menot"
            TYPE_PROFIT_PREV -> "Edellisten tilikausien voitto"
            TYPE_PROFIT -> "Tilikauden voitto"
            else -> "Tuntematon"
        }
    }
    
    /**
     * Vertaa tämän tilin numeroa toisen tilin numeroon.
     * 
     * @return pienempi kuin 0, jos tämän tilin numero aakkosjärjestyksessä
     * aikaisemmin; suurempi kuin 0, jos tämän tilin numero on aakkosjärjestyksessä
     * myöhemmin
     */
    override fun compareTo(other: AccountData): Int = number.compareTo(other.number)
    
    /**
     * Tarkistaa onko tili tasetili (vastaavaa, vastattavaa, oma pääoma).
     */
    fun isBalanceSheetAccount(): Boolean = type in TYPE_ASSET..TYPE_EQUITY
    
    /**
     * Tarkistaa onko tili tuloslaskelman tili (tulot, menot).
     */
    fun isIncomeStatementAccount(): Boolean = type == TYPE_REVENUE || type == TYPE_EXPENSE
    
    /**
     * Tarkistaa onko tili tulostili.
     */
    fun isRevenueAccount(): Boolean = type == TYPE_REVENUE
    
    /**
     * Tarkistaa onko tili menotili.
     */
    fun isExpenseAccount(): Boolean = type == TYPE_EXPENSE
    
    /**
     * Tarkistaa onko tilillä ALV-käsittely.
     */
    fun hasVat(): Boolean = vatCode > 0 && vatRate > BigDecimal.ZERO
    
    /**
     * Palauttaa tilin tyypin nimen.
     */
    fun typeName(): String = Companion.typeName(type)
    
    /**
     * Palauttaa tilin näyttönimen muodossa "numero nimi".
     */
    fun displayName(): String = "$number $name"
}
