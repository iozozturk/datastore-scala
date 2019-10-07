package com.ismetozozturk

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.datastore.v1.KindExpression
import com.google.datastore.v1.Query
import com.ismetozozturk.datastore._
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.Seconds
import org.scalatest.time.Span

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble

class DatastoreIntegrationTest extends WordSpec with Matchers {
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private implicit val patience: PatienceConfiguration.Timeout = PatienceConfiguration.Timeout(Span(3, Seconds))

  private val repositoryInTest = DatastoreRepository[IntegrationUser]()

  "Datastore repository" should {

    "insert user entity in datastore" in {
      val user = IntegrationUser("ismet", 35)
      Await.result(repositoryInTest.insert(user), 3.second) shouldEqual user
    }

    "insert many user entities in datastore" in {
      val user1 = IntegrationUser("merve", 26)
      val user2 = IntegrationUser("maya", 26)
      Await.result(repositoryInTest.insertMany(Seq(user1, user2)), 3.second) shouldEqual Seq(
        user1,
        user2
      )
    }

    "update existing entity in datastore" in {
      val user1 = IntegrationUser("anon-user", 26)
      Await.result(repositoryInTest.insert(user1), 3.second)
      val updatedUser = user1.copy(age = 36)
      Await.result(repositoryInTest.update(updatedUser), 3.second) shouldEqual updatedUser
    }

    "update many existing entity in datastore" in {
      val user1 = IntegrationUser("one-user", 26)
      val user2 = IntegrationUser("another-user", 26)
      Await.result(repositoryInTest.insertMany(Seq(user1, user2)), 3.second)
      val updatedUsers = Seq(user1.copy(age = 36), user2.copy(age = 37))
      Await.result(repositoryInTest.updateMany(updatedUsers), 3.second) shouldEqual updatedUsers
    }

    "delete existing entity in datastore" in {
      val user1 = IntegrationUser("user-to-delete", 26)
      Await.result(repositoryInTest.insert(user1), 3.second)
      Await.result(repositoryInTest.delete(user1), 3.second) shouldEqual ()
    }

    "delete many existing entity in datastore" in {
      val users = Seq(IntegrationUser("user-to-delete-1", 26), IntegrationUser("user-to-delete-2", 26))
      Await.result(repositoryInTest.insertMany(users), 3.second)
      Await.result(repositoryInTest.deleteMany(users), 3.second) shouldEqual ()
    }

    "get existing entity from datastore" in {
      val user = IntegrationUser("user-to-find", 26)
      Await.result(repositoryInTest.insert(user), 3.second)
      Await.result(repositoryInTest.get(user.id, user.kind), 3.second) shouldEqual Seq(user)
    }

    "get many existing entities from datastore" in {
      val users = Seq(IntegrationUser("user-to-find-many", 26), IntegrationUser("user-to-find-many-1", 26))
      Await.result(repositoryInTest.insertMany(users), 3.second)
      Await.result(repositoryInTest.getMany(Seq(users.head.id, users.tail.head.id), users.head.kind), 3.second) shouldEqual users
    }

    "query entities from datastore" in {
      val users = Seq(IntegrationUser("user-to-query", 26), IntegrationUser("user-to-query-1", 27))
      Await.result(repositoryInTest.insertMany(users), 3.second)
      Await.result(repositoryInTest.runQuery(Query(kind = Seq(KindExpression(users.head.kind)))), 3.second) contains users
    }

    "health check datastore" in {
      Await.result(repositoryInTest.healthCheck(), 3.second) shouldEqual true
    }

  }

}

case class IntegrationUser(name: String, age: Int) extends BaseEntity {
  override def id: Any = name

  override def kind: String = "users"
}
