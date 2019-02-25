package cz.zcu.kiv.multicloud.oauth2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.oauth2/RedirectServer.java			<br /><br />
 *
 * Simple HTTP server listening for OAuth 2.0 authorization code redirect. Its behavior fits the step C of figure 3 of section 4.1. of RFC 6749.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RedirectServer implements Container {

	/** Default local address of the server. */
	public static final String DEFAULT_LOCAL_ADDRESS = "127.0.0.1";
	/** Default local port of the server. */
	public static final int DEFAULT_LOCAL_PORT = 0;
	/** Minimum length of the state parameter. */
	public static final int STATE_MIN_LEN = 16;
	/** Maximum length of the state parameter. */
	public static final int STATE_MAX_LEN = 32;

	/** Actual address the server is listening on. */
	protected String boundAddress;
	/** Actual port the server is listening on. */
	protected int boundPort;
	/** Local address of the server. */
	protected String localAddress;
	/** Local port of the server. */
	protected int localPort;
	/** Callback called after the server receives a request. */
	protected RedirectCallback redirectCallback;

	/** Active connection of the server. */
	protected Connection connection;

	/**
	 * Ctor.
	 */
	public RedirectServer() {
		boundAddress = "";
		boundPort = -1;
		localAddress = DEFAULT_LOCAL_ADDRESS;
		localPort = DEFAULT_LOCAL_PORT;
		redirectCallback = null;
		connection = null;
	}

	/**
	 * Method for generating a random state parameter with the option to include the port number the server is listening on.
	 * @param encodePortNumber If port number should be included.
	 * @return Random state parameter.
	 */
	public String generateRandomState(boolean encodePortNumber) {
		StringBuilder sb = new StringBuilder();
		Random r = new Random();
		int stateLength = r.nextInt(STATE_MAX_LEN - STATE_MIN_LEN) + STATE_MIN_LEN;
		int length = 0;
		/* include port number */
		if (encodePortNumber) {
			sb.append(boundPort);
			sb.append('-');
			length = sb.length();
		}
		/* generate random data */
		while (length < stateLength) {
			char ch = (char) r.nextInt();
			if (Utils.isUriLetterOrDigit(ch)) {
				sb.append(ch);
				length++;
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the actual address the server is listening on.
	 * @return Actual address.
	 */
	public String getBoundAddress() {
		return boundAddress;
	}

	/**
	 * Returns the actual port number the server is listening on.
	 * @return Actual port number.
	 */
	public int getBoundPort() {
		return boundPort;
	}

	/**
	 * Return the whole URI the server is listening on.
	 * @return Whole URI of the server.
	 */
	public String getBoundUri() {
		return "http://" + boundAddress + ":" + boundPort;
	}

	/**
	 * Returns local address of the server.
	 * @return Local address.
	 */
	public String getLocalAddress() {
		return localAddress;
	}

	/**
	 * Returns local port of the server.
	 * @return Local port.
	 */
	public int getLocalPort() {
		return localPort;
	}

	/**
	 * Returns the callback after the server receives a request.
	 * @return Callback.
	 */
	public RedirectCallback getRedirectCallback() {
		return redirectCallback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(Request request, Response response) {
		try {
			/* get all the parameters from the request */
			Map<String, String> params = new HashMap<>();
			params.putAll(request.getQuery());

			/* prepare a web page as a response */
			WebPage page = null;
			if (!request.getQuery().isEmpty()) {
				if (redirectCallback != null) {
					page = redirectCallback.onRedirect(params);
				}
			}

			/* return the web page */
			if (page != null) {
				response.setStatus(page.getStatus());
				for (Entry<String, String> entry: page.getHeaders().entrySet()) {
					response.setValue(entry.getKey(), entry.getValue());
				}
				PrintStream body = response.getPrintStream();
				for (String line: page.getContentLines()) {
					body.println(line);
				}
				body.close();
			} else {
				response.getPrintStream().close();
			}
		} catch (IOException e) {
			/* ignore handling exception */
		}
	}

	/**
	 * Returns if the server is running.
	 * @return If the server is running.
	 */
	public boolean isRunning() {
		return (connection != null);
	}

	/**
	 * Sets local address of the server.
	 * @param address Local address.
	 */
	public void setLocalAddress(String address) {
		localAddress = address;
	}

	/**
	 * Sets local port of the server.
	 * @param port
	 */
	public void setLocalPort(int port) {
		localPort = port;
	}

	/**
	 * Sets the callback after the server receives a request.
	 * @param callback Callback.
	 */
	public void setRedirectCallback(RedirectCallback callback) {
		redirectCallback = callback;
	}

	/**
	 * Method to start the server.
	 * @throws IllegalStateException Exception thrown if the server is already running.
	 * @throws IOException Exception when the server is unable to start.
	 */
	public void start() throws IllegalStateException, IOException {
		if (connection != null) {
			throw new IllegalStateException("Server already started.");
		}
		Server server = new ContainerServer(this);
		connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(InetAddress.getByName(localAddress), localPort);
		SocketAddress actual = connection.connect(address);
		if (actual instanceof InetSocketAddress) {
			boundAddress = ((InetSocketAddress)actual).getHostString();
			boundPort = ((InetSocketAddress)actual).getPort();
		}
	}

	/**
	 * Stops the server.
	 * @throws IOException Exception when there is a problem with stopping the server.
	 */
	public void stop() throws IOException {
		if (connection != null) {
			connection.close();
			connection = null;
			boundAddress = "";
			boundPort = -1;
		}
	}
}
