package workshop.server

import doobie.Transactor
import doobie.implicits._
import workshop.server.Foods.Foods
import zio._
import zio.interop.catz.taskConcurrentInstance

object Pets {
  type Pets = Has[Pets.Service]

  trait Service {
    def getPets: Task[List[Pet]]
  }

  def getPets: RIO[Pets, List[Pet]] =
    ZIO.accessM(_.get.getPets)

  def getPetsWithFood: RIO[Pets with Foods, List[PetWithFood]] =
    for {
      pets  <- getPets
      foods <- Foods.getFoods
      foodMap = foods.groupMap(_.id)(identity).view.mapValues(_.head)
    } yield pets.map(pet => PetWithFood(pet.id, pet.name, foodMap(pet.foodId)))

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
           | SELECT id, name, food_id
           | FROM pet
           |""".stripMargin.query[Pet].to[List]
  }
}
