package workers;

import models.push.Channel;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.libs.Json;

public class LifecycleTriggeredPushNotification implements Runnable
{

        private String pushToProd;
	private String channelName;
	private String badge;
	private String alert;
	private String messagePayload;
	
	public LifecycleTriggeredPushNotification(String pushToProd, String channelName, String badge, String alert, String messagePayload) {
                this.pushToProd = pushToProd;
		this.channelName = channelName;
		this.badge = badge;
		this.alert = alert;
		this.messagePayload = messagePayload;
	}

	@Override
	public void run() {
		Channel channel = Channel.findByName(channelName);
		int badge = 0;
		if (channel != null) {
			
			if (this.badge == null) { 
				badge = 0;
			}

			boolean shouldPush = Boolean.parseBoolean(pushToProd);

			try {
				badge = Integer.parseInt(this.badge);
			} catch(NumberFormatException e) {
				Logger.info("Provided value for badge field was not a valid Integer : " + this.badge + ". Defaulting to 0");
				badge = 0;
			}
		
			try {
				JsonNode alertNode = Json.parse(alert);
				JsonNode messagePayloadNode = Json.parse(messagePayload);
				
				channel.publish(badge, alertNode, messagePayloadNode, shouldPush);
			} catch(Exception e) {
				Logger.error("Couldn't send push notification because alert or message payload could not be parsed to a valid JSON node. \n  alert : " + alert + "\n  messagePayload : " + messagePayload);
			}
			
		} else {
			Logger.error("Could not send push notification because channel " + channelName + " does not exist");
		}
	}
	
}
