package cz.zcu.kiv.multicloud.oauth2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.oauth2/AuthorizationCodeGrant.java			<br /><br />
 *
 * Implementation of the <a href="http://tools.ietf.org/html/rfc6749#section-4.1">OAuth 2.0 Authorization Code Grant</a>.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class AuthorizationCodeGrant implements OAuth2Grant, RedirectCallback {

	/** Time to wait for a thread to finish. */
	public static final int THREAD_JOIN_TIMEOUT = 500;

	/** JSON factory and Object mapper. */
	private final Json json;

	/** Thread to exchange authorization code for access token. */
	private Thread tokenRequest;
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
	/** URI of the token server. */
	protected String tokenServer;
	/** Parameters passed to the authorization server. */
	protected Map<String, String> authorizeParams;
	/** Parameters passed to the token server. */
	protected Map<String, String> tokenParams;

	/**
	 * Ctor.
	 */
	public AuthorizationCodeGrant() {
		json = Json.getInstance();
		token = null;
		error = null;
		server = new RedirectServer();
		server.setRedirectCallback(this);
		authorizeParams = new HashMap<>();
		tokenParams = new HashMap<>();
		tokenRequest = null;
		ready = true;
		waitObject = new Object();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AuthorizationRequest authorize() {
		if (ready) {
			ready = false;
			if (tokenRequest != null) {
				try {
					tokenRequest.join(THREAD_JOIN_TIMEOUT);
				} catch (InterruptedException e) {
					/* ignore interrupted exception */
				}
			}
			tokenRequest = new Thread() {
				/**
				 * Obtain access (and refresh) token from the authorization server.
				 */
				@Override
				public void run() {
					obtainAccessToken();
				}
			};
		}
		String queryString = URLEncodedUtils.format(Utils.mapToList(authorizeParams), "UTF-8");
		return new AuthorizationRequest(authorizeServer + "?" + queryString);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		/* interrupt running thread */
		if (tokenRequest != null && tokenRequest.isAlive()) {
			tokenRequest.interrupt();
		}
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
			tokenRequest.join();
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
			tokenRequest.join();
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
	 * Sends a POST request to obtain an access token.
	 */
	private void obtainAccessToken() {
		try {
			/* build the request and send it to the token server */
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost request = new HttpPost(tokenServer);
			request.setEntity(new UrlEncodedFormEntity(Utils.mapToList(tokenParams)));
			CloseableHttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			/* get the response and parse it */
			JsonParser jp = json.getFactory().createParser(entity.getContent());
			while (jp.nextToken() != null) {
				JsonToken jsonToken = jp.getCurrentToken();
				switch (jsonToken) {
				case FIELD_NAME:
					String name = jp.getCurrentName();
					jsonToken = jp.nextToken();
					if (name.equals("access_token")) {
						token.setAccessToken(jp.getValueAsString());
					} else if (name.equals("token_type")) {
						token.setType(OAuth2TokenType.valueOf(jp.getValueAsString().toUpperCase()));
					} else if (name.equals("expires_in")) {
						token.setExpiresIn(jp.getValueAsInt());
					} else if (name.equals("refresh_token")) {
						token.setRefreshToken(jp.getValueAsString());
					} else if (name.equals("kid")) {
						token.setKeyId(jp.getValueAsString());
					} else if (name.equals("mac_key")) {
						token.setMacKey(jp.getValueAsString());
					} else if (name.equals("mac_algorithm")) {
						token.setMacAlgorithm(jp.getValueAsString());
					} else if (name.equals("error")) {
						error.setType(OAuth2ErrorType.valueOf(jp.getValueAsString().toUpperCase()));
					} else if (name.equals("error_description")) {
						error.setDescription(jp.getValueAsString());
					} else if (name.equals("error_uri")) {
						error.setUri(jp.getValueAsString());
					}
					ready = true;
					break;
				default:
					break;
				}
			}
			jp.close();
			response.close();
			client.close();
		} catch (IOException e) {
			error.setType(OAuth2ErrorType.SERVER_ERROR);
			error.setDescription("Failed to obtain access token from server.");

		}
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
							errorType += ": " + error.getDescription();
						}
						page.addBodyLine(errorType);
					}
					if (request.containsKey("error_uri")) {
						error.setUri(request.get("error_uri"));
						page.addBodyLine("<br />");
						page.addBodyLine("For more information, visit: <a href=\"" + error.getUri() + "\">" + request.get("error_uri") + "</a>");
					}
					page.addBodyLine("</p>");
				} else {
					if (request.containsKey("code")) { // successful authorization
						tokenParams.put("code", request.get("code"));
						if (!tokenRequest.isAlive()) {
							tokenRequest.start();
						}
						page.setTitle("Authorization successful");
						page.addBodyLine("<p id=\"success\">Authorization successful.</p>");
						page.addBodyLine("<p>You may now close this page and return to the application.</p>");
					} else { // authorization code not found in the request
						error.setType(OAuth2ErrorType.CODE_MISSING);
						error.setDescription("Authorization code missing.");
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
		if (error.getType() != OAuth2ErrorType.SUCCESS) {
			/* notify all waiting objects */
			synchronized (waitObject) {
				ready = true;
				waitObject.notifyAll();
			}
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
		if (Utils.isNullOrEmpty(settings.getTokenUri())) {
			throw new OAuth2SettingsException("Token server URI missing.");
		} else {
			tokenServer = settings.getTokenUri();
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
		authorizeParams.put("response_type", "code");
		authorizeParams.put("state", state);
		for (Entry<String, String> entry: settings.getExtraAuthorizeParams().entrySet()) {
			if (!authorizeParams.containsKey(entry.getKey())) {
				authorizeParams.put(entry.getKey(), entry.getValue());
			}
		}

		/* populate token request params */
		tokenParams.put("client_id", settings.getClientId());
		if (settings.getClientSecret() != null) {
			tokenParams.put("client_secret", settings.getClientSecret());
		}
		tokenParams.put("grant_type", "authorization_code");
		tokenParams.put("redirect_uri", settings.getRedirectUri());
		for (Entry<String, String> entry: settings.getExtraTokenParams().entrySet()) {
			if (!tokenParams.containsKey(entry.getKey())) {
				tokenParams.put(entry.getKey(), entry.getValue());
			}
		}
	}

}
