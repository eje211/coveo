package com.regularoddity.coveo

import com.google.inject.{ AbstractModule, Guice, ImplementedBy, Inject }
import com.typesafe.config.{ Config, ConfigFactory }

@ImplementedBy(classOf[ConfigurationHandle])
trait Configuration {
  @Inject()
  def configuration: Config
}

class ConfigurationHandle(fileName: String = "default.conf") extends Configuration {
  def configuration: Config = ConfigFactory.load(fileName)
}

class ConfigurationModule extends AbstractModule {
  override protected def configure() {
    bind(classOf[Configuration]).to(classOf[ConfigurationHandle])
  }
}

/*
 * Objects are lazy. They are only instantiated on first call.
 * Having both of the following objects here does not mean it will necessarily be
 * instantiated. It will only be instantiated if and when called.
 */
object AppConfiguration {
  val injector = Guice.createInjector(new ConfigurationModule)
  val config = injector.getInstance(classOf[Configuration])

  def apply() = config.configuration
}