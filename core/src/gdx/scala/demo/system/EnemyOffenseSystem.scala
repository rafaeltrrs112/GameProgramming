package gdx.scala.demo.system

import java.util.{Timer, TimerTask}

import com.badlogic.ashley.core._
import com.badlogic.ashley.core.systems.IteratingSystem
import com.badlogic.ashley.core.utils.ImmutableArray
import com.badlogic.gdx.graphics.g2d.Animation
import com.uwsoft.editor.renderer.components.sprite.{SpriteAnimationComponent, SpriteAnimationStateComponent}
import com.uwsoft.editor.renderer.components.{DimensionsComponent, TransformComponent}
import gdx.scala.demo.character.Player
import gdx.scala.demo.components._

import scala.collection.JavaConversions._
import scala.util.Random

/**
  * System responsible for handling the enemy's attack pattern.
  */
case class DefaultRetriever(transformMapper: ComponentMapper[TransformComponent], dimensionsMapper: ComponentMapper[DimensionsComponent])

object Retriever {
  val MainRetriever: DefaultRetriever = DefaultRetriever(ComponentMapper.getFor(classOf[TransformComponent]), ComponentMapper.getFor(classOf[DimensionsComponent]))
  val EnemyBulletMapper: ComponentMapper[EnemyBullet] = ComponentMapper.getFor(classOf[EnemyBullet])
  val SpriteAnimationMapper: ComponentMapper[SpriteAnimationComponent] = ComponentMapper.getFor(classOf[SpriteAnimationComponent])
  val SpriteAnimationStateMapper: ComponentMapper[SpriteAnimationStateComponent] = ComponentMapper.getFor(classOf[SpriteAnimationStateComponent])
}

object EnemyOffenseSystem {
  def apply(engine: Engine, player: Player): EnemyOffenseSystem = new EnemyOffenseSystem(engine, player)
}

class EnemyOffenseSystem(engine: Engine, player: Player) extends IteratingSystem(Family.all(classOf[EnemyBullet]).get) {
  private val enemyEntities: ImmutableArray[Entity] = engine.getEntitiesFor(Family.all(classOf[PeonComponent]).get)
  private val enemyBulletEntities: ImmutableArray[Entity] = engine.getEntitiesFor(Family.all(classOf[EnemyBullet]).get)
  private val shields: ImmutableArray[Entity] = engine.getEntitiesFor(Family.all(classOf[Shield]).get)

  enemyBulletEntities.foreach(setOriginalPosition)

  private def startTimer(): TimerTask = {
    val timer = new Timer
    val allowTrigger = new java.util.TimerTask {
      override def run(): Unit = {
        println(enemyEntities.size())
        val randIndex = Random.nextInt(enemyEntities.size)

        /*
         * Get a random enemy to shoot the bullet.
         * Get the next available bullet that the enemy can shoot.
         */
        val randomEnemy = enemyEntities.get(randIndex)
        val chosenBullet = nextBullet

        /*
         * If a bullet is available then trigger it and place it in
         * front of the enemy that is shooting it.
         */
        chosenBullet match {
          case Some(_) => {
            val notInFlight = !Retriever.EnemyBulletMapper.get(chosenBullet.get).inFlight
            if (notInFlight) triggerBullet(randomEnemy, chosenBullet.get)
          }
          case None =>
        }
      }
    }
    timer.schedule(allowTrigger, 500l, 100l)
    allowTrigger
  }

