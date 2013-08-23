package workers

import play.api.Logger._

class LifecycleTriggeredPushNotification(val prod: Boolean, val channel: String, val badge: String, val alert: String, val sound : String, val messagePayload: String) extends Runnable
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