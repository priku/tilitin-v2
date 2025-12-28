package kirjanpito.util

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.logging.Logger

/**
 * PDF-tiedostojen käsittelyyn tarkoitettuja apufunktioita.
 * 
 * @since 2.2.0
 */
object PdfUtils {
    private val logger = Logger.getLogger(PdfUtils::class.java.name)

    /**
     * Laskee PDF-tiedoston sivumäärän.
     * 
     * @param pdfData PDF-tiedoston data tavuina
     * @return sivumäärä tai null jos PDF on virheellinen
     */
    fun calculatePageCount(pdfData: ByteArray): Int? {
        return try {
            Loader.loadPDF(pdfData).use { document ->
                val pageCount = document.numberOfPages
                if (pageCount > 0) {
                    pageCount
                } else {
                    logger.warning("PDF has 0 pages")
                    null
                }
            }
        } catch (e: IOException) {
            logger.warning("Invalid PDF file: ${e.message}")
            null
        } catch (e: Exception) {
            logger.warning("Error reading PDF: ${e.message}")
            null
        }
    }

    /**
     * Tarkistaa onko tiedosto kelvollinen PDF-tiedosto.
     * 
     * @param pdfData PDF-tiedoston data tavuina
     * @return true jos tiedosto on kelvollinen PDF, muuten false
     */
    fun isValidPdf(pdfData: ByteArray): Boolean {
        return try {
            Loader.loadPDF(pdfData).use { document ->
                document.numberOfPages > 0
            }
        } catch (e: Exception) {
            false
        }
    }
}

