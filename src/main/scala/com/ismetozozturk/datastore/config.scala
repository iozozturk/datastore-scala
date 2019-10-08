package com.ismetozozturk.datastore

import com.typesafe.config.Config

case class DatastoreConfig(projectId: String, environment: Environment)

object DatastoreConfig {
  def apply(config: Config): DatastoreConfig = {
    new DatastoreConfig(
      config.getString("gcp-project-id"),
      Environment(config)
    )
  }
}

object Environment {
  def apply(config: Config): Environment = {
    config.getString("environment") match {
      case "prod" => PROD
      case _      => DEV
    }
  }
}

sealed trait Environment

case object PROD extends Environment

case object DEV extends Environment
