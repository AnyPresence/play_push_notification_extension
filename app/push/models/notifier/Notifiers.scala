package push.models.notifier

import java.io.{ BufferedInputStream, FileInputStream }

import javapns.Push
import javapns.communication.exceptions.CommunicationException
import javapns.communication.exceptions.KeystoreException
import javapns.notification.PushNotificationPayload
import javapns.notification.PushedNotification
import javapns.notification.PushedNotifications
import javapns.notification.ResponsePacket

import play.api.Logger._
import play.api.Play.current
import play.api.Mode
import play.api.libs.json._
import play.api.libs.json.Json.JsValueWrapper
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

object Notifiers {

  type Notifier = { def notify(badge: Option[Int], alert: JsValue, sound: Option[String], messagePayload: JsObject, deviceTokens: Set[String]): Unit }

  def apnsKeystore: Array[Byte] = {
    val filename = current.configuration.getString("apple_cert").getOrElse{ throw new RuntimeException("Application config key apple_cert must be defined!") }
    scala.io.Source.fromFile(filename)(scala.io.Codec.ISO8859).map(_.toByte).toArray
  }
  
  def apnsPassword: String = {
    current.configuration.getString("apple_cert_password").getOrElse { throw new RuntimeException("APNS_KEYSTORE_PASSWORD env variable not defined") }
  }

  object ApnsNotifier {
    
    def notify(badgeMaybe: Option[Int], alert: JsValue, soundMaybe: Option[String], messagePayload: JsObject, deviceTokens: Set[String]) = {
      try {
        info("ApnsNotifier notifying " + deviceTokens + " of event")
        val lb = ListBuffer[Tuple2[String, JsValueWrapper]]()
        badgeMaybe.map { badge => lb.append("badge" -> JsNumber(badge)) }
        soundMaybe.map { sound => lb.append("sound" -> sound) }
        lb.append("alert" -> alert)
      
        val jsObj = Json.obj( "aps" -> (Json.obj(lb.toSeq:_*) ++ messagePayload))
    
        debug("Sending the following packet to apple : " + jsObj.toString())
    
        val payload = new PushNotificationPayload(jsObj.toString())
        val notifications = Push.payload(payload, keystore, password, !current.configuration.getBoolean("apple_cert_is_apns_dev_cert").getOrElse(false), deviceTokens.toArray)
        info("Successful push count : " + notifications.getSuccessfulNotifications().size())
        info("Failed push count : " + notifications.getFailedNotifications().size())
      
        notifications.getFailedNotifications().foreach { push =>
          Option(push.getException()).map { exc =>
            info("Received exception on push! " + exc.getMessage(), exc)
          }
          Option(push.getResponse()).map { responsePacket =>
            Option(responsePacket.getMessage()).map { message => 
              info("Response from Apple on failed notification: " + message) 
            }
          }
        }
      } catch {
        case e: KeystoreException => throw new PushNotificationException("Encountered KeystoreException : " + e.getMessage(), e, "Application security settings are not configured correctly")
        case e: CommunicationException => throw new PushNotificationException("Encountered CommunicationException : " + e.getMessage(), e, "Unable to communicate successfully with Apple to send push notification")
        case e: Exception => throw new PushNotificationException("Encountered unexpected error : " + e.getMessage(), e, "An unexpected error occurred")
      }
      
    }
    
    lazy val keystore = apnsKeystore
    lazy val password = apnsPassword
  }

  object GcmNotifier {
    def notify(badge: Option[Int], alert: JsValue, sound: Option[String], messagePayload: JsObject, deviceTokens: Set[String]) = {
      info("Dummy implementation of GcmNotifier does nothing ... need to implement!")
    }
  }

}

case class PushNotificationException(msg: String, throwable: Throwable, friendlyError: String) extends RuntimeException(msg, throwable)