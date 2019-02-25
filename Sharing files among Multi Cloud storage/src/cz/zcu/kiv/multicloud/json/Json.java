package cz.zcu.kiv.multicloud.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * cz.zcu.kiv.multicloud.json/Json.java			<br /><br />
 *
 * Basic class for holding single instances of {@link com.fasterxml.jackson.core.JsonFactory} and {@link com.fasterxml.jackson.databind.ObjectMapper} across the whole library.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class Json {

	/** Instance of this class. */
	private static Json instance;

	/**
	 * Get an already existing instance.
	 * @return Instance of this class.
	 */
	public static Json getInstance() {
		if (instance == null) {
			instance = new Json();
		}
		return instance;
	}

	/** Local JSON factory. */
	private final JsonFactory factory;
	/** Local JSON Object mapper. */
	private final ObjectMapper mapper;

	/**
	 * Private ctor.
	 */
	private Json() {
		factory = new JsonFactory();
		mapper = new ObjectMapper();
	}

	/**
	 * Returns instance of already created JSON factory.
	 * @return JSON factory.
	 */
	public JsonFactory getFactory() {
		return factory;
	}

	/**
	 * Returns instance of already created JSON Object mapper.
	 * @return JSON Object mapper.
	 */
	public ObjectMapper getMapper() {
		return mapper;
	}

}
