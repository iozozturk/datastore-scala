package com.ismetozozturk.datastore

import akka.actor.ActorSystem
import akka.event.Logging
import akka.stream.ActorMaterializer
import com.google.datastore.v1.Filter.FilterType
import com.google.datastore.v1.PropertyFilter.Operator
import com.google.datastore.v1.Filter
import com.google.datastore.v1.PropertyFilter
import com.google.datastore.v1.PropertyReference
import com.google.datastore.v1.Query
import com.google.datastore.v1.Value
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class DatastoreRepository[E <: BaseEntity: TypeTag: ClassTag](datastoreGrpc: DatastoreGrpc)(
  implicit ec: ExecutionContext,
  actorSystem: ActorSystem
) extends EntityResolver {

  val logger = Logging(actorSystem.eventStream, "datastore-grpc")

  def insert(entity: E): Future[E] = {
    val datastoreEntity = instanceToDatastoreEntity[E](entity)
    datastoreGrpc.insert(Seq(datastoreEntity)).map(_ => entity)
  }

  def insertMany(entities: Seq[E]): Future[Seq[E]] = {
    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
    datastoreGrpc.insert(datastoreEntities).map(_ => entities)
  }

  def update(entity: E): Future[E] = {
    val datastoreEntity = instanceToDatastoreEntity[E](entity)
    datastoreGrpc.update(Seq(datastoreEntity)).map(_ => entity)
  }

  def updateMany(entities: Seq[E]): Future[Seq[E]] = {
    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
    datastoreGrpc.update(datastoreEntities).map(_ => entities)
  }

  def delete(entity: E): Future[Unit] = {
    val datastoreEntity = instanceToDatastoreEntity[E](entity)
    datastoreGrpc.delete(Seq(datastoreEntity)).map(_ => ())
  }

  def deleteMany(entities: Seq[E]): Future[Unit] = {
    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
    datastoreGrpc.delete(datastoreEntities).map(_ => ())
  }

  def get(id: Any, kind: String): Future[Seq[E]] = {
    datastoreGrpc.get(Seq(createKey(Some(id), kind))).map(entities => entities.map(datastoreEntityToInstance[E]))
  }

  def getMany(ids: Seq[Any], kind: String): Future[Seq[E]] = {
    datastoreGrpc
      .get(ids.map(id => createKey(Some(id), kind)))
      .map(entities => entities.map(datastoreEntityToInstance[E]))
  }

  def runQuery(query: Query): Future[Seq[E]] = {
    datastoreGrpc.runQuery(query).map(entities => entities.map(datastoreEntityToInstance[E]))
  }

  def healthCheck(): Future[Boolean] = {
    datastoreGrpc
      .runQuery(Query().withLimit(1))
      .map(_ => true)
      .recover {
        case e: RuntimeException =>
          logger.error("health check failed", e)
          false
      }
  }

}

object DatastoreRepository {
  def apply[E <: BaseEntity: TypeTag: ClassTag]()(
    implicit ec: ExecutionContext,
    materializer: ActorMaterializer,
    actorSystem: ActorSystem
  ) =
    new DatastoreRepository[E](new DatastoreGrpc(new DatastoreHelper(DatastoreConfig(ConfigFactory.load))))
}
