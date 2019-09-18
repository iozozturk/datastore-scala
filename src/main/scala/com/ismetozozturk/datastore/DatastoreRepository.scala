package com.ismetozozturk.datastore

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class DatastoreRepository(datastoreGrpc: DatastoreGrpc)(implicit ec: ExecutionContext) extends EntityResolver {

  def insert[E <: BaseEntity : TypeTag : ClassTag](entity: E): Future[E] = {
    val datastoreEntity = instanceToDatastoreEntity[E](entity)
    datastoreGrpc.insert(Seq(datastoreEntity)).map(_ => entity)
  }

  def insertMany[E <: BaseEntity : TypeTag : ClassTag](entities: Seq[E]): Future[Seq[E]] = {
    val datastoreEntities = entities.map(instanceToDatastoreEntity[E])
    datastoreGrpc.insert(datastoreEntities).map(_ => entities)
  }


}
