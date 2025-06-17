package com.popple.potmixer;

import com.google.inject.Provides;
import com.popple.potmixer.tasks.BankIngredients;
import com.popple.potmixer.tasks.MixPotions;
import net.runelite.api.Client;
import net.runelite.api.GameState;
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
        name = "<html><font color=\"#ff0000\">Popple's potion mixer</font></html>",
        description = "What it says on the tin.",
        enabledByDefault = false
)
public class PotMixerPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    private final Logger log = Logger.getLogger(getName());

    @Provides
    PotMixerConfig getConfig(final ConfigManager configManager) {
        return configManager.getConfig(PotMixerConfig.class);
    }

    @Inject
    public PotMixerConfig config;


    private final TaskSet tasks = new TaskSet();

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            MessageUtils.addMessage("Initialising potion mixer :)");
            setRunning(false);
        }
    }

    @Subscribe
    private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
        if (configButtonClicked.getGroup().equalsIgnoreCase("popple-potion-mixer")) {
            setRunning(!config.running());
            loadTasks();

            MessageUtils.addMessage(config.running() ? "Started mixing potions." : "Stopped mixing potions.");
        }
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (
                !config.running() ||
                        client == null ||
                        client.getLocalPlayer() == null ||
                        client.getGameState() != GameState.LOGGED_IN
        ) return;

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
                injector.getInstance(MixPotions.class),
                injector.getInstance(BankIngredients.class)
        );
    }

    @Override
    protected void shutDown() {
        log.info(getName() + " stopped");
        setRunning(false);
    }

    public void setRunning(boolean running) {
        configManager.setConfiguration(
                "popple-potion-mixer",
                "running",
                running
        );
    }

}