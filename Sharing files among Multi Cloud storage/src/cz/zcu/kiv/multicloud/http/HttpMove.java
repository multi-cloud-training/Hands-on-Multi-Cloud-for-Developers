package cz.zcu.kiv.multicloud.http;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * cz.zcu.kiv.multicloud.http/HttpMove.java			<br /><br />
 *
 * Implementation of the HTTP MOVE method for use in OneDrive storage service.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class HttpMove extends HttpEntityEnclosingRequestBase {

	/** Method name used in the request. */
	public static final String METHOD_NAME = "MOVE";

	/**
	 * Empty ctor.
	 */
	public HttpMove() {
		super();
	}

	/**
	 * Ctor with supplied uri in the form of string.
	 * @param uri Supplied uri.
	 * @throws IllegalArgumentException if the uri is invalid.
	 */
	public HttpMove(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	/**
	 * Ctor with supplied {@link java.net.URI}.
	 * @param uri Supplied uri.
	 */
	public HttpMove(final URI uri) {
		super();
		setURI(uri);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMethod() {
		return METHOD_NAME;
	}

}
