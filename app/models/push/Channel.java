package models.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.push.notification.PushNotification;
import models.push.provider.PushNotificationException;
import models.push.provider.PushNotificationProvider;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;

import play.Logger;
import plugin.morphia.MorphiaBootstrapPlugin;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

@Entity(value = "channels")
public class Channel extends BaseEntity {

	static { datastore().ensureIndex(Channel.class, "channels_name_index", "name", true, false); }

	@Id
	public ObjectId id;
	public String name;
	@Reference
	public Set<Device> devices = new HashSet<Device>();

	public Channel() {

	}

	public Channel(String name) {
		this.name = name;
	}

	private static Datastore datastore() {
		return play.Play.application().plugin(MorphiaBootstrapPlugin.class).getDatastore();
	}

	public static void create(Channel channel) {
		Key<Channel> key = datastore().save(channel);
		channel.id = (ObjectId) key.getId();
		Logger.info("Saved new instance of channel : " + channel);		
	}

	public static void update(Channel channel) {
		datastore().save(channel);
	}

	public static Channel findByName(String name) {
		return datastore().createQuery(Channel.class).filter("name", name).get();
	}
	
	private Map<DeviceType, List<String>> sortDeviceIds(Set<Device> devices) {
		
		Map<DeviceType, List<String>> deviceMap = new HashMap<DeviceType, List<String>>();
		for (Device d : devices) {
			List<String> deviceIds = deviceMap.get(d.type);
			if (deviceIds == null) {
				deviceIds = new ArrayList<String>();
				deviceMap.put(d.type, deviceIds);
			}
			deviceIds.add(d.token);
		}
		return deviceMap;
	}
	
	public <T extends PushNotification> String publish(Integer badge, JsonNode alert, JsonNode messagePayload, boolean pushToProd) {
		
		Map<DeviceType, List<String>> deviceMap = sortDeviceIds(devices);
		
		StringBuffer errors = new StringBuffer("");
		
		for (Map.Entry<DeviceType, List<String>> entry : deviceMap.entrySet()) {
			DeviceType type = entry.getKey();
			List<String> deviceIds = entry.getValue();
			
			PushNotificationProvider<? extends PushNotification> provider = type.getPushNotificationProvider();
			PushNotification pushNotification = provider.createPushNotification(badge, alert, messagePayload, deviceIds, pushToProd);
			
			try {
				provider.push(pushNotification);
			} catch(PushNotificationException e) {
				Logger.error("Encountered error attempting to push: " + e.getMessage(), e);
				if (!errors.toString().isEmpty()) {
					errors.append("; ");
				}
				errors.append(e.getFriendlyApiError());
			} catch(Exception e) {
				Logger.error("Encountered error attempting to push: " + e.getMessage(), e);
				if (!errors.toString().isEmpty()) {
					errors.append("; ");
				}
				errors.append("Unexpected error encountered");
			}
		}
		
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Channel other = (Channel) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Channel [id=" + id + ", name=" + name + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
}
