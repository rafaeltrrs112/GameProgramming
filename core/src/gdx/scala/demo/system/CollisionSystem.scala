package gdx.scala.demo.system

import com.badlogic.ashley.core.systems.IteratingSystem
import com.badlogic.ashley.core.utils.ImmutableArray
import com.badlogic.ashley.core.{ComponentMapper, Entity, Family, Engine}

import com.badlogic.gdx.graphics.g2d.Animation
import com.uwsoft.editor.renderer.components.sprite.{SpriteAnimationComponent, SpriteAnimationStateComponent}
import com.uwsoft.editor.renderer.components.{DimensionsComponent, TransformComponent}
import com.uwsoft.editor.renderer.utils.ComponentRetriever
import gdx.scala.demo.character.Player
import gdx.scala.demo.components._
import scala.collection.JavaConversions._

object CollisionSystem {
  def apply(engine: Engine, player: Player): CollisionSystem = new CollisionSystem(engine, player)
}

class CollisionSystem(engine: Engine, player: Player) extends IteratingSystem(Family.all(classOf[Collidable]).get) {
  val collidableMapper = ComponentMapper.getFor(classOf[Collidable])

  val bulletMapper: ComponentMapper[PlayerBullet] = ComponentMapper.getFor(classOf[PlayerBullet])
  val transformMapper : ComponentMapper[TransformComponent] = ComponentMapper.getFor(classOf[TransformComponent])
  val peonMapper: ComponentMapper[PeonComponent] = ComponentMapper.getFor(classOf[PeonComponent])

  val spriteAnimStateMapper : ComponentMapper[SpriteAnimationStateComponent] = ComponentMapper.getFor(classOf[SpriteAnimationStateComponent])
  val spriteAnimMapper : ComponentMapper[SpriteAnimationComponent] = ComponentMapper.getFor(classOf[SpriteAnimationComponent])

  val dimensionMapper : ComponentMapper[DimensionsComponent] = ComponentMapper.getFor(classOf[DimensionsComponent])
  val collidableEntities = engine.getEntitiesFor(Family.all(classOf[Collidable]).get)


  var bulletEntities: ImmutableArray[Entity] = engine.getEntitiesFor(Family.all(classOf[PlayerBullet]).get)

  collidableEntities.foreach(setOriginalPosition)

  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    if(entity != null){
      val collidableEntity = collidableMapper.get(entity)
      updatePosition(collidableEntity, transformMapper.get(entity), dimensionMapper.get(entity))
      bulletCollisionCheck(entity, collidableEntity)
    }
  }

  def explosionState(entity : Entity) : Unit = {
    val animationComponent = spriteAnimMapper.get(entity)
    val animationStateComponent  = spriteAnimStateMapper.get(entity)
    animationStateComponent.set(animationComponent.frameRangeMap.get("dead"), 0, Animation.PlayMode.LOOP)
  }

  def isBullet(entity: Entity) : Boolean = entity.getComponent(classOf[PlayerBullet]) != null

  def bulletCollisionCheck(entity: Entity, collidable: Collidable): Boolean = {
    val collidedEntities: Option[Entity] = bulletEntities.find(collidesWith(collidable))
    if (collidedEntities.nonEmpty) {
      collidedEntities.foreach(reInitBullet)
      explosionState(entity)
      peonMapper.get(entity).isAlive = false
    } else {
    }
    false
  }

  def collidesWith(collidable: Collidable): Entity => Boolean = entity => {
    val bulletDimensions = ComponentRetriever.get(entity, classOf[DimensionsComponent])
    val bulletTransformComp = transformMapper.get(entity)
    val bulletPosition = Point(bulletTransformComp.x + bulletDimensions.width / 2, bulletTransformComp.y + bulletDimensions.height / 2)

    val bulletHeight = bulletDimensions.height
    val bulletWidth = bulletDimensions.width

    val result = collidable.position.get.dst(bulletPosition) <= ((bulletHeight / 2) + (collidable.height.get / 2))
    val result2 = collidable.position.get.dst(bulletPosition) <= ((bulletWidth / 2) + (collidable.width.get / 2))

    result || result2
  }

  def setOriginalPosition(collidableEntity: Entity): Unit = {
    val collidable = collidableMapper.get(collidableEntity)
    val transformComponent = ComponentRetriever.get(collidableEntity, classOf[TransformComponent])
    val dimensionComponent = ComponentRetriever.get(collidableEntity, classOf[DimensionsComponent])

    collidable.position = Some(Point(transformComponent.x, transformComponent.y))
    collidable.width = Some(dimensionComponent.width)
    collidable.height = Some(dimensionComponent.height)
  }

  def updatePosition(collidable : Collidable, transformComponent: TransformComponent, dimensionsComponent: DimensionsComponent) : Unit  = {
    collidable.position = Some(Point(transformComponent.x + dimensionsComponent.width / 2, transformComponent.y + dimensionsComponent.height / 2))
  }

  def reInitBullet(entity : Entity): Unit = {
    val bullet: PlayerBullet = bulletMapper.get(entity)
    val transformComponent: TransformComponent = transformMapper.get(entity)
    transformComponent.y = bullet.originalPosition.get.y
    transformComponent.x = bullet.originalPosition.get.x
    bullet.inFlight = false
    bullet.triggered = false
  }
}
