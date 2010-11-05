package rokclock;

/**
 * An interface that describes the actions that the time logging program should
 * implement.
 */
interface TimeLog {
	/**
	 * Start recording a particular project.
	 *
	 * @param projectPath
	 *            The full path to the sub-project.
	 * @throws Exception
	 *             Thrown if anything goes wrong.
	 */
	void startRecording(String[] projectPath) throws Exception;

	/**
	 * Stop recording. Nothing happens if recording is not happening.
	 *
	 * @throws Exception
	 *             Thrown if anything goes wrong.
	 */
	void stopRecording() throws Exception;

	/**
	 * This method is called when the timer completes its set period.
	 */
	void doPeriodicAction();

	/**
	 * The log entry is written according to the last recording.
	 *
	 * @param startTime
	 *            The start time in milliseconds from epoch.
	 * @param endTime
	 *            The end time in milliseconds from epoch.
	 * @throws Exception
	 *             Thrown if anything goes wrong.
	 */
	void writeLogEntry(long startTime, long endTime) throws Exception;

	/**
	 * This method is ran when the active period starts.
	 *
	 * @param projectPath
	 *            The full path the sub-project that should be recorded.
	 */
	void switchToActiveState(String[] projectPath);

	/**
	 * This method is ran when the active period ends.
	 */
	void switchToSemiActiveState();

	/**
	 * This method is ran when the semi-active period ends.
	 */
	void switchToStoppedState();

	/**
	 * This method defines what happens to the interface after the user has
	 * selected a sub-project.
	 */
	void minimiseOrHide();

	/**
	 * This method defines what happens to the interface after the active period
	 * ends.
	 */
	void unminimiseOrShow();

	/**
	 * This method defines the way in which any error is displayed to the user.
	 *
	 * @param e
	 *            The error/exception to display.
	 */
	void displayProblem(Exception e);
}
