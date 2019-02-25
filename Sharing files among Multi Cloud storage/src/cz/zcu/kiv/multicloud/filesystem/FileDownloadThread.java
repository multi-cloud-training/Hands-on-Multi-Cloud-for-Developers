package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileDownloadThread.java			<br /><br />
 *
 * Worker thread for downloading partial file content from the storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileDownloadThread extends Thread {

	/** Threshold to stop after 5 failed requests. */
	public static final int FAIL_THRESHOLD = 5;

	/** Queue to get work from. */
	private final BlockingQueue<DataChunk> queue;
	/** Request to get the file data from. */
	private final HttpUriRequest request;
	/** File writer. */
	private final FileDownloadWriter writer;
	/** If the thread should terminate. */
	private boolean terminate;
	/** Number of failed requests. */
	private int failCount;
	/** Progress listener. */
	private final ProgressListener listener;
	/** Size of the chunk. */
	private final long chunkSize;

	/**
	 * Ctor with necessary parameters.
	 * @param queue Queue to get work from.
	 * @param request Request to get the file data from.
	 * @param writer File writer.
	 */
	public FileDownloadThread(BlockingQueue<DataChunk> queue, HttpUriRequest request, FileDownloadWriter writer, ProgressListener listener, long chunkSize) {
		this.queue = queue;
		this.request = request;
		this.writer = writer;
		this.listener = listener;
		this.chunkSize = chunkSize;
		this.terminate = false;
		this.failCount = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		byte[] buffer = new byte[(int) chunkSize];
		CloseableHttpClient client = HttpClients.createDefault();
		while (!shouldTerminate()) {
			try {
				if (queue.isEmpty()) {
					break;
				}
				DataChunk chunk = queue.take();
				if (chunk.size() != buffer.length) {
					buffer = new byte[(int) chunk.size()];
				}
				request.addHeader("Range", "bytes=" + chunk.getBeginByte() + "-" + (chunk.getEndByte() - 1));
				CloseableHttpResponse response = client.execute(request);
				request.removeHeaders("Range");
				if (response.getStatusLine().getStatusCode() >= 400) {
					queue.put(chunk);
					failCount++;
					if (failCount >= FAIL_THRESHOLD) {
						terminate = true;
					}
				} else {
					int read;
					int total = 0;
					InputStream content = response.getEntity().getContent();
					while (true) {
						read = content.read();
						if (read == -1 || total >= buffer.length) {
							break;
						}
						listener.addTransferred(1);
						buffer[total++] = (byte) read;
					}
					writer.write(buffer, chunk.getBeginByte());
				}
				response.close();
			} catch (InterruptedException e) {
				/* exception when the thread should finish */
			} catch (IOException e) {
				/* failed to download the chunk */
			}
		}
		try {
			client.close();
		} catch (IOException e) {
			/* ignore closing failure */
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
