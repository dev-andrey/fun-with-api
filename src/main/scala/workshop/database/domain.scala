package workshop.database

final case class Pet(id: Long, name: String, food: Food)

final case class Food(id: Long, name: String)
