package com.mygdx.game.desktop

import com.badlogic.gdx.backends.lwjgl.{LwjglApplication, LwjglApplicationConfiguration}
import gdx.scala.demo.SpaceInvaders


object DesktopLauncher {
  def main(arg: Array[String]) {
    val config: LwjglApplicationConfiguration = new LwjglApplicationConfiguration
    config.width = 1700
    config.height = 1600
    new LwjglApplication(new SpaceInvaders, config)

  }
}