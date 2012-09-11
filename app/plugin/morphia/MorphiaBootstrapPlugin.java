package plugin.morphia;

import play.Application;
import play.Logger;
import play.Play;
import play.Plugin;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.Morphia;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

public class MorphiaBootstrapPlugin extends Plugin {

	private Mongo mongo;
	private Morphia morphia;

	private Application app;
	
	public MorphiaBootstrapPlugin(Application app) {
		this.app = app;
	}
	
	@Override
	public boolean enabled() {
		String mongoUri = app.configuration().getString("mongodb.uri");
		Boolean pluginEnabled = app.configuration().getBoolean("morphiabootstrapplugin.enabled");
		if (pluginEnabled == null) {
			pluginEnabled = true;
		}
		if (mongoUri == null || mongoUri.trim().isEmpty()) {
			Logger.info("MorphiaBootstrapPlugin not enabled because configuration parameter mongodb.uri was not provided in app configuration");
			return false;
		} else {
			Logger.info("MorphiaBootstrapPlugin is " + (pluginEnabled? "" : "not ") + "enabled");
			return pluginEnabled;
		}
	}

	@Override
	public void onStart() {
		
		super.onStart();
	
		try {
			mongo = new MongoURI(app.configuration().getString("mongodb.uri")).connect();
			morphia = new Morphia();
		} catch (Exception e) {
			Logger.error("Failed to initialize MorphiaBootstrapPlugin due to exception: " + e.getMessage(), e);
		}
		
	}

	public Mongo getMongo() {
		return mongo;
	}

	public Morphia getMorphia() {
		return morphia;
	}

	/**
	 * Returns the default datastore for the morphia bootstrap plugin, as defined by the
	 * mongodb.datastore property in application.conf
	 * 
	 * Same as calling <code>getDatastore(app.configuration().getString("mongodb.datastore")</code>
	 * 
	 * @return
	 */
	public Datastore getDatastore() {
		String defaultDs = app.configuration().getString("mongodb.datastore");
		if (defaultDs == null) { 
			throw new RuntimeException("Can't connect to default datastore because application configuration property mongodb.datastore is not defined");
		} else {
			return getDatastore(defaultDs);
		}
	}
	
	/**
	 * Returns a named datastore in the mongodb instance.  If the datastore does not
	 * exist, it will be created.
	 * 
	 * @param datastore The name of the datastore
	 * @return
	 */
	public Datastore getDatastore(String datastore) {
		return morphia.createDatastore(mongo, datastore);
	}
	
	public static MorphiaBootstrapPlugin getPlugin() {
		return Play.application().plugin(MorphiaBootstrapPlugin.class);
	}

}
