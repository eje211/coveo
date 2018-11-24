package com.regularoddity.coveo

import java.io.File

import com.google.inject.{ AbstractModule, Guice, ImplementedBy, Inject }
import com.typesafe.config.{ Config, ConfigFactory }

/**
 * How to configure the main service for this application.
 * The main way can be overridden if necessary.
 */
@ImplementedBy(classOf[ConfigurationHandle])
trait Configuration {
  /**
   * @return the configuration for the service.
   */
  @Inject()
  def configuration: Config
}

/**
 * Fetches the configuration file on the filesystem or otherwise, it falls back to getting one from a resource.
 * This class can be overridden if another way of getting a configuration is required.
 *
 * @param fileName The name of default configuration file to fall back on.
 */
class ConfigurationHandle @Inject() (fileName: String = "default.conf") extends Configuration {
  /**
   * Change any file path given to `File` objects. The first file to actually exist is returned.
   * @param paths The paths to convert.
   * @return The first path that represents a file that exists or the last path given as a `File` object.
   */
  def pathsToFile(paths: Seq[String]): File = paths.foldRight(new File(""))((path, file) =>
    if (file.exists()) return file else new File(path))

  /**
   * All the places where the configuration file can be on the filesystem.
   */
  val configLocations: Seq[String] =
    System.getProperty("user.home") + File.separator + ".coveo.conf" ::
      "/etc/coveolocationservice.conf" :: Nil

  /**
   * The best guess where the external configuration file is on the filesystem.
   */
  val configFile: File = pathsToFile(configLocations)

  /**
   * The best guess where the configuration file is.
   * @return the configuration for this application.
   */
  override def configuration: Config =
    Some(configFile).filter(_.exists()) map ConfigFactory.parseFile getOrElse ConfigFactory.load(fileName)
}

/**
 * The main interface for this service.
 * @param config How this service should be configured.
 */
@ImplementedBy(classOf[ServerApplication])
abstract class Server(val config: Configuration)

/**
 * Sets up the bindings for the dependency injections.
 */
class ConfigurationModule extends AbstractModule {
  override protected def configure() {
    bind(classOf[Configuration]) to classOf[ConfigurationHandle]
    bind(classOf[Server]) to classOf[ServerApplication]
  }
}
