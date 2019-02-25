package cz.zcu.kiv.multicloud.http;

import java.net.URI;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * cz.zcu.kiv.multicloud.http/HttpCopy.java			<br /><br />
 *
 * Implementation of the HTTP COPY method for use in OneDrive storage service.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class HttpCopy extends HttpEntityEnclosingRequestBase {

	/** Method name used in the request. */
	public static final String METHOD_NAME = "COPY";

	/**
	 * Empty ctor.
	 */
	public HttpCopy() {
		super();
	}

	/**
	 * Ctor with supplied uri in the form of string.
	 * @param uri Supplied uri.
	 * @throws IllegalArgumentException if the uri is invalid.
	 */
	public HttpCopy(final String uri) {
		super();
		setURI(URI.create(uri));
	}

	/**
	 * Ctor with supplied {@link java.net.URI}.
	 * @param uri Supplied uri.
	 */
	public HttpCopy(final URI uri) {
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
