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
		if (Play.application().isProd()) {
			Logger.debug("App running in production -- pushing notification");
			pushIt(t);
		} else if (Play.application().configuration().getBoolean("push.test.enabled")) {
			Logger.debug("App not running in production, but push.test.enabled is true");
			pushIt(t);
		} else {
			Logger.debug("Not pushing notification because test is not enabled and not running in prod");
		}
	}
	
	public abstract PushNotification createPushNotification(Integer badge, JsonNode alert, JsonNode messagePayload, List<String> deviceIds);
	
	protected abstract Class<T> getPushNotificationImplementationClass();
	
	protected abstract void pushIt(T pushNotification);
	
}
