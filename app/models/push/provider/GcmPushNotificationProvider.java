package models.push.provider;

import java.io.StringWriter;
import java.util.List;

import models.push.notification.BasicPushNotification;
import models.push.notification.PushNotification;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import play.Logger;
import play.libs.F.Promise;
import play.libs.WS;

public class GcmPushNotificationProvider extends PushNotificationProvider<BasicPushNotification> {

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
			
			StringBuffer buf = writer.getBuffer();
			String json = buf.toString();
			try { writer.close(); } catch(Exception e) { } // don't care!
			
			Logger.info("json is : " + json);
	
			String result = null;
			Promise<WS.Response> resp = WS.url(gcmPushUrl).setHeader("Content-Type", "application/json").setHeader("Authorization", "key=" + gcmApiKey).post(json);
			result = resp.get().getBody();
			Logger.info("Response from GCM: " + result);
			
		} catch (Exception e) {
			throw new PushNotificationException("Encountered unexpected error : " + e.getMessage(), e, "An unexpected error occurred");
		}
	}

	@Override
	public Class<BasicPushNotification> getPushNotificationImplementationClass() {
		return BasicPushNotification.class;
	}

	@Override
	public PushNotification createPushNotification(Integer badge, JsonNode alert, JsonNode messagePayload, List<String> deviceIds, boolean pushToProd) {
		return new BasicPushNotification(badge, messagePayload, deviceIds, pushToProd);
	}

}
