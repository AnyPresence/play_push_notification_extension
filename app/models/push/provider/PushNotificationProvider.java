package models.push.provider;

import java.util.List;

import models.push.notification.PushNotification;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.Play;


public abstract class PushNotificationProvider<T extends PushNotification> {

	public  void push(PushNotification pushNotification) {
		Class<T> klass = getPushNotificationImplementationClass();
		T t = klass.cast(pushNotification);
		Boolean pushEnabled = Play.application().configuration().getBoolean("push.enabled");
		Boolean pushToProd = Play.application().configuration().getBoolean("push.to.prod");
		if (pushToProd == null) { 
			pushToProd = false;
		}
		if (pushEnabled && pushToProd) {
			Logger.debug("Sending push notification to production");
			pushIt(t, pushToProd);
		} else if (pushEnabled  && !pushToProd) {
			Logger.debug("Sending push notification through non-production servers");
			pushIt(t, pushToProd);
		} else {
			Logger.debug("Not pushing notification because test is not enabled and not running in prod");
		}
	}
	
	public abstract PushNotification createPushNotification(Integer badge, JsonNode alert, JsonNode messagePayload, List<String> deviceIds);
	
	protected abstract Class<T> getPushNotificationImplementationClass();
	
	protected abstract void pushIt(T pushNotification, boolean prod);
	
}
