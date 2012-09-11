package models.push.provider;

import java.io.File;
import java.util.List;

import models.push.DeviceType;
import models.push.notification.ApnsPushNotification;
import models.push.notification.BasicPushNotification;
import models.push.notification.PushNotification;

import org.codehaus.jackson.JsonNode;

import play.Play;

public class PushNotificationProviderFactory {
	
	private static final File APNS_KEYSTORE_FILE = new File(Play.application().configuration().getString("apns.keystore.file"));
	private static final String APNS_KEYSTORE_PASSWORD = Play.application().configuration().getString("apns.keystore.password");
	
	private static final String GCM_URL = Play.application().configuration().getString("gcm.url");
	private static final String GCM_API_KEY = Play.application().configuration().getString("gcm.api.key");
	
	private static enum Provider {
		
		ANDROID(new GcmPushNotificationProvider(GCM_URL, GCM_API_KEY)),
		IOS(new ApnsPushNotificationProvider(APNS_KEYSTORE_FILE, APNS_KEYSTORE_PASSWORD));
		
		private PushNotificationProvider<? extends PushNotification> provider;
		
		private Provider(PushNotificationProvider<? extends PushNotification> provider) {
			this.provider = provider;
		}
		
		public static PushNotificationProvider<? extends PushNotification> providerForDeviceType(DeviceType deviceType) {
			switch(deviceType) {
				case android:
					return ANDROID.provider;
				case ios:
					return IOS.provider;
				default:
					throw new IllegalArgumentException("No PushNotificationProvider configured for device type " + deviceType);
			}
		}
		
		
		public static PushNotification pushNotificationForDeviceType(DeviceType type, Integer badge, JsonNode alert, JsonNode payload, List<String> deviceIds) {
			switch(type) {
				case android:
					return new BasicPushNotification(badge, payload, deviceIds);
				case ios:
					return new ApnsPushNotification(badge, payload, deviceIds, alert);
				default:
					throw new IllegalArgumentException("No PushNotification configured for device type " + type);
			}
		}
	}
	
	
	public static PushNotificationProvider<? extends PushNotification> getPushNotificationProvider(DeviceType type) {
		return Provider.providerForDeviceType(type);
	}
	
	public static PushNotification createPushNotification(DeviceType type, Integer badge, JsonNode alert, JsonNode payload, List<String> deviceIds) {
		return Provider.pushNotificationForDeviceType(type, badge, alert, payload, deviceIds);
	}
	
}
