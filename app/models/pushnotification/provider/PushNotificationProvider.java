package models.pushnotification.provider;

import models.pushnotification.notification.PushNotification;


public abstract class PushNotificationProvider<T extends PushNotification> {

	public  void push(PushNotification pushNotification) {
		Class<T> klass = getPushNotificationImplementationClass();
		T t = klass.cast(pushNotification);
		pushIt(t);
	}
	
	protected abstract Class<T> getPushNotificationImplementationClass();
	
	protected abstract void pushIt(T pushNotification);
	
}
