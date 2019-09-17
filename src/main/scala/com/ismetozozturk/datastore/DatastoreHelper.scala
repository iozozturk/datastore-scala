package com.ismetozozturk.datastore

import java.util.Collections

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.Materializer
import com.google.auth.oauth2.GoogleCredentials
import com.google.datastore.v1.DatastoreClient
import io.grpc.auth.MoreCallCredentials

import scala.concurrent.ExecutionContext

class DatastoreHelper(val datastoreConfig: DatastoreConfig)(
  implicit executionContext: ExecutionContext,
  actorSystem: ActorSystem,
  materializer: Materializer
) {

  private val credentials = MoreCallCredentials.from(
    GoogleCredentials.getApplicationDefault.createScoped(
      Collections.singletonList("https://www.googleapis.com/auth/datastore")
    )
  )

  var client: DatastoreClient =
    if (datastoreConfig.environment == DEV) {
      DatastoreClient(GrpcClientSettings.fromConfig("datastore-grpc").withTls(false))
    } else {
      DatastoreClient(GrpcClientSettings.fromConfig("datastore-grpc").withCallCredentials(credentials))
    }
}
