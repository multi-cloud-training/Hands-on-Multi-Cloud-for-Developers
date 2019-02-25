package cz.zcu.kiv.multicloud.filesystem;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * cz.zcu.kiv.multicloud.filesystem/CountingInputStream.java			<br /><br />
 *
 * Input stream counting number of bytes read and reporting them to the listener.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class CountingInputStream extends FilterInputStream {

	/** Listener to receive reports. */
	private final ProgressListener listener;

	/**
	 * Ctor with input stream and listener.
	 * @param stream Input stream.
	 * @param listener Listener.
	 */
	public CountingInputStream(InputStream stream, ProgressListener listener) {
		super(stream);
		this.listener = listener;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		super.close();
		if (listener != null) {
			listener.finishTransfer();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		int read = super.read();
		if (listener != null) {
			listener.addTransferred(read);
		}
		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b) throws IOException {
		int read = super.read(b);
		if (listener != null) {
			listener.addTransferred(read);
		}
		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = super.read(b, off, len);
		if (listener != null) {
			listener.addTransferred(read);
		}
		return read;
	}

}
