package com.popple.kraken

import javax.inject.Singleton

@Singleton
class State {
    var poolsDone: Boolean = false
    var killCount: Int = 0
    var stateString: String = "Idle"

    fun reset() {
        poolsDone = false
        killCount = 0
        stateString = "Idle"
    }
}