package cz.zcu.kiv.multicloud.oauth2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.client.utils.URLEncodedUtils;

import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.oauth2/ImplicitGrant.java			<br /><br />
 *
 * Implementation of the <a href="http://tools.ietf.org/html/rfc6749#section-4.2">OAuth 2.0 Implicit Grant</a>.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class ImplicitGrant implements OAuth2Grant, RedirectCallback {

	/** Local server for listening for incoming redirects. */
	private final RedirectServer server;
	/** The state string used in the authorization process. */
	private String state;

	/** OAuth access and optional refresh token. */
	protected OAuth2Token token;
	/** OAuth error that occurred. */
	protected OAuth2Error error;
	/** If the OAuth token and error are ready. */
	protected boolean ready;
	/** Synchronization object. */
	protected Object waitObject;

	/** URI of the authorization server. */
	protected String authorizeServer;
	/** Parameters passed to the authorization server. */
	protected Map<String, String> authorizeParams;

	/**
	 * Ctor.
	 */
	public ImplicitGrant() {
		token = null;
		error = null;
		server = new RedirectServer();
		server.setRedirectCallback(this);
		authorizeParams = new HashMap<>();
		ready = true;
		waitObject = new Object();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationRequest authorize() {
		ready = false;
		String queryString = URLEncodedUtils.format(Utils.mapToList(authorizeParams), "UTF-8");
		return new AuthorizationRequest(authorizeServer + "?" + queryString);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		/* stops listening on local port */
		server.stop();
		/* notify all waiting objects */
		synchronized (waitObject) {
			ready = true;
			waitObject.notifyAll();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OAuth2Error getError() {
		try {
			synchronized (waitObject) {
				while (!ready) {
					waitObject.wait();
				}
			}
		} catch (InterruptedException e) {
			synchronized (waitObject) {
				ready = true;
				waitObject.notifyAll();
			}
			return null;
		}
		return error;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OAuth2Token getToken() {
		try {
			synchronized (waitObject) {
				while (!ready) {
					waitObject.wait();
				}
			}
		} catch (InterruptedException e) {
			synchronized (waitObject) {
				ready = true;
				waitObject.notifyAll();
			}
			return null;
		}
		return token;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WebPage onRedirect(Map<String, String> request) {
		token = new OAuth2Token();
		error = new OAuth2Error();
		RedirectWebPage page = new RedirectWebPage();
		page.addHeader("Content-type", "text/html; charset=utf-8");
		page.setTitle("Error occured");
		if (request.containsKey("state")) { // state parameter found
			if (request.get("state").equals(state)) { // state parameter matches expected value
				if (request.containsKey("error")) { // error during authorization
					error.setType(OAuth2ErrorType.valueOf(request.get("error").toUpperCase()));
					String errorType = error.getType().toString();
					page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
					page.addBodyLine("<p>");
					if (errorType != null) {
						errorType = "<strong>" + errorType.replace('_', ' ') + "</strong>";
						if (request.containsKey("error_description")) {
							error.setDescription(request.get("error_description"));
							errorType += ": " + request.get("error_description");
						}
						page.addBodyLine(errorType);
					}
					if (request.containsKey("error_uri")) {
						error.setUri(request.get("error_uri"));
						page.addBodyLine("<br />");
						page.addBodyLine("For more information, visit: <a href=\"" + request.get("error_uri") + "\">" + request.get("error_uri") + "</a>");
					}
					page.addBodyLine("</p>");
				} else {
					if (request.containsKey("access_token") || request.containsKey("authentication_token")) { // successful authorization
						if (request.containsKey("token_type")) {
							if (request.containsKey("access_token")) {
								token.setAccessToken(request.get("access_token"));
							} else {
								token.setAccessToken(request.get("authentication_token"));
							}
							token.setType(OAuth2TokenType.valueOf(request.get("token_type").toUpperCase()));
							if (request.containsKey("expires_in")) {
								try {
									token.setExpiresIn(Integer.parseInt(request.get("expires_in")));
								} catch (NumberFormatException e) {
									/* unable to parse expiration time - ignore */
								}
							}
							page.setTitle("Authorization successful");
							page.addBodyLine("<p id=\"success\">Authorization successful.</p>");
							page.addBodyLine("<p>You may now close this page and return to the application.</p>");
						} else {
							error.setType(OAuth2ErrorType.TOKEN_TYPE_MISSING);
							error.setDescription("Token type not specified..");
							page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
							page.addBodyLine("<p><strong>" + error.getType().toString().replace('_', ' ') + "</strong>: " + error.getDescription() + "</p>");
						}
					} else { // authorization code not found in the request
						error.setType(OAuth2ErrorType.ACCESS_TOKEN_MISSING);
						error.setDescription("Access token missing.");
						page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
						page.addBodyLine("<p><strong>" + error.getType().toString().replace('_', ' ') + "</strong>: " + error.getDescription() + "</p>");
					}
				}
			} else { // state parameter doesn't match the actual state
				error.setType(OAuth2ErrorType.STATE_MISMATCH);
				error.setDescription("Mismatch in <code>state</code> parameter.");
				page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
				page.addBodyLine("<p><strong>" + error.getType().toString().replace('_', ' ') + "</strong>: " + error.getDescription() + "</p>");
			}
		} else { // state parameter not set in the request
			error.setType(OAuth2ErrorType.STATE_MISSING);
			error.setDescription("Missing <code>state</code> parameter.");
			page.addBodyLine("<p id=\"error\">Error occured during authorization.</p>");
			page.addBodyLine("<p><strong>" + error.getType().toString().replace('_', ' ') + "</strong>: " + error.getDescription() + "</p>");
		}
		/* notify all waiting objects */
		synchronized (waitObject) {
			ready = true;
			waitObject.notifyAll();
		}
		return page;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setup(OAuth2Settings settings) throws OAuth2SettingsException {
		/* validate supplied settings */
		if (settings == null) {
			throw new OAuth2SettingsException("Missing settings.");
		}
		if (Utils.isNullOrEmpty(settings.getAuthorizeUri())) {
			throw new OAuth2SettingsException("Authorization server URI missing.");
		} else {
			authorizeServer = settings.getAuthorizeUri();
		}
		if (Utils.isNullOrEmpty(settings.getClientId())) {
			throw new OAuth2SettingsException("Client ID cannot be null or empty.");
		}
		if (Utils.isNullOrEmpty(state)) {
			state = server.generateRandomState(false);
		}
		if (!Utils.isNullOrEmpty(settings.getScope())) {
			authorizeParams.put("scope", settings.getScope());
		}

		/* start listening for incoming redirects */
		try {
			server.start();
			state = server.generateRandomState(true);
		} catch (IllegalStateException e) {
			/* server already running - ignore */
		} catch (IOException e) {
			/* read or write operation failed - ignore */
		}

		/* set redirect URI if necessary */
		if (Utils.isNullOrEmpty(settings.getRedirectUri())) {
			settings.setRedirectUri(server.getBoundUri());
		}

		/* populate authorization request params */
		authorizeParams.put("client_id", settings.getClientId());
		authorizeParams.put("redirect_uri", settings.getRedirectUri());
		authorizeParams.put("response_type", "token");
		authorizeParams.put("state", state);
		for (Entry<String, String> entry: settings.getExtraAuthorizeParams().entrySet()) {
			if (!authorizeParams.containsKey(entry.getKey())) {
				authorizeParams.put(entry.getKey(), entry.getValue());
			}
		}
	}

}
