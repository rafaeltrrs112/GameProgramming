package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import flocking.FlockGuardGame
import flocking.steeringtest.AiSteeringState
import gdx.scala.demo.SpaceInvaders


object DesktopLauncher {
  def main(arg: Array[String]) {
    val config: LwjglApplicationConfiguration = new LwjglApplicationConfiguration
    config.width = 1000
    config.height = 1000
    new LwjglApplication(new AiSteeringState, config)

  }
}