package flocking.steeringtest

import com.badlogic.gdx.physics.box2d.{Contact, ContactImpulse, ContactListener, Manifold}

/**
  * Created by rotor on 3/31/2016.
  */
class SteerContactListener extends ContactListener {
  override def postSolve(contact: Contact, impulse: ContactImpulse): Unit = {

  }

  override def endContact(contact: Contact): Unit = {

  }

  override def beginContact(contact: Contact): Unit = {
    val dataA = Option(contact.getFixtureA.getBody.getUserData)
    val dataB = Option(contact.getFixtureB.getBody.getUserData)

    dataA match {
      case Some(aData) => aData match {
        case FlagCharacterData(true) => contact.setEnabled(false)
        case _ => ()
      }
      case _ => ()
    }

    dataB match {
      case Some(aData) => aData match {
        case FlagCharacterData(true) => contact.setEnabled(false)
        case _ => ()
      }
      case _ => ()
    }

    println("Contact detected")
  }

  override def preSolve(contact: Contact, oldManifold: Manifold): Unit = {

  }
}
