package workshop.website

import doobie.Transactor
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.ztapir._
import workshop.website.Foods.Foods
import workshop.website.Pets.Pets
import zio.clock.Clock
import zio.interop.catz.{ taskConcurrentInstance, taskEffectInstance, zioContextShift, zioResourceSyntax, zioTimer }
import zio._

object Endpoints {
  type WebsiteEnv = Foods with Pets

  private val getPets  = endpoint.get.in("pets").out(jsonBody[List[Pet]])
  private val getFoods = endpoint.get.in("foods").out(jsonBody[List[Food]])

  private val endpoints =
    List(
      getPets.zServerLogic(_ => Pets.getPets.orDie).widen[WebsiteEnv],
      getFoods.zServerLogic(_ => Foods.getFoods.orDie).widen[WebsiteEnv],
    )

  private val routes: HttpRoutes[RIO[WebsiteEnv with Clock, *]] =
    endpoints.toRoutes

  def makeServer(transactor: Transactor[Task]) =
    ZIO
      .runtime[WebsiteEnv with ZEnv]
      .toManaged_
      .flatMap { implicit rts =>
        BlazeServerBuilder[RIO[WebsiteEnv with Clock, *]](rts.platform.executor.asEC)
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
