package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import flocking.steeringtest.{AISteeringGame, FlockGuardGame}
import gdx.scala.demo.SpaceInvaders


object DesktopLauncher {
  def main(arg: Array[String]) {
    val config: LwjglApplicationConfiguration = new LwjglApplicationConfiguration
    config.width = 1280
    config.height = 820
    new LwjglApplication(new AISteeringGame, config)

  }
}