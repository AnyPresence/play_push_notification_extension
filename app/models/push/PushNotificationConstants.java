package models.push;

import java.io.File;

import play.Play;

public interface PushNotificationConstants {

	public static final File APNS_KEYSTORE_FILE = new File(Play.application().configuration().getString("apns.keystore.file"));
	public static final String APNS_KEYSTORE_PASSWORD = Play.application().configuration().getString("apns.keystore.password");
	
	public static final String GCM_URL = Play.application().configuration().getString("gcm.url");
	public static final String GCM_API_KEY = Play.application().configuration().getString("gcm.api.key");
	
}
