package workshop
import cats.effect.Blocker
import doobie.hikari.HikariTransactor
import workshop.infra.{ Config, DbConfig, DbEvolution }
import zio._
import zio.blocking.Blocking
import zio.interop.catz.{ taskConcurrentInstance, zioContextShift, zioResourceSyntax }

object Main extends App {
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    for {
      config <- readConfig
      _      <- DbEvolution.evolve(config.db).toManaged_
      xa     <- makeTransactor(config.db)
      server <- database.Endpoints.makeServer(xa)
    } yield server
  }.useForever.exitCode

  private def readConfig = {
    import pureconfig.ConfigSource
    import pureconfig.generic.auto._
    ZIO
      .fromEither(ConfigSource.default.load[Config])
      .toManaged_
  }

  private def makeTransactor(dbConfig: DbConfig): ZManaged[Blocking, Throwable, HikariTransactor[Task]] =
    for {
      connectEC <- ZIO.runtime[Any].map(_.platform.executor.asEC).toManaged_
      blocker   <- ZManaged.access[Blocking](_.get.blockingExecutor.asEC)
      xa <- HikariTransactor
        .newHikariTransactor[Task](
          dbConfig.driver,
          dbConfig.url,
          dbConfig.user,
          dbConfig.pass,
          connectEC,                            // await connection
          Blocker.liftExecutionContext(blocker),// execute JDBC calls
        )
        .toManagedZIO
    } yield xa

}
