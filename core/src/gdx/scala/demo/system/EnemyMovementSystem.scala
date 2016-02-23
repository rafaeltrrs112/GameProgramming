package gdx.scala.demo.system

import com.badlogic.ashley.core.systems.IntervalSystem
import com.badlogic.ashley.core.utils.ImmutableArray
import com.badlogic.ashley.core.{ComponentMapper, Engine, Entity, Family}
import com.uwsoft.editor.renderer.components.{DimensionsComponent, TransformComponent}
import gdx.scala.demo.Window
import gdx.scala.demo.character.{Direction, Left, Right}
import gdx.scala.demo.components._

import scala.collection.JavaConversions._
/**
  * Created by rtorres on 2/7/2016.
  */
object EnemyMovement {
  val Velocity = Point(2, 2)
}

object EnemyMovementSystem {
  def apply(interval: Float, engine: Engine): EnemyMovementSystem = new EnemyMovementSystem(interval, engine)
}

class EnemyMovementSystem(interval: Float, engine: Engine) extends IntervalSystem(interval) {
  private val speed = EnemyMovement.Velocity

  private var entities: Option[ImmutableArray[Entity]] = None


  private val transformMapper: ComponentMapper[TransformComponent] = ComponentMapper.getFor(classOf[TransformComponent])
  private val dimensionMapper: ComponentMapper[DimensionsComponent] = ComponentMapper.getFor(classOf[DimensionsComponent])

  private val peonMapper: ComponentMapper[PeonComponent] = ComponentMapper.getFor(classOf[PeonComponent])

  override def addedToEngine(engine: Engine): Unit = {
    entities = Some(engine.getEntitiesFor(Family.all(classOf[Collidable]).get()))
  }

  override def updateInterval(): Unit = {
    entities.get.foreach(clearIfDead)
    val needsRedirect = entities.get.exists(checkMovement)
    if(needsRedirect) entities.get.foreach(switchEntity)
    entities.get.foreach(act)

  }

  def clearIfDead(entity: Entity): Unit = {
    val peonComponent = peonMapper.get(entity)
    if (!peonComponent.isAlive) engine.removeEntity(entity)
  }

  def act(entity: Entity): Unit = {
    val transformComponent = transformMapper.get(entity)
    val dimensionsComponent = dimensionMapper.get(entity)
    val peonComponent = peonMapper.get(entity)

    nextPosition(entity, transformComponent, peonComponent, dimensionsComponent) match {
      case Some(position) => transformComponent.x = position
      case None => None
    }
  }

  def checkMovement(entity: Entity): Boolean = {
    val transformComponent = transformMapper.get(entity)
    val dimensionsComponent = dimensionMapper.get(entity)
    val peonComponent = peonMapper.get(entity)
    !courseGood(entity, transformComponent, peonComponent, dimensionsComponent)
  }

  def atBorder(nextPosit: Float, dimensionsComponent: DimensionsComponent): Boolean = {
    nextPosit <= 0 || nextPosit >= Window.Width - dimensionsComponent.width
  }

  def nextPosition(entity: Entity, transformComponent: TransformComponent, peonComponent: PeonComponent, dimensionsComponent: DimensionsComponent): Option[Float] = {
    val direction = peonComponent.direction
    val testDirection = direction match {
      case Left() => Some(transformComponent.x - (speed.x * interval))
      case Right() => Some(transformComponent.x + speed.x * interval)
    }
    Some(testDirection.get)
  }

  def directionSwitch(component: PeonComponent): Unit = {
    component.direction = Direction.switch(component.direction)
  }

  def switchEntity(entity: Entity): Unit = {
    Retriever.MainRetriever.transformMapper.get(entity).y -=   .5f
    directionSwitch(peonMapper.get(entity))
  }

  def courseGood(entity: Entity, transformComponent: TransformComponent, peonComponent: PeonComponent, dimensionsComponent: DimensionsComponent): Boolean = {
    val direction = peonComponent.direction
    val testDirection = direction match {
      case Left() => Some(transformComponent.x - (speed.x * interval))
      case Right() => Some(transformComponent.x + speed.x * interval)
    }

    !atBorder(testDirection.get, dimensionsComponent)
  }

  def reInitBullet(bullet: PlayerBullet, transformComponent: TransformComponent): Unit = {
    transformComponent.y = bullet.originalPosition.get.y
    transformComponent.x = bullet.originalPosition.get.x
    bullet.inFlight = false
    bullet.triggered = false
  }


}
