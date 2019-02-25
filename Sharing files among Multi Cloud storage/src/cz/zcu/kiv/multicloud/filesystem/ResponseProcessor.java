package cz.zcu.kiv.multicloud.filesystem;

import org.apache.http.HttpResponse;

/**
 * cz.zcu.kiv.multicloud.filesystem/ResponseProcessor.java			<br /><br />
 *
 * Interface for processing {@link org.apache.http.HttpResponse} in a custom way and returning a defined result type.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface ResponseProcessor<T> {

	/**
	 * Process the {@link org.apache.http.HttpResponse}.
	 * @param response HTTP response.
	 * @return Result of the processing.
	 */
	T processResponse(HttpResponse response);

}
