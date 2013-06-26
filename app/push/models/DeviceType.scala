package push.models

import notifier.Notifiers._

object DeviceType extends Enumeration {
  val Ios = Value("Ios")
  val Android = Value("Android")
  
  def fromDescription(description: String) = {
    val lowerDesc = description.toLowerCase()
    if (lowerDesc.contains("iphone") || lowerDesc.contains("ipad") || lowerDesc.contains("ipod")) {
      Ios
    } else if (lowerDesc.contains("android")) {
      Android
    } else {
      throw new IllegalArgumentException("Unsupported device type " + description)
    }
  }
  
  def pushProviderForDevice(deviceType: DeviceType.Value): Notifier = {
    deviceType match {
      case Ios => ApnsNotifier
      case Android => GcmNotifier
    }
  }
}
