package com.ismetozozturk.datastore

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.datastore.v1.CommitRequest.TransactionSelector
import com.google.datastore.v1.Mutation.Operation
import com.google.datastore.v1._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class DatastoreGrpc(val datastore: DatastoreHelper)(
  implicit executionContext: ExecutionContext,
  actorSystem: ActorSystem,
  materializer: Materializer
) {

  private[datastore] def insert(entities: Seq[Entity]): Future[CommitResponse] = {
    datastore.client
      .beginTransaction(BeginTransactionRequest(datastore.datastoreConfig.projectId))
      .flatMap { response =>
        datastore.client
          .commit(
            CommitRequest(
              datastore.datastoreConfig.projectId,
              transactionSelector = TransactionSelector.Transaction(response.transaction),
              mutations = entities.map(e => Mutation(Operation.Insert(e)))
            )
          )
      }
  }

  private[datastore] def update(entities: Seq[Entity]): Future[CommitResponse] = {
    datastore.client
      .beginTransaction(BeginTransactionRequest(datastore.datastoreConfig.projectId))
      .flatMap { response =>
        datastore.client
          .commit(
            CommitRequest(
              datastore.datastoreConfig.projectId,
              transactionSelector = TransactionSelector.Transaction(response.transaction),
              mutations = entities.map(e => Mutation(Operation.Update(e)))
            )
          )
      }
  }

  private[datastore] def delete(entities: Seq[Entity]): Future[CommitResponse] = {
    datastore.client
      .beginTransaction(BeginTransactionRequest(datastore.datastoreConfig.projectId))
      .flatMap { response =>
        datastore.client
          .commit(
            CommitRequest(
              datastore.datastoreConfig.projectId,
              transactionSelector = TransactionSelector.Transaction(response.transaction),
              mutations = entities.map(e => Mutation(Operation.Delete(e.key.get)))
            )
          )
      }
  }

  private[datastore] def get(keys: Seq[Key]): Future[Seq[Entity]] = {
    datastore.client.lookup(LookupRequest(datastore.datastoreConfig.projectId, None, keys)).map { response =>
      response.found.map(_.getEntity)
    }
  }

  private[datastore] def runQuery(query: Query) = {
    datastore.client
      .runQuery(RunQueryRequest(datastore.datastoreConfig.projectId).withQuery(query))
      .map { response =>
        response.getBatch.entityResults.map(_.getEntity)
      }
  }

}
