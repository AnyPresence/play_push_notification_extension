package push.controllers

import Functions._
import play.api.Logger._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import play.api.mvc.Results._
import push.models.Channel
import push.models.Device
import push.models.DeviceType

object PubSubController extends Controller {

  case class SubscribeRequest(channel: String, deviceToken: String)
  case class PublishRequest(channel: String, badge: Option[Int], alert: JsValue, sound: Option[String], messagePayload: JsObject)
  
  val subscriptionForm = Form(
    tuple(
      "channel" -> nonEmptyText,
      "device_token" -> nonEmptyText
    )
  )
    
  private val publishReads = (
    (__ \ 'channel).read[String] and
    (__ \ 'badge).readNullable[Int] and
    (__ \ 'alert).read[JsValue] and 
    (__ \ 'sound).readNullable[String] and
    (__ \ 'message_payload).read[JsObject])(PublishRequest)
  
  private def badRequest(errorMessage: String) = {
    BadRequest(Json.obj("success" -> false, "error" -> errorMessage))
  }
  
  private def ok(success: Boolean = true, errorMessage: Option[String] = None) = {
    errorMessage.map { error: String =>
      Ok(Json.obj("success" -> success, "error" -> error))
    }.getOrElse {
      Ok(Json.obj("success" -> success))
    }
  }
  
  def subscribe = {
    DeviceTypeAware { deviceType : DeviceType.Value =>
      Action { implicit request: Request[_] =>
        debug("Received subscribe request") 
        subscriptionForm.bindFromRequest.fold(
          formWithErrors => {
            debug("Failed to extract channel and device_token parameters from request")
            badRequest("Unable to extract channel and device_token from request")
          },
          result => result match {
            case (channel, deviceToken) => {
              Device.findByTokenAndType(deviceToken, deviceType).orElse {
                val newDevice = Device(id = null, token = deviceToken, deviceType = deviceType)
                debug("Device not found, so attempting to create it : " + newDevice)
                Device.create(newDevice)
              }.map { device: Device =>
                Channel.findByName(channel).orElse {
                  val newChannel = Channel(id = null, name = channel)
                  debug("Channel not found, so attempting to create it : " + newChannel)              
                  Channel.create(newChannel)
                }.map { channel: Channel =>
                  if (Channel.update(channel.copy(devices = channel.devices + device.id))) {
                    debug("Channel updated successfully " + channel)
                    if (Device.update(device.copy(channels = device.channels + channel.id))) {
                      debug("Device updated successfully " + device)
                      ok()
                    } else {
                      ok(false, Some("Unable to subscribe device to channel"))
                    }
                  } else {
                    ok(false, Some("Unable to subscribe device to channel"))
                  }
                }.getOrElse(ok(false, Some("Unable to subscribe device to channel")))
              }.getOrElse(ok(false, Some("Unable to subscribe device to channel")))
            }
          }
        )
      }
    }
  }
  
  def unsubscribe = {
    DeviceTypeAware { deviceType : DeviceType.Value =>
      Action { implicit request: Request[_] =>
        debug("Received unsubscribe request")
        subscriptionForm.bindFromRequest.fold(
          formWithErrors => {
            debug("Failed to extract channel and device_token parameters from request")
            badRequest("Unable to extract channel and device_token from request")
          },
          result => result match {
            case (channel, deviceToken) => {
              val deviceMaybe = Device.findByTokenAndType(deviceToken, deviceType)
              val channelMaybe = Channel.findByName(channel) 
              channelMaybe.map { channel =>
                deviceMaybe.map { device =>
                  debug("Removing channel " + channel + " from device " + device)
                  Device.update(device.copy(channels = (device.channels - channel.id)))
                  debug("Removing device " + device + " from channel " + channel)
                  Channel.update(channel.copy(devices = (channel.devices - device.id)))
                }
              }
              ok(true)
            }
          }
        )
      }
    }
  }
  
  def publish = {
    implicit val reads = publishReads
    Action(parse.json) { implicit request: Request[JsValue] => 
      debug("Received publish request")
      Json.fromJson(request.body).map { req: PublishRequest =>
        debug("Publish request is " + req)
        Channel.findByName(req.channel).map { channel =>
          debug("Publishing to channel " + channel)
          val errorMaybe = Channel.publish(req.badge, req.alert, req.sound, req.messagePayload, channel)
          debug("Publish call produced result " + errorMaybe)
          ok(!errorMaybe.isDefined, errorMaybe)
        }.getOrElse(ok(false, Some("Invalid channel")))
      }.getOrElse(badRequest("Invalid json request format"))
    }
  }
  
}