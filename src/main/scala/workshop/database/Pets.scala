package workshop.database

import doobie.Transactor
import doobie.implicits._
import zio._
import zio.interop.catz.taskConcurrentInstance

object Pets {
  type Pets = Has[Pets.Service]

  trait Service {
    def getPets: Task[List[Pet]]
  }

  def getPets: RIO[Pets, List[Pet]] =
    ZIO.accessM(_.get.getPets)

  val live: URLayer[Has[Transactor[Task]], Pets] =
    ZLayer.fromService[Transactor[Task], Service] { xa =>
      new Service {
        override def getPets: Task[List[Pet]] =
          SQL
            .all
            .transact(xa)
      }
    }

  private object SQL {
    def all: doobie.ConnectionIO[List[Pet]] =
      sql"""
           | SELECT p.id, p.name, f.id, f.name
           | FROM pet p JOIN food f on p.food_id = f.id
           |""".stripMargin.query[Pet].to[List]
  }
}
