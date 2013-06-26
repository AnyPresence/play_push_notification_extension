package push.controllers

import play.api.Logger._
import play.api.http.HeaderNames._
import play.api.libs.iteratee.Done
import play.api.mvc.Action
import play.api.mvc.EssentialAction
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results._
import push.models.DeviceType

object Functions {
  
  def DeviceTypeAware(action: DeviceType.Value => EssentialAction): EssentialAction = {
    
    def extractDeviceType(request: RequestHeader): Option[DeviceType.Value] = {
      val userAgentMaybe = request.headers.get(USER_AGENT)
      userAgentMaybe.map { userAgent =>
        DeviceType.fromDescription(userAgent)
      }
    }
    
    EssentialAction { request =>
      extractDeviceType(request).map { deviceType: DeviceType.Value =>
        action(deviceType)(request)
      }.getOrElse {
        info("Received request but was unable to extract the device type from the user agent header: " + request.headers.get(USER_AGENT))
        Done(BadRequest)
      }
    }
    
  }
  
  def Logging(action: () => EssentialAction): EssentialAction = {
    
    EssentialAction { request => 
      debug("Received request " + request)
      try {
        action()(request)
      } finally {
        debug("Finished processing request: " + request)
      }
    }
  }
  
}

