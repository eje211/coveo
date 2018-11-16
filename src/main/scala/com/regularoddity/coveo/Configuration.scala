package com.regularoddity.coveo

import com.google.inject.{ AbstractModule, Guice, ImplementedBy, Inject }
import com.typesafe.config.{ Config, ConfigFactory }

@ImplementedBy(classOf[ConfigurationDefault])
trait Configuration {
  @Inject()
  def configuration: Config
}

class ConfigurationDefault extends Configuration {
  def configuration: Config = ConfigFactory.load("default.conf")
}

class ConfigurationModule extends AbstractModule {
  override protected def configure() {
    bind(classOf[Configuration]).to(classOf[ConfigurationDefault])
  }
}

object AppConfiguration {
  val injector = Guice.createInjector(new ConfigurationModule)
  val config = injector.getInstance(classOf[Configuration])

  def apply() = config.configuration

}