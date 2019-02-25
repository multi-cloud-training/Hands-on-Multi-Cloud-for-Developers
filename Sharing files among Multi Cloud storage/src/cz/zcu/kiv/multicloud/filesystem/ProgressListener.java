package cz.zcu.kiv.multicloud.filesystem;

/**
 * cz.zcu.kiv.multicloud.filesystem/ProgressListener.java			<br /><br />
 *
 * Abstract progress listener. Receives the progress updates from the library operations.
 *
 * @author Jaromír Staněk
 * @version 1.0
 *
 */
public abstract class ProgressListener {

	/** Default number of milliseconds before the the progress is reported. */
	public static final long DEFAULT_REFRESH_INTERVAL = 200;

	/** Number of milliseconds before the the progress is reported. */
	private final long refreshInterval;
	/** Total number of bytes. */
	private long totalSize;
	/** Number of bytes already reported. */
	private long transferred;
	/** Time of the last report of progress. */
	private long lastUpdate;
	/** Number of bytes that count as one. */
	private int divisor;

	/**
	 * Empty ctor.
	 */
	public ProgressListener() {
		this.refreshInterval = DEFAULT_REFRESH_INTERVAL;
		this.lastUpdate = 0;
		this.divisor = 1;
	}

	/**
	 * Ctor with refresh interval supplied.
	 * @param refreshInterval Refresh interval.
	 */
	public ProgressListener(long refreshInterval) {
		this.refreshInterval = refreshInterval;
		this.lastUpdate = 0;
		this.divisor = 1;
	}

	/**
	 * Adds the increment to transferred bytes.
	 * @param increment Number of bytes of the progress.
	 */
	public synchronized void addTransferred(long increment) {
		transferred += increment;
		long now = System.currentTimeMillis();
		if (now - refreshInterval > lastUpdate) {
			lastUpdate = now;
			onProgress();
		}
	}

	/**
	 * Reports the last data progress not waiting the refresh interval.
	 */
	public synchronized void finishTransfer() {
		lastUpdate = 0;
		onProgress();
	}

	/**
	 * Returns the number of bytes that count as one.
	 * @return Number of bytes that count as one.
	 */
	public int getDivisor() {
		return divisor;
	}

	/**
	 * Returns the refresh interval.
	 * @return Refresh interval.
	 */
	public long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * Return the total number of bytes.
	 * @return Total number of bytes.
	 */
	public long getTotalSize() {
		return totalSize;
	}

	/**
	 * Returns the number of bytes transferred.
	 * @return Bytes transferred.
	 */
	public long getTransferred() {
		return transferred;
	}

	/**
	 * Reports the progress.
	 */
	protected abstract void onProgress();

	/**
	 * Sets the number of bytes that count as one.
	 * @param divisor Number of bytes that count as one.
	 */
	public void setDivisor(int divisor) {
		if (divisor < 1) {
			this.divisor = 1;
		} else {
			this.divisor = divisor;
		}
	}

	/**
	 * Sets the total number of bytes.
	 * @param totalSize Total number of bytes.
	 */
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
		this.transferred = 0;
		this.lastUpdate = 0;
	}

}
