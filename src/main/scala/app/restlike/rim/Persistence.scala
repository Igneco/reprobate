package app.restlike.rim

import java.nio.file.Paths

import im.mange.little.file.Filepath
//import net.liftweb.json._
import org.json4s.native.JsonMethods._

import scala.collection.immutable

case class NewRim(toke: String, access: Access, model: Model)

//TODO: use app name
object Persistence {
  private val file = Paths.get(s"${Rim.appName}.json")
  private val defaultStatuses = List("next", "doing", "done")
  private val config = Config("rim", "backlog", defaultStatuses, "released", List[String]())

  //TODO: could Model be 'T'ed up?
  def load: Universe = {
    if (!file.toFile.exists()) { save(createEmpty) }
    Json.deserialise(Filepath.load(file))
  }

  def add(email: String) = {
    val model = Model(config, immutable.Map[String, String](), List[Issue](), List[Release]())
    val token = java.util.UUID.randomUUID.toString
    val newRim = NewRim(token, Access(Seq(email)), model)

    val universe = load

    save(
      universe.copy(
        tokenToModel = universe.tokenToModel.updated(token, newRim.model),
        tokenToAccess = universe.tokenToAccess.updated(token, newRim.access)
      )
    )
  }

  def save(state: Universe) {
    Filepath.save(pretty(render(Json.serialise(state))), file)
  }

  private def createEmpty() = Universe(Map.empty, Map.empty)
}
