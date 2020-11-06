package workshop.server

import doobie.Transactor
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import workshop.server.Foods.Foods
import workshop.server.Pets.Pets
import zio._
import zio.clock.Clock
import zio.interop.catz.{ taskConcurrentInstance, taskEffectInstance, zioContextShift, zioResourceSyntax, zioTimer }

object Endpoints {
  type ServerEnv = Foods with Pets

  private val getPets  = endpoint.get.in("pets").out(jsonBody[List[PetWithFood]])
  private val getFoods = endpoint.get.in("foods").out(jsonBody[List[Food]])

  private val endpoints =
    List(
      getPets.zServerLogic(_ => Pets.getPetsWithFood.orDie).widen[ServerEnv],
      getFoods.zServerLogic(_ => Foods.getFoods.orDie).widen[ServerEnv],
    )

  private val routes: HttpRoutes[RIO[ServerEnv with Clock, *]] =
    endpoints.toRoutes

  def makeServer(transactor: Transactor[Task]) =
    ZIO
      .runtime[ServerEnv with ZEnv]
      .toManaged_
      .flatMap { implicit rts =>
        BlazeServerBuilder[RIO[ServerEnv with Clock, *]](rts.platform.executor.asEC)
          .bindHttp(8080, "localhost")
          .withHttpApp(
            Router(
              "/" -> routes
            ).orNotFound
          )
          .resource
          .toManagedZIO
      }
      .provideCustomLayer(ZLayer.succeed(transactor) >>> (Pets.live ++ Foods.live))
}
