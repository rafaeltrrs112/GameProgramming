package gdx.scala.demo.character

import java.util.{Timer, TimerTask}

import com.badlogic.ashley.core.utils.ImmutableArray
import com.badlogic.ashley.core.{ComponentMapper, Entity}
import com.badlogic.gdx.utils.Array
import gdx.scala.demo.components.PlayerBullet

import scala.collection.JavaConversions._
/**
  * Created by rtorres on 2/2/2016.
  */
sealed trait Trigger {
  def pullTrigger() : Unit
}

case class ShipTrigger(entities : ImmutableArray[Entity], componentMapper: ComponentMapper[PlayerBullet]) extends Trigger {
  val bullets = new ImmutableArray[PlayerBullet](new Array(entities.map(componentMapper.get).toArray))
  var canPull = true

  private val FireRate = 200L
  startTimer()

  private def startTimer() : TimerTask = {
    val timer = new Timer
    val allowTrigger = new java.util.TimerTask {
      override def run() : Unit = {
        canPull = true
      }
    }
    timer.schedule(allowTrigger, 0, FireRate)
    allowTrigger
  }

  override def pullTrigger(): Unit = {
    val nextBullet = bullets.find(_.inFlight == false)
    if(nextBullet.isDefined && canPull) {
      nextBullet.get.triggered = true
    }
    canPull = false
  }

}
