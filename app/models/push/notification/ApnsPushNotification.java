package models.push.notification;

import java.util.List;

import org.codehaus.jackson.JsonNode;

public class ApnsPushNotification extends PushNotification {

	private static final long serialVersionUID = 1L;

	private JsonNode alert;
	
	public ApnsPushNotification(Integer badge, JsonNode messagePayload, List<String> deviceIds, JsonNode alert, boolean pushToProd) {
		super(badge, messagePayload, deviceIds, pushToProd);
		this.alert = alert;
	}

	public JsonNode getAlert() {
		return alert;
	}

	public void setAlert(JsonNode alert) {
		this.alert = alert;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((alert == null) ? 0 : alert.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApnsPushNotification other = (ApnsPushNotification) obj;
		if (alert == null) {
			if (other.alert != null)
				return false;
		} else if (!alert.equals(other.alert))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ApnsPushNotification [alert=" + alert + ", badge="
				+ getBadge() + ", messagePayload=" + getMessagePayload()
				+ ", deviceIds=" + getDeviceIds() + "]";
	}
	
}
