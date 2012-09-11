package controllers.pushnotification;

import models.pushnotification.Channel;
import models.pushnotification.Device;
import models.pushnotification.DeviceType;

import org.codehaus.jackson.JsonNode;

import play.Logger;
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
			JsonNode deviceToken = json.get("device_token");

			if (channelName == null || deviceToken == null) {
				return ResponseBuilder.badRequestJsonResponse(false, "must provide channel and device_token parameters");
			} else {
				String channelStr = channelName.asText();
				String deviceTokenStr = deviceToken.asText();
				DeviceType deviceType = DeviceType.valueOf(flash().get(
						DeviceType.class.getSimpleName()));

				if (channelStr == null || channelStr.isEmpty()
						|| deviceTokenStr == null || deviceTokenStr.isEmpty()) {
					return ResponseBuilder.badRequestJsonResponse(false, "must provide valid channel and device_token parameters");
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
			JsonNode deviceToken = json.get("device_token");

			if (channelName == null || deviceToken == null) {
				return ResponseBuilder.badRequestJsonResponse(false, "must provide channel and device_token parameters");
			} else {
				String channelStr = channelName.asText();
				String deviceTokenStr = deviceToken.asText();
				DeviceType deviceType = DeviceType.valueOf(flash().get(
						DeviceType.class.getSimpleName()));

				Logger.debug("Received unsubscription request for channel " + channelStr + ", deviceToken " + deviceTokenStr + ", " + deviceType);
				
				if (channelStr == null || channelStr.isEmpty()
						|| deviceTokenStr == null || deviceTokenStr.isEmpty()) {
					return ResponseBuilder.badRequestJsonResponse(false, "must provide valid channel and device_token parameters");
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

	/**
	def publish
      channel = ::PushNotificationExtension::Channel.where(name: params[:channel]).first
      if channel
        begin
          channel.publish params[:badge], params[:alert], params[:message_payload]
          render :json => { :success => true }
        rescue
          render :json => { :success => false, :error => $!.message }
        end
      else
        render :json => { :success => false, :error => "invalid channel" }
      end
    end

	 */
	
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
			JsonNode messagePayload = json.get("message_payload");
			Logger.info("Serialized : " + messagePayload.toString());

			if (channelName == null || badge == null || alert == null || messagePayload == null)  {
				return ResponseBuilder.badRequestJsonResponse(false, "must provide channel, badge, alert, and message_payload parameters");
			} else {
				String channelStr = channelName.asText();
				Integer badgeInt = badge == null ? null : badge.asInt();
				
				if (channelStr.trim().isEmpty()) {
					return ResponseBuilder.badRequestJsonResponse(false, "must provide values for channel, badge, alert, and message_payload parameters");
				} else {
					Channel channel = Channel.findByName(channelStr);
					if (channel == null) { 
						return ResponseBuilder.okJsonResponse(false, "Invalid channel");
					} else {
						String errorMessage = channel.publish(badgeInt, alert, messagePayload);
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
