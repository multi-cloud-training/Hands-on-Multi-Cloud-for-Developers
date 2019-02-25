package cz.zcu.kiv.multicloud.oauth2;

import java.util.ArrayList;
import java.util.List;

/**
 * cz.zcu.kiv.multicloud.oauth2/RedirectWebPage.java			<br /><br />
 *
 * Simple HTML web page serving as a response after authorization of the application on an external authorization site.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RedirectWebPage extends WebPage {

	/** Title of the web page. */
	private String title;
	/** Lines of CSS code defining the visual outlook of the web page. */
	private final List<String> styleLines;
	/** Lines of the body of the web page. */
	private final List<String> bodyLines;

	/**
	 * Ctor.
	 */
	public RedirectWebPage() {
		title = "";
		styleLines = new ArrayList<>();
		bodyLines = new ArrayList<>();
		populateStyle();
	}

	/**
	 * Adding lines to the body of the web page.
	 * @param line Line of HTML code.
	 */
	public void addBodyLine(String line) {
		bodyLines.add(line);
	}

	/**
	 * {@inheritDoc}
	 */	@Override
	 public List<String> getContentLines() {
		 List<String> content = new ArrayList<>();
		 content.add("<!doctype html>");
		 content.add("<head>");
		 content.add("<title>" + title + "</title>");
		 content.add("<style type=\"text/css\">");
		 content.addAll(styleLines);
		 content.add("</style>");
		 content.add("</head>");
		 content.add("<body>");
		 content.addAll(bodyLines);
		 content.add("</body>");
		 content.add("</html>");
		 return content;
	 }

	 /**
	  * Internal method to populate the styles used on the web page.
	  */
	 private void populateStyle() {
		 styleLines.add("body{background:#f8f8f8;color:#2b5061;font-family:Verdana,Arial,sans-serif;font-size:11pt}");
		 styleLines.add("p{padding:16px}");
		 styleLines.add("code{font-size:13pt;font-weight:bold;padding:0 8px}");
		 styleLines.add("#success{background:#00b526;color:#f8f8f8;font-size:18pt}");
		 styleLines.add("#error{background:#c40f0f;color:#f8f8f8;font-size:18pt}");
	 }

	 /**
	  * Sets the title of the web page to a desired string.
	  * @param title Title of the web page.
	  */
	 public void setTitle(String title) {
		 this.title = title;
	 }

}
