package models.push.notification;

import java.util.List;

import org.codehaus.jackson.JsonNode;

public class BasicPushNotification extends PushNotification{

	private static final long serialVersionUID = 1L;

	public BasicPushNotification(Integer badge, JsonNode messagePayload, List<String> deviceIds, boolean pushToProd) {
		super(badge, messagePayload, deviceIds, pushToProd);
	}

	@Override
	public String toString() {
		return "BasicPushNotification [badge=" + getBadge()
				+ ", messagePayload=" + getMessagePayload()
				+ ", deviceIds=" + getDeviceIds() 
				+ ", pushToProd=" + isPushToProd()+ "]";
	}
	
}
