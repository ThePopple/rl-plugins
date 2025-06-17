package com.popple.jugs;

import com.google.inject.Provides;
import com.popple.jugs.tasks.BankJugs;
import com.popple.jugs.tasks.BuyJugs;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
import java.util.logging.Logger;

@Extension
@PluginDescriptor(
        name = "<html><font color=\"#ff0000\">Popple's jug buyer</font></html>",
        description = "What it says on the tin.",
        enabledByDefault = false,
        tags = {}
)
public class Jugin extends Plugin {

    @Inject
    private Client client;

    private final Logger log = Logger.getLogger(getName());

    @Provides
    JugConfig getConfig(final ConfigManager configManager) {
        return configManager.getConfig(JugConfig.class);
    }

    private boolean juginRunning = false;


    private final TaskSet tasks = new TaskSet();

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialising jug buyer :)");
            juginRunning = false;
        }
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (configButtonClicked.getGroup().equalsIgnoreCase("popple-water-jugs")) {
            juginRunning = !juginRunning;
            loadTasks();
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!juginRunning) return;
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
                injector.getInstance(BankJugs.class),
                injector.getInstance(BuyJugs.class)
        );
    }

    @Override
    protected void shutDown() {
        log.info(getName() + " stopped");
        juginRunning = false;
    }

}