package flocking.character

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.LongArray
import com.uwsoft.editor.renderer.scripts.IScript
import flocking.steeringtest.FlockGuardGame

/**
  * Created by rotor on 3/28/2016.
  */
class Ship extends IScript {
  var maxSpeed = 300f
  var acceleration = 200f
  var deceleration = 10
  var acceleratingTime = 0f

  var shapex = new LongArray(Array[Long](0, 0, 0, 0))
  var shapey = new LongArray(Array[Long](0, 0, 0, 0))

  var radians: Float = 3.1415f / 2f
  var rotationSpeed = 3

  var x: Float = FlockGuardGame.WIDTH / 2
  var y: Float = FlockGuardGame.WIDTH / 2

  var dx = 0f
  var dy = 0f

  var left = true

  setShape()


  def setShape(): Unit = {
    shapex set(0, (x + MathUtils.cos(radians) * 8).toLong)
    shapey set(0, (y + MathUtils.sin(radians) * 8).toLong)

    //left point (left wing)
    shapex set(1, (x + MathUtils.cos(radians - 4 * 3.1415f / 5) * 8).toLong)
    shapey set(1, (y + MathUtils.sin(radians - 4 * 3.1415f / 5) * 8).toLong)

    //center bottom
    shapex set(2, (x + MathUtils.cos(radians + 3.1415f) * 5).toLong)
    shapey set(2, (y + MathUtils.sin(radians + 3.1415f) * 5).toLong)


    //right point (right wing)
    shapex set(3, (x + MathUtils.cos(radians + 4 * 3.1415f / 5) * 8).toLong)
    shapey set(3, (y + MathUtils.sin(radians + 4 * 3.1415f / 5) * 8).toLong)
  }

  def render(shapeRenderer: ShapeRenderer): Unit = {
    val dt = Gdx.graphics.getDeltaTime

    if (Gdx.input.isKeyPressed(Keys.LEFT)) {
      radians -= rotationSpeed * dt
    }

    if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
      radians += rotationSpeed * dt
    }

    if (Gdx.input.isKeyPressed(Keys.UP)) {
      dx = dx + MathUtils.cos(radians) * acceleration * dt
      dy = dy + MathUtils.sin(radians) * acceleration * dt

      acceleratingTime += dt
      if (acceleratingTime > 0.1f) {
        acceleratingTime = 0
      }
    }
    else {
      acceleratingTime = 0
    }

    val vec: Float = Math.sqrt(dx * dx + dy * dy).toFloat

    if (vec > 0) {
      dx -= (dx / vec) * deceleration * dt
      dy -= (dy / vec) * deceleration * dt
    }
    if (vec > maxSpeed) {
      dx = (dx / vec) * maxSpeed
      dy = (dy / vec) * maxSpeed
    }

    x += dx * dt
    y += dy * dt

    setShape()

    shapeRenderer.setColor(1, 1, 1, 1)

    // draw in between these two
    shapeRenderer.begin(ShapeType.Line)

    var i: Int = 0
    var j: Int = shapex.size - 1

    while (i < shapex.size) {
      {
        shapeRenderer.line(shapex.get(i), shapey.get(i), shapex.get(j), shapey.get(j))
      }
      j = {
        i += 1
        i - 1
      }
    }

    shapeRenderer.end()

  }

  override def init(entity: Entity): Unit = {

  }

  override def dispose(): Unit = {

  }

  override def act(delta: Float): Unit = {

  }
}
