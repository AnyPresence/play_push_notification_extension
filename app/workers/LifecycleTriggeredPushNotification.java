package workers;

import models.push.Channel;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.libs.Json;

public class LifecycleTriggeredPushNotification implements Runnable
{

    private boolean isProd;
	private String channelName;
	private String badge;
	private String alert;
	private String messagePayload;
	
	public LifecycleTriggeredPushNotification(boolean isProd, String channelName, String badge, String alert, String messagePayload) {
        this.isProd = isProd;
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

			try {
				badge = Integer.parseInt(this.badge);
			} catch(NumberFormatException e) {
				Logger.info("Provided value for badge field was not a valid Integer : " + this.badge + ". Defaulting to 0");
				badge = 0;
			}
		
			JsonNode alertNode = null;
			JsonNode messagePayloadNode = null;

			try {
				alertNode = Json.parse("\"" + alert + "\"");
			} catch(Exception e) { 
				Logger.warn("alert " + alert + " could not be parsed to a valid JSON string : " + e.getMessage());
			}

			try {
				messagePayloadNode = Json.parse(messagePayload);
			} catch(Exception e) { 
				Logger.warn("message payload " + messagePayload + " could not be parsed to a valid JSON node : " + e.getMessage());
			}

			try {
				channel.publish(badge, alertNode, messagePayloadNode, isProd);
			} catch(Exception e) {
				Logger.error("Couldn't send push notification because alert or message payload could not be parsed to a valid JSON node. \n  alert : " + alert + "\n  messagePayload : " + messagePayload, e);
			}
			
		} else {
			Logger.error("Could not send push notification because channel " + channelName + " does not exist");
		}
	}
	
}
