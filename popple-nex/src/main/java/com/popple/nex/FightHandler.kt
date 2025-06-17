package com.popple.nex

import net.runelite.api.NPC
import net.runelite.api.events.ActorDeath
import net.runelite.api.events.ChatMessage
import net.runelite.client.eventbus.Subscribe
import net.unethicalite.api.utils.MessageUtils

class FightHandler {

    private var state: State? = null;
    private var minion: Minion? = null;

    fun process() {
        MessageUtils.addMessage("state: $state")
        MessageUtils.addMessage("minion: $minion")
    }

    @Subscribe
    fun onChatMessage(chatMessage: ChatMessage) {
        fun setMinion(minion: Minion) {
            state = State.MINION
            this.minion = minion
        }

        when (chatMessage.message.lowercase()) {
            "fumus, don't fail me!" -> setMinion(Minion.FUMUS)
            "umbra, don't fail me!" -> setMinion(Minion.UMBRA)
            "cruor, don't fail me!" -> setMinion(Minion.CRUOR)
            "glacies, don't fail me!" -> setMinion(Minion.GLACIES)
        }
    }

    @Subscribe
    fun onActorDeath(event: ActorDeath) {
        fun phaseChange(state: State) {
            minion = null
            this.state = state
        }

        if (event.actor is NPC && event.actor.name != null) {
            when (event.actor.name.lowercase()) {
                Minion.FUMUS.npcName() -> phaseChange(State.SHADOW_PHASE)
                Minion.UMBRA.npcName() -> phaseChange(State.BLOOD_PHASE)
                Minion.CRUOR.npcName() -> phaseChange(State.ICE_PHASE)
                Minion.GLACIES.npcName() -> phaseChange(State.ZAROS_PHASE)
                "nex" -> phaseChange(State.LOOT)
            }
        }
    }

}