package models.push.provider;

public class PushNotificationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String friendlyApiError;
	
	public PushNotificationException(String msg, String friendlyApiError) {
		super(msg);
	}
	
	public PushNotificationException(String msg, Throwable t, String friendlyApiError) {
		super(msg, t);
	}

	public String getFriendlyApiError() {
		return friendlyApiError;
	}
	
	
}
