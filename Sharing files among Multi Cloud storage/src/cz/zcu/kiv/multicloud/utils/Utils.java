package cz.zcu.kiv.multicloud.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import cz.zcu.kiv.multicloud.json.CloudSettings;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Settings;

/**
 * cz.zcu.kiv.multicloud.utils/Utils.java			<br /><br />
 *
 * General purpose methods for use in the multicloud core.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class Utils {

	/**
	 * cz.zcu.kiv.multicloud.utils/Utils.java
	 *
	 * List of supported size formattings.
	 *
	 * @author Jaromír Staněk
	 * @version 1.0
	 *
	 */
	public static enum UnitsFormat {
		DECIMAL,
		BINARY
	}

	/** List of decimal based file size units. */
	public static final String[] SIZE_DECIMAL_UNITS = {
		"B",
		"kB",
		"MB",
		"GB",
		"TB",
		"PB",
		"EB",
		"ZB",
		"YB",
		"?B"
	};
	/** List of binary based file size units. */
	public static final String[] SIZE_BINARY_UNITS = {
		"B",
		"KiB",
		"MiB",
		"GiB",
		"TiB",
		"PiB",
		"EiB",
		"ZiB",
		"YiB",
		"?iB"
	};
	/** Division step for decimal file size units. */
	public static final int SIZE_DECIMAL_STEP = 1000;
	/** Division step for binary file size units. */
	public static final int SIZE_BINARY_STEP = 1024;

	/**
	 * Extracts {@link cz.zcu.kiv.multicloud.oauth2.OAuth2Settings} from {@link cz.zcu.kiv.multicloud.json.CloudSettings}.
	 * @param settings Cloud settings.
	 * @return Authorization settings.
	 */
	public static OAuth2Settings cloudSettingsToOAuth2Settings(CloudSettings settings) {
		/* check the cloud settings */
		if (settings.getTokenRequest() == null) {
			return null;
		}
		OAuth2Settings out = new OAuth2Settings();
		/* fill the OAuth2 settings */
		if (settings.getAuthorizeRequest() == null) {
			return null;
		} else {
			out.setAuthorizeUri(settings.getAuthorizeRequest().getUri());
			if (settings.getAuthorizeRequest().getParams() != null) {
				for (Entry<String, String> param: settings.getAuthorizeRequest().getParams().entrySet()) {
					out.addExtraAuthorizeParams(param.getKey(), param.getValue());
				}
			}
		}
		out.setTokenUri(settings.getTokenRequest().getUri());
		if (settings.getTokenRequest().getParams() != null) {
			for (Entry<String, String> param: settings.getTokenRequest().getParams().entrySet()) {
				out.addExtraTokenParams(param.getKey(), param.getValue());
			}
		}
		out.setClientId(settings.getClientId());
		out.setClientSecret(settings.getClientSecret());
		out.setGrantType(settings.getGrantType());
		out.setGrantClass(settings.getGrantClass());
		out.setRedirectUri(settings.getRedirectUri());
		out.setScope(settings.getScope());
		out.setUsername(settings.getUsername());
		out.setPassword(settings.getPassword());
		return out;
	}

	/**
	 * Parses the supplied string and obtains URI query parameters.
	 * @param s String to be parsed.
	 * @return Obtained parameters.
	 */
	public static Map<String, String> extractParams(String s) {
		Map<String, String> params = new HashMap<>();
		URI uri;
		try {
			uri = new URI(s);
			List<NameValuePair> list = URLEncodedUtils.parse(uri, "UTF-8");
			for (NameValuePair pair: list) {
				params.put(pair.getName(), pair.getValue());
			}
		} catch (URISyntaxException e) {
			/* do nothing if the string doesn't match the URI syntax */
		}
		return params;
	}

	/**
	 * Formats the file size to human readable string. Final number resolution is to two decimal places.
	 * @param size File size to be formated.
	 * @param format Formatting used to format the size.
	 * @return Human readable string.
	 */
	public static String formatSize(long size, UnitsFormat format) {
		String s = null;
		double decimalSize = size;
		int index = 0;
		switch (format) {
		case DECIMAL:
			while (decimalSize >= SIZE_DECIMAL_STEP) {
				decimalSize /= SIZE_DECIMAL_STEP;
				index++;
			}
			if (index >= SIZE_DECIMAL_UNITS.length) {
				index = SIZE_DECIMAL_UNITS.length - 1;
			}
			s = String.format("%.2f %s", decimalSize, SIZE_DECIMAL_UNITS[index]);
			break;
		case BINARY:
			while (decimalSize >= SIZE_BINARY_STEP) {
				decimalSize /= SIZE_BINARY_STEP;
				index++;
			}
			if (decimalSize >= SIZE_DECIMAL_STEP) {
				decimalSize /= SIZE_BINARY_STEP;
				index++;
			}
			if (index >= SIZE_BINARY_UNITS.length) {
				index = SIZE_BINARY_UNITS.length - 1;
			}
			s = String.format("%.2f %s", decimalSize, SIZE_BINARY_UNITS[index]);
			break;
		}
		return s;
	}

	/**
	 * Removes files that are not visible from the folder listing.
	 * @param contents Folder listing.
	 * @param visible Visible files.
	 * @return Visible folder listing.
	 */
	public static FileInfo formVisibilityTree(FileInfo contents, FileInfo visible) {
		if (contents == null || visible == null) {
			return contents;
		}
		List<FileInfo> remove = new ArrayList<>();
		for (FileInfo file: contents.getContent()) {
			boolean found = false;
			for (FileInfo v: visible.getContent()) {
				if (file.getId().equals(v.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				remove.add(file);
			}
		}
		/* manual removal due to overloaded equals method of FileInfo */
		for (FileInfo r: remove) {
			for (int i = 0; i < contents.getContent().size(); i++) {
				FileInfo file = contents.getContent().get(i);
				if (file.getId().equals(r.getId())) {
					contents.getContent().remove(i);
					break;
				}
			}
		}
		return contents;
	}

	/**
	 * Check if the supplied string is null, zero length or contains only whitespace characters.
	 * @param s Tested string.
	 * @return If it is null or empty.
	 */
	public static boolean isNullOrEmpty(String s) {
		return (s == null || s.isEmpty() || s.trim().isEmpty());
	}

	/**
	 * Determines if the supplied character belongs to the group of digits allowed in URI.
	 * Allowed digits conform to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC3986 Unreserved Characters</a> to group identified as DIGIT.
	 * DIGIT is defined as hexadecimal range 0x30-0x39.
	 * @param c Tested character.
	 * @return If it is allowed in URI.
	 */
	public static boolean isUriDigit(char c) {
		return ((c >= '0') && (c <= '9'));
	}

	/**
	 * Determines if the supplied character belongs to the group of letters allowed in URI.
	 * Allowed letters conform to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC3986 Unreserved Characters</a> to group identified as ALPHA.
	 * ALPHA is defined as hexadecimal ranges 0x41-0x5A and 0x61-0x7A.
	 * @param c Tested character.
	 * @return If it is allowed in URI.
	 */
	public static boolean isUriLetter(char c) {
		return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')));
	}

	/**
	 * Determines if the supplied character belongs to the group of letters or digits allowed in URI.
	 * Allowed letters and digits conform to <a href="http://tools.ietf.org/html/rfc3986#section-2.3">RFC3986 Unreserved Characters</a> to groups identified as ALPHA and DIGIT.
	 * ALPHA is defined as hexadecimal ranges 0x41-0x5A and 0x61-0x7A.
	 * DIGIT is defined as hexadecimal range 0x30-0x39.
	 * @param c Tested character.
	 * @return If it is allowed in URI.
	 */
	public static boolean isUriLetterOrDigit(char c) {
		return (isUriLetter(c) || isUriDigit(c));
	}

	/**
	 * Converts a {@link java.util.Map} to a {@link java.util.List} of {@link org.apache.http.NameValuePair} objects.
	 * @param map Map to be converted.
	 * @return List of NameValuePair objects.
	 */
	public static List<NameValuePair> mapToList(Map<String, String> map) {
		List<NameValuePair> list = new LinkedList<>();
		if (map != null) {
			for (Entry<String, String> entry: map.entrySet()) {
				list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		return list;
	}

}
