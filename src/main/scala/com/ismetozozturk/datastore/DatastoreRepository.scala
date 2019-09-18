package com.ismetozozturk.datastore

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

}
