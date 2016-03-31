package flocking.steeringtest

import com.badlogic.gdx.{ApplicationAdapter, Gdx, Input}
import com.badlogic.gdx.ai.steer.behaviors.Arrive
import com.badlogic.gdx.graphics.g2d.{Batch, SpriteBatch}
import com.badlogic.gdx.graphics.{Camera, GL20, OrthographicCamera}
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d
import com.badlogic.gdx.physics.box2d._
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef

/**
  * Created by rotor on 3/31/2016.
  */
object SteeringUtils {
  val PPM = 50
  val FIELD_OF_VIEW = 120

  def vectorToAngle(vector: Vector2) = Math.atan2(vector.x * -1d, vector.y).toFloat

  def angleToVector(outVector: Vector2, angle: Float) = {
    outVector.x = Math.sin(angle).toFloat * -1f
    outVector.y = Math.cos(angle).toFloat

    outVector
  }

  def createCircle(world: World, x: Float, y: Float, r: Float, isStatic: Boolean, canRotate: Boolean): Body = {
    val bodyDef = new BodyDef()
    bodyDef.fixedRotation = canRotate
    bodyDef.linearDamping = 10f
    bodyDef.position.set(x / PPM, y / PPM)

    isStatic match {
      case true => bodyDef.`type` = BodyDef.BodyType.StaticBody
      case false => bodyDef.`type` = BodyDef.BodyType.DynamicBody
    }

    val shape = new CircleShape()
    shape.setRadius(r / PPM)
    val fixtureDef = new FixtureDef()
    fixtureDef.shape = shape
    fixtureDef.friction = .95f
    fixtureDef.density = 1.0f

    world.createBody(bodyDef).createFixture(fixtureDef).getBody
  }

  def createBox(world: World, x: Float, y: Float, w: Float, h: Float, isStatic: Boolean, canRotate: Boolean): Body = {
    val bodyDef = new BodyDef()
    bodyDef.fixedRotation = canRotate
    bodyDef.linearDamping = 10f

    bodyDef.position.set(x / PPM, y / PPM)

    isStatic match {
      case true => bodyDef.`type` = BodyDef.BodyType.StaticBody
      case false => bodyDef.`type` = BodyDef.BodyType.DynamicBody
    }

    val shape = new PolygonShape()

    shape.setAsBox(w / PPM, h / PPM)

    val fixtureDef = new FixtureDef()
    fixtureDef.shape = shape
    fixtureDef.density = 1.0f

    world.createBody(bodyDef).createFixture(fixtureDef).getBody
  }

  def lerpToTarget(camera: Camera, target: Vector2): Unit = {
    val position = camera.position
    position.x = camera.position.x + (target.x - camera.position.x) * .1f
    position.y = camera.position.y + (target.x - camera.position.y) * .1f
    camera.position.set(position)
    camera.update()
  }

  def makeCone(world: World): Body = {
    val bodyDef = new BodyDef()
    bodyDef.fixedRotation = true
    bodyDef.linearDamping = 10f
    bodyDef.`type` = BodyDef.BodyType.DynamicBody


    val triangle = new PolygonShape()

    val vertices = new Array[Vector2](3)

    vertices(0) = new Vector2(0f, 10 / PPM)
    val x = (Math.tan((FIELD_OF_VIEW / 2) * MathUtils.degreesToRadians) * 2).toFloat
    vertices(1) = new Vector2(x, 10 / PPM + 2)
    vertices(2) = new Vector2(-x, 10 / PPM + 2)

    triangle.set(vertices)

    val fixtureDef = new FixtureDef()


    fixtureDef.shape = triangle
    fixtureDef.isSensor = true

    val body = world.createBody(bodyDef).createFixture(fixtureDef).getBody
    body
  }

  def linkBodies(world: World, bodyA: Body, bodyB: Body): Unit = {
    val weldJointDef = new WeldJointDef()
    weldJointDef.bodyA = bodyA
    weldJointDef.bodyB = bodyB

    weldJointDef.collideConnected = false
    weldJointDef.localAnchorA.set(0, 0)

    world.createJoint(weldJointDef)
  }
}
