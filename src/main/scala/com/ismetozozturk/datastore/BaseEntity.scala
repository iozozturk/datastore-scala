package com.ismetozozturk.datastore

trait BaseEntity {
  def id: Any

  def kind: String

  def excludeFromIndex: Boolean

}
