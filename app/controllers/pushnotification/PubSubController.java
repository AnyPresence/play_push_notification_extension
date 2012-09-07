package controllers.pushnotification;

import models.pushnotification.Channel;
import models.pushnotification.Device;
import models.pushnotification.Device.DeviceType;

import org.codehaus.jackson.JsonNode;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import com.mongodb.MongoException;

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

	/*
	 def unsubscribe
      device = ::PushNotificationExtension::Device.where(token: params[:device_token], type: @device_type).first
      if device
        Rails.logger.info "Received unsubscribe request from mobile device: " + device.inspect
        channel = ::PushNotificationExtension::Channel.where(name: params[:channel]).first
        if channel
          begin
            channel.devices.delete device
            device.channels.delete channel
            render :json => { :success => true }
          rescue
            render :json => { :success => false, :error => $!.message }
          end
        else
          render :json => { :success => false, :error => "invalid channel" }
        end
      else
        render :json => { :success => false, :error => "invalid device" }
      end
    end

	 */
	
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

	public static Result publish() {
		Logger.debug("Publishing");
		return ok("<html><body><h1>Published</h1></body></html>").as(
				"text/html");
	}

}
