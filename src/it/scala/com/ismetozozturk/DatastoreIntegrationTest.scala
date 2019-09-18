package com.ismetozozturk

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.ismetozozturk.datastore._
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatest.Matchers
import org.scalatest.WordSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble

class DatastoreIntegrationTest extends WordSpec with Matchers {
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val config = ConfigFactory.load
  private val datastoreConfig = DatastoreConfig(config)
  private val datastoreHelper = new DatastoreHelper(datastoreConfig)
  private val datastoreGrpc = new DatastoreGrpc(datastoreHelper)
  private implicit val patience: PatienceConfiguration.Timeout = PatienceConfiguration.Timeout(Span(3, Seconds))

  private val repositoryInTest = new DatastoreRepository(datastoreGrpc)

  "Datastore repository" should {

    "insert user entity in datastore" in {
      Await.result(repositoryInTest.insert[fixture.User](fixture.user), 3.second) shouldEqual fixture.user
    }

    "insert many user entities in datastore" in {
      Await.result(repositoryInTest.insertMany[fixture.User](Seq(fixture.user1, fixture.user2)), 3.second) shouldEqual Seq(
        fixture.user1,
        fixture.user2
      )
    }
  }

  object fixture {

    case class User(name: String, age: Int) extends BaseEntity {
      override def id: Any = name

      override def kind: String = "users"

      override def excludeFromIndex: Boolean = false
    }

    val user = User("ismet", 35)
    val user1 = User("merve", 26)
    val user2 = User("maya", 26)

  }

}
