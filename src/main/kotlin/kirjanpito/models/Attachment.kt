package kirjanpito.models

import java.time.LocalDateTime

/**
 * PDF-liitteen domain-malli.
 * 
 * Tämä data class edustaa PDF-liitettä, joka on liitetty tositteeseen.
 * Liite tallennetaan tietokantaan BLOB-kenttänä.
 * 
 * @property id Liitteen uniikki tunniste (0 = uusi liite)
 * @property documentId Viittaus tositteeseen
 * @property filename Alkuperäinen tiedostonimi
 * @property contentType MIME-tyyppi (oletus: "application/pdf")
 * @property data PDF-data tavuina
 * @property fileSize Tiedoston koko tavuina
 * @property pageCount Sivumäärä (lasketaan tallennuksen yhteydessä)
 * @property createdDate Lisäyspäivämäärä
 * @property description Valinnainen kuvaus
 * 
 * @since 2.2.0
 */
data class Attachment(
    val id: Int = 0,
    val documentId: Int,
    val filename: String,
    val contentType: String = "application/pdf",
    val data: ByteArray,
    val fileSize: Int = data.size,
    val pageCount: Int? = null,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val description: String? = null
) {
    /**
     * Tarkistaa onko liite uusi (ei vielä tallennettu tietokantaan).
     */
    val isNew: Boolean get() = id == 0

    /**
     * Tarkistaa onko liite liian suuri.
     */
    val isTooLarge: Boolean get() = fileSize > MAX_FILE_SIZE

    /**
     * Tarkistaa onko liite varoituksen kokoinen.
     */
    val isLarge: Boolean get() = fileSize > WARNING_FILE_SIZE

    /**
     * Palauttaa tiedostokoon muotoiltuna merkkijonona.
     */
    fun formatFileSize(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / 1024 / 1024} MB"
        }
    }

    /**
     * Sanitize tiedostonimi (poistaa vaaralliset merkit).
     */
    fun sanitizeFilename(): String {
        val name = java.io.File(filename).name
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(255)  // Limit length
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Attachment

        if (id != other.id) return false
        if (documentId != other.documentId) return false
        if (filename != other.filename) return false
        if (contentType != other.contentType) return false
        if (!data.contentEquals(other.data)) return false
        if (fileSize != other.fileSize) return false
        if (pageCount != other.pageCount) return false
        if (createdDate != other.createdDate) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + documentId
        result = 31 * result + filename.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + fileSize
        result = 31 * result + (pageCount ?: 0)
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }

    companion object {
        /**
         * Maksimikoko liitteelle (10 MB).
         */
        const val MAX_FILE_SIZE = 10 * 1024 * 1024

        /**
         * Varoituksen koko (5 MB).
         */
        const val WARNING_FILE_SIZE = 5 * 1024 * 1024

        /**
         * Luo Attachment-olion tiedostosta.
         * 
         * @param documentId Tosite-ID
         * @param filename Tiedostonimi
         * @param data PDF-data
         * @param description Valinnainen kuvaus
         * @return Attachment-olio
         * @throws IllegalArgumentException jos tiedosto on tyhjä tai liian suuri
         */
        fun fromFile(
            documentId: Int,
            filename: String,
            data: ByteArray,
            description: String? = null
        ): Attachment {
            require(data.isNotEmpty()) { "PDF data cannot be empty" }
            require(data.size <= MAX_FILE_SIZE) {
                "PDF file too large: ${data.size / 1024 / 1024} MB (max: ${MAX_FILE_SIZE / 1024 / 1024} MB)"
            }

            return Attachment(
                documentId = documentId,
                filename = filename,
                data = data,
                description = description
            )
        }
    }
}

