package workshop.website

import doobie.Transactor
import doobie.implicits._
import zio._
import zio.interop.catz.taskConcurrentInstance

object Foods {
  type Foods = Has[Foods.Service]

  trait Service {
    def getFoods: Task[List[Food]]
  }

  def getFoods: RIO[Foods, List[Food]] =
    ZIO.accessM(_.get.getFoods)

  val live: URLayer[Has[Transactor[Task]], Foods] =
    ZLayer.fromService[Transactor[Task], Service] { xa =>
      new Service {
        override def getFoods: Task[List[Food]] =
          SQL
            .all
            .transact(xa)
      }
    }

  private object SQL {
    def all: doobie.ConnectionIO[List[Food]] =
      sql"""
           | SELECT id, name
           | FROM food
           |""".stripMargin.query[Food].to[List]
  }
}
