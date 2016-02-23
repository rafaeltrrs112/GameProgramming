package gdx.scala.demo.components

import com.badlogic.ashley.core.Component
import gdx.scala.demo.character.Direction

/**
  * PeonComponent holding data about an enemy.
  */
class PeonComponent extends Component {
  var direction : Direction = Direction.LEFT
  var isAlive : Boolean = true
}

object Peon {
  val Tag : String = "peon"
}
