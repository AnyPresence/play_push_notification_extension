package plugin.push;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import play.Application;
import play.Logger;
import play.Play;
import play.Plugin;

public class PushNotificationExtensionPlugin extends Plugin {

	private Application app;
	private String gcmUrl;
	private String gcmApiKey;
	private byte[] appleCert;
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

		InputStream appleCert = null;
		
		if (appleCertFile == null) { 
			Logger.info("No apple cert file defined.  No push notifications will be sent to Apple");
		} else {

			File appleCertFileObj = Play.application().getFile(appleCertFile);

	                if (!appleCertFileObj.exists()) {
				// the cert file doesn't exist ... let's check to see if it is available inside
				// the bundled artifact -- this happens if we're deployed inside a WAR file

				String conflessFilePath = appleCertFile;
				if (appleCertFile.startsWith("conf/")) {
					conflessFilePath = appleCertFile.substring(5);	
				}

				appleCert = Play.application().resourceAsStream("/" + conflessFilePath);
				if (appleCert == null) {
					throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- apple cert file " + appleCertFile + " was not found!  Should be relative to the root of the play application (or if running in a war file, included in the war at the specified location");
				}
			} else {
				try {
					appleCert = new FileInputStream(appleCertFileObj);
				} catch(FileNotFoundException e) {
					throw new RuntimeException("Failed to start PushNotificationExtensionPlugin -- received file not found exception for file " + appleCertFileObj.getAbsolutePath(), e);
				}
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = null;
			try {
				is = new BufferedInputStream(appleCert);
				int i = 0;
				while ((i = is.read()) != -1) {
					baos.write(i);
				}
			} catch(IOException e) { 
				throw new RuntimeException("Failed to start PushNotificationExtensionPlugin because certificate could not be read, due to IOException", e);
			} finally {
				try { if (is != null) { is.close(); } } catch(IOException e) { } // don't care about exc on close
				try { baos.close(); } catch(IOException e) { } // don't care -- this doesn't happen for ByteArrayOutputStream
			}
			this.appleCert = baos.toByteArray();

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
		
		this.appleCertPassword = appleCertPassword;
		this.gcmUrl = gcmUrl;
		this.gcmApiKey = gcmApiKey;
		
	}
	
	public static PushNotificationExtensionPlugin getPlugin() { 
		return Play.application().plugin(PushNotificationExtensionPlugin.class);
	}
	
	public byte[] getAppleCert() {
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
