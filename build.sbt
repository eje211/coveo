import com.typesafe.sbt.SbtNativePackager.autoImport.packageSummary

lazy val akkaHttpVersion = "10.0.11"
lazy val akkaVersion    = "2.5.11"

name := "Coveo locations service"

version := "1.0"

maintainer := "Emmanuel Eytan <eje211@gmail.com>"

packageSummary := "Coveo backend challenge"

packageDescription := """An API that return places based on a name and a location and gives each pair a score.."""

rpmVendor := "regularoddity"

rpmLicense := Some("Apache 2")


lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging, LinuxPlugin, RpmPlugin, UpstartPlugin)
  .settings(
    inThisBuild(List(
      organization    := "com.regularoddity.coveo",
      scalaVersion    := "2.12.4"
    )),
    maintainer := "Emmanuel Eytan <eje211@gmail.com>",
    packageSummary := "Coveo backend challenge",
    packageDescription := """An API that return places based on a name and a location and gives each pair a score..""",
    rpmVendor := "regularoddity",
    rpmLicense := Some("Apache 2"),
    name := "coveo-backend-test",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"  %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"  %% "akka-stream"          % akkaVersion,
      "postgresql"         %  "postgresql"           % "9.1-901-1.jdbc4",
      "com.typesafe.slick" %% "slick"                % "3.2.3",
      "org.slf4j"          %  "slf4j-nop"            % "1.6.4",
      "org.apache.logging.log4j" % "log4j-api"       % "2.11.1",
      "org.apache.logging.log4j" % "log4j-core"      % "2.11.1",
      "org.apache.logging.log4j" %% "log4j-api-scala" % "11.0",
      "net.codingwell"     %% "scala-guice"          % "4.2.1",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    ),
    scalacOptions in (Compile, doc) := List("-skip-packages", "akka")
  )
