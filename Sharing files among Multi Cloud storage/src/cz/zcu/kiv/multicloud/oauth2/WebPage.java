package cz.zcu.kiv.multicloud.oauth2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.http.Status;

/**
 * cz.zcu.kiv.multicloud.oauth2/WebPage.java			<br /><br />
 *
 * Abstract class defining basic structure of a web page returned by the {@link cz.zcu.kiv.multicloud.oauth2.RedirectServer} after the redirect from an external authorization site.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public abstract class WebPage {

	/** Status code of the web page. */
	protected Status status;
	/** Headers of the web page. */
	protected Map<String, String> headers;

	/**
	 * Ctor.
	 */
	public WebPage() {
		status = Status.OK;
		headers = new HashMap<>();
	}

	/**
	 * Adds one header entry to the web page.
	 * @param header Name of the header.
	 * @param value Value of the header.
	 */
	public void addHeader(String header, String value) {
		headers.put(header, value);
	}

	/**
	 * Converts all the content lines to one final string.
	 * @return Webpage as a string.
	 */
	public String getContent() {
		StringBuilder sb = new StringBuilder();
		List<String> lines = getContentLines();
		if (lines != null) {
			for (String line: lines) {
				sb.append(line);
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Returns all the lines of the content of the web page in a {@link java.util.List}.
	 * @return List of content lines.
	 */
	public abstract List<String> getContentLines();

	/**
	 * Returns all the specified headers of the web page as a {@link java.util.Map}.
	 * @return All the headers.
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Returns the status code of the web page.
	 * @return Status code.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets all the headers of the web page to the supplied {@link java.util.Map}.
	 * @param headers New headers in a Map.
	 */
	public void setHeaders(Map<String, String> headers) {
		if (headers != null) {
			this.headers = headers;
		}
	}

	/**
	 * Sets the status code of the web page.
	 * @param code Status code entered as an integer.
	 */
	public void setStatus(int code) {
		status = Status.getStatus(code);
	}

	/**
	 * Sets the status code of the web page.
	 * @param status Status code.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

}
