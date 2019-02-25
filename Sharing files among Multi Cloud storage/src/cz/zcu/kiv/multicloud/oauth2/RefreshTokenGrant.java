package cz.zcu.kiv.multicloud.oauth2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import cz.zcu.kiv.multicloud.json.Json;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.oauth2/RefreshTokenGrant.java			<br /><br />
 *
 * Implementation of the <a href="http://tools.ietf.org/html/rfc6749#section-6">Refreshing an Access Token</a> section of the OAuth 2.0 specification.
 * For easier use, it implements the {@link cz.zcu.kiv.multicloud.oauth2.OAuth2Grant}.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RefreshTokenGrant implements OAuth2Grant {

	/** Time to wait for a thread to finish. */
	public static final int THREAD_JOIN_TIMEOUT = 500;

	/** JSON factory and Object mapper. */
	private final Json json;

	/** Thread to exchange authorization code for access token. */
	private Thread tokenRequest;

	/** OAuth access and optional refresh token. */
	protected OAuth2Token token;
	/** OAuth error that occurred. */
	protected OAuth2Error error;
	/** If the OAuth token and error are ready. */
	protected boolean ready;
	/** Synchronization object. */
	protected Object waitObject;

	/** URI of the token server. */
	protected String tokenServer;
	/** Parameters passed to the token server. */
	protected Map<String, String> tokenParams;

	/**
	 * Ctor.
	 */
	public RefreshTokenGrant() {
		json = Json.getInstance();
		token = null;
		error = null;
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
			tokenRequest.start();
		}
		return new AuthorizationRequest(null);
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
			token = new OAuth2Token();
			error = new OAuth2Error();
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
			error.setDescription("Failed to obtain access token from the server.");
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
	public void setup(OAuth2Settings settings) throws OAuth2SettingsException {
		/* validate supplied settings */
		if (settings == null) {
			throw new OAuth2SettingsException("Missing settings.");
		}
		if (Utils.isNullOrEmpty(settings.getTokenUri())) {
			throw new OAuth2SettingsException("Token server URI missing.");
		} else {
			tokenServer = settings.getTokenUri();
		}
		if (Utils.isNullOrEmpty(settings.getRefreshToken())) {
			throw new OAuth2SettingsException("Refresh token cannot be null or empty.");
		}
		if (Utils.isNullOrEmpty(settings.getClientId())) {
			throw new OAuth2SettingsException("Client ID cannot be null or empty.");
		}
		if (settings.getClientSecret() != null) {
			tokenParams.put("client_secret", settings.getClientSecret());
		}
		if (!Utils.isNullOrEmpty(settings.getScope())) {
			tokenParams.put("scope", settings.getScope());
		}

		/* populate token request params */
		tokenParams.put("client_id", settings.getClientId());
		tokenParams.put("grant_type", "refresh_token");
		tokenParams.put("refresh_token", settings.getRefreshToken());
		for (Entry<String, String> entry: settings.getExtraTokenParams().entrySet()) {
			if (!tokenParams.containsKey(entry.getKey())) {
				tokenParams.put(entry.getKey(), entry.getValue());
			}
		}
	}

}
