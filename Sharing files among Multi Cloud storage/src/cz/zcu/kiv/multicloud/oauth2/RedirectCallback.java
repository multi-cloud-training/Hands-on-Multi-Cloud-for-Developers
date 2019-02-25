package cz.zcu.kiv.multicloud.oauth2;

import java.util.Map;

/**
 * cz.zcu.kiv.multicloud.oauth2/RedirectCallback.java			<br /><br />
 *
 * Interface to return a {@link cz.zcu.kiv.multicloud.oauth2.WebPage} as a response to a redirect to a local {@link cz.zcu.kiv.multicloud.oauth2.RedirectServer}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public interface RedirectCallback {

	/**
	 * Returns a {@link cz.zcu.kiv.multicloud.oauth2.WebPage} based on the supplied parameters in a Map.
	 * @param request Parameters obtained from a request.
	 * @return Web page to be displayed.
	 */
	WebPage onRedirect(Map<String, String> request);

}
