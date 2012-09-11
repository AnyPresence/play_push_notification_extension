package models.push.provider;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import models.push.notification.ApnsPushNotification;
import models.push.notification.PushNotification;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;

import play.Logger;
import play.Play;

public class ApnsPushNotificationProvider extends PushNotificationProvider<ApnsPushNotification> {

	private File apnsKeystoreFile;
	private String apnsKeystorePassword;
	
	public ApnsPushNotificationProvider(File apnsKeystoreFile, String apnsKeystorePassword) {
		this.apnsKeystoreFile = apnsKeystoreFile;
		this.apnsKeystorePassword = apnsKeystorePassword;
	}
	
	@Override
	protected void pushIt(ApnsPushNotification pushNotification) {
		Logger.info("Pushing to apple...");
		
		JsonNode messagePayload = pushNotification.getMessagePayload();
		JsonNode alert = pushNotification.getAlert();
		Integer badge = pushNotification.getBadge();
		List<String> appleDeviceTokens = pushNotification.getDeviceIds();
		
		JsonFactory fact = new ObjectMapper().getJsonFactory();
		StringWriter writer = new StringWriter();
		
		
		try {
			JsonGenerator gen = fact.createJsonGenerator(writer);
			
			gen.writeStartObject();
			
			gen.writeFieldName("aps");
			gen.writeStartObject();
			if (alert != null) {
				gen.writeFieldName("alert");
				gen.writeTree(alert);
			}
			if (badge != null) {
				gen.writeFieldName("badge");
				gen.writeNumber(badge);
			}
			gen.writeEndObject();

			if (messagePayload != null) {
				
				Iterator<Entry<String, JsonNode>> it = messagePayload.getFields();
				while(it.hasNext()) {
					Entry<String, JsonNode> entry = it.next();
					String fieldName = entry.getKey();
					JsonNode value = entry.getValue();
					gen.writeFieldName(fieldName);
					gen.writeTree(value);
				}
			}
			
			gen.writeEndObject();
			gen.flush();
			
			gen.close();
			
			Logger.info("APNS JSON String : " + writer.toString());
			PushNotificationPayload payload = new PushNotificationPayload(writer.toString());
			Push.payload(payload, apnsKeystoreFile, apnsKeystorePassword, Play.application().isProd(), appleDeviceTokens.toArray(new String[appleDeviceTokens.size()]));
			//TODO: return some sort of result here
		} catch(IOException e) {
			throw new PushNotificationException("Encountered IOException : " + e.getMessage() + ", for message payload " + messagePayload.toString(), e, "Unable to parse message payload into JSON");
		} catch(JSONException e) {
			throw new PushNotificationException("Encountered JSONException : " + e.getMessage() + ", for message payload " + messagePayload.toString(), e, "Message payload is not valid JSON");
		} catch(KeystoreException e) { 
			throw new PushNotificationException("Encountered KeystoreException : " + e.getMessage() + ", for message keystore " + apnsKeystoreFile.getAbsolutePath(), e, "Application security settings not configured correctly");
		} catch (CommunicationException e) {
			throw new PushNotificationException("Encountered CommunicationException : " + e.getMessage(), e, "Unable to communicate with Apple");
		} catch(Exception e) {
			throw new PushNotificationException("Encountered unexpected error : " + e.getMessage(), e, "An unexpected error occurred");
		}
	}

	@Override
	public Class<ApnsPushNotification> getPushNotificationImplementationClass() {
		return ApnsPushNotification.class;
	}

	@Override
	public PushNotification createPushNotification(Integer badge, JsonNode alert, JsonNode messagePayload, List<String> deviceIds) {
		return new ApnsPushNotification(badge, messagePayload, deviceIds, alert);
	}

}
