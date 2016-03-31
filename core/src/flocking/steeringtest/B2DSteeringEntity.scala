package flocking.steeringtest

import com.badlogic.gdx.ai.steer.limiters.FullLimiter
import com.badlogic.gdx.ai.steer.{Steerable, SteeringAcceleration, SteeringBehavior}
import com.badlogic.gdx.ai.utils.Location
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d._

/**
  * Created by rotor on 3/29/2016.
  */
class B2DSteeringEntity(val body: Body, val boundingRadius: Float) extends Steerable[Vector2] {
  var tagged: Boolean = false

  val location = Box2dLocation()

  val fullLimiter = new FullLimiter(8000f, 500f, 5, 30)

  private var _behavior: SteeringBehavior[Vector2] = null

  val steerOutput: SteeringAcceleration[Vector2] = new SteeringAcceleration[Vector2](new Vector2())


  def update(delta: Float): Unit = {
    val behaviorExists = Option(_behavior)
    behaviorExists match {
      case None => throw new RuntimeException("Behavior not set")
      case Some(currentBehavior) =>
        currentBehavior.calculateSteering(steerOutput)
        applySteering(delta)
    }
  }

  def applySteering(delta: Float): Unit = {
    var anyAccels = false

    steerOutput.linear.isZero match {
      case false => anyAccels = handleNonZero(delta)
      case _ =>
    }

    anyAccels match {
      case true => handleAcceleration(delta)
      case _ =>
    }

  }

  def handleNonZero(delta: Float): Boolean = {
    val force = steerOutput.linear.scl(delta)
    body.applyForceToCenter(force, true)
    true
  }

  def handleAcceleration(delta: Float) = {
    val velocity = body.getLinearVelocity
    val currentSpeedSquare = velocity.len2
    if (currentSpeedSquare > (getMaxLinearSpeed * getMaxLinearSpeed)) {
      body.setLinearVelocity(velocity.scl(getMaxLinearSpeed / Math.sqrt(currentSpeedSquare).toFloat))
    }
  }

  override def getLinearVelocity: Vector2 = body.getLinearVelocity

  override def isTagged: Boolean = tagged

  override def getBoundingRadius: Float = boundingRadius

  override def setTagged(tagged: Boolean): Unit = this.tagged = tagged

  override def getAngularVelocity: Float = body.getAngularVelocity

  override def getOrientation: Float = body.getAngle

  override def getPosition: Vector2 = body.getPosition

  override def angleToVector(outVector: Vector2, angle: Float): Vector2 = SteeringUtils.angleToVector(outVector, angle)

  override def vectorToAngle(vector: Vector2): Float = SteeringUtils.vectorToAngle(vector)

  override def setMaxAngularAcceleration(maxAngularAcceleration: Float): Unit = fullLimiter.setMaxAngularAcceleration(maxAngularAcceleration)

  override def getMaxAngularAcceleration: Float = fullLimiter.getMaxAngularAcceleration

  override def setMaxLinearAcceleration(maxLinearAcceleration: Float): Unit = fullLimiter.setMaxLinearAcceleration(maxLinearAcceleration)

  override def getMaxLinearSpeed: Float = fullLimiter.getMaxLinearSpeed

  override def setMaxLinearSpeed(maxLinearSpeed: Float): Unit = fullLimiter.setMaxLinearSpeed(maxLinearSpeed)

  override def getMaxLinearAcceleration: Float = fullLimiter.getMaxLinearAcceleration

  override def setMaxAngularSpeed(maxAngularSpeed: Float): Unit = fullLimiter.setMaxAngularSpeed(maxAngularSpeed)

  override def getMaxAngularSpeed: Float = fullLimiter.getMaxAngularSpeed

  override def newLocation(): Location[Vector2] = Box2dLocation()

  override def setOrientation(orientation: Float): Unit = location.setOrientation(orientation)

  override def getZeroLinearSpeedThreshold: Float = fullLimiter.getZeroLinearSpeedThreshold

  override def setZeroLinearSpeedThreshold(value: Float): Unit = fullLimiter.setZeroLinearSpeedThreshold(value)

  def behavior = _behavior

  def behavior_=(steeringBehavior: SteeringBehavior[Vector2]) = _behavior = steeringBehavior

}




