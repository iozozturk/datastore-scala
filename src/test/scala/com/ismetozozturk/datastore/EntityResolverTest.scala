package com.ismetozozturk.datastore

import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1.{Entity, Key, Value}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.immutable.HashMap

class EntityResolverTest extends WordSpec with MockitoSugar with Matchers {
  private val entityResolverInTest = new EntityResolver {}

  case class User(name: String, age: Int) extends BaseEntity {
    override def id: Any = "user1"

    override def kind: String = "users"

    override def excludeFromIndex: Boolean = false
  }

  val user = User("ismet", 35)

  "Entity resolver" should {

    "resolve runtime class" in {
      entityResolverInTest.extractRuntimeClass[User]() shouldEqual User("", 1).getClass
    }
    "convert base entity to datastore entity" in {
      entityResolverInTest.instanceToDatastoreEntity[User](user) shouldEqual
        Entity(Some(Key(path = Seq(PathElement(user.kind, IdType.Name(user.id.toString))))),
          HashMap(
            "name" -> Value().withStringValue(user.name),
            "age" -> Value().withIntegerValue(user.age)
          ))
    }
  }

}
