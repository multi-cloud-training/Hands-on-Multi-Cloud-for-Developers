package cz.zcu.kiv.multicloud.filesystem;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileDownloadWriter.java			<br /><br />
 *
 * Random access file writer.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileDownloadWriter implements Closeable {

	/** Random access file. */
	private RandomAccessFile raf;

	/**
	 * Ctor with target file.
	 * @param file Target file.
	 */
	public FileDownloadWriter(File file) {
		try {
			raf = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			raf = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				/* ignore closing exception */
			}
		}
	}

	/**
	 * Synchronized method for writing data to the file.
	 * @param buffer Data buffer.
	 * @param position Position in the file.
	 */
	public synchronized void write(byte[] buffer, long position) {
		if (raf != null) {
			try {
				raf.seek(position);
				raf.write(buffer);
			} catch (IOException e) {
				/* ignore write failure */
			}
		}
	}

}
