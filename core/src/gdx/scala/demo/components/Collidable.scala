package gdx.scala.demo.components

import com.badlogic.ashley.core.Component

/**
  * Created by rtorres on 2/2/2016.
  */
class Collidable extends Component {
  var position : Option[Point] = None
  var width : Option[Float] = None
  var height : Option[Float] = None

}

object Collidable {
  val Tag = "collidable"
}
