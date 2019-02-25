package cz.zcu.kiv.multicloud.utils;

/**
 * cz.zcu.kiv.multicloud.utils/FileSerializationType.java			<br /><br />
 *
 * List of supported serialization schemes for storing data in a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public enum FileSerializationType {

	OBJECT,				// use default Java Object serialization
	JSON				// use Jackson JSON serialization

}
