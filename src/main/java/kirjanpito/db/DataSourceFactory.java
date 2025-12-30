package kirjanpito.db;

/**
 * Factory for creating DataSource instances.
 * Delegates to Kotlin implementation.
 * 
 * @author Tommi Helineva (original)
 * @author Kotlin migration by Claude
 */
public class DataSourceFactory {
	private DataSourceFactory() {
	}
	
	/**
	 * Creates a DataSource instance using Kotlin implementation.
	 */
	public static DataSource create(String url, String username, String password)
		throws DataAccessException {
		return DataSourceFactoryKt.create(url, username, password);
	}
}
