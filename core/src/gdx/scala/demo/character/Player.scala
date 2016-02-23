package gdx.scala.demo.character


import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.{Gdx, Input}
import com.uwsoft.editor.renderer.components.sprite.{SpriteAnimationComponent, SpriteAnimationStateComponent}
import com.uwsoft.editor.renderer.components.{DimensionsComponent, TransformComponent}
import com.uwsoft.editor.renderer.scripts.IScript
import com.uwsoft.editor.renderer.utils.ComponentRetriever
import gdx.scala.demo.Window
import gdx.scala.demo.components.Point

/**
  * Created by rtorres on 2/2/2016.
  */
class Player(world: World, var trigger: Option[Trigger] = None) extends IScript {
  val speed = Point(20, 0)
  var player: Entity = null
  var transformComponent: TransformComponent = null
  var dimensionsComponent: DimensionsComponent = null
  var spriteAnimationComponent: SpriteAnimationComponent = null
  var spriteAnimationStateComponent: SpriteAnimationStateComponent = null
  var bulletTransformComponent: TransformComponent = null

  override def init(entity: Entity): Unit = {
    player = entity
    transformComponent = ComponentRetriever.get(entity, classOf[TransformComponent])
    dimensionsComponent = ComponentRetriever.get(entity, classOf[DimensionsComponent])

  }


  override def dispose(): Unit = {
  }


  override def act(delta: Float): Unit = {
    nextPosition(delta) match {
      case Some(position) =>  {
        transformComponent.x = position
        val component = player >> classOf[DimensionsComponent]
        println(component.height)
      }
      case None =>
    }

    pollTriggerPull(delta)
  }


  def pollTriggerPull(delta: Float): Unit = {
    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && trigger.isDefined) {
      trigger.get.pullTrigger()
    }

  }

  def atBorder(nextPosit : Float) : Boolean = {
    nextPosit <= 0 || nextPosit >= Window.Width - dimensionsComponent.width
  }

  def nextPosition(delta : Float) : Option[Float] = {
    if(Gdx.input.isKeyPressed(Input.Keys.A)) {
      Some(transformComponent.x - (speed.x * delta))
    } else if(Gdx.input.isKeyPressed(Input.Keys.D)) {
      Some(transformComponent.x + speed.x * delta)
    } else {
      None
    }
  }

  def x = transformComponent.x

  def y = transformComponent.y

  def width = dimensionsComponent.width

  def height = dimensionsComponent.height

  def currentPosition = Point(x, y)
}
