package flocking.steeringtest

import com.badlogic.gdx.ai.utils.Location
import com.badlogic.gdx.math.Vector2

/**
  * Created by rotor on 3/31/2016.
  */
class Box2dLocation extends Location[Vector2] {

  var position = new Vector2()
  var orientation: Float = 0f

  override def getPosition: Vector2 = position

  override def newLocation(): Location[Vector2] = new Box2dLocation()

  override def angleToVector(outVector: Vector2, angle: Float): Vector2 = SteeringUtils.angleToVector(outVector, angle)

  override def setOrientation(orientation: Float): Unit = this.orientation = orientation

  override def vectorToAngle(vector: Vector2): Float = SteeringUtils.vectorToAngle(vector)

  override def getOrientation: Float = orientation
}

object Box2dLocation {
  def apply() : Box2dLocation = new Box2dLocation
}
