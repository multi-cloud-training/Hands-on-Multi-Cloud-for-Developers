package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/RenameOp.java			<br /><br />
 *
 * Operation for renaming file or folder in the storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class RenameOp extends Operation<FileInfo> {

	/** File or folder name to be renamed to. */
	private final String name;
	/** File type of the original file or folder. */
	private final FileType type;
	/** JSON body represented as a map. */
	private final Map<String, Object> jsonBody;
	/** Body of the request. */
	private String body;
	/** The request of the operation. */
	private HttpUriRequest request;
	/** Lock object for concurrent method calls. */
	private final Object lock;

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 * @param file Source file or folder to be renamed.
	 * @param fileName New name of the file or folder.
	 */
	public RenameOp(OAuth2Token token, CloudRequest request, FileInfo file, String fileName) {
		super(OperationType.RENAME, token, request);
		addPropertyMapping("id", file.getId());
		addPropertyMapping("path", file.getPath());
		addPropertyMapping("source_id", file.getId());
		addPropertyMapping("source_path", file.getPath());
		String path = file.getPath();
		if (path != null) {
			if (path.contains(FileInfo.PATH_SEPARATOR)) {
				path = path.substring(0, path.lastIndexOf(FileInfo.PATH_SEPARATOR) + 1) + fileName;
			}
		}
		addPropertyMapping("destination_path", path);
		addPropertyMapping("name", fileName);
		name = fileName;
		type = file.getFileType();
		jsonBody = request.getJsonBody();
		body = request.getBody();
		lock = new Object();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void abort() {
		synchronized (lock) {
			if (request != null) {
				request.abort();
				isAborted = true;
			}
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
		try {
			if (jsonBody != null) {
				ObjectMapper mapper = json.getMapper();
				body = mapper.writeValueAsString(jsonBody);
			}
			if (body != null) {
				synchronized (lock) {
					request = prepareRequest(new StringEntity(doPropertyMapping(body, false)));
				}
			} else {
				synchronized (lock) {
					request = prepareRequest(null);
				}
			}
		} catch (UnsupportedEncodingException | JsonProcessingException e1) {
			throw new MultiCloudException("Failed to prepare request.");
		}
		try {
			setResult(executeRequest(request, new ResponseProcessor<FileInfo>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public FileInfo processResponse(HttpResponse response) {
					FileInfo info = null;
					try {
						if (response.getStatusLine().getStatusCode() >= 400) {
							parseOperationError(response);
						} else {
							JsonNode tree = parseJsonResponse(response);
							if (tree != null) {
								info = json.getMapper().treeToValue(tree, FileInfo.class);
								info.fillMissing();
								for (FileInfo content: info.getContent()) {
									content.fillMissing();
								}
							} else {
								info = new FileInfo();
								info.setName(name);
								info.setFileType(type);
							}
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
					}
					return info;
				}
			}));
		} catch (IOException e) {
			synchronized (lock) {
				if (!isAborted) {
					throw new MultiCloudException("Failed to rename the specified file or folder.");
				}
			}
		}
		synchronized (lock) {
			request = null;
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
