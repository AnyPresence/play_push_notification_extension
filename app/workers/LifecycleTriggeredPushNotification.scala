package workers

import org.codehaus.jackson.JsonParseException
import play.api.Logger._
import play.api.libs.json.{ JsNull, JsObject, JsString, JsValue, Json }
import push.models.Channel

class LifecycleTriggeredPushNotification(prod: Boolean, channel: String, badge: String, alert: String, sound : String, messagePayload: String) extends Runnable
{
  
  def run: Unit = {
    // STUB
    debug("LifecycleTriggeredPushNotification is running")
    debug("  prod           : " + prod)
    debug("  channel        : " + channel)
    debug("  badge          : " + badge)
    debug("  alert          : " + alert)
    debug("  sound          : " + sound)
    debug("  messagePayload : " + messagePayload)
    
    val channelMaybe = Channel.findByName(channel)
    
    channelMaybe.map { channel: Channel =>
      val badgeIntMaybe = try {
        Some(badge.toInt)
      } catch {
        case e: NumberFormatException => {
          warn("Provided value for badge field was not a valid integer : " + badge + ". Defaulting to 0")
          None
        }
      }
      
      val alertJs: JsValue = if (alert.isEmpty) JsNull else JsString(alert)
      
      val parsedMessagePayload: JsObject = try {
        val payload = Json.parse(messagePayload)
        if (!payload.isInstanceOf[JsObject]) {
          warn("Unable to parse messagePayload into valid JsObject node : " + messagePayload)
          null
        } else {
          payload.asInstanceOf[JsObject]
        }
      } catch {
        case e: JsonParseException => {
          warn("Unable to parse message payload in to valid Json node : " + messagePayload + ". defaulting to null")
          null
        }
      }
      
      val soundMaybe = if (sound.isEmpty) None else Some(sound)
      
      try {
        Channel.publish(badgeIntMaybe, alertJs, soundMaybe, parsedMessagePayload, channel)
      } catch {
        case e: Exception => warn("Couldn't send push notification because an unexpected exception was encountered: " + e.getMessage(), e)
      }
      
    }.orElse {
      warn("Unable to send push notification because channel " + channel + " does not exist")
      None
    }
  }
  
}