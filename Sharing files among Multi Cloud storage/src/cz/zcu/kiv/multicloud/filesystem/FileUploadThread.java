package cz.zcu.kiv.multicloud.filesystem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.UploadSession;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileUploadThread.java			<br /><br />
 *
 * Worker thread for uploading file to the storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileUploadThread extends Thread {

	/** Parent operation. */
	private final FileUploadOp operation;
	/** Destination information for the upload. */
	private final FileCloudSource destination;
	/** Current request. */
	private HttpUriRequest request;
	/** HTTP client of the upload thread. */
	private CloseableHttpClient client;
	/** If the thread should terminate. */
	private boolean terminate;
	/** Lock object for concurrent method calls. */
	private final Object lock;

	/** Size of the data uploaded. */
	private final long size;
	/** File data to be uploaded. */
	private final InputStream data;
	/** Number of bytes already sent to the server. */
	private long transferred;
	/** Identifier of the chunked upload session. */
	private UploadSession session;
	/** Byte buffer of the last chunk. */
	private byte[] buffer;
	/** If the upload failed. */
	private boolean failed;

	/**
	 * Ctor with necessary parameters.
	 * @param operation Parent operation.
	 * @param destination Information about the destination.
	 * @param file File to be uploaded.
	 * @throws MultiCloudException Exception if there is a problem with the file.
	 */
	public FileUploadThread(FileUploadOp operation, FileCloudSource destination, File file) throws MultiCloudException {
		this.operation = operation;
		this.destination = destination;
		this.lock = new Object();
		transferred = 0;
		failed = false;

		try {
			data = new FileInputStream(file);
			size = file.length();
		} catch (FileNotFoundException e) {
			throw new MultiCloudException("File not found.");
		}
	}

	/**
	 * Beginning of the chunked upload. If the request parameters for this method are supplied, chunked upload is started.
	 * The main purpose of this method is to obtain a session identifier for further data upload.
	 * This request uploads maximum of one single chunk of data.
	 * @throws IOException If something fails.
	 */
	private void begin() throws IOException {
		CloseableHttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("Failed to upload the file.");
		}
		session = operation.getParsedSessionResponse(destination.getBeginRequest(), response);
		response.close();
	}

	/**
	 * Chunked upload progress. Uploads all the remaining data chunks.
	 * @throws IOException If something fails.
	 */
	private void exec() throws IOException {
		CloseableHttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("Failed to upload the file.");
		}
		response.close();
	}

	/**
	 * Finish the upload of the file. If no data were submitted so far, a normal direct upload is performed.
	 * @throws IOException If something fails.
	 */
	private void finish() throws IOException {
		CloseableHttpResponse response = client.execute(request);
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new IOException("Failed to upload the file.");
		}
		response.close();
	}

	/**
	 * Returns if the upload failed.
	 * @return If the upload failed.
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Read chunk from the input stream, save it to a buffer and return input stream made off that buffer.
	 * @return Chunk data stream.
	 */
	private ByteArrayInputStream readData() {
		long size = FileUploadOp.CHUNK_SIZE;
		if (this.size - transferred < size) {
			size = this.size - transferred;
		}
		buffer = new byte[(int) size];
		try {
			data.read(buffer, 0, (int) size);
		} catch (IOException e) {
			/* returns empty buffer */
		}
		return new ByteArrayInputStream(buffer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		client = HttpClients.createDefault();

		try {
			ByteArrayInputStream chunk = null;
			/* begin the upload */
			if (destination.getBeginRequest() != null) {
				chunk = readData();
				synchronized (lock) {
					request = operation.getPreparedRequest(destination, destination.getBeginRequest(), null, chunk, transferred, buffer.length);
				}
				begin();
			}
			if (session != null) {
				transferred = session.getOffset();
			}
			/* upload the data */
			if (destination.getExecRequest() != null) {
				while (transferred < size && !shouldTerminate()) {
					if (transferred > 0) {
						chunk = readData();
					}
					synchronized (lock) {
						request = operation.getPreparedRequest(destination, destination.getExecRequest(), session, chunk, transferred, buffer.length);
					}
					exec();
					transferred += buffer.length;
				}
			}
			/* finish the upload */
			if (destination.getFinishRequest() != null) {
				if (!shouldTerminate()) {
					synchronized (lock) {
						request = operation.getPreparedRequest(destination, destination.getFinishRequest(), session, data, transferred, size);
					}
					finish();
				}
			}
		} catch (MultiCloudException | IOException e) {
			failed = true;
		}

		/* close the streams when the operation finishes */
		try {
			if (data != null) {
				data.close();
			}
			client.close();
		} catch (IOException e) {
			/* ignore closing exception */
		}
	}

	/**
	 * Synchronized method to determine if the thread should terminate.
	 * @return If the thread should terminate.
	 */
	public synchronized boolean shouldTerminate() {
		return terminate;
	}

	/**
	 * Synchronized method to tell the thread that it should terminate.
	 */
	public synchronized void terminate() {
		terminate = true;
		request.abort();
		interrupt();
	}

}
