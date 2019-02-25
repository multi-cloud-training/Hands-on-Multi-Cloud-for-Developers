package cz.zcu.kiv.multicloud.json;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import cz.zcu.kiv.multicloud.http.HttpMethod;

/**
 * cz.zcu.kiv.multicloud.json/CloudRequest.java			<br /><br />
 *
 * Bean for holding basic information about a request to the cloud storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CloudRequest {

	/** URI of the request. */
	private String uri;
	/** HTTP method of the request. */
	private HttpMethod method;
	/** Parameters of the request. */
	private Map<String, String> params;
	/** Header for the request. */
	private Map<String, String> headers;
	/** Mapping of the returned JSON parameters to object properties. */
	private Map<String, String> mapping;
	/** Body of the request. */
	private String body;
	/** JSON body of the request. Empty parameters are omitted in the final request. */
	@JsonProperty("json_body")
	private Map<String, Object> jsonBody;
	/** Authorization parameter for accessing protected resources. Null or empty to disable. */
	@JsonProperty("auth_param")
	private String authorizationParam;

	/**
	 * Returns the authorization parameter.
	 * @return Authorization parameter.
	 */
	public String getAuthorizationParam() {
		return authorizationParam;
	}

	/**
	 * Returns the body of the request.
	 * @return Body of the request.
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Returns the headers for the request.
	 * @return Headers.
	 */
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Returns the JSON body of the request.
	 * @return JSON body of the request.
	 */
	public Map<String, Object> getJsonBody() {
		return jsonBody;
	}

	/**
	 * Returns the JSON parameters mapping.
	 * @return JSON parameters mapping.
	 */
	public Map<String, String> getMapping() {
		return mapping;
	}

	/**
	 * Returns the HTTP method of the request.
	 * @return HTTP method of the request.
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Return the list of all parameters of the request.
	 * @return List of parameters.
	 */
	public Map<String, String> getParams() {
		return params;
	}

	/**
	 * Returns the URI of the request.
	 * @return URI of the request.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the authorization parameter for accessing protected resources. Null or empty to disable.
	 * @param authorizationParam Authorization parameter.
	 */
	public void setAuthorizationParam(String authorizationParam) {
		this.authorizationParam = authorizationParam;
	}

	/**
	 * Sets the body of the request.
	 * @param body Body of the request.
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Sets the headers for the request.
	 * @param headers Headers.
	 */
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	/**
	 * Sets the JSON body of the request.
	 * @param jsonBody JSON body of the request.
	 */
	public void setJsonBody(Map<String, Object> jsonBody) {
		this.jsonBody = jsonBody;
	}

	/**
	 * Sets the JSON parameters mapping.
	 * @param mapping JSON parameters mapping.
	 */
	public void setMapping(Map<String, String> mapping) {
		this.mapping = mapping;
	}

	/**
	 * Sets the HTTP method of the request.
	 * @param method HTTP method of the request.
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	/**
	 * Sets the list of all parameters of the request.
	 * @param params List of parameters.
	 */
	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	/**
	 * Sets the URI of the request.
	 * @param uri URI of the request.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

}
