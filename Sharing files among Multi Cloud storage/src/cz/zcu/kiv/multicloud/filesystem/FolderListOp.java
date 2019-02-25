package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.fasterxml.jackson.databind.JsonNode;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;
import cz.zcu.kiv.multicloud.utils.Utils;

/**
 * cz.zcu.kiv.multicloud.filesystem/FolderListOp.java			<br /><br />
 *
 * Operation for getting the list of folders and files inside the specified folder of the storage space.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class FolderListOp extends Operation<FileInfo> {

	/** Folder list preparation request parameters. */
	private final CloudRequest beginRequest;
	/** Folder list request parameters. */
	private final CloudRequest execRequest;
	/** Original information about the folder to be listed. */
	private final FileInfo original;
	/** Actual contents of the folder. */
	private FileInfo contents;
	/** The request of the operation. */
	private HttpUriRequest request;
	/** Lock object for concurrent method calls. */
	private final Object lock;

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param execRequest Parameters of the request.
	 * @param folder Folder to be listed.
	 * @param showDeleted If deleted content should be listed.
	 */
	public FolderListOp(OAuth2Token token, CloudRequest beginRequest, CloudRequest execRequest, FileInfo folder, boolean showDeleted) {
		super(OperationType.FOLDER_LIST, token, execRequest);
		addPropertyMapping("id", folder.getId());
		addPropertyMapping("path", folder.getPath());
		addPropertyMapping("deleted", showDeleted ? "true" : "false");
		this.beginRequest = beginRequest;
		this.execRequest = execRequest;
		original = folder;
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
		if (beginRequest != null) {
			setRequest(beginRequest);
			synchronized (lock) {
				request = prepareRequest(null);
			}
			try {
				contents = executeRequest(request, new ResponseProcessor<FileInfo>() {
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
									if (info.getId() == null) {
										info.setId(original.getId());
									}
									if (info.getName() == null) {
										info.setName(original.getName());
									}
									if (info.getPath() == null) {
										info.setPath(original.getPath());
									}
									info.setIsRoot(original.isRoot());
									info.fillMissing();
									for (FileInfo content: info.getContent()) {
										content.fillMissing();
									}
								}
							}
						} catch (IllegalStateException | IOException e) {
							/* return null value instead of throwing exception */
						}
						return info;
					}
				});
			} catch (IOException e) {
				synchronized (lock) {
					if (!isAborted) {
						throw new MultiCloudException("Failed to list the selected folder.");
					}
				}
			}
			synchronized (lock) {
				request = null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void operationExecute() throws MultiCloudException {
		synchronized (lock) {
			if (isAborted) {
				return;
			}
		}
		if (execRequest != null) {
			setRequest(execRequest);
			synchronized (lock) {
				request = prepareRequest(null);
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
									if (info.getId() == null) {
										info.setId(original.getId());
									}
									if (info.getName() == null) {
										info.setName(original.getName());
									}
									if (info.getPath() == null) {
										info.setPath(original.getPath());
									}
									info.setIsRoot(original.isRoot());
									info.fillMissing();
									for (FileInfo content: info.getContent()) {
										content.fillMissing();
									}
									if (contents != null) {
										info = Utils.formVisibilityTree(info, contents);
									}
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
						throw new MultiCloudException("Failed to list the selected folder.");
					}
				}
			}
			synchronized (lock) {
				request = null;
			}
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
