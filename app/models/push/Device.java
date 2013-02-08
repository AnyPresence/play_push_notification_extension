package models.push;

import java.util.HashSet;
import java.util.Set;


import org.bson.types.ObjectId;

import play.Logger;
import plugin.morphia.MorphiaBootstrapPlugin;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.PrePersist;
import com.google.code.morphia.annotations.PreSave;
import com.google.code.morphia.annotations.Reference;
import com.mongodb.WriteResult;

@Entity(value = "devices")
public class Device extends BaseEntity {

	static { datastore().ensureIndex(Device.class, "devices_token_type_index", "token, type", true, false); }

	@Id
	public ObjectId id;
	public String token;
	public DeviceType type;
	@Reference
	public Set<Channel> channels = new HashSet<Channel>();

	public Device() {

	}

	public Device(String token, DeviceType type) {
		this.token = token;
		this.type = type;
	}
	
	private static Datastore datastore() { 
		return play.Play.application().plugin(MorphiaBootstrapPlugin.class).getDatastore();
	}

	public static void create(Device device) {
		Key<Device> deviceKey = datastore().save(device);
		device.id = (ObjectId) deviceKey.getId();
		Logger.debug("Created device with key " + deviceKey.getId());
	}

	public static boolean delete(Device device) {
		WriteResult res = datastore().delete(device);
		return res.getN() == 1;
	}

	public static Device findByTokenAndType(String token, DeviceType type) {
		return datastore().createQuery(Device.class).filter("token", scrubToken(token)).filter("type", type.toString()).get();
	}
	
	public static void update(Device device) {
		datastore().save(device);
	}

	@PreSave
	@PrePersist
	public void scrub() {
	    this.token = scrubToken(this.token);
	}
	
	private static String scrubToken(String token) {
	    return token.replaceAll("[\\s<>]", "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Device other = (Device) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Device [id=" + id + ", token=" + token + ", type=" + type
				+ ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
	}
	
}
