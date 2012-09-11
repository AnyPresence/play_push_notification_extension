package plugin.push;

import play.Play;

public interface PushNotificationConstants {

	public static final String APPLE_CERT = Play.application().configuration().getString("apple_cert");
	public static final String APPLE_CERT_PASSWORD = Play.application().configuration().getString("apple_cert_password");
	
	public static final String GCM_URL_DEFAULT = Play.application().configuration().getString("gcm_url");
	public static final String GCM_URL = (GCM_URL_DEFAULT == null ? "https://android.googleapis.com/gcm/send" : GCM_URL_DEFAULT); 
	public static final String GCM_API_KEY = Play.application().configuration().getString("gcm_api_key");
	
}
