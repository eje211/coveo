package com.regularoddity.coveo

import com.typesafe.config.Config

import scala.collection.mutable

class ServicesMap extends mutable.HashMap[String, Server] with collection.concurrent.Map[String, Server] {
  override def putIfAbsent(k: String, v: Server): Option[Server] = {
    val result = get(k)
    if (result.isEmpty) {
      put(k, v)
    }
    result
  }

  override def remove(k: String, v: Server): Boolean = {
    val result = contains(k)
    remove(k)
    result
  }

  override def replace(k: String, oldvalue: Server, newvalue: Server): Boolean = {
    val result = get(k) contains oldvalue
    if (result) {
      put(k, newvalue)
    }
    result
  }

  override def replace(k: String, v: Server): Option[Server] = {
    val result = get(k)
    if (result.isEmpty) {
      put(k, v)
    }
    result
  }
}

class ConfMap extends mutable.HashMap[String, Config] with collection.concurrent.Map[String, Config] {
  override def putIfAbsent(k: String, v: Config): Option[Config] = {
    val result = get(k)
    if (result.isEmpty) {
      put(k, v)
    }
    result
  }

  override def remove(k: String, v: Config): Boolean = {
    val result = contains(k)
    remove(k)
    result
  }

  override def replace(k: String, oldvalue: Config, newvalue: Config): Boolean = {
    val result = get(k) contains oldvalue
    if (result) {
      put(k, newvalue)
    }
    result
  }

  override def replace(k: String, v: Config): Option[Config] = {
    val result = get(k)
    if (result.isEmpty) {
      put(k, v)
    }
    result
  }
}