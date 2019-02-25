package cz.zcu.kiv.multicloud.filesystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.json.UploadSession;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.filesystem/FileUploadOp.java			<br /><br />
 *
 * Operation for uploading a file.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FileUploadOp extends Operation<FileInfo> {

	/** Size of a chunk for file download. Default value is set to 4 MiB. */
	public static final long CHUNK_SIZE = 4 * 1024 * 1024;
	/** String to indicate that the body of the request should contain upload data. */
	public static final String DATA_MAPPING = "<data>";

	/** List of destinations to upload the file to. */
	private final List<FileCloudSource> destinations;
	/** Thread pool of worker threads. */
	private final List<FileUploadThread> pool;
	/** File to be uploaded. */
	private final File data;
	/** Progress listener. */
	private final ProgressListener listener;
	/** Lock object for concurrent method calls. */
	private final Object lock;
	/** If all the uploads are done. */
	private boolean done;

	/**
	 * Ctor with necessary parameters.
	 * @param destinations List of destinations to upload the file to.
	 * @param overwrite If the destination file should be overwritten.
	 * @param data The uploaded file.
	 * @param listener Progress listener.
	 */
	public FileUploadOp(List<FileCloudSource> destinations, boolean overwrite, File data, ProgressListener listener) {
		super(OperationType.FILE_UPLOAD, null, null);
		this.destinations = destinations;
		this.data = data;
		this.listener = listener;
		this.pool = new ArrayList<>();

		if (this.listener != null) {
			this.listener.setTotalSize(data.length());
		}
		addPropertyMapping("overwrite", overwrite ? "true" : "false");
		addPropertyMapping("size", String.valueOf(data.length()));
		lock = new Object();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void abort() {
		synchronized (lock) {
			for (FileUploadThread thread: pool) {
				thread.terminate();
			}
			isAborted = true;
		}
	}

	/**
	 * Method for parsing upload session information out of a response.
	 * @param request Request to which the response belongs to.
	 * @param response Response to be parsed.
	 * @return Upload session.
	 */
	protected synchronized UploadSession getParsedSessionResponse(CloudRequest request, HttpResponse response) {
		setRequest(request);
		for (Header header: response.getAllHeaders()) {
			responseHeaders.put(header.getName(), header.getValue());
		}
		UploadSession session = null;
		try {
			if (response.getStatusLine().getStatusCode() < 400) {
				JsonNode tree = parseJsonResponse(response);
				if (tree != null) {
					session = json.getMapper().treeToValue(tree, UploadSession.class);
				} else {
					for (Entry<String, String> header: responseHeaders.entrySet()) {
						if (header.getKey().equals("Location")) {
							responseParams.putAll(Utils.extractParams(header.getValue()));
							doResponseParamsMapping();
							session = new UploadSession();
							session.setSession(responseParams.get("session"));
							try {
								long offset = Long.parseLong(responseParams.get("offset"));
								session.setOffset(offset);
							} catch (NumberFormatException e) {
								session.setOffset(0);
							}
						}
					}
				}
			}
		} catch (IllegalStateException | IOException e) {
			/* return null value instead of throwing exception */
		}
		return session;
	}

	/**
	 * Method for preparing a request for the worker thread.
	 * @param dst Information about the destination.
	 * @param request Request to be prepared.
	 * @param session Upload session information.
	 * @param data Data to be transferred.
	 * @param transferred Amount of data already transferred.
	 * @param buffer Size of the data to be transferred.
	 * @return Request for the worker.
	 * @throws MultiCloudException If preparation of the request failed.
	 */
	protected synchronized HttpUriRequest getPreparedRequest(FileCloudSource dst, CloudRequest request, UploadSession session, InputStream data, long transferred, long buffer) throws MultiCloudException {
		HttpUriRequest preparedRequest = null;
		setToken(dst.getToken());
		setRequest(request);
		Map<String, Object> jsonBody = request.getJsonBody();
		String body = request.getBody();
		addPropertyMapping("id", dst.getFile().getId());
		addPropertyMapping("destination_id", dst.getFile().getId());
		if (dst.getRemote() != null) {
			addPropertyMapping("file_id", dst.getRemote().getId());
		}
		String path = dst.getFile().getPath();
		if (path != null) {
			if (path.endsWith(FileInfo.PATH_SEPARATOR)) {
				if (dst.getFileName() != null) {
					path += dst.getFileName();
				}
			} else {
				if (dst.getFileName() != null) {
					path += FileInfo.PATH_SEPARATOR + dst.getFileName();
				}
			}
		}
		addPropertyMapping("path", path);
		addPropertyMapping("destination_path", path);
		if (dst.getFileName() != null) {
			addPropertyMapping("name", dst.getFileName());
		}
		addPropertyMapping("offset", String.valueOf(transferred));
		if (session != null) {
			addPropertyMapping("session", session.getSession());
		}
		try {
			if (jsonBody != null) {
				ObjectMapper mapper = json.getMapper();
				body = mapper.writeValueAsString(jsonBody);
				preparedRequest = prepareRequest(new StringEntity(doPropertyMapping(body, false)));
			} else {
				if (body != null) {
					if (body.equals(DATA_MAPPING)) {
						addPropertyMapping("offsetbuffer", String.valueOf(transferred + buffer - 1));
						preparedRequest = prepareRequest(new InputStreamEntity(new CountingInputStream(data, listener), buffer));
					} else {
						preparedRequest = prepareRequest(new StringEntity(doPropertyMapping(body, false)));
					}
				} else {
					preparedRequest = prepareRequest(null);
				}
			}
		} catch (UnsupportedEncodingException | JsonProcessingException e1) {
			throw new MultiCloudException("Failed to prepare request.");
		}
		return preparedRequest;
	}

	/**
	 * Returns if all the uploads are done.
	 * @return If all the uploads are done.
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationBegin() {
		/* no preparation necessary */
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		if (!destinations.isEmpty()) {
			/* create threads and start them */
			listener.setDivisor(destinations.size());
			for (FileCloudSource dst: destinations) {
				pool.add(new FileUploadThread(this, dst, data));
			}
			for (FileUploadThread thread: pool) {
				thread.start();
			}
			for (FileUploadThread thread: pool) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					/* join interrupted */
				}
			}
			listener.finishTransfer();
			if (getError() == null && getResult() == null) {
				FileInfo info = new FileInfo();
				info.setName(data.getName());
				info.setFileType(FileType.FILE);
				setResult(info);
			}
			done = true;
			for (FileUploadThread thread: pool) {
				if (thread.isFailed()) {
					done = false;
					break;
				}
			}
		} else {
			throw new MultiCloudException("No destination specified.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationFinish() {
		/* no finalization necessary */
	}

}
