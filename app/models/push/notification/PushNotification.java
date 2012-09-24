package models.push.notification;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.JsonNode;

public abstract class PushNotification implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer badge;
	private JsonNode messagePayload;
	private List<String> deviceIds;
	private boolean pushToProd;
	
	public PushNotification(Integer badge, JsonNode messagePayload, List<String> deviceIds, boolean pushToProd) {
		this.badge = badge;
		this.messagePayload = messagePayload;
		this.deviceIds = deviceIds;
		this.pushToProd = pushToProd;
	}
	
	public Integer getBadge() {
		return badge;
	}
	public void setBadge(Integer badge) {
		this.badge = badge;
	}
	
	public JsonNode getMessagePayload() {
		return messagePayload;
	}
	public void setMessagePayload(JsonNode messagePayload) {
		this.messagePayload = messagePayload;
	}
	
	public List<String> getDeviceIds() {
		return deviceIds;
	}
	public void setDeviceIds(List<String> deviceIds) {
		this.deviceIds = deviceIds;
	}

	public boolean isPushToProd() { 
		return pushToProd;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((badge == null) ? 0 : badge.hashCode());
		result = prime * result
				+ ((deviceIds == null) ? 0 : deviceIds.hashCode());
		result = prime * result
				+ ((messagePayload == null) ? 0 : messagePayload.hashCode());
		result = prime * result
				+ (pushToProd ? 1 : 0);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PushNotification other = (PushNotification) obj;
		if (badge == null) {
			if (other.badge != null)
				return false;
		} else if (!badge.equals(other.badge))
			return false;
		if (deviceIds == null) {
			if (other.deviceIds != null)
				return false;
		} else if (!deviceIds.equals(other.deviceIds))
			return false;
		if (messagePayload == null) {
			if (other.messagePayload != null)
				return false;
		} else if (!messagePayload.equals(other.messagePayload))
			return false;
                if (pushToProd != other.pushToProd) {
			return false;
		}
		return true;
	}
	
}
