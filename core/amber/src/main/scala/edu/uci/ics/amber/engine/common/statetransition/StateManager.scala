package edu.uci.ics.amber.engine.common.statetransition

import edu.uci.ics.amber.engine.common.statetransition.StateManager.{InvalidStateException, InvalidTransitionException}

object StateManager{
  case class InvalidStateException(message: String) extends RuntimeException(message) with Serializable
  case class InvalidTransitionException(message: String) extends RuntimeException(message) with Serializable
}


class StateManager[T](stateTransitionGraph:Map[T,Set[T]], initialState:T) {

  private var currentState:T = initialState

  def shouldBe(state:T): Unit ={
    if(currentState != state){
      throw InvalidStateException(s"except state = $state but current state = $currentState")
    }
  }

  def shouldBe(states:T*):Unit = {
    if(!states.contains(currentState)){
      throw InvalidStateException(s"except state in [${states.mkString(",")}] but current state = $currentState")
    }
  }

  def transitTo(state:T):Unit = {
    if(state == currentState) return
    if(!stateTransitionGraph.getOrElse(state,Set()).contains(state)){
      throw InvalidTransitionException(s"cannot transit from $currentState to $state")
    }
    currentState = state
  }

  def getCurrentState: T = currentState

}
