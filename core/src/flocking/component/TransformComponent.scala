package flocking.component

import com.badlogic.ashley.core.Component
import gdx.scala.demo.components.Point

/**
  * Created by rotor on 3/28/2016.
  */
case class TransformComponent(x: Int, y: Int) extends Component {
  val posit = Point(x, y)
  val scale = Point(1.0f, 1.0f)
  var rotation = 0.0f
}
