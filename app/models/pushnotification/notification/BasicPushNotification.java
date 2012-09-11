package models.pushnotification.notification;

import java.util.List;

import org.codehaus.jackson.JsonNode;

public class BasicPushNotification extends PushNotification{

	private static final long serialVersionUID = 1L;

	public BasicPushNotification(Integer badge, JsonNode messagePayload, List<String> deviceIds) {
		super(badge, messagePayload, deviceIds);
	}

	@Override
	public String toString() {
		return "BasicPushNotification [badge=" + getBadge()
				+ ", messagePayload=" + getMessagePayload()
				+ ", deviceIds=" + getDeviceIds() + "]";
	}
	
}
