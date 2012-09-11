package models.pushnotification.provider;

import java.io.IOException;
import java.io.StringWriter;

import models.pushnotification.notification.BasicPushNotification;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import play.Logger;

public class GcmPushNotificationProvider extends PushNotificationProvider<BasicPushNotification> {

	private static final HttpClient CLIENT = new DefaultHttpClient();
	
	private String gcmPushUrl;
	private String gcmApiKey;
	
	public GcmPushNotificationProvider(String gcmPushUrl, String gcmApiKey) {
		this.gcmPushUrl = gcmPushUrl;
		this.gcmApiKey = gcmApiKey;
	}

	@Override
	public void pushIt(BasicPushNotification push) {
		BasicPushNotification pushNotification = getPushNotificationImplementationClass().cast(push);
		
		try {
			
			JsonFactory factory = new ObjectMapper().getJsonFactory();
			StringWriter writer = new StringWriter();
			
			JsonGenerator gen = factory.createJsonGenerator(writer);
			
			gen.writeStartObject();
			
			gen.writeFieldName("data");
			
			gen.writeStartObject();
			
			if (pushNotification.getMessagePayload() != null) {
				gen.writeFieldName("data");
				gen.writeTree(pushNotification.getMessagePayload());
			}
				
			if (pushNotification.getBadge() != null) {
				gen.writeFieldName("badge");
				gen.writeNumber(pushNotification.getBadge());
			}
			
			gen.writeEndObject();
			
			gen.writeFieldName("registration_ids");
			gen.writeStartArray();
			for (String dev : pushNotification.getDeviceIds()) {
				gen.writeString(dev);
			}
			gen.writeEndArray();
			
			gen.writeEndObject();
			
			gen.close();
			
			writer.flush();
			
			HttpPost request = new HttpPost(gcmPushUrl);
			
			StringBuffer buf = writer.getBuffer();
			String json = buf.toString();
			try { writer.close(); } catch(Exception e) { } // don't care!
			
			Logger.info("json is : " + json);
	
			StringEntity se = new StringEntity(json, HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
	
			request.addHeader("Content-Type", "application/json");
			request.addHeader("Authorization", "key=" + gcmApiKey);
			request.setEntity(se);
	
			ResponseHandler<String> handler = new BasicResponseHandler();
			String result = null;
			result = CLIENT.execute(request, handler);
			Logger.info("Got result " + result);
			
			// TODO : return some kind of status object
			
		} catch (ClientProtocolException e) {
			throw new PushNotificationException("Encountered ClientProtocolException : " + e.getMessage(), e, " Unable to establish successful communication with GCM services");
		} catch (IOException e) {
			throw new PushNotificationException("Encountered IOException : " + e.getMessage(), e, "Unable to establish successful communication with GCM services");
		} catch (Exception e) {
			throw new PushNotificationException("Encountered unexpected error : " + e.getMessage(), e, "An unexpected error occurred");
		}
	}

	@Override
	public Class<BasicPushNotification> getPushNotificationImplementationClass() {
		return BasicPushNotification.class;
	}

}
