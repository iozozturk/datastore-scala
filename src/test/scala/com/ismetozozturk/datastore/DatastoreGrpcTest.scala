package com.ismetozozturk.datastore

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.datastore.v1.Key.PathElement
import com.google.datastore.v1.Key.PathElement.IdType
import com.google.datastore.v1.BeginTransactionRequest
import com.google.datastore.v1.BeginTransactionResponse
import com.google.datastore.v1.CommitRequest
import com.google.datastore.v1.CommitResponse
import com.google.datastore.v1.DatastoreClient
import com.google.datastore.v1.Entity
import com.google.datastore.v1.EntityResult
import com.google.datastore.v1.Key
import com.google.datastore.v1.KindExpression
import com.google.datastore.v1.LookupRequest
import com.google.datastore.v1.LookupResponse
import com.google.datastore.v1.MutationResult
import com.google.datastore.v1.Query
import com.google.datastore.v1.QueryResultBatch
import com.google.datastore.v1.RunQueryRequest
import com.google.datastore.v1.RunQueryResponse
import com.google.datastore.v1.Value
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationDouble
import scala.concurrent.Await
import scala.concurrent.Future

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
    CommitResponse(Seq(MutationResult(repoFixture.userEntity.key)))
  }
  when(mockDatastoreClient.lookup(any[LookupRequest])) thenReturn Future {
    LookupResponse(found = Seq(EntityResult(Some(repoFixture.userEntity))))
  }
  when(mockDatastoreClient.runQuery(any[RunQueryRequest])) thenReturn Future {
    RunQueryResponse(Some(QueryResultBatch(entityResults = Seq(EntityResult(Some(repoFixture.userEntity))))))
  }

  private val datastoreGrpcInTest = new DatastoreGrpc(mockDatastoreHelper)

  "Datastore Grpc" should {
    "insert entity to datastore" in {
      Await.result(datastoreGrpcInTest.insert(Seq(grpcFixture.userEntity)), 3.second) shouldEqual CommitResponse(
        Seq(MutationResult(repoFixture.userEntity.key))
      )
    }

    "update entity in datastore" in {
      Await.result(datastoreGrpcInTest.update(Seq(grpcFixture.userEntity)), 3.second) shouldEqual CommitResponse(
        Seq(MutationResult(repoFixture.userEntity.key))
      )
    }

    "delete entity in datastore" in {
      Await.result(datastoreGrpcInTest.delete(Seq(grpcFixture.userEntity)), 3.second) shouldEqual CommitResponse(
        Seq(MutationResult(repoFixture.userEntity.key))
      )
    }

    "get entity from datastore" in {
      Await.result(datastoreGrpcInTest.get(Seq(grpcFixture.userKey)), 3.second) shouldEqual Seq(repoFixture.userEntity)
    }

    "query entities from datastore" in {
      Await.result(datastoreGrpcInTest.runQuery(Query(kind = Seq(KindExpression(grpcFixture.user.kind)))), 3.second) shouldEqual Seq(
        repoFixture.userEntity
      )
    }
  }
}

object grpcFixture {

  case class User(name: String, age: Int) extends BaseEntity {
    override def id: Any = name

    override def kind: String = "users"

    override def excludeFromIndex: Boolean = false
  }

  val user = User("ismet", 35)

  val userKey = Key(path = Seq(PathElement(user.kind, IdType.Name(user.id.toString))))

  val userEntity = Entity(
    Some(userKey),
    HashMap(
      "name" -> Value().withStringValue(user.name),
      "age" -> Value().withIntegerValue(user.age)
    )
  )
}
