package com.ismetozozturk.datastore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1.{BeginTransactionRequest, BeginTransactionResponse, CommitRequest, CommitResponse, DatastoreClient, Entity, Key, MutationResult, Value}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scala.concurrent.{Await, Future}

class DatastoreGrpcTest extends WordSpec with MockitoSugar with Matchers {
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val mockDatastoreHelper: DatastoreHelper = mock[DatastoreHelper]
  private val mockDatastoreClient = mock[DatastoreClient]
  private val mockDatastoreConfig = mock[DatastoreConfig]
  when(mockDatastoreHelper.datastoreConfig) thenReturn mockDatastoreConfig
  when(mockDatastoreConfig.projectId) thenReturn ""
  when(mockDatastoreHelper.client) thenReturn mockDatastoreClient
  when(mockDatastoreClient.beginTransaction(any[BeginTransactionRequest])) thenReturn Future {
    BeginTransactionResponse()
  }
  when(mockDatastoreClient.commit(any[CommitRequest])) thenReturn Future {
    CommitResponse(Seq(MutationResult(fixture.userEntity.key)))
  }

  private val datastoreGrpcInTest = new DatastoreGrpc(mockDatastoreHelper)

  "Datastore Grpc" should {
    "insert entity to datastore" in {
      Await.result(datastoreGrpcInTest.insert(fixture.userEntity), 3.second) shouldEqual CommitResponse(Seq(MutationResult(fixture.userEntity.key)))
    }
  }

  object fixture {

    case class User(name: String, age: Int) extends BaseEntity {
      override def id: Any = "user1"

      override def kind: String = "users"

      override def excludeFromIndex: Boolean = false
    }

    val user = User("ismet", 35)

    val userEntity = Entity(Some(Key(path = Seq(PathElement(user.kind, IdType.Name(user.id.toString))))),
      HashMap(
        "name" -> Value().withStringValue(user.name),
        "age" -> Value().withIntegerValue(user.age)
      ))
  }

}
