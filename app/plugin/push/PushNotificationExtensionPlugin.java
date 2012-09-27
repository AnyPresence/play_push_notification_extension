package plugin.push;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Application;
import play.Play;
import play.Plugin;

public class PushNotificationExtensionPlugin extends Plugin {

	private Application app;
	private String gcmUrl;
	private String gcmApiKey;
	private File appleCert;
	private String appleCertPassword;
	
	public PushNotificationExtensionPlugin(Application app) { 
		this.app = app;
	}
	
	@Override
	public boolean enabled() {
		Boolean enabled = app.configuration().getBoolean("push_notification_extension_enabled");
		return enabled == null ? false : enabled;
	}

	@Override
	public void onStart() {
		
		String appleCertFile = PushNotificationConstants.APPLE_CERT;
		
		if (appleCertFile == null) { 
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- no 'apple_cert' option provided in configuration -- should be file containing the apple certificate, relative to root of play application!");
		}
		
		File appleCert = Play.application().getFile(appleCertFile);
		if (!appleCert.exists()) {
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- apple cert file " + appleCert.getAbsolutePath() + " was not found!  Should be relative to the root of the play application");
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(appleCert));
			String line = null;
			Pattern pattern = Pattern.compile("Apple Development IOS Push Services");
			boolean match = false;
			while ((line = reader.readLine()) != null && !match) { 
				Matcher m = pattern.matcher(line);
				match = m.find();
			}
		} catch(FileNotFoundException e) { 
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin because file " + appleCert.getAbsolutePath() + " could not be read, due to FileNotFoundException", e);
		} catch(IOException e) { 
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin because file " + appleCert.getAbsolutePath() + " could not be read, due to IOException", e);
		} finally {
			try { if (reader != null) { reader.close(); } } catch(IOException e) { } // don't care about exc on close
		}
		
		
		String appleCertPassword = PushNotificationConstants.APPLE_CERT_PASSWORD;
		if (appleCertPassword == null) { 
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- no password provided for apple cert under config key 'apple_cert_password'!");
		}
		
		String gcmUrl = PushNotificationConstants.GCM_URL;
		
		String gcmApiKey = PushNotificationConstants.GCM_API_KEY;
		if (gcmApiKey == null) { 
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- no GCM api key defined in configuration -- should be the API key used to connect to google cloud messaging (GCM) services under config option 'gcm_api_key'");
		}
		
		this.appleCert = appleCert;
		this.appleCertPassword = appleCertPassword;
		this.gcmUrl = gcmUrl;
		this.gcmApiKey = gcmApiKey;
		
	}
	
	public static PushNotificationExtensionPlugin getPlugin() { 
		return Play.application().plugin(PushNotificationExtensionPlugin.class);
	}
	
	public File getAppleCert() {
		return appleCert;
	}
	
	public String getAppleCertPassword() { 
		return appleCertPassword;
	}
	
	public String getGcmUrl() {
		return gcmUrl;
	}

	public String getGcmApiKey() { 
		return gcmApiKey;
	}
	
}
