package com.lucidplugins.lucidcannonreloader;

import com.google.inject.Provides;
import com.lucidplugins.lucidcannonreloader.api.util.GameObjectUtils;
import com.lucidplugins.lucidcannonreloader.api.util.InventoryUtils;
import com.lucidplugins.lucidcannonreloader.api.util.MessageUtils;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@Extension
@PluginDescriptor(
        name = "Lucid Cannon Reloader",
        description = "A plugin that will reload your cannon so you don't have to",
        tags = {"lucid", "cannon", "cball", "reload"})
public class LucidCannonReloaderPlugin extends Plugin
{

    @Inject
    private LucidCannonReloaderConfig config;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    private int cballsLeft = 0;

    private int nextReloadAmount = 0;

    private int nextReloadDelay = 0;

    private int lastReloadAttempt = 0;

    private int lastRepairAttempt = 0;

    private WorldPoint cannonLocation = null;

    private boolean goodDelayRange;

    private boolean goodReloadRange;

    private Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private Random rand = new Random();

    private final String RELOAD_RANGE_WARNING = "Minimum cannonball amount must be less than or equal to maximum!";

    private final String DELAY_RANGE_WARNING = "Minimum reload delay must be less than or equal to maximum!";

    @Override
    public void startUp()
    {
        log.info(getName() + " Started");

        this.clientThread.invoke(() -> this.cballsLeft = client.getVarpValue(VarPlayer.CANNON_AMMO));

        checkConfigRanges();

        nextReloadAmount = nextInt(config.minCannonballAmount(), config.maxCannonballAmount());
    }

    @Override
    public void shutDown()
    {
        log.info(getName() + " Stopped.");
    }

    @Provides
    public LucidCannonReloaderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucidCannonReloaderConfig.class);
    }

    @Subscribe
    private void onGameTick(GameTick event)
    {
        final GameObject cannon = getCannon();
        final GameObject brokenCannon = getBrokenCannon();

        if (!goodReloadRange || !goodDelayRange)
        {
            return;
        }

        if (cannon == null && brokenCannon == null)
        {
            return;
        }

        if (brokenCannon != null && GameObjectUtils.hasAction(client, brokenCannon.getId(), "Repair"))
        {
            if (ticksSinceLastRepairAttempt() > 3)
            {
                GameObjectUtils.interact(brokenCannon, "Repair");
                lastRepairAttempt = client.getTickCount();
            }
        }

        if (!(InventoryUtils.contains("Cannonball")) || InventoryUtils.contains("Granite cannonball"))
        {
            if (ticksSinceLastReloadAttempt() > 15 && client.getGameState() == GameState.LOGGED_IN)
            {
                MessageUtils.addMessage(client, "Out of cannonballs!");
                lastReloadAttempt = client.getTickCount();
            }
            return;
        }

        if (cballsLeft < nextReloadAmount)
        {
            if (ticksSinceLastReloadAttempt() > nextReloadDelay)
            {
                if (cannon != null)
                {
                    GameObjectUtils.interact(cannon, "Fire");
                }

                lastReloadAttempt = client.getTickCount();
                nextReloadAmount = nextInt(config.minCannonballAmount(), config.maxCannonballAmount());
                nextReloadDelay = nextInt(config.minReloadDelay(), config.maxReloadDelay());
            }
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event)
    {
        if (event.getType() != ChatMessageType.GAMEMESSAGE)
        {
            return;
        }

        if (event.getMessage().contains("That isn't your cannon") && ticksSinceLastReloadAttempt() < 10)
        {
            cannonLocation = null;
        }
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("lucid-cannon-reloader"))
        {
            return;
        }

        checkConfigRanges();
    }

    @Subscribe
    private void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarpId() == VarPlayer.CANNON_AMMO.getId())
        {
            cballsLeft = event.getValue();
        }
    }

    @Subscribe
    private void onMenuOpened(MenuOpened event)
    {
        final Optional<MenuEntry> fireEntry = Arrays.stream(event.getMenuEntries()).filter(menuEntry -> {
            return (menuEntry.getOption().equals("Fire") || menuEntry.getOption().equals("Repair"))  &&
                    (menuEntry.getTarget().contains("Dwarf multicannon") || menuEntry.getTarget().contains("Broken multicannon"));
        }).findFirst();

        if (fireEntry.isEmpty())
        {
            return;
        }

        final int targetLocalX = fireEntry.get().getParam0();
        final int targetLocalY = fireEntry.get().getParam1();

        final WorldPoint targetWorldPoint = WorldPoint.fromScene(client, targetLocalX, targetLocalY, client.getLocalPlayer().getPlane());

        if (targetWorldPoint.equals(cannonLocation))
        {
            client.createMenuEntry(1)
                    .setOption("Un-claim Cannon")
                    .setTarget("<col=00ff00>Lucid Cannon Reloader</col>")
                    .setType(MenuAction.RUNELITE)
                    .onClick((entry) -> {
                        cannonLocation = null;
                        MessageUtils.addMessage(client, "Cannon un-claimed");
                    });
        }
        else
        {
            if (cannonLocation != null)
            {
                MessageUtils.addMessage(client, "Cannon: " + cannonLocation + ", Target: " + targetWorldPoint);

            }

            client.createMenuEntry(1)
                    .setOption("Claim Cannon")
                    .setTarget("<col=00ff00>Lucid Cannon Reloader</col>")
                    .setType(MenuAction.RUNELITE)
                    .onClick((entry) -> {
                        cannonLocation = targetWorldPoint;
                        MessageUtils.addMessage(client, "Cannon claimed");
                    });
        }
    }


    private void checkConfigRanges()
    {
        goodDelayRange = true;
        goodReloadRange = true;

        if (config.minReloadDelay() > config.maxReloadDelay())
        {
            if (client != null && client.getGameState() == GameState.LOGGED_IN)
            {
                this.clientThread.invoke(() -> MessageUtils.addMessage(client, DELAY_RANGE_WARNING));
            }

            goodDelayRange = false;
        }

        if (config.minCannonballAmount() > config.maxCannonballAmount())
        {
            if (client != null && client.getGameState() == GameState.LOGGED_IN)
            {
                this.clientThread.invoke(() -> MessageUtils.addMessage(client, RELOAD_RANGE_WARNING));
            }

            goodReloadRange = false;
        }
    }

    private int ticksSinceLastReloadAttempt()
    {
        return client.getTickCount() - lastReloadAttempt;
    }

    private int ticksSinceLastRepairAttempt()
    {
        return client.getTickCount() - lastRepairAttempt;
    }

    private GameObject getCannon()
    {
        return new GameObjectQuery().filter(gameObject -> gameObject.getName().contains("Dwarf multicannon") && gameObject.getWorldLocation().dx(-1).dy(-1).equals(cannonLocation)).result(client).first();
    }

    private GameObject getBrokenCannon()
    {
        return new GameObjectQuery().filter(gameObject -> gameObject.getName().contains("Broken multicannon") && gameObject.getWorldLocation().dx(-1).dy(-1).equals(cannonLocation)).result(client).first();
    }


    private int nextInt(int min, int max)
    {
        return rand.nextInt((max - min) + 1) + min;
    }
}
