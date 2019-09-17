package com.ismetozozturk.datastore

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class DatastoreRepository(datastoreGrpc: DatastoreGrpc)(implicit ec: ExecutionContext) extends EntityResolver {

  def insert[E <: BaseEntity : TypeTag : ClassTag](entity: E): Future[E] = {
    val datastoreEntity = instanceToDatastoreEntity[E](entity)
    datastoreGrpc.save(datastoreEntity).map(_ => entity)
  }


}
