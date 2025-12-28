package kirjanpito.db;

import java.util.List;
import kirjanpito.models.Attachment;

/**
 * DAO-rajapinta PDF-liitteiden käsittelyyn.
 * 
 * @since 2.2.0
 */
public interface AttachmentDAO {
	/**
	 * Hakee liitteen ID:n perusteella.
	 * 
	 * @param id liitteen ID
	 * @return Attachment-olio tai null jos ei löydy
	 * @throws DataAccessException jos hakeminen epäonnistuu
	 */
	Attachment findById(int id) throws DataAccessException;

	/**
	 * Hakee kaikki liitteet tositteen ID:n perusteella.
	 * 
	 * @param documentId tositteen ID
	 * @return liitteiden lista (tyhjä lista jos ei löydy)
	 * @throws DataAccessException jos hakeminen epäonnistuu
	 */
	List<Attachment> findByDocumentId(int documentId) throws DataAccessException;

	/**
	 * Tallentaa liitteen tietokantaan.
	 * 
	 * @param attachment tallennettava liite
	 * @return tallennetun liitteen ID (jos uusi liite)
	 * @throws DataAccessException jos tallennus epäonnistuu
	 */
	int save(Attachment attachment) throws DataAccessException;

	/**
	 * Poistaa liitteen tietokannasta.
	 * 
	 * @param id poistettavan liitteen ID
	 * @return true jos poisto onnistui, false jos liitettä ei löytynyt
	 * @throws DataAccessException jos poisto epäonnistuu
	 */
	boolean delete(int id) throws DataAccessException;

	/**
	 * Laskee liitteiden määrän tositteelle.
	 * 
	 * @param documentId tositteen ID
	 * @return liitteiden määrä
	 * @throws DataAccessException jos laskeminen epäonnistuu
	 */
	int countByDocumentId(int documentId) throws DataAccessException;

	/**
	 * Laskee liitteiden kokonaismäärän tositteelle.
	 * 
	 * @param documentId tositteen ID
	 * @return liitteiden kokonaiskoko tavuina
	 * @throws DataAccessException jos laskeminen epäonnistuu
	 */
	long getTotalSize(int documentId) throws DataAccessException;
}

