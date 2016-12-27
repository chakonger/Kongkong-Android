package com.espressif.iot.esptouch;

public interface IEsptouchTask {

	/**
	 * Interrupt the Esptouch Task when User tap back or close the Application.
	 */
	void interrupt();

	/**
	 * check whether the task is cancelled by user
	 * 
	 * @return whether the task is cancelled by user
	 */
	boolean isCancelled();
}
