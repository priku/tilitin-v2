package kirjanpito.ui.javafx

import kirjanpito.db.Account
import kirjanpito.db.Entry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

/**
 * Tests for EntryRowModel.
 * 
 * Tests the model class used in the entry table.
 */
class EntryRowModelTest {

    @Test
    fun `test EntryRowModel creation with entry`() {
        val account = Account().apply {
            setNumber("1000")
            setName("Test Account")
            setType(Account.TYPE_ASSET)
        }
        
        val entry = Entry().apply {
            setAccountId(1)
            setDescription("Test entry")
            setDebit(true)
            setAmount(BigDecimal("100.00"))
        }
        
        val model = EntryRowModel(1, entry, account)
        
        assertEquals(1, model.getRowNumber())
        assertEquals("Test entry", model.getDescription())
        assertNotNull(model.getDebit())
        assertEquals(0, BigDecimal("100.00").compareTo(model.getDebit()!!))
        assertNull(model.getCredit())
    }

    @Test
    fun `test EntryRowModel creation with credit entry`() {
        val account = Account().apply {
            setNumber("2000")
            setName("Credit Account")
            setType(Account.TYPE_LIABILITY)
        }
        
        val entry = Entry().apply {
            setAccountId(2)
            setDescription("Credit entry")
            setDebit(false)
            setAmount(BigDecimal("50.00"))
        }
        
        val model = EntryRowModel(2, entry, account)
        
        assertNull(model.getDebit())
        assertNotNull(model.getCredit())
        assertEquals(0, BigDecimal("50.00").compareTo(model.getCredit()!!))
    }

    @Test
    fun `test EntryRowModel creation without entry`() {
        val model = EntryRowModel(1, null, null)
        
        assertEquals(1, model.getRowNumber())
        assertEquals("", model.getDescription())
        assertNull(model.getDebit())
        assertNull(model.getCredit())
    }

    @Test
    fun `test setDescription updates description`() {
        val model = EntryRowModel()
        
        model.setDescription("New description")
        
        assertEquals("New description", model.getDescription())
    }

    @Test
    fun `test setDebit clears credit`() {
        val model = EntryRowModel().apply {
            setCredit(BigDecimal("100.00"))
        }
        
        model.setDebit(BigDecimal("50.00"))
        
        assertNotNull(model.getDebit())
        assertNull(model.getCredit())
    }

    @Test
    fun `test setCredit clears debit`() {
        val model = EntryRowModel().apply {
            setDebit(BigDecimal("100.00"))
        }
        
        model.setCredit(BigDecimal("50.00"))
        
        assertNull(model.getDebit())
        assertNotNull(model.getCredit())
    }
}
