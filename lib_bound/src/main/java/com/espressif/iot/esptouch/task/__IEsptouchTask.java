package com.espressif.iot.esptouch.task;

/**
 * IEsptouchTask defined the task of esptouch should offer. INTERVAL here means
 * the milliseconds of interval of the step. REPEAT here means the repeat times
 * of the step.
 * 
 * @author afunx
 * 
 */
public interface __IEsptouchTask {

	/**
	 * Turn on or off the log.
	 */
	static final boolean DEBUG = true;

	void execute();

	/**
	 * Interrupt the Esptouch Task when User tap back or close the Application.
	 */
	void interrupt();


	boolean isCancelled();
}
