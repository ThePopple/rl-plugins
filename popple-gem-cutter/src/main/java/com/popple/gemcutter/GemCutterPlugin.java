package com.popple.gemcutter;

import com.google.inject.Provides;
import com.popple.gemcutter.tasks.BankGems;
import com.popple.gemcutter.tasks.CutGems;
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
        name = "<html><font color=\"#ff0000\">Popple's gem cutter</font></html>",
        description = "What it says on the tin.",
        enabledByDefault = false
)
public class GemCutterPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    private final Logger log = Logger.getLogger(getName());

    @Provides
    GemCutterConfig getConfig(final ConfigManager configManager) {
        return configManager.getConfig(GemCutterConfig.class);
    }

    @Inject
    public GemCutterConfig config;


    private final TaskSet tasks = new TaskSet();

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialising gem cutter :)");
        }
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (configButtonClicked.getGroup().equalsIgnoreCase("popple-gem-cutter")) {
            setRunning(!config.running());
            loadTasks();

            if (config.running()) {
                MessageUtils.addMessage(String.format("Started cutting %ss.", config.gem().name().toLowerCase()));
            } else {
                MessageUtils.addMessage("Gem cutter stopped.");
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!config.running()) return;

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
                injector.getInstance(CutGems.class),
                injector.getInstance(BankGems.class)
        );
    }

    @Override
    protected void shutDown() {
        log.info(getName() + " stopped");
        setRunning(false);
    }

    public void setRunning(boolean running) {
        configManager.setConfiguration(
                "popple-gem-cutter",
                "running",
                running
        );
    }

}