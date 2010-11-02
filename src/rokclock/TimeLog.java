package rokclock;

interface TimeLog {
	void startRecording(String[] projectPath) throws Exception;
	void stopRecording() throws Exception;
	void doPeriodicAction();
	void writeLogEntry(long startTime, long endTime) throws Exception;
	void switchToActiveState(String[] projectPath);
	void switchToSemiActiveState();
	void switchToStoppedState();
	void minimiseOrHide();
	void unminimiseOrShow();
	void displayProblem(Exception e);
}
