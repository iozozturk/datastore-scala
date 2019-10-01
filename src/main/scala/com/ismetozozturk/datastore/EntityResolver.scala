package com.ismetozozturk.datastore

import java.sql.Blob
import java.time.Instant
import java.util.Date

import com.google.`type`.LatLng
import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1.Entity
import com.google.datastore.v1.Key
import com.google.datastore.v1.Value
import com.google.protobuf.ByteString
import com.google.protobuf.timestamp.Timestamp

import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.reflect.runtime.universe._

trait EntityResolver {
  private[datastore] def extractRuntimeClass[E: ClassTag](): RuntimeClass = classTag[E].runtimeClass

  private[datastore] def instanceToDatastoreEntity[E <: BaseEntity: TypeTag: ClassTag](
    classInstance: E
  )(implicit tt: TypeTag[E]): Entity = {
    val entityValueMap = extractRuntimeClass[E]().getDeclaredFields
      .filterNot(_.isSynthetic)
      .map(field => {
        field.setAccessible(true)
        fieldToValueTuple(field.getName, field.get(classInstance))
      })
      .toMap
    Entity(Some(createKey(Some(classInstance.id), classInstance.kind)), entityValueMap)
  }

  private def fieldToValueTuple(fieldName: String, value: Any) = {
    value match {
      case v: String  => (fieldName, Value().withStringValue(v))
      case v: Boolean => (fieldName, Value().withBooleanValue(v))
      case v: Int     => (fieldName, Value().withIntegerValue(v))
      case v: Long    => (fieldName, Value().withIntegerValue(v))
      case v: Float   => (fieldName, Value().withDoubleValue(v))
      case v: Double  => (fieldName, Value().withDoubleValue(v))
      case v: Date =>
        (fieldName, Value().withTimestampValue(Timestamp.of(v.getTime / 1000, (v.getTime % 1000 * 1000000).toInt)))
      case v: LatLng     => (fieldName, Value().withGeoPointValue(v))
      case v: ByteString => (fieldName, Value().withBlobValue(v))
      case v: Any        => throw UnsupportedFieldTypeException(v.getClass.getCanonicalName, fieldName)
    }
  }

  private[datastore] def createKey(id: Option[Any], kind: String) =
    id match {
      case Some(id: String) => Key(path = Seq(PathElement(kind, IdType.Name(id))))
      case Some(id: Long)   => Key(path = Seq(PathElement(kind, IdType.Id(id))))
      case Some(id: Int)    => Key(path = Seq(PathElement(kind, IdType.Id(id))))
      case Some(id: Key)    => id
      case None             => Key(path = Seq(PathElement(kind, IdType.Empty)))
      case otherId          => throw UnsupportedIdTypeException(otherId.getClass.getCanonicalName, kind)
    }

  private[datastore] def datastoreEntityToInstance[E <: BaseEntity: TypeTag: ClassTag](entity: Entity): E = {
    val clazz = extractRuntimeClass[E]()
    val defaultInstance = createBlankInstance[E](clazz)
    setInstanceValues(defaultInstance, entity)
    defaultInstance.asInstanceOf[E]
  }

  private def setInstanceValues[T](blankInstance: T, entity: Entity)(implicit tt: TypeTag[T], ct: ClassTag[T]): Unit = {
    tt.tpe.members collect {
      case member if member.isMethod && member.asMethod.isCaseAccessor => member.asMethod
    } foreach { member =>
      val field = tt.mirror.reflect(blankInstance).reflectField(member)
      val fieldClassName = member.returnType.typeSymbol.fullName
      val fieldName = member.name.toString
      val value = fieldClassName match {
        case OptionClassName =>
          if (!entity.properties.contains(fieldName) || entity.properties(fieldName).valueType.isNullValue) {
            None
          } else {
            val genericClassName = member.returnType.typeArgs.head.typeSymbol.fullName
            Some(extractValueFromEntity(genericClassName, fieldName, entity))
          }
        case className =>
          extractValueFromEntity(className, fieldName, entity)
      }
      field.set(value)
    }
  }

  private def extractValueFromEntity(className: String, fieldName: String, entity: Entity): Any = {
    className match {
      case IntClassName     => entity.properties(fieldName).getIntegerValue.toInt
      case LongClassName    => entity.properties(fieldName).getIntegerValue
      case StringClassName  => entity.properties(fieldName).getStringValue
      case FloatClassName   => entity.properties(fieldName).getDoubleValue
      case DoubleClassName  => entity.properties(fieldName).getDoubleValue
      case BooleanClassName => entity.properties(fieldName).getBooleanValue
      case JavaDateClassName =>
        Date.from(Instant.ofEpochMilli(entity.properties(fieldName).getTimestampValue.nanos / 1000000))
      case DatastoreLatLongClassName => entity.properties(fieldName).getGeoPointValue
      case DatastoreBlobClassName    => entity.properties(fieldName).getBlobValue
      case _                         => throw UnsupportedEntityFieldTypeException(className, fieldName)
    }
  }

  private def createBlankInstance[E](clazz: Class[_]): E = {
    val constructor = clazz.getConstructors.head
    val params = constructor.getParameterTypes
      .map(
        paramClass =>
          getClassName(paramClass) match {
            case IntClassName              => Int.MinValue
            case LongClassName             => 0L
            case StringClassName           => ""
            case FloatClassName            => 0.0F
            case DoubleClassName           => 0.0
            case BooleanClassName          => false
            case JavaDateClassName         => new Date(0)
            case DatastoreLatLongClassName => LatLng.of(0.0, 0.0)
            case DatastoreBlobClassName    => ByteString.copyFrom(Array[Byte]())
            case OptionClassName           => None
            case _                         => null
        }
      )
      .map(_.asInstanceOf[Object])
    constructor.newInstance(params: _*).asInstanceOf[E]
  }

  private def getClassName[E: TypeTag]: String = {
    typeOf[E].typeSymbol.fullName
  }

  protected def getClassName(clazz: Class[_]): String = {
    runtimeMirror(clazz.getClassLoader).classSymbol(clazz).fullName
  }

  private val IntClassName = getClassName[Int]
  private val LongClassName = getClassName[Long]
  private val FloatClassName = getClassName[Float]
  private val DoubleClassName = getClassName[Double]
  private val StringClassName = getClassName[String]
  private val JavaDateClassName = getClassName[Date]
  private val BooleanClassName = getClassName[Boolean]
  private val DatastoreLatLongClassName = getClassName[LatLng]
  private val DatastoreBlobClassName = getClassName[Blob]
  private val OptionClassName = getClassName[Option[_]]

}

case class UnsupportedFieldTypeException(fieldTypeName: String, fieldName: String)
    extends RuntimeException(
      s"Fields of type: $fieldTypeName of field: $fieldName are not supported. If you think it's essential open an issue on Github."
    )

case class UnsupportedIdTypeException(idTypeName: String, kind: String)
    extends RuntimeException(s"Fields of type $idTypeName not supported, kind: $kind")

case class UnsupportedEntityFieldTypeException(idTypeName: String, kind: String)
    extends RuntimeException(s"Fields of type $idTypeName not supported, kind: $kind")
