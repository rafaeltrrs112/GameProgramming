package gdx.scala.demo.character

/**
  * Created by rtorres on 2/9/2016.
  */

trait Direction

case class Left() extends Direction

case class Right() extends Direction

case class Up() extends Direction

case class Down() extends Direction

object Direction {
  val LEFT = Left()
  val RIGHT = Right()
  val UP = Up()
  val DOWN = Down()

  def switch(direction: Direction): Direction = {
    direction match {
      case Left() => RIGHT
      case Right() => LEFT
    }
  }

}