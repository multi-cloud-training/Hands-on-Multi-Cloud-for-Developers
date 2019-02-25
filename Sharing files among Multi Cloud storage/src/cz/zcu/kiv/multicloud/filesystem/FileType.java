package cz.zcu.kiv.multicloud.filesystem;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileType.java			<br /><br />
 *
 * File type used in {@link cz.zcu.kiv.multicloud.json.FileInfo} to determine if the metadata belongs to a folder or a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum FileType {

	FOLDER("folder"),					// default option
	FILE("file");

	/** Recognized file type strings. */
	public static final String[] FILE_TYPES = new String[] {
		"file",
		"notebook",
		"audio",
		"photo",
		"video"
	};

	/**
	 * Convert string to enumeration ignoring case.
	 * @param text String to be converted.
	 * @return Enumeration returned.
	 */
	public static FileType fromString(String text) {
		if (text != null) {
			/* try to match the supplied text with associated strings */
			for (FileType type: FileType.values()) {
				if (text.equalsIgnoreCase(type.text)) {
					return type;
				}
			}
			for (String s: FILE_TYPES) {
				if (text.equalsIgnoreCase(s)) {
					return FILE;
				}
			}
		}
		/* if the text doesn't match any option, return folder as default */
		return FOLDER;
	}

	/** Text of the enumeration. */
	private String text;

	/**
	 * Ctor with parameter.
	 * @param text Text of the enumeration.
	 */
	FileType(String text) {
		this.text = text;
	}

	/**
	 * Returns the text of the enumeration.
	 * @return Text of the enumeration.
	 */
	public String getText() {
		return text;
	}

}
