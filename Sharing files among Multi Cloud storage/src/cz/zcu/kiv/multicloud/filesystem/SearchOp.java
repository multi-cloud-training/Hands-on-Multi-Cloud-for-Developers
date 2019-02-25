package cz.zcu.kiv.multicloud.filesystem;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.zcu.kiv.multicloud.MultiCloudException;
import cz.zcu.kiv.multicloud.json.CloudRequest;
import cz.zcu.kiv.multicloud.json.FileInfo;
import cz.zcu.kiv.multicloud.oauth2.OAuth2Token;

/**
 * cz.zcu.kiv.multicloud.filesystem/SearchOp.java			<br /><br />
 *
 * Operation for searching files in the cloud storage.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class SearchOp extends Operation<List<FileInfo>> {

	/** The request of the operation. */
	private HttpUriRequest request;
	/** Lock object for concurrent method calls. */
	private final Object lock;

	/**
	 * Ctor with necessary parameters.
	 * @param token Access token for the storage service.
	 * @param request Parameters of the request.
	 * @param search Search string.
	 * @param showDeleted If deleted content should be listed.
	 */
	public SearchOp(OAuth2Token token, CloudRequest request, String search, boolean showDeleted) {
		super(OperationType.SEARCH, token, request);
		addPropertyMapping("deleted", showDeleted ? "true" : "false");
		addPropertyMapping("query", search);
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
		synchronized (lock) {
			request = prepareRequest(null);
		}
		try {
			setResult(executeRequest(request, new ResponseProcessor<List<FileInfo>>() {
				/**
				 * {@inheritDoc}
				 */
				@Override
				public List<FileInfo> processResponse(HttpResponse response) {
					List<FileInfo> list = null;
					try {
						if (response.getStatusLine().getStatusCode() >= 400) {
							parseOperationError(response);
						} else {
							JsonNode tree = parseJsonResponse(response);
							if (tree.getNodeType() == JsonNodeType.ARRAY) {
								ObjectNode root = new ObjectNode(json.getMapper().getNodeFactory());
								root.put("content", tree);
								tree = root;
							}
							FileInfo data = json.getMapper().treeToValue(tree, FileInfo.class);
							list = data.getContent();
							for (FileInfo item: list) {
								item.fillMissing();
							}
						}
					} catch (IllegalStateException | IOException e) {
						/* return null value instead of throwing exception */
					}
					return list;
				}
			}));
		} catch (IOException e) {
			synchronized (lock) {
				if (!isAborted) {
					throw new MultiCloudException("Failed to obtain search results.");
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
