package com.popple.nex

enum class Minion {
    FUMUS,
    UMBRA,
    CRUOR,
    GLACIES;

    fun npcName(): String {
        return this.name.lowercase()
    }
}