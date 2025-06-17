package com.popple.gembuyer;

import com.google.inject.Provides;
import com.popple.gembuyer.tasks.BankGems;
import com.popple.gembuyer.tasks.BuyGems;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemID;
import net.runelite.api.Player;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.utils.MessageUtils;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#ff0000\">Popple's gem buyer</font></html>",
        description = "What it says on the tin.",
        enabledByDefault = false
)
public class GemBuyerPlugin extends Plugin {

    @Inject
    private Client client;

    private final Logger log = Logger.getLogger(getName());

    private static final Integer[] barrowsItems = List.of(
            ItemID.DHAROKS_HELM,
            ItemID.DHAROKS_PLATEBODY,
            ItemID.DHAROKS_PLATELEGS,
            ItemID.DHAROKS_GREATAXE,

            ItemID.AHRIMS_HOOD,
            ItemID.AHRIMS_ROBETOP,
            ItemID.AHRIMS_ROBESKIRT,
            ItemID.AHRIMS_STAFF,

            ItemID.GUTHANS_HELM,
            ItemID.GUTHANS_PLATEBODY,
            ItemID.GUTHANS_CHAINSKIRT,
            ItemID.GUTHANS_WARSPEAR,

            ItemID.KARILS_COIF,
            ItemID.KARILS_LEATHERTOP,
            ItemID.KARILS_LEATHERSKIRT,
            ItemID.BOLT_RACK,

            ItemID.VERACS_HELM,
            ItemID.VERACS_BRASSARD,
            ItemID.VERACS_PLATESKIRT,
            ItemID.VERACS_FLAIL,

            ItemID.TORAGS_HELM,
            ItemID.TORAGS_PLATEBODY,
            ItemID.TORAGS_PLATELEGS,
            ItemID.TORAGS_HAMMERS
    ).toArray(new Integer[0]);

    @Provides
    GemBuyerConfig getConfig(final ConfigManager configManager) {
        return configManager.getConfig(GemBuyerConfig.class);
    }

    private boolean running = false;


    private final TaskSet tasks = new TaskSet();

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialising gem buyer :)");
            running = false;
        }
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (configButtonClicked.getGroup().equalsIgnoreCase("popple-gem-buyer")) {
            running = !running;
            loadTasks();
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!running) return;
        Player player = client.getLocalPlayer();
        if (client == null || player == null || client.getGameState() != GameState.LOGGED_IN) return;

        Task task = tasks.getValidTask();

        if (task != null) {
            log.info(task.getTaskDescription());
            task.onGameTick(event);
        } else {
            log.info("No tasks.");
        }
    }


    private void loadTasks() {
        tasks.clear();
        tasks.addAll(
                injector.getInstance(BankGems.class),
                injector.getInstance(BuyGems.class)
        );
    }

    @Override
    protected void shutDown() {
        log.info(getName() + " stopped");
        running = false;
    }

}