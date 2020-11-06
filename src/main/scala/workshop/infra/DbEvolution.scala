package workshop.infra

import org.flywaydb.core.Flyway
import zio.Task

object DbEvolution {
  def evolve(config: DbConfig): Task[Unit] =
    Task {
      val flyway = Flyway
        .configure(this.getClass.getClassLoader)
        .dataSource(config.url, config.user, config.pass)
        .load()
      flyway.repair()
      flyway.migrate()
    }.unit
}
