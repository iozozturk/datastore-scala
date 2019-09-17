package com.ismetozozturk.datastore

import java.util.Date

import com.google.`type`.LatLng
import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1.{Entity, Key, Value}
import com.google.protobuf.ByteString
import com.google.protobuf.timestamp.Timestamp

import scala.reflect.runtime.universe._
import scala.reflect.{ClassTag, classTag}

trait EntityResolver {
  private[datastore] def extractRuntimeClass[E: ClassTag](): RuntimeClass = classTag[E].runtimeClass

  private[datastore] def instanceToDatastoreEntity[E <: BaseEntity : TypeTag : ClassTag](classInstance: E)(implicit tt: TypeTag[E]): Entity = {
    val entityValueMap = extractRuntimeClass[E]().getDeclaredFields
      .filterNot(_.isSynthetic)
      .map(field => {
        field.setAccessible(true)
        fieldToValueTuple(field.getName, field.get(classInstance))
      }).toMap
    Entity(Some(createKey(classInstance.id, classInstance.kind)), entityValueMap)
  }

  private def fieldToValueTuple(fieldName: String, value: Any) = {
    value match {
      case v: String => (fieldName, Value().withStringValue(v))
      case v: Boolean => (fieldName, Value().withBooleanValue(v))
      case v: Int => (fieldName, Value().withIntegerValue(v))
      case v: Long => (fieldName, Value().withIntegerValue(v))
      case v: Float => (fieldName, Value().withDoubleValue(v))
      case v: Double => (fieldName, Value().withDoubleValue(v))
      case v: Date => (fieldName, Value().withTimestampValue(Timestamp.of(v.getTime / 1000, (v.getTime % 1000 * 1000000).toInt)))
      case v: LatLng => (fieldName, Value().withGeoPointValue(v))
      case v: ByteString => (fieldName, Value().withBlobValue(v))
      case v: Any => throw UnsupportedFieldTypeException(v.getClass.getCanonicalName, fieldName)
    }
  }

  private def createKey(id: Any, kind: String) =
    id match {
      case id: String => Key(path = Seq(PathElement(kind, IdType.Name(id))))
      case id: Long => Key(path = Seq(PathElement(kind, IdType.Id(id))))
      case id: Int => Key(path = Seq(PathElement(kind, IdType.Id(id))))
      case id: Key => id
      case otherId => throw UnsupportedIdTypeException(otherId.getClass.getCanonicalName, kind)
    }

}

case class UnsupportedFieldTypeException(fieldTypeName: String, fieldName: String)
  extends RuntimeException(s"Fields of type: $fieldTypeName of field: $fieldName are not supported. If you think it's essential open an issue on Github.")

case class UnsupportedIdTypeException(idTypeName: String, kind: String) extends
  RuntimeException(s"Fields of type $idTypeName not supported, kind: $kind")