package cz.zcu.kiv.multicloud.filesystem;

/**
 * cz.zcu.kiv.multicloud.filesystem/DataChunk.java			<br /><br />
 *
 * Simple container for holding information about chunk boundaries and size.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public class DataChunk {

	/** First byte of the chunk. */
	private long beginByte;
	/** Last byte of the chunk. */
	private long endByte;

	/**
	 * Empty ctor.
	 */
	public DataChunk() {
		beginByte = 0;
		endByte = 0;
	}

	/**
	 * Ctor with boundaries.
	 * @param beginByte First byte of the chunk.
	 * @param endByte Last byte of the chunk.
	 */
	public DataChunk(long beginByte, long endByte) {
		this.beginByte = beginByte;
		this.endByte = endByte;
	}

	/**
	 * Returns the first byte of the chunk.
	 * @return First byte of the chunk.
	 */
	public long getBeginByte() {
		return beginByte;
	}

	/**
	 * Returns the last byte of the chunk.
	 * @return Last byte of the chunk.
	 */
	public long getEndByte() {
		return endByte;
	}

	/**
	 * Sets the first byte of the chunk.
	 * @param beginByte First byte of the chunk.
	 */
	public void setBeginByte(long beginByte) {
		this.beginByte = beginByte;
	}

	/**
	 * Sets the last byte of the chunk.
	 * @param endByte Last byte of the chunk.
	 */
	public void setEndByte(long endByte) {
		this.endByte = endByte;
	}

	/**
	 * Returns the size of the chunk.
	 * @return Size of the chunk, -1 if boundaries invalid.
	 */
	public long size() {
		if (endByte >= beginByte) {
			return endByte - beginByte;
		}
		return -1;
	}

}
