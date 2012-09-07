package models.pushnotification;

import java.util.HashSet;
import java.util.Set;

import morphia.MorphiaBootstrapPlugin;

import org.bson.types.ObjectId;

import play.Logger;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Key;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

@Entity(value = "channels")
public class Channel {

	private static final Datastore DS = MorphiaBootstrapPlugin.getPlugin().getDatastore();
	static { DS.ensureIndex(Channel.class, "channels_name_index", "name", true, false); }

	@Id
	public ObjectId id;

	public String name;

	@Reference
	public Set<Device> devices = new HashSet<Device>();

	public Channel() {

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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

	public Channel(String name) {
		this.name = name;
	}

	public static void create(Channel channel) {
		Key<Channel> key = DS.save(channel);
		channel.id = (ObjectId) key.getId();
		Logger.info("Saved new instance of channel : " + channel);		
	}

	public static void update(Channel channel) {
		DS.save(channel);
	}

	public static Channel findByName(String name) {
		return DS.createQuery(Channel.class).filter("name", name).get();
	}

	@Override
	public String toString() {
		return "Channel [id=" + id + ", name=" + name + "]";
	}

}
