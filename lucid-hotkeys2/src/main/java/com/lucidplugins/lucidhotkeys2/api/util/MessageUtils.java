package com.lucidplugins.lucidhotkeys2.api.util;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;

public class MessageUtils
{
    public static void addMessage(Client client, String message)
    {
        client.addChatMessage(ChatMessageType.ENGINE, "", message, "", false);
    }
}
