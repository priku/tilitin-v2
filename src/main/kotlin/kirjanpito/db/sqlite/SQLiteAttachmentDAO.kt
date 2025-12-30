package kirjanpito.db.sqlite

import kirjanpito.db.*
import kirjanpito.models.Attachment
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * SQLite-toteutus AttachmentDAO-rajapinnasta.
 * 
 * @since 2.2.0
 */
class SQLiteAttachmentDAO(session: Session) : AttachmentDAO {
    private val session: Session = session

    override fun findById(id: Int): Attachment? {
        return try {
            session.prepareStatement(
                "SELECT id, document_id, filename, content_type, data, file_size, " +
                "page_count, created_date, description FROM attachments WHERE id = ?"
            ).use { stmt ->
                stmt.setInt(1, id)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        createAttachment(rs)
                    } else {
                        null
                    }
                }
            }
        } catch (e: SQLException) {
            throw DataAccessException("Failed to find attachment by id: ${e.message}", e)
        }
    }

    override fun findByDocumentId(documentId: Int): List<Attachment> {
        return try {
            session.prepareStatement(
                "SELECT id, document_id, filename, content_type, data, file_size, " +
                "page_count, created_date, description FROM attachments " +
                "WHERE document_id = ? ORDER BY created_date ASC"
            ).use { stmt ->
                stmt.setInt(1, documentId)
                stmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(createAttachment(rs))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            throw DataAccessException("Failed to find attachments by document id: ${e.message}", e)
        }
    }

    override fun save(attachment: Attachment): Int {
        return try {
            if (attachment.isNew) {
                insertAttachment(attachment)
            } else {
                updateAttachment(attachment)
                attachment.id
            }
        } catch (e: SQLException) {
            throw DataAccessException("Failed to save attachment: ${e.message}", e)
        }
    }

    override fun delete(id: Int): Boolean {
        return try {
            session.prepareStatement("DELETE FROM attachments WHERE id = ?").use { stmt ->
                stmt.setInt(1, id)
                val rowsAffected = stmt.executeUpdate()
                rowsAffected > 0
            }
        } catch (e: SQLException) {
            throw DataAccessException("Failed to delete attachment: ${e.message}", e)
        }
    }

    override fun countByDocumentId(documentId: Int): Int {
        return try {
            session.prepareStatement(
                "SELECT COUNT(*) FROM attachments WHERE document_id = ?"
            ).use { stmt ->
                stmt.setInt(1, documentId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        rs.getInt(1)
                    } else {
                        0
                    }
                }
            }
        } catch (e: SQLException) {
            throw DataAccessException("Failed to count attachments: ${e.message}", e)
        }
    }

    override fun getTotalSize(documentId: Int): Long {
        return try {
            session.prepareStatement(
                "SELECT COALESCE(SUM(file_size), 0) FROM attachments WHERE document_id = ?"
            ).use { stmt ->
                stmt.setInt(1, documentId)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        rs.getLong(1)
                    } else {
                        0L
                    }
                }
            }
        } catch (e: SQLException) {
            throw DataAccessException("Failed to get total size: ${e.message}", e)
        }
    }

    // ========================================================================
    // Private Helper Methods
    // ========================================================================

    private fun insertAttachment(attachment: Attachment): Int {
        session.prepareStatement(
            "INSERT INTO attachments (document_id, filename, content_type, data, " +
            "file_size, page_count, created_date, description) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        ).use { stmt ->
            setAttachmentParameters(stmt, attachment)
            stmt.executeUpdate()
        }
        
        // Get generated key using session's extension property
        return try {
            session.insertId
        } catch (e: SQLException) {
            throw DataAccessException("Failed to get generated key for attachment: ${e.message}", e)
        }
    }

    private fun updateAttachment(attachment: Attachment) {
        session.prepareStatement(
            "UPDATE attachments SET document_id = ?, filename = ?, content_type = ?, " +
            "data = ?, file_size = ?, page_count = ?, created_date = ?, description = ? " +
            "WHERE id = ?"
        ).use { stmt ->
            setAttachmentParameters(stmt, attachment)
            stmt.setInt(9, attachment.id)
            stmt.executeUpdate()
        }
    }

    private fun setAttachmentParameters(stmt: PreparedStatement, attachment: Attachment) {
        stmt.setInt(1, attachment.documentId)
        stmt.setString(2, attachment.filename)
        stmt.setString(3, attachment.contentType)
        stmt.setBytes(4, attachment.data)
        stmt.setInt(5, attachment.fileSize)
        
        if (attachment.pageCount != null) {
            stmt.setInt(6, attachment.pageCount)
        } else {
            stmt.setNull(6, java.sql.Types.INTEGER)
        }
        
        val timestamp = Timestamp.valueOf(attachment.createdDate)
        stmt.setTimestamp(7, timestamp)
        
        if (attachment.description != null) {
            stmt.setString(8, attachment.description)
        } else {
            stmt.setNull(8, java.sql.Types.VARCHAR)
        }
    }

    private fun createAttachment(rs: ResultSet): Attachment {
        val timestamp = rs.getTimestamp(8)
        val createdDate = if (timestamp != null) {
            timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        } else {
            LocalDateTime.now()
        }

        return Attachment(
            id = rs.getInt(1),
            documentId = rs.getInt(2),
            filename = rs.getString(3),
            contentType = rs.getString(4) ?: "application/pdf",
            data = rs.getBytes(5),
            fileSize = rs.getInt(6),
            pageCount = rs.getIntOrNull(7),
            createdDate = createdDate,
            description = rs.getStringOrNull(9)
        )
    }

    // Extension function for ResultSet.getIntOrNull
    private fun ResultSet.getIntOrNull(columnIndex: Int): Int? {
        val value = getInt(columnIndex)
        return if (wasNull()) null else value
    }

    // Extension function for ResultSet.getStringOrNull
    private fun ResultSet.getStringOrNull(columnIndex: Int): String? {
        val value = getString(columnIndex)
        return if (wasNull()) null else value
    }
}

