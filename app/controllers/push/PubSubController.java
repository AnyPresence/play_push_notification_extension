package controllers.push;

import models.push.Channel;
import models.push.Device;
import models.push.DeviceType;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import com.mongodb.MongoException;

/**
 * TODO: refactor some of the validation logic -- also there might be a better
 * 'built-in' way of doing it using the play framework features ...
 * 
 * @author rsnyder
 *
 */
public class PubSubController extends Controller {

	@With(value = DeviceTypeDetector.class)
	public static Result subscribe() {

		Logger.debug("Subscribing");

		JsonNode json = request().body().asJson();

		Logger.debug("Converted to JsonNode object " + json
				+ "...attempting to convert to Device object");

		if (json == null) {
			return ResponseBuilder.badRequestJsonResponse(false, "no json provided in request body");
		} else {
			JsonNode channelName = json.get("channel");
			JsonNode deviceToken = json.get("deviceToken");

			if (channelName == null || deviceToken == null) {
				return ResponseBuilder.badRequestJsonResponse(false, "must provide channel and deviceToken parameters");
			} else if (!channelName.isTextual()) {
				return ResponseBuilder.badRequestJsonResponse(false, "channel must be a string");
			} else if (!deviceToken.isTextual()) {
				return ResponseBuilder.badRequestJsonResponse(false, "deviceToken must be a string");
			} else {
				String channelStr = channelName.asText();
				String deviceTokenStr = deviceToken.asText();
				DeviceType deviceType = DeviceType.valueOf(flash().get(
						DeviceType.class.getSimpleName()));

				if (channelStr == null || channelStr.isEmpty()
						|| deviceTokenStr == null || deviceTokenStr.isEmpty()) {
					return ResponseBuilder.badRequestJsonResponse(false, "must provide valid channel and deviceToken parameters");
				} else {
					Logger.debug("Received subscription request for channel "
							+ channelStr + " and device token "
							+ deviceTokenStr);

					try {
						Device device = Device.findByTokenAndType(deviceTokenStr,
								deviceType);
						if (device == null) {
							device = new Device(deviceTokenStr, deviceType);
							Device.create(device);
							Logger.debug("Device created : " + device.toString());
						} else {
							Logger.debug("Using existing device : " + device.toString());
						}
	
						Channel channel = Channel.findByName(channelStr);
						if (channel == null) {
							channel = new Channel(channelStr);
							Channel.create(channel);
							Logger.debug("New channel created : " + channel.toString());
						} else {
							Logger.debug("Using existing channel : " + channel.toString());
						}
	
						Logger.debug("added device to channel : " + channel.devices.add(device));
						Logger.debug("added channel to device : " + device.channels.add(channel));
						
						Channel.update(channel);
						Device.update(device);
						return ResponseBuilder.okJsonResponse(true);
					} catch(MongoException e) {
						Logger.error("Caught MongoException attempting to create subscription with mongo error code " + e.getCode() + " : " + e.getMessage(), e);
						return ResponseBuilder.okJsonResponse(false, "error code: " + e.getCode() + ", message: " + e.getMessage());
					} catch(Exception e) {
						Logger.error("Caught Exception attempting to create subscription : " + e.getMessage(), e);
						return ResponseBuilder.okJsonResponse(false, e.getMessage());
					}
				}
			}
		}
	}
	
