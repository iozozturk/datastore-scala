package com.ismetozozturk.datastore

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.google.datastore.v1.CommitRequest.TransactionSelector
import com.google.datastore.v1.{BeginTransactionRequest, CommitRequest, CommitResponse, Entity, Mutation}
import com.google.datastore.v1.Mutation.Operation

import scala.concurrent.{ExecutionContext, Future}

class DatastoreGrpc(val datastore: DatastoreHelper)(
  implicit executionContext: ExecutionContext,
  actorSystem: ActorSystem,
  materializer: Materializer
) {

  def insert(entities: Seq[Entity]): Future[CommitResponse] = {
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

}
