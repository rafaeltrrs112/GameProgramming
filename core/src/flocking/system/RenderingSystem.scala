package flocking.system

import com.badlogic.ashley.core.systems.IteratingSystem
import com.badlogic.ashley.core.{ComponentMapper, Entity, Family}
import com.badlogic.gdx.graphics.OrthographicCamera
import flocking.component.{Renderable, TransformComponent}

/**
  * Created by rotor on 3/28/2016.
  */
class RenderingSystem extends IteratingSystem(Family.all(classOf[Renderable], classOf[TransformComponent]).get) {

  val rendererM = ComponentMapper.getFor(classOf[Renderable])
  val transformM = ComponentMapper.getFor(classOf[TransformComponent])

  override protected def processEntity(entity: Entity, deltaTime: Float): Unit = ???

}

object RenderingSystem {
  val WIDTH = 500
  val HEIGHT = 500
}
