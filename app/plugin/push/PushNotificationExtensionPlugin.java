package plugin.push;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import play.Application;
import play.Logger;
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
		Boolean enabled = app.configuration().getBoolean("push_notifications_extension_enabled");
		return enabled == null ? false : enabled;
	}

	@Override
	public void onStart() {
		
		String appleCertFile = PushNotificationConstants.APPLE_CERT;

		File appleCert = null;
		
		if (appleCertFile == null) { 
			Logger.info("No apple cert file defined.  No push notifications will be sent to Apple");
		} else {
			appleCert = Play.application().getFile(appleCertFile);
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

		}
		
		
		String appleCertPassword = PushNotificationConstants.APPLE_CERT_PASSWORD;
		if (appleCert != null && appleCertPassword == null) { 
			throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- no password provided for apple cert under config key 'apple_cert_password'!");
		}
		
		String gcmUrl = PushNotificationConstants.GCM_URL;
		
		String gcmApiKey = PushNotificationConstants.GCM_API_KEY;
		if (gcmApiKey == null) { 
			Logger.info("GCM API KEY is null -- no push notifications will be sent to google.");
		}

		if (gcmApiKey == null && appleCert == null) { 
			Logger.error("No push notification extension providers configured!");
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
