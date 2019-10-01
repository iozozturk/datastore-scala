package com.ismetozozturk.datastore

import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1.Entity
import com.google.datastore.v1.Key
import com.google.datastore.v1.Value
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.immutable.HashMap

case class User(name: String, age: Int) extends BaseEntity {
  override def id: Any = name

  override def kind: String = "users"

  override def excludeFromIndex: Boolean = false
}

class EntityResolverTest extends WordSpec with MockitoSugar with Matchers {
  private val entityResolverInTest = new EntityResolver {}

  val user = User("ismet", 35)

  "Entity resolver" should {

    "resolve runtime class" in {
      entityResolverInTest.extractRuntimeClass[User]() shouldEqual User("", 1).getClass
    }

    "convert instance to datastore entity" in {
      entityResolverInTest.instanceToDatastoreEntity[User](user) shouldEqual
        Entity(
          Some(Key(path = Seq(PathElement(user.kind, IdType.Name(user.id.toString))))),
          HashMap(
            "name" -> Value().withStringValue(user.name),
            "age" -> Value().withIntegerValue(user.age)
          )
        )
    }

    "convert entity to instance" in {
      entityResolverInTest.datastoreEntityToInstance[User](
        Entity(
          Some(Key(path = Seq(PathElement(user.kind, IdType.Name(user.id.toString))))),
          HashMap(
            "name" -> Value().withStringValue(user.name),
            "age" -> Value().withIntegerValue(user.age)
          )
        )
      ) shouldEqual user
    }
  }

}
