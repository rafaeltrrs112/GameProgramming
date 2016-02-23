/** *****************************************************************************
  * Copyright 2014 See AUTHORS file.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * *****************************************************************************/
package com.badlogic.ashley.core

import com.badlogic.ashley.core.signals.Signal
import com.badlogic.ashley.core.utils.Bag
import com.badlogic.ashley.core.utils.ImmutableArray
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Bits

/**
  * Simple containers of {@link Component}s that give them "data". The component's data is then processed by {@link EntitySystem}s.
  *
  * @author Stefan Bachmann
  */
class Entity {
  /** A flag that can be used to bit mask this entity. Up to the user to manage. */
  var flags: Int = 0
  /** Will dispatch an event when a component is added. */
  final var componentAdded: Signal[Entity] = null
  /** Will dispatch an event when a component is removed. */
  final var componentRemoved: Signal[Entity] = null
  var scheduledForRemoval: Boolean = false
  private[core] var removing: Boolean = false
  private[core] var componentOperationHandler: ComponentOperationHandler = null
  private var components: Bag[Component] = null
  private var componentsArray: Array[Component] = null
  private var immutableComponentsArray: ImmutableArray[Component] = null
  private var componentBits: Bits = null
  private var familyBits: Bits = null

  def flagsSet(num : Integer) : Unit = this.flags = num
  def removing(r : Boolean): Unit = this.removing = r
  def scheduleForRemoval(r : Boolean): Unit = this.scheduledForRemoval = scheduledForRemoval
  def orientationHandlerSet(componentOperationHandler: ComponentOperationHandler) = this.componentOperationHandler = componentOperationHandler
  /** Creates an empty Entity. */
    components = new Bag[Component]
    componentsArray = new Array[Component](false, 16)
    immutableComponentsArray = new ImmutableArray[Component](componentsArray)
    componentBits = new Bits
    familyBits = new Bits
    flags = 0
    componentAdded = new Signal[Entity]
    componentRemoved = new Signal[Entity]

  /**
    * Adds a {@link Component} to this Entity. If a {@link Component} of the same type already exists, it'll be replaced.
    *
    * @return The Entity for easy chaining
    */
  def add(component: Component): Entity = {
    if (addInternal(component)) {
      if (componentOperationHandler != null) {
        componentOperationHandler.add(this)
      }
      else {
        notifyComponentAdded
      }
    }
    return this
  }

  /**
    * Removes the {@link Component} of the specified type. Since there is only ever one component of one type, we don't need an
    * instance reference.
    *
    * @return The removed { @link Component}, or null if the Entity did no contain such a component.
    */
  def remove(componentClass: Class[_ <: Component]): Component = {
    val componentType: ComponentType = ComponentType.getFor(componentClass)
    val componentTypeIndex: Int = componentType.getIndex
    val removeComponent: Component = components.get(componentTypeIndex)
    if (removeComponent != null && removeInternal(componentClass)) {
      if (componentOperationHandler != null) {
        componentOperationHandler.remove(this)
      }
      else {
        notifyComponentRemoved
      }
    }
    return removeComponent
  }

  /** Removes all the {@link Component}'s from the Entity. */
  def removeAll {
    while (componentsArray.size > 0) {
      remove(componentsArray.get(0).getClass)
    }
  }

  /** @return immutable collection with all the Entity { @link Component}s. */
  def getComponents: ImmutableArray[Component] = {
    return immutableComponentsArray
  }

  /**
    * Retrieve a component from this {@link Entity} by class. <em>Note:</em> the preferred way of retrieving {@link Component}s is
    * using {@link ComponentMapper}s. This method is provided for convenience; using a ComponentMapper provides O(1) access to
    * components while this method provides only O(logn).
    *
    * @param componentClass the class of the component to be retrieved.
    * @return the instance of the specified { @link Component} attached to this { @link Entity}, or null if no such
    *                                               { @link Component} exists.
    */
  def getComponent[T <: Component](componentClass: Class[T]): Option[T] = {
    return getComponent(ComponentType.getFor(componentClass))
  }

  /**
    * Internal use.
    *
    * @return The { @link Component} object for the specified class, null if the Entity does not have any components for that class.
    */
    private[core] def getComponent[T <: Component](componentType: ComponentType): Option[T] = {
    val componentTypeIndex: Int = componentType.getIndex
    if (componentTypeIndex < components.getCapacity) {
      return Some(components.get(componentType.getIndex).asInstanceOf[T])
    }
    else {
      return None
    }
  }

  /**
    * @return Whether or not the Entity has a { @link Component} for the specified class.
    */
  private[core] def hasComponent(componentType: ComponentType): Boolean = {
    return componentBits.get(componentType.getIndex)
  }

  /**
    * @return This Entity's component bits, describing all the { @link Component}s it contains.
    */
  private[core] def getComponentBits: Bits = {
    return componentBits
  }

  /** @return This Entity's { @link Family} bits, describing all the { @link EntitySystem}s it currently is being processed by. */
  private[core] def getFamilyBits: Bits = {
    return familyBits
  }

  /**
    * @param component
    * @return whether or not the component was added.
    */
  private[core] def addInternal(component: Component): Boolean = {
    val componentClass: Class[_ <: Component] = component.getClass
    val oldComponent: Option[Component] = getComponent(componentClass)
    val result : Boolean = oldComponent match {
      case Some(oldy) => !(component eq oldy)
      case None => {
        removeInternal(componentClass)
        true
      }
    }
    val componentTypeIndex: Int = ComponentType.getIndexFor(componentClass)
    components.set(componentTypeIndex, component)
    componentsArray.add(component)
    componentBits.set(componentTypeIndex)
    result
  }

  /**
    * @param componentClass
    * @return whether or not a component with the specified class was found and removed.
    */
  private[core] def removeInternal(componentClass: Class[_ <: Component]): Boolean = {
    val componentType: ComponentType = ComponentType.getFor(componentClass)
    val componentTypeIndex: Int = componentType.getIndex
    val removeComponent: Component = components.get(componentTypeIndex)
    if (removeComponent != null) {
      components.set(componentTypeIndex, null)
      componentsArray.removeValue(removeComponent, true)
      componentBits.clear(componentTypeIndex)
      return true
    }
    return false
  }

  private[core] def notifyComponentAdded {
    componentAdded.dispatch(this)
  }

  private[core] def notifyComponentRemoved {
    componentRemoved.dispatch(this)
  }

  /** @return true if the entity is scheduled to be removed */
  def isScheduledForRemoval: Boolean = {
    return scheduledForRemoval
  }

  def >>[T <: Component] (compClass : Class[T]) = ComponentMapper.getFor(compClass).get(this)

  def >>[T <: Component] (compClass : Class[T]) = ComponentMapper.getFor(compClass).get(this)


}