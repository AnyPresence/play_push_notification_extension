package push.models

import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.novus.salat.global._
import java.util.Date
import play.api.Play.current
import play.api.Logger._
import mongoContext._
import se.radley.plugin.salat._
import DeviceType._
import scala.collection.Set

case class Device(id: ObjectId, token: String, deviceType: DeviceType.Value, channels: Set[ObjectId] = Set[ObjectId](), createdAt: Option[Date] = None, updatedAt: Option[Date] = None)

trait DeviceImpl {
  
  val collection = mongoCollection("Device")
  val dao = new SalatDAO[Device, ObjectId](collection) {}
  
  collection.ensureIndex(MongoDBObject("token" -> 1, "deviceType" -> 1), "token_and_type_index", true)
  
  def findByTokenAndType(token: String, deviceType: DeviceType.Value): Option[Device] = dao.findOne(MongoDBObject("token" -> scrubToken(token), "deviceType" -> deviceType.toString()))
  def create(device: Device) : Option[Device] = {
    try {
      val now = Some(new Date())
      dao.insert(device.copy(token = scrubToken(device.token), createdAt = now, updatedAt = now)).map { id => device.copy(id = id) }
    } catch {
      case e: Exception => error("Failed to create device " + device, e)
      None
    }
  }
  
  def update(device: Device) : Boolean = {
    try {
      info("Attempting to find device " + device)
      dao.findOneById(device.id).map { d:Device =>
        dao.save(device.copy(token = scrubToken(device.token), updatedAt = Some(new Date())))
        info("Saved device!")
        true
      }.getOrElse(false)
    } catch {
      case e: Exception => error("Failed to update device " + device, e)
      false
    }
  }
  
  def delete(device: Device) : Boolean = {
    try {
      dao.findOneById(device.id).map { d:Device =>
        dao.remove(device)
        true
      }.getOrElse(false)
    } catch {
      case e: Exception => error("Failed to remove device " + device + " from database", e)
      false
    }
  }
  
  private def scrubToken(token:String) = token.replaceAll("[\\s<>]", "")
  
}

object Device extends DeviceImpl