package cz.zcu.kiv.multicloud.http;

/**
 * cz.zcu.kiv.multicloud.http/HttpMethod.java			<br /><br />
 *
 * List of all methods of the HTTP/1.1 protocol as defined in the <a href="http://tools.ietf.org/html/rfc2616">RFC 2616</a>. CONNECT method is omitted.
 * It also contains the PATCH method defined in <a href="http://tools.ietf.org/html/rfc2068">RFC 2068</a> since it is used in the implementation of Google Drive. LINK and UNLINK methods from the same RFC are omitted.
 * Also, MOVE and COPY methods are included for Microsoft's OneDrive internal move and copy operations.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum HttpMethod {

	OPTIONS,
	GET,
	HEAD,
	POST,
	PUT,
	DELETE,
	TRACE,
	PATCH,
	MOVE,
	COPY

}
