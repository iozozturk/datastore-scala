package com.ismetozozturk.datastore

import com.google.datastore.v1.Key
import com.google.datastore.v1.Query

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class DatastoreRepository[E <: BaseEntity: TypeTag: ClassTag](datastoreGrpc: DatastoreGrpc)(
  implicit ec: ExecutionContext
) extends EntityResolver {

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
    datastoreGrpc.delete(Seq(datastoreEntity)).map(_ => Unit)
  }

  def deleteMany(entities: Seq[E]): Future[Unit] = {
    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
    datastoreGrpc.delete(datastoreEntities).map(_ => Unit)
  }

//  def list(kind: String): Future[Seq[E]] = {
//    datastoreGrpc.get(None, kind).map(datastoreEntityToInstance[E])
//  }
//
//  def get(id: String) = {
//    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
//    datastoreGrpc.update(datastoreEntities).map(_ => entities)
//  }
//
//  def getMany(ids: Seq[String]) = {
//    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
//    datastoreGrpc.update(datastoreEntities).map(_ => entities)
//  }

  def runQuery(query: Query) = {}

}
