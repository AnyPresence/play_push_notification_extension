package models.push;

import models.push.notification.PushNotification;
import models.push.provider.ApnsPushNotificationProvider;
import models.push.provider.GcmPushNotificationProvider;

public enum DeviceType {
	
	
	android(new GcmPushNotificationProvider(PushNotificationConstants.GCM_URL, PushNotificationConstants.GCM_API_KEY)), 
	ios(new ApnsPushNotificationProvider(PushNotificationConstants.APNS_KEYSTORE_FILE, PushNotificationConstants.APNS_KEYSTORE_PASSWORD));
	
	private PushNotificationProvider<? extends PushNotification> pushNotificationProvider;
	
	private DeviceType(PushNotificationProvider<? extends PushNotification> pushNotificationProvider) {
		this.pushNotificationProvider = pushNotificationProvider;
	}
	
	public static DeviceType fromDescription(String description) {
		if (description == null || description.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"description must be provided");
		}

		String lowerUa = description.toLowerCase();

		if (lowerUa.contains("iphone") || lowerUa.contains("ipad")
				|| lowerUa.contains("ipod")) {
			return ios;
		} else if (lowerUa.contains("android")) {
			return android;
		} else {
			throw new IllegalArgumentException(
					"Don't know how to match device type with description "
							+ description);
		}
	}
	
	public PushNotificationProvider<? extends PushNotification> getPushNotificationProvider() {
		return pushNotificationProvider;
	}
}