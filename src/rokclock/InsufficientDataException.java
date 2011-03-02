package rokclock;

@SuppressWarnings("serial")
class InsufficientDataException extends Exception {
	public InsufficientDataException(String message) {
		super(message);
	}
}
