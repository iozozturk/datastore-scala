package com.ismetozozturk.datastore

import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble
import scala.concurrent.Await
import scala.concurrent.Future

class DatastoreRepositoryTest extends WordSpec with MockitoSugar with Matchers {
  private val mockDatastoreGrpc = mock[DatastoreGrpc]
  when(mockDatastoreGrpc.insert(any[Seq[Entity]])) thenReturn Future {
    CommitResponse(Seq(MutationResult(fixture.userEntity.key)))
  }
  when(mockDatastoreGrpc.update(any[Seq[Entity]])) thenReturn Future {
    CommitResponse(Seq(MutationResult(fixture.userEntity.key)))
  }
  when(mockDatastoreGrpc.delete(any[Seq[Entity]])) thenReturn Future {
    CommitResponse(Seq(MutationResult(fixture.userEntity.key)))
  }
  when(mockDatastoreGrpc.get(any[Option[Seq[Key]]], any[String])) thenReturn Future {
    Seq(fixture.userEntity)
  }

  val datastoreRepositoryInTest = new DatastoreRepository[fixture.User](mockDatastoreGrpc)

  "Datastore Repository" should {

    "save object" in {
      Await.result(datastoreRepositoryInTest.insert(fixture.user), 3.second) shouldEqual fixture.user
    }

    "save many objects" in {
      Await.result(datastoreRepositoryInTest.insertMany(Seq(fixture.user)), 3.second) shouldEqual Seq(fixture.user)
    }

    "update object" in {
      Await.result(datastoreRepositoryInTest.update(fixture.user), 3.second) shouldEqual fixture.user
    }

    "update many objects" in {
      Await.result(datastoreRepositoryInTest.updateMany(Seq(fixture.user)), 3.second) shouldEqual Seq(fixture.user)
    }

    "delete object" in {
      Await.result(datastoreRepositoryInTest.delete(fixture.user), 3.second) shouldEqual ()
    }

    "delete many objects" in {
      Await.result(datastoreRepositoryInTest.deleteMany(Seq(fixture.user)), 3.second) shouldEqual ()
    }

    "list objects" in {
      Await.result(datastoreRepositoryInTest.list(fixture.user.kind), 3.second) shouldEqual Seq(fixture.user)
    }

  }

}

object fixture {

  case class User(name: String, age: Int) extends BaseEntity {
    override def id: Any = name

    override def kind: String = "users"

    override def excludeFromIndex: Boolean = false
  }

  val user = User("ismet", 35)

  val userEntity = Entity(
    Some(Key(path = Seq(PathElement(user.kind, IdType.Name(user.id.toString))))),
    HashMap(
      "name" -> Value().withStringValue(user.name),
      "age" -> Value().withIntegerValue(user.age)
    )
  )
}