  startTimer()


  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    updateBullet(entity, deltaTime)
    shields.foreach(destroyShields(_, entity))
  }

  def destroyShields(shieldEntity: Entity, bulletEntity: Entity): Unit = {
    val bulletTransform = bulletEntity >> classOf[TransformComponent]
    val bulletDimension = bulletEntity >> classOf[DimensionsComponent]

    val shieldTransform = shieldEntity >> classOf[TransformComponent]
    val shieldDimension = shieldEntity >> classOf[DimensionsComponent]

    val totalWidth = shieldDimension.width / 2 + bulletDimension.width / 2
    val totalHeight = shieldDimension.height / 2 + bulletDimension.height / 2

    val pointDistance = Point(bulletTransform.x, bulletTransform.y).dst(Point(shieldTransform.x, shieldTransform.y))
    val colliding = pointDistance <= totalWidth || pointDistance <= totalHeight

    if (colliding) {
      engine.removeEntity(shieldEntity)
      reInitBullet(bulletEntity >> classOf[Bullet], bulletTransform)
    }
  }

  def triggerBullet(enemyEntity: Entity, bulletEntity: Entity): Unit = {
    val enemyTransform = enemyEntity >> classOf[TransformComponent]
    val enemyDimension = enemyEntity >> classOf[DimensionsComponent]

    val bulletComponent = bulletEntity >> classOf[Bullet]
    val bulletTransform = bulletEntity >> classOf[TransformComponent]

    bulletComponent.triggered = true
    shootBullet(enemyTransform, enemyDimension, bulletComponent, bulletTransform)
  }

  def nextBullet: Option[Entity] = {
    val availableBullet: Option[Entity] = enemyBulletEntities.find(notInFlight)
    availableBullet
  }

  def notInFlight(entity: Entity): Boolean = !(entity >> classOf[EnemyBullet]).inFlight

  def updateBullet(entity: Entity, deltaTime: Float): Unit = {
    val transformComponent = entity >> classOf[TransformComponent]
    val bulletComponent = entity >> classOf[EnemyBullet]

    inView(transformComponent) match {
      case true =>
        new SpriteAnimationComponent()
        transformComponent.y -= (PlayerBullet.Speed * deltaTime)
        if (collidesWithPlayer(player, entity)) explosionState(player.player)
      case false => reInitBullet(bulletComponent, transformComponent)
    }
  }

  def explosionState(entity: Entity): Unit = {
    val animationComponent = entity >> classOf[SpriteAnimationComponent]
    val animationStateComponent = entity >> classOf[SpriteAnimationStateComponent]
    animationStateComponent.set(animationComponent.frameRangeMap.get("dead"), 0, Animation.PlayMode.LOOP)
  }

  def collidesWithPlayer(player: Player, entity: Entity): Boolean = {
    val bulletDimensions = entity >> classOf[DimensionsComponent]
    val bulletTransformComp = entity >> classOf[TransformComponent]
    val bulletPosition = Point(bulletTransformComp.x + bulletDimensions.width, bulletTransformComp.y + bulletDimensions.height)

    val bulletHeight = bulletDimensions.height
    val bulletWidth = bulletDimensions.width

    val result = player.currentPosition.dst(bulletPosition) <= (bulletHeight + player.height)
    val result2 = player.currentPosition.dst(bulletPosition) <= (bulletWidth + player.width)

    result || result2
  }

  def inFlightBullet(entity: Entity): Boolean = (entity >> classOf[EnemyBullet]).inFlight

  def inView(transformComponent: TransformComponent): Boolean = transformComponent.y >= 0


  def reInitBullet(bullet: Bullet, transformComponent: TransformComponent): Unit = {
    transformComponent.y = bullet.originalPosition.get.y
    transformComponent.x = bullet.originalPosition.get.x
    bullet.inFlight = false
    bullet.triggered = false
  }

  def setOriginalPosition(bulletEntity: Entity): Unit = {
    val bulletComponent: Bullet = bulletEntity >> classOf[EnemyBullet]
    val transformComponent: TransformComponent = bulletEntity >> classOf[TransformComponent]
    bulletComponent.originalPosition = Some(Point(transformComponent.x, transformComponent.y))
  }

  def shootBullet(enemyTransform: TransformComponent, enemyDimension: DimensionsComponent, bullet: Bullet, bulletTransform: TransformComponent) = {
    bulletTransform.x = enemyTransform.x - enemyDimension.width / 2
    bulletTransform.y = enemyTransform.y - enemyDimension.height / 2
    bullet.inFlight = true
  }

}

object EnemyBulletUpdater {

  def update(entity: Entity): Unit = {
    updateBullet(entity)
  }

  def updateBullet(entity: Entity): Unit = {
    val transformComponent = entity >> classOf[TransformComponent]
    val bulletComponent = entity >> classOf[EnemyBullet]


    inView(transformComponent) match {
      case true => transformComponent.y -= 1
      case false => reInitBullet(bulletComponent, transformComponent)
    }
  }

  def inFlightBullet(entity: Entity): Boolean = Retriever.EnemyBulletMapper.get(entity).inFlight

  def inView(transformComponent: TransformComponent): Boolean = transformComponent.y >= 0

  def reInitBullet(bullet: Bullet, transformComponent: TransformComponent): Unit = {
    transformComponent.y = bullet.originalPosition.get.y
    transformComponent.x = bullet.originalPosition.get.x
    bullet.inFlight = false
    bullet.triggered = false
  }

}
