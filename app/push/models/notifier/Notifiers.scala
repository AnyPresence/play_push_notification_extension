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
    val filename = current.configuration.getString("apple_cert").getOrElse{ throw new RuntimeException("apple_cert in extensions.conf not defined") }
    scala.io.Source.fromFile(filename)(scala.io.Codec.ISO8859).map(_.toByte).toArray
  }
  
  def apnsPassword: String = {
    current.configuration.getString("apple_cert_password").getOrElse { throw new RuntimeException("apple_cert_password in extensions.conf not defined") }
  }

  def gcmApiKey: String = {
    current.configuration.getString("gcm_api_key").getOrElse { throw new RuntimeException("gcm_api_key in extensions.conf not defined") }
  }
  
  def gcmApiUrl: String = {
    current.configuration.getString("gcm_url").getOrElse("https://android.googleapis.com/gcm/send")
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
    
    import play.api.libs.ws.WS
    import play.api.http.HeaderNames._
    import play.api.http.Status._
    import scala.concurrent.ExecutionContext.Implicits.global
    
    def notify(badgeMaybe: Option[Int], alert: JsValue, soundMaybe: Option[String], messagePayload: JsObject, deviceTokens: Set[String]) = {
      try {
        info("GcmNotifier notifying " + deviceTokens + " of event")
        val lb = ListBuffer[Tuple2[String, JsValueWrapper]]()
        lb.append("data" -> messagePayload)
        badgeMaybe.map { badge => lb.append("badge" -> JsNumber(badge)) }
        val jsObj = Json.obj("registration_ids" -> deviceTokens, "data" -> (Json.obj(lb.toSeq:_*)))
        debug("Sending the following packet to google : " + jsObj.toString())
        val key = "key=" + apiKey
        val responseFuture = WS.url(gcmUrl).withHeaders(CONTENT_TYPE -> "application/json", "Authorization" -> key).post(jsObj.toString())
        val resultFuture = responseFuture.map { response => 
          response.status match {
            case OK => {
              debug("OK Response received from Google for push notification")
              Some(response.json)
            }
            case _ => {
              error("Unable to send push notification to google.  Response status received was " + response.status + ", with body: " + response.body)
              None
            }
          }
        }
      } catch {
        case e: Exception => throw new PushNotificationException("Encountered unexpected error : " + e.getMessage(), e, "An unexpected error occurred")
      }
    }
    
    lazy val apiKey = gcmApiKey
    lazy val gcmUrl = gcmApiUrl
    
  }

}

case class PushNotificationException(msg: String, throwable: Throwable, friendlyError: String) extends RuntimeException(msg, throwable)