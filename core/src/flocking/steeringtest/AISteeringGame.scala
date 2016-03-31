package flocking.steeringtest

import com.badlogic.gdx.{ApplicationAdapter, Gdx, Input}
import com.badlogic.gdx.ai.steer.behaviors.Arrive
import com.badlogic.gdx.graphics.g2d.{Batch, SpriteBatch}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import com.badlogic.gdx.math.{MathUtils, Vector2}
import com.badlogic.gdx.physics.box2d._

/**
  * Created by rotor on 3/29/2016.
  */
class AISteeringGame extends ApplicationAdapter {

  val PPM = 50
  var renderer: Box2DDebugRenderer = null
  var world: World = null

  var camera: OrthographicCamera = null

  var batch: Batch = null

  var entityBody: Body = null
  var entity: B2DSteeringEntity = null

  var targetBody: Body = null
  var target: B2DSteeringEntity = null

  var coneBody: Body = null

  val ANGLE_OFFSET = 90

  val CIRCE_BODY_RADIUS: Float = 30f

  val TIME_TO_TARGET: Float = 0.01f

  val ARRIVAL_TOLERANCE: Float = 1.3f

  val DECELERATION_RADIUS: Float = 10

  override def create(): Unit = {
    init()
    createBox(world, -200, 200, 60, 20, isStatic = true, canRotate = false)
    createBox(world, 140, 200, 60, 20, isStatic = true, canRotate = false)
    createBox(world, -200, -200, 60, 20, isStatic = true, canRotate = false)
    createBox(world, 140, -2000, 60, 20, isStatic = true, canRotate = false)

    world.setContactListener(new SteerContactListener)
  }

  def init(): Unit = {
    world = new World(new Vector2(0, 0), false)
    renderer = new Box2DDebugRenderer()
    batch = new SpriteBatch()
    camera = new OrthographicCamera(1280, 720)

    entityBody = SteeringUtils.createCircle(world, 0, 50, 10, isStatic = false, canRotate = true)
    entity = new B2DSteeringEntity(entityBody, CIRCE_BODY_RADIUS)

    targetBody = SteeringUtils.createCircle(world, 0, 0, 10, isStatic = false, canRotate = true)

    coneBody = SteeringUtils.makeCone(world)

    target = new B2DSteeringEntity(targetBody, CIRCE_BODY_RADIUS)

    val arriveSB: Arrive[Vector2] = new Arrive[Vector2](entity, target)
      .setTimeToTarget(TIME_TO_TARGET)
      .setArrivalTolerance(ARRIVAL_TOLERANCE)
      .setDecelerationRadius(DECELERATION_RADIUS)

    entity.behavior = arriveSB

    SteeringUtils.linkBodies(world, entityBody, coneBody)
  }

  override def render(): Unit = {

    Gdx.gl.glClearColor(.25f, .25f, .25f, 1f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    renderer.render(world, camera.combined.cpy().scl(PPM))
    update(Gdx.graphics.getDeltaTime)
  }

  def update(delta: Float): Unit = {
    world.step(1 / 60f, 6, 2)

    var x = 0
    var y = 0

    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      x += 1
    }

    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      x -= 1
    }

    if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
      y += 1
    }

    if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      y -= 1
    }


    if (x != 0) {
      val vel = target.body.getLinearVelocity
      target.body.setLinearVelocity(x * 10, vel.y)
    }


    if (y != 0) {
      val vel = target.body.getLinearVelocity
      target.body.setLinearVelocity(vel.x, y * 10)
    }


    entity.update(delta)

    SteeringUtils.lerpToTarget(camera, target.getPosition.scl(PPM))
    batch.setProjectionMatrix(camera.combined)

    coneBody.setUserData(FlagCharacterData(false))

    transformBodys()
  }


  def transformBodys(): Unit = {
    entityBody.setTransform(entityBody.getPosition, MathUtils.degreesToRadians * entity.getLinearVelocity.angle)
    coneBody.setTransform(entityBody.getPosition, MathUtils.degreesToRadians * (entity.getLinearVelocity.angle - ANGLE_OFFSET))
    targetBody.setTransform(targetBody.getPosition, MathUtils.degreesToRadians * targetBody.getLinearVelocity.angle)
  }

  def createBox(world: World, x: Float, y: Float,
                w: Float, h: Float,
                isStatic: Boolean, canRotate: Boolean) = SteeringUtils.createBox(world, x, y, w, h, isStatic, canRotate)

}



