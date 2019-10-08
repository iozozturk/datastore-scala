# Google Cloud Datastore Scala client

True asynchronous and non-blocking Scala client library for Google Cloud Datastore with gRPC backend. 

## Features
- Insert, Update, Delete, Query, Lookup, HealthCheck operations
- All operations are asynchronous and non blocking
- Connections are handled in HTTP/2 gRPC layer
- Transactional by default
- All fields are indexed
- Automatic class to entity, entity to class resolution
- Configurations can be overridden on demand
- Supports Scala 2.13

## Overview

Current implementations of Google Cloud Datastore Java client library and other custom implementations found on Github
 are synchronous and blocking. This creates performance issues at scale. This library use Datastore Proto files and gRPC 
 as backend so that all communication layer is async and return `Future`s. Also relevant Datastore classes are created from 
 official Google Cloud Datastore Proto files to be consistent and provides easy migration in future.
 
## Usage
Simply add library as a dependency.
```
libraryDependencies ++= Seq(
  "com.ismetozozturk" %% "datastore-scala" % "0.1.3"
)
```

## Example: Insert

Extend your classes from BaseEntity and override `kind` and `id` attributes.

```
  case class User(name: String, age: Int) extends BaseEntity {
    override def id: Any = name

    override def kind: String = "users"
  }

  val user = User("ismet", 35)

  val userRepository = DatastoreRepository[User]()

  val userF: Future[User] = userRepository.insert(user)

```

## Example: Query

Use native Query classes coming from Google Cloud Datastore to write your queries. For lookups based on Ids, use `get` and `getMany`
 methods from this library.
 
```
  val userRepository = DatastoreRepository[User]()

  val userF: Future[Seq[User]] = userRepository.runQuery(Query(kind = Seq(KindExpression("users"))))
```

## Example: HealthCheck

For implementing you service healthchecks, you can use `healthcheck` method from this library to check if you can access datastore.

```
  val datastoreHealth: Future[Boolean] = repositoryInTest.healthCheck()
```

Check Integration Tests and Unit Tests for further library documentation.

## Running Tests

To run unit tests simply run: 

`sbt test`

Integration test setup include datastore emulator as well. To run integration tests run:

`sbt 'dockerComposeTest it:test'`

## TODO
[] excludeFromIndex support
[] transactional and non-transactional commit support
[] handle array types
[] travis build, test
[] support for auto generated ids, currently only custom keys supported
[] get rid of default instance creation, blocks require statements in classes