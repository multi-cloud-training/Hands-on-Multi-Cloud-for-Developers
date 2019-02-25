package cz.zcu.kiv.multicloud.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.client.methods.HttpUriRequest;

import cz.zcu.kiv.multicloud.MultiCloudException;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileDownloadOp.java			<br /><br />
 *
 * Operation for downloading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileDownloadOp extends Operation<File> {

	/** Minimum size of chunk for file download, set to 256 kiB. */
	public static final long MIN_CHUNK_SIZE = 256 * 1024;
	/** Maximum size of chunk for file download, set to 16 MiB. */
	public static final long MAX_CHUNK_SIZE = 16 * 1024 * 1024;
	/** Optimal number of chunks for each worker. */
	public static final int CHUNK_NUM_PER_WORKER = 5;

	/** List of sources used to download the file. */
	private final List<FileCloudSource> sources;
	/** Destination to save the file to. */
	private final File destination;
	/** Thread pool of worker threads. */
	private final List<FileDownloadThread> pool;
	/** Queue with chunks for the workers. */
	private final BlockingQueue<DataChunk> queue;
	/** File writer. */
	private FileDownloadWriter writer;
	/** Progress listener. */
	private final ProgressListener listener;
	/** Lock object for concurrent method calls. */
	private final Object lock;

	/**
	 * Ctor with necessary parameters.
	 * @param sources List of sources used to download the file.
	 * @param destination Destination to save the file to.
	 */
	public FileDownloadOp(List<FileCloudSource> sources, File destination, ProgressListener listener) {
		super(OperationType.FILE_DOWNLOAD, null, null);
		this.sources = new ArrayList<>();
		for (FileCloudSource pair: sources) {
			if (pair.getFile() != null && pair.getExecRequest() != null) {
				this.sources.add(pair);
			}
		}
		this.destination = destination;
		this.pool = new ArrayList<>();
		this.queue = new LinkedBlockingQueue<>();
		this.listener = listener;
		this.lock = new Object();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void abort() {
		synchronized (lock) {
			for (FileDownloadThread thread: pool) {
				thread.terminate();
			}
			isAborted = true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationBegin() throws MultiCloudException {
		/* no preparation necessary */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		if (!sources.isEmpty()) {
			/* remove inconsistent files */
			FileCloudSource base = sources.get(0);
			List<FileCloudSource> remove = new ArrayList<>();
			for (FileCloudSource source: sources) {
				if (!source.equals(base)) {
					if (!base.getFile().equals(source.getFile())) {
						remove.add(source);
					}
				}
			}
			sources.removeAll(remove);
			/* prepare chunks */
			long pos = 0;
			long size = base.getFile().getSize();
			long chunk = MIN_CHUNK_SIZE;
			listener.setTotalSize(size);
			/* dynamic chunk size */
			while (size / chunk > sources.size() * CHUNK_NUM_PER_WORKER) {
				chunk *= 2;
				if (chunk >= MAX_CHUNK_SIZE) {
					break;
				}
			}
			while (size > chunk) {
				queue.add(new DataChunk(pos, pos + chunk));
				pos += chunk;
				size -= chunk;
			}
			queue.add(new DataChunk(pos, pos + size));
			/* open file for writing */
			writer = new FileDownloadWriter(destination);
			/* create threads and start them */
			for (FileCloudSource source: sources) {
				setToken(source.getToken());
				setRequest(source.getExecRequest());
				addPropertyMapping("download_url", source.getFile().getDownloadUrl());
				addPropertyMapping("id", source.getFile().getId());
				addPropertyMapping("path", source.getFile().getPath());
				HttpUriRequest request = prepareRequest(null);
				pool.add(new FileDownloadThread(queue, request, writer, listener, chunk));
			}
			for (FileDownloadThread thread: pool) {
				thread.start();
			}
			for (FileDownloadThread thread: pool) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					/* join interrupted */
				}
			}
			/* close the file after writing all the data and set the result */
			listener.finishTransfer();
			writer.close();
			if (queue.isEmpty()) {
				setResult(destination);
			} else {
				synchronized (lock) {
					if (!isAborted) {
						throw new MultiCloudException("Failed to download the file.");
					}
				}
			}
		} else {
			throw new MultiCloudException("No source specified.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationFinish() throws MultiCloudException {
		/* no finalization necessary */
	}

}
