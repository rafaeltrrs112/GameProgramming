package gdx.scala.demo.components

import com.badlogic.ashley.core.Component
import gdx.scala.demo.character.Direction


/**
  * Bullet component object.
  */

sealed trait Bullet extends Component {
  var inFlight: Boolean = false
  var triggered: Boolean = false
  var originalPosition: Option[Point] = None
  val direction: Direction = Direction.UP
}

class PlayerBullet extends Bullet

class EnemyBullet extends Bullet {
  override val direction: Direction = Direction.DOWN
}

object EnemyBullet {
  val Tag: String = "enemyBullet"
}

object PlayerBullet {
  val Speed: Int = 10
  val Tag: String = "bullet"
}

case class Point(x: Float, y: Float) {
  def dst(otherPoint: Point): Float = {
    val xDistance: Float = otherPoint.x - x
    val yDistance: Float = otherPoint.y - y
    Math.sqrt(xDistance * xDistance + yDistance * yDistance).toFloat
  }

  /**
    * Measures distance.
    *
    * @param x The x-component of the other vector
    * @param y The y-component of the other vector
    * @return the distance between this and the other vector */
  def dst(x: Float, y: Float): Float = {
    val x_d: Float = x - this.x
    val y_d: Float = y - this.y
    Math.sqrt(x_d * x_d + y_d * y_d).toFloat
  }
}


