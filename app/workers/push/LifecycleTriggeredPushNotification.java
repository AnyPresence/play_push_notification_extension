package workers.push;

import models.push.Channel;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.libs.Json;

public class LifecycleTriggeredPushNotification implements Runnable
{

	private String channelName;
	private Integer badge;
	private String alert;
	private String messagePayload;
	
	public LifecycleTriggeredPushNotification(String channelName, Integer badge, String alert, String messagePayload) {
		this.channelName = channelName;
		this.badge = badge;
		this.alert = alert;
		this.messagePayload = messagePayload;
	}

	@Override
	public void run() {
		Channel channel = Channel.findByName(channelName);
		if (channel != null) {
			
			if (badge == null) { 
				badge = 0;
			}
		
			try {
				JsonNode alertNode = Json.parse(alert);
				JsonNode messagePayloadNode = Json.parse(messagePayload);
				
				channel.publish(badge, alertNode, messagePayloadNode);
			} catch(Exception e) {
				Logger.error("Couldn't send push notification because alert or message payload could not be parsed to a valid JSON node. \n  alert : " + alert + "\n  messagePayload : " + messagePayload);
			}
			
		} else {
			Logger.error("Could not send push notification because channel " + channelName + " does not exist");
		}
	}
	
}