	@With(value = DeviceTypeDetector.class)
	public static Result unsubscribe() {
		Logger.debug("Unsubscribing");

		JsonNode json = request().body().asJson();

		Logger.debug("Converted to JsonNode object " + json
				+ "...attempting to convert to Device object");

		if (json == null) {
			return ResponseBuilder.badRequestJsonResponse(false, "no json provided in request body");
		} else {
			JsonNode channelName = json.get("channel");
			JsonNode deviceToken = json.get("deviceToken");

			if (channelName == null || deviceToken == null) {
				return ResponseBuilder.badRequestJsonResponse(false, "must provide channel and deviceToken parameters");
			} else if (!channelName.isTextual()) {
				return ResponseBuilder.badRequestJsonResponse(false, "channel must be a string");
			} else if (!deviceToken.isTextual()) {
				return ResponseBuilder.badRequestJsonResponse(false, "deviceToken must be a string");
			} else {
				String channelStr = channelName.asText();
				String deviceTokenStr = deviceToken.asText();
				DeviceType deviceType = DeviceType.valueOf(flash().get(
						DeviceType.class.getSimpleName()));

				Logger.debug("Received unsubscription request for channel " + channelStr + ", deviceToken " + deviceTokenStr + ", " + deviceType);
				
				if (channelStr == null || channelStr.isEmpty()
						|| deviceTokenStr == null || deviceTokenStr.isEmpty()) {
					return ResponseBuilder.badRequestJsonResponse(false, "must provide valid channel and deviceToken parameters");
				} else {
					Device device = Device.findByTokenAndType(deviceTokenStr, deviceType);
					if (device == null) { 
						return ResponseBuilder.okJsonResponse(false, "Invalid device");
					}
					
					Channel channel = Channel.findByName(channelStr);
					if (channel == null) {
						return ResponseBuilder.okJsonResponse(false, "Invalid channel");
					}
					
					try {
						channel.devices.remove(device);
						device.channels.remove(channel);
						
						Channel.update(channel);
						Device.update(device);
						
						return ResponseBuilder.okJsonResponse(true);
					} catch(Exception e) {
						return ResponseBuilder.okJsonResponse(false, e.getMessage());
					}
				}
			}
		}
	}
	
	public static Result publish() {
		Logger.debug("Publishing");
		
		JsonNode json = request().body().asJson();

		Logger.debug("Converted to JsonNode object " + json
				+ "...attempting to convert to Device object");

		if (json == null) {
			return ResponseBuilder.badRequestJsonResponse(false, "no json provided in request body");
		} else {
			JsonNode channelName = json.get("channel");
			JsonNode badge = json.get("badge");
			JsonNode alert = json.get("alert");
			JsonNode messagePayload = json.get("messagePayload");

			if (channelName == null || alert == null || messagePayload == null)  {
				return ResponseBuilder.badRequestJsonResponse(false, "must provide channel, alert, and messagePayload parameters");
			} else if(!channelName.isTextual()) {
				return ResponseBuilder.badRequestJsonResponse(false, "channel must be a string!");
			} else if (!alert.isTextual()) { 
				return ResponseBuilder.badRequestJsonResponse(false, "alert must be a string!");
			} else if (!messagePayload.isObject()) {
				return ResponseBuilder.badRequestJsonResponse(false, "messagePayload must be valid json object");
			} else {
				Logger.debug("Serialized messagePayload : " + messagePayload.toString());
				String channelStr = channelName.asText();
				if (channelStr == null) { 
					return ResponseBuilder.badRequestJsonResponse(false, "channel must be a string");
				}
				
				Integer badgeInt = null;
				if (badge != null) {
					if (!badge.isInt()) {
						return ResponseBuilder.badRequestJsonResponse(false, "badge, if provided, must be a valid integer");
					} else {
						badgeInt = badge.asInt();
					}
				}
				
				if (channelStr.trim().isEmpty()) {
					return ResponseBuilder.badRequestJsonResponse(false, "must provide values for channel, badge, alert, and messagePayload parameters");
				} else {
					Channel channel = Channel.findByName(channelStr);
					if (channel == null) { 
						return ResponseBuilder.okJsonResponse(false, "Invalid channel");
					} else {
						String errorMessage = channel.publish(badgeInt, alert, messagePayload, Play.application().isProd());
						if (errorMessage == null) {
							return ResponseBuilder.okJsonResponse(true);
						} else {
							return ResponseBuilder.okJsonResponse(false, errorMessage);
						}
					}
				}
			}
		}
	}

}
