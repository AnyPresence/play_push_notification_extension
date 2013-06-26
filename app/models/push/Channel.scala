package models.push

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.novus.salat.global._
import java.util.Date
import models.push.notifier.PushNotificationException
import play.api.Play.current
import play.api.Logger._
import play.api.libs.json.{JsObject, JsValue}
import mongoContext._
import se.radley.plugin.salat._
import scala.collection.Set
import scala.collection.mutable.{Buffer, HashMap => MutableHashMap, MultiMap, Set => MutableSet}


case class Channel(id: ObjectId, name: String, devices: Set[ObjectId] = Set[ObjectId](), createdAt: Option[Date] = None, updatedAt: Option[Date] = None)

trait ChannelImpl {
  
  protected val deviceDao: DeviceImpl
  
  val collection = mongoCollection("Channel")
  val dao = new SalatDAO[Channel, ObjectId](collection) {}
  
  collection.ensureIndex(MongoDBObject("name" -> 1), "name_index", true)
  
  def findByName(name: String): Option[Channel] = dao.findOne(MongoDBObject("name" -> name))
  
  def create(channel: Channel) : Option[Channel] = {
    try {
      val now = Some(new Date())
      dao.insert(channel.copy(createdAt = now, updatedAt = now)).map { id => channel.copy(id = id) }
    } catch {
      case e: Exception => error("Unable to create channel " + channel, e)
      None
    }
  }
  
  def update(channel: Channel) : Boolean = {
    try {
      dao.findOneById(channel.id).map { c:Channel =>
        dao.save(channel.copy(updatedAt = Some(new Date())))
        true
      }.getOrElse(false)
    } catch {
      case e: Exception => error("Unable to update channel " + channel, e)
      false
    }
  }
  
  def publish(badge: Option[Int], alert: JsValue, sound: Option[String], messagePayload: JsObject, channel: Channel) : Option[String] = {
    debug("Publish called with values badge : " + badge + ", alert : " + alert + ", sound : " +  sound + ", messagePayload : " + messagePayload.toString())
    val mongoObj = $or(channel.devices.map { ("_id" -> _) }.toSeq: _*)
    val results = deviceDao.dao.find(mongoObj)
    val deviceTokens = new MutableHashMap[DeviceType.Value, MutableSet[String]] with MultiMap[DeviceType.Value, String]
    while(results.hasNext) {
      val result = results.next()
      deviceTokens.addBinding(result.deviceType, result.token)
    }
    
    debug("Have the following deviceTokens : " + deviceTokens)
    val buf = Buffer[String]()
    deviceTokens.foreach { entry =>
      val deviceType = entry._1
      val deviceTokens = entry._2
      try {
        DeviceType.pushProviderForDevice(deviceType).notify(badge, alert, sound, messagePayload, deviceTokens.toSet)
      } catch {
        case e: PushNotificationException => {
          error("Encountered error attempting to push: " + e.getMessage(), e)
          buf.append(e.friendlyError)
        }
        case e: Exception => {
          error("Unexpected error encountered attempting to push: " + e.getMessage(), e)
          buf.append("Unexpected error encountered")
        }
      }
    }
    
    if (buf.isEmpty) None else Some(buf.addString(new StringBuilder(), "; ").toString())
    
  }
  
}

object Channel extends ChannelImpl {
  protected val deviceDao = Device
}