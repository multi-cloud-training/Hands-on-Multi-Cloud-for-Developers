package test;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import cz.zcu.kiv.multicloud.oauth2.AuthorizationCallback;
import cz.zcu.kiv.multicloud.oauth2.AuthorizationRequest;

/**
 * test/TestCallback.java
 *
 * Authorization callback implementation for testing purposes.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class TestCallback implements AuthorizationCallback {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onAuthorizationRequest(AuthorizationRequest request) {
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.browse(new URI(request.getRequestUri()));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
