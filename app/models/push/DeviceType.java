package models.push;

import models.push.notification.PushNotification;
import models.push.provider.ApnsPushNotificationProvider;
import models.push.provider.GcmPushNotificationProvider;
import models.push.provider.PushNotificationProvider;
import plugin.push.PushNotificationExtensionPlugin;

public enum DeviceType {
	
	
	android(new GcmPushNotificationProvider(PushNotificationExtensionPlugin.getPlugin().getGcmUrl(), PushNotificationExtensionPlugin.getPlugin().getGcmApiKey())), 
	ios(new ApnsPushNotificationProvider(PushNotificationExtensionPlugin.getPlugin().getAppleCert(), PushNotificationExtensionPlugin.getPlugin().getAppleCertPassword()));
	
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