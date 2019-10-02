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
    CommitResponse(Seq(MutationResult(repoFixture.userEntity.key)))
  }
  when(mockDatastoreGrpc.update(any[Seq[Entity]])) thenReturn Future {
    CommitResponse(Seq(MutationResult(repoFixture.userEntity.key)))
  }
  when(mockDatastoreGrpc.delete(any[Seq[Entity]])) thenReturn Future {
    CommitResponse(Seq(MutationResult(repoFixture.userEntity.key)))
  }
  when(mockDatastoreGrpc.get(any[Seq[Key]])) thenReturn Future {
    Seq(repoFixture.userEntity)
  }
  when(mockDatastoreGrpc.runQuery(any[Query])) thenReturn Future {
    Seq(repoFixture.userEntity)
  }

  val datastoreRepositoryInTest = new DatastoreRepository[repoFixture.User](mockDatastoreGrpc)

  "Datastore Repository" should {

    "save object" in {
      Await.result(datastoreRepositoryInTest.insert(repoFixture.user), 3.second) shouldEqual repoFixture.user
    }

    "save many objects" in {
      Await.result(datastoreRepositoryInTest.insertMany(Seq(repoFixture.user)), 3.second) shouldEqual Seq(
        repoFixture.user
      )
    }

    "update object" in {
      Await.result(datastoreRepositoryInTest.update(repoFixture.user), 3.second) shouldEqual repoFixture.user
    }

    "update many objects" in {
      Await.result(datastoreRepositoryInTest.updateMany(Seq(repoFixture.user)), 3.second) shouldEqual Seq(
        repoFixture.user
      )
    }

    "delete object" in {
      Await.result(datastoreRepositoryInTest.delete(repoFixture.user), 3.second) shouldEqual ()
    }

    "delete many objects" in {
      Await.result(datastoreRepositoryInTest.deleteMany(Seq(repoFixture.user)), 3.second) shouldEqual ()
    }

    "get objects" in {
      Await.result(datastoreRepositoryInTest.get(repoFixture.user.id, repoFixture.user.kind), 3.second) shouldEqual Seq(
        repoFixture.user
      )
    }

    "get many objects" in {
      Await.result(datastoreRepositoryInTest.getMany(Seq(repoFixture.user.id), repoFixture.user.kind), 3.second) shouldEqual Seq(
        repoFixture.user
      )
    }

    "query objects" in {
      Await.result(
        datastoreRepositoryInTest.runQuery(Query(kind = Seq(KindExpression(repoFixture.user.kind)))),
        3.second
      ) shouldEqual Seq(
        repoFixture.user
      )
    }

  }

}

object repoFixture {

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
