package controllers.pushnotification;

import models.pushnotification.DeviceType;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Http.HeaderNames;
import play.mvc.Http.Request;
import play.mvc.Result;

public class DeviceTypeDetector extends Action.Simple {

	@Override
	public Result call(Context arg0) throws Throwable {
		DeviceType device = detectDeviceType(arg0.request());
		if (device == null) {
			Logger.info("Unable to detect device type from User-Agent field");
			return ResponseBuilder.badRequestJsonResponse(false, "Unable to detect device type from User-Agent header.");
		} else {
			arg0.flash().put(DeviceType.class.getSimpleName(),
					device.toString());
			return delegate.call(arg0);
		}
	}

	private DeviceType detectDeviceType(Request request) {
		String userAgent = request.getHeader(HeaderNames.USER_AGENT);
		Logger.debug("Trying to extract device type from userAgent "
				+ userAgent);
		if (userAgent == null) {
			Logger.info("No User-Agent provided, so can't determine device type");
			return null;
		} else {
			try {
				return DeviceType.fromDescription(userAgent);
			} catch (IllegalArgumentException e) {
				Logger.info("Could not extract device type from User-Agent "
						+ userAgent);
				return null;
			}
		}
	}

}
