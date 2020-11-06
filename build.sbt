Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / organization := "dev.alebe"
ThisBuild / version := "0.1.0-SNAPSHOT"

name := "fun-with-api"

lazy val v = new {
  val zio            = "1.0.3"
  val zioInteropCats = "2.2.0.1"
  val tapir          = "0.17.0-M8"
  val circe          = "0.13.0"
  val doobie         = "0.9.2"
  val pureConfig     = "0.14.0"
}

lazy val root = project
  .in(file("."))
  .settings(
    name := "fun-with-api",
    libraryDependencies ++= Seq(
      // Effects
      "dev.zio" %% "zio"              % v.zio,
      "dev.zio" %% "zio-streams"      % v.zio,
      "dev.zio" %% "zio-interop-cats" % v.zioInteropCats,
      "dev.zio" %% "zio-test"         % v.zio % Test,
      "dev.zio" %% "zio-test-sbt"     % v.zio % Test,
      // Database
      "org.tpolecat" %% "doobie-core"   % v.doobie,
      "org.tpolecat" %% "doobie-h2"     % v.doobie,
      "org.tpolecat" %% "doobie-hikari" % v.doobie,
      // RestApi
      "com.softwaremill.sttp.tapir" %% "tapir-core"               % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"                % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-redoc-http4s"       % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http4s-server"  % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % v.tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % v.tapir,
      // Json
      "io.circe" %% "circe-generic-extras" % v.circe,
      // Config
      "com.github.pureconfig" %% "pureconfig" % v.pureConfig,
      // Java libs
      "org.flywaydb"   % "flyway-core"     % "7.1.1",
      "com.h2database" % "h2"              % "1.4.200",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.11.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
  )
