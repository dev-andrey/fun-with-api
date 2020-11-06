package workshop.server

final case class Pet(id: Long, name: String, foodId: Long)
final case class Food(id: Long, name: String)

final case class PetWithFood(id: Long, name: String, food: Food)
