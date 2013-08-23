package workers

import play.api.Logger._

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
  }
  
}