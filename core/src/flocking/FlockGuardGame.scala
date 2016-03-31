package flocking

import com.badlogic.ashley.core.{Entity, PooledEngine}
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.{ApplicationAdapter, Gdx}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera}
import flocking.character.Ship

/**
  * Created by rotor on 3/28/2016.
  */
class FlockGuardGame extends ApplicationAdapter {

  var camera: OrthographicCamera = null
  var shapeRenderer: ShapeRenderer = null
  var player: Ship = null

  override def create(): Unit = {
    camera = new OrthographicCamera(FlockGuardGame.WIDTH, FlockGuardGame.HEIGHT)
    camera.translate(FlockGuardGame.WIDTH/2, FlockGuardGame.HEIGHT/2)
    camera.update()

    shapeRenderer = new ShapeRenderer()

    player = new Ship()
  }

  override def render(): Unit = {
    Gdx.gl.glClearColor(0, 0, 0, 1)

    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    player.render(shapeRenderer)

  }

}

object FlockGuardGame {
  val WIDTH = 500
  val HEIGHT = 500
}
