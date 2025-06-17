package com.lucidplugins.lucidcombat;

import com.google.inject.Provides;
import com.lucidplugins.lucidcombat.api.item.SlottedItem;
import com.lucidplugins.lucidcombat.api.spells.Runes;
import com.lucidplugins.lucidcombat.api.spells.WidgetInfo;
import com.lucidplugins.lucidcombat.api.util.*;
import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.unethicalite.api.packets.MousePackets;
import net.unethicalite.client.Static;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Predicate;


@Extension
@PluginDescriptor(name = "Lucid Combat", description = "Helps with Combat related stuff", enabledByDefault = false)
public class LucidCombatPlugin extends Plugin implements KeyListener
{

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private LucidCombatTileOverlay overlay;

    @Inject
    private LucidCombatPanelOverlay panelOverlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

    @Inject
    private KeyManager keyManager;

    @Inject
    private LucidCombatConfig config;

    @Inject
    private ItemManager itemManager;

    private int nextSolidFoodTick = 0;
    private int nextPotionTick = 0;
    private int nextKarambwanTick = 0;

    private boolean eatingToMaxHp = false;

    private boolean drinkingToMaxPrayer = false;

    private int timesBrewedDown = 0;

    private Random random = new Random();

    private int nextHpToRestoreAt = 0;

    private int nextPrayerLevelToRestoreAt = 0;

    private int lastTickActive = 0;

    private int nextReactionTick = 0;

    private int lastFinisherAttempt = 0;

    private int nonSpecWeaponId = -1;
    private int offhandWeaponID = -1;

    private boolean isSpeccing = false;

    @Getter
    private Actor lastTarget = null;

    @Getter
    private boolean autoCombatRunning = false;

    @Getter
    private String secondaryStatus = "Starting...";

    @Getter
    private WorldPoint startLocation = null;

    @Getter
    private Map<LocalPoint, Integer> expectedLootLocations = new HashMap<>();

    private LocalPoint lastLootedTile = null;

    private int nextLootAttempt = 0;

    private int lastAlchTick = 0;

    private int lastThrallTick = 0;
    private boolean taskEnded = false;

    private boolean tabbed = true;

    private int lastCannonAttempt = 0;

    private List<NPC> npcsKilled = new ArrayList<>();

    private final List<String> prayerRestoreNames = List.of("Prayer potion", "Super restore", "Sanfew serum", "Blighted super restore", "Moonlight potion");

    private final Predicate<SlottedItem> foodFilterNoBlacklistItems = (item) -> {
        final ItemComposition itemComposition = client.getItemDefinition(item.getItem().getId());
        return itemComposition.getName() != null &&
                (!itemComposition.getName().equals("Cooked karambwan") && !itemComposition.getName().equals("Blighted karambwan")) &&
            !config.foodBlacklist().contains(itemComposition.getName()) &&
            (Arrays.asList(itemComposition.getInventoryActions()).contains("Eat"));
    };

    private final Predicate<SlottedItem> karambwanFilter = (item) -> {
        final ItemComposition itemComposition = client.getItemDefinition(item.getItem().getId());
        return itemComposition.getName() != null &&
                (itemComposition.getName().equals("Cooked karambwan") || itemComposition.getName().equals("Blighted karambwan")) &&
                (Arrays.asList(itemComposition.getInventoryActions()).contains("Eat"));
    };

    @Override
    protected void startUp()
    {
        clientThread.invoke(this::pluginEnabled);
    }

    private void pluginEnabled()
    {
        keyManager.registerKeyListener(this);

        if (!overlayManager.anyMatch(p -> p == overlay))
        {
            overlayManager.add(overlay);
        }

        if (!overlayManager.anyMatch(p -> p == panelOverlay))
        {
            overlayManager.add(panelOverlay);
        }

        expectedLootLocations.clear();
        npcsKilled.clear();
        tabbed = false;
        taskEnded = false;
    }

    @Override
    protected void shutDown()
    {
        keyManager.unregisterKeyListener(this);
        autoCombatRunning = false;

        if (overlayManager.anyMatch(p -> p == overlay))
        {
            overlayManager.remove(overlay);
        }

        if (overlayManager.anyMatch(p -> p == panelOverlay))
        {
            overlayManager.remove(panelOverlay);
        }
    }

    @Provides
    LucidCombatConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(LucidCombatConfig.class);
    }

    @Subscribe
    private void onMenuOpened(MenuOpened event)
    {
        if (!config.rightClickMenu())
        {
            return;
        }

        final Optional<MenuEntry> attackEntry = Arrays.stream(event.getMenuEntries()).filter(menu -> menu.getOption().equals("Attack") && menu.getNpc() != null && menu.getNpc().getName() != null).findFirst();

        if (attackEntry.isEmpty())
        {
            return;
        }

        if (!autoCombatRunning)
        {
            client.createMenuEntry(1)
            .setOption("Start Killing")
            .setTarget("<col=ffff00>" + attackEntry.get().getNpc().getName() + "</col>")
            .setType(MenuAction.RUNELITE)
            .onClick((entry) -> {
                clientThread.invoke(() -> configManager.setConfiguration("lucid-combat", "npcToFight", attackEntry.get().getNpc().getName()));
                lastTickActive = client.getTickCount();
                lastAlchTick = client.getTickCount();
                expectedLootLocations.clear();
                npcsKilled.clear();
                taskEnded = false;
                tabbed = false;
                startLocation = client.getLocalPlayer().getWorldLocation();
                autoCombatRunning = true;
            });
        }
        else
        {
            if (attackEntry.get().getNpc() == null || attackEntry.get().getNpc().getName() == null)
            {
                return;
            }

            if (isNameInNpcsToFight(attackEntry.get().getNpc().getName()))
            {
                client.createMenuEntry(1)
                .setOption("Stop Killing")
                .setTarget("<col=ffff00>" + attackEntry.get().getNpc().getName() + "</col>")
                .setType(MenuAction.RUNELITE)
                .onClick((entry) -> {
                    autoCombatRunning = false;
                    lastTickActive = client.getTickCount();
                    lastTarget = null;
                    lastLootedTile = null;
                    expectedLootLocations.clear();
                    npcsKilled.clear();
                    taskEnded = false;
                    tabbed = false;
                    startLocation = null;
                });
            }
        }
    }

    private boolean isNameInNpcsToFight(String name)
    {
        if (config.npcToFight().trim().isEmpty())
        {
            return false;
        }

        for (String npcName : config.npcToFight().split(","))
        {
            npcName = npcName.trim();

            if (name.contains(npcName))
            {
                return true;
            }
        }

        return false;
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("lucid-combat"))
        {
            return;
        }

        clientThread.invoke(() -> {
            lastTickActive = client.getTickCount();
            taskEnded = false;
            tabbed = false;
            nextHpToRestoreAt = Math.max(1, config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0));
            nextPrayerLevelToRestoreAt = Math.max(1, config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0));
        });
    }

    private boolean idInNpcBlackList(int id)
    {
        if (config.idBlacklist().trim().isEmpty())
        {
            return false;
        }

        for (String stringId : config.idBlacklist().split(","))
        {
            String idTrimmed = stringId.trim();
            int npcId = Integer.parseInt(idTrimmed);

            if (id == npcId)
            {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event)
    {
        if (config.specIfEquipped() && (event.getMenuOption().equals("Wield") || event.getMenuOption().equals("Wear")) && (!config.specWeapon().isEmpty() && event.getMenuTarget().contains(config.specWeapon())))
       {
            lastTarget = client.getLocalPlayer().getInteracting();

            if (EquipmentUtils.getWepSlotItem() != null)
            {
                nonSpecWeaponId = EquipmentUtils.getWepSlotItem().getId();
            }

            if (EquipmentUtils.getShieldSlotItem() != null)
            {
                offhandWeaponID = EquipmentUtils.getShieldSlotItem().getId();
            }
        }
    }

    @Subscribe
    private void onChatMessage(ChatMessage event)
    {
        if (event.getType() == ChatMessageType.GAMEMESSAGE && (event.getMessage().contains("return to a Slayer master") || event.getMessage().contains("more advanced Slayer Master")))
        {

            if (config.stopOnTaskCompletion() && autoCombatRunning)
            {
                secondaryStatus = "Slayer Task Done";
                startLocation = null;
                autoCombatRunning = false;
                taskEnded = true;
                lastTarget = null;
            }

            if (config.stopUpkeepOnTaskCompletion() && taskEnded)
            {
                expectedLootLocations.clear();
                lastTickActive = 0;
            }
        }

        if (event.getType() == ChatMessageType.GAMEMESSAGE && (event.getMessage().contains("can't take items that other") || event.getMessage().contains("have enough inventory space")))
        {
            expectedLootLocations.keySet().removeIf(tile -> tile.equals(lastLootedTile));
        }

        if (event.getType() == ChatMessageType.GAMEMESSAGE && event.getMessage().contains("can't reach that"))
        {
            lastTarget = null;
            lastLootedTile = null;
            lastTickActive = client.getTickCount();
            nextReactionTick = client.getTickCount() + 1;
        }

        if (event.getType() == ChatMessageType.GAMEMESSAGE && (event.getMessage().contains("cannot cast that") || event.getMessage().contains("can't resurrect a thrall here")))
        {
            lastThrallTick = client.getTickCount();
        }
    }

    @Subscribe
    private void onClientTick(ClientTick tick)
    {
        if (client.getLocalPlayer().getInteracting() != null && client.getLocalPlayer().getInteracting().isDead())
        {
            if (client.getLocalPlayer().getInteracting() instanceof NPC && !npcsKilled.contains((NPC)client.getLocalPlayer().getInteracting()))
            {
                npcsKilled.add((NPC)client.getLocalPlayer().getInteracting());
            }
        }
    }

    @Subscribe
    private void onGameTick(GameTick tick)
    {
        expectedLootLocations.entrySet().removeIf(i -> client.getTickCount() > i.getValue() + 500);

        if (client.getGameState() != GameState.LOGGED_IN || BankUtils.isOpen())
        {
            return;
        }

        updatePluginVars();

        if (hpFull() && eatingToMaxHp)
        {
            secondaryStatus = "HP Full Now";
            eatingToMaxHp = false;
        }

        if (prayerFull() && drinkingToMaxPrayer)
        {
            secondaryStatus = "Prayer Full Now";
            drinkingToMaxPrayer = false;
        }

        boolean actionTakenThisTick = restorePrimaries();

        // Stop other upkeep besides HP if we haven't animated in the last minute
        if (getInactiveTicks() > 200)
        {
            secondaryStatus = "Idle for > 2 min";
            return;
        }

        if (client.getTickCount() > 10 && client.getTickCount() - lastAlchTick == 1)
        {
            client.runScript(915, 3);
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = restoreStats();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Restoring Stats";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = restoreBoosts();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Restoring Boosts";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleSlayerFinisher();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Finishing Slayer Monster";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleAutoSpec();

            if (actionTakenThisTick)
            {
                secondaryStatus = "Auto-Spec";
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleSlaughterEquip();
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleExpeditiousEquip();
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleLooting();

            if (!actionTakenThisTick && (nextLootAttempt - client.getTickCount()) < 0 && lastTarget != null)
            {
                actionTakenThisTick = handleReAttack();
            }
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleThralls();
        }

        if (!actionTakenThisTick)
        {
            actionTakenThisTick = handleAutoCombat();
        }

    }

    public boolean isMoving()
    {
        return client.getLocalPlayer().getPoseAnimation() != client.getLocalPlayer().getIdlePoseAnimation();
    }

    private boolean handleSlaughterEquip()
    {
        if (!config.equipSlaughterBracelet())
        {
            return false;
        }

        if (!autoCombatRunning)
        {
            return false;
        }

        if (!InventoryUtils.contains("Bracelet of slaughter"))
        {
            return false;
        }

        if (config.equipExpeditiousBracelet() && EquipmentUtils.contains("Expeditious bracelet"))
        {
            return false;
        }

        if (EquipmentUtils.contains("Bracelet of slaughter"))
        {
            return false;
        }

        Item bracelet = InventoryUtils.getFirstItem("Bracelet of slaughter");

        if (bracelet != null)
        {
            InventoryUtils.itemInteract(bracelet.getId(), "Wear");
            return true;
        }

        return false;
    }

    private boolean handleExpeditiousEquip()
    {
        if (!config.equipExpeditiousBracelet())
        {
            return false;
        }

        if (!autoCombatRunning)
        {
            return false;
        }

        if (!InventoryUtils.contains("Expeditious bracelet"))
        {
            return false;
        }

        if (EquipmentUtils.contains("Expeditious bracelet"))
        {
            return false;
        }

        if (config.equipSlaughterBracelet() && EquipmentUtils.contains("Bracelet of slaughter"))
        {
            return false;
        }

        Item bracelet = InventoryUtils.getFirstItem("Expeditious bracelet");

        if (bracelet != null)
        {
            InventoryUtils.itemInteract(bracelet.getId(), "Wear");
            return true;
        }

        return false;
    }

    private boolean handleReAttack()
    {
        if (config.alchStuff() && !getAlchableItems().isEmpty())
        {
            return false;
        }

        if (lastTarget == null || isMoving())
        {
            return false;
        }

        if (client.getLocalPlayer().getInteracting() == lastTarget)
        {
            return false;
        }

        if (lastTarget.getInteracting() != client.getLocalPlayer())
        {
            lastTarget = null;
            return false;
        }

        if (lastTarget instanceof Player && isPlayerEligible((Player)lastTarget))
        {
            PlayerUtils.interactPlayer(lastTarget.getName(), "Attack");
            lastTarget = null;
            secondaryStatus = "Re-attacking previous target";
            return true;
        }
        else if (lastTarget instanceof NPC && isNpcEligible((NPC)lastTarget))
        {
            NpcUtils.interact((NPC)lastTarget, "Attack");
            lastTarget = null;
            secondaryStatus = "Re-attacking previous target";
            return true;
        }

        return false;
    }


    private boolean handleSlayerFinisher()
    {
        Actor target = client.getLocalPlayer().getInteracting();
        if (!(target instanceof NPC))
        {
            return false;
        }

        NPC npcTarget = (NPC) target;
        int ratio = npcTarget.getHealthRatio();
        int scale = npcTarget.getHealthScale();

        double targetHpPercent = Math.floor((double) ratio  / (double) scale * 100);
        if (targetHpPercent < config.slayerFinisherHpPercent() && targetHpPercent >= 0)
        {
            Item slayerFinisher = InventoryUtils.getFirstItem(config.slayerFinisherItem().getItemName());
            if (config.autoSlayerFinisher() && slayerFinisher != null &&
                    client.getTickCount() - lastFinisherAttempt > 5)
            {
                InteractionUtils.useItemOnNPC(slayerFinisher, npcTarget);
                lastFinisherAttempt = client.getTickCount();
                return true;
            }
        }
        return false;
    }

    private boolean handleAutoSpec()
    {
        if (!config.enableAutoSpec() || config.specWeapon().isEmpty())
        {
            return false;
        }

        if (config.specIfAutocombat() && !autoCombatRunning)
        {
            return false;
        }

        if (!isSpeccing && canStartSpeccing())
        {
            isSpeccing = true;
        }
        else if (isSpeccing && !canSpec())
        {
            isSpeccing = false;
        }

        if (nonSpecWeaponId != -1 && !isSpeccing)
        {
            lastTarget = client.getLocalPlayer().getInteracting();
            if (InventoryUtils.itemHasAction(client, nonSpecWeaponId, "Wield"))
            {
                InventoryUtils.itemInteract(nonSpecWeaponId, "Wield");
            }
            else if (InventoryUtils.itemHasAction(client, nonSpecWeaponId, "Wear"))
            {
                InventoryUtils.itemInteract(nonSpecWeaponId, "Wear");
            }

            if (offhandWeaponID != -1)
            {
                if (InventoryUtils.itemHasAction(client, offhandWeaponID, "Wield"))
                {
                    InventoryUtils.itemInteract(offhandWeaponID, "Wield");
                }
                else if (InventoryUtils.itemHasAction(client, offhandWeaponID, "Wear"))
                {
                    InventoryUtils.itemInteract(offhandWeaponID, "Wear");
                }
            }

            nonSpecWeaponId = -1;
            offhandWeaponID = -1;
            return true;
        }

        if (!isSpeccing)
        {
            return false;
        }

        boolean equippedItem = false;
        if (!EquipmentUtils.contains(config.specWeapon()))
        {
            if (!config.specIfEquipped())
            {
                Item specWeapon = InventoryUtils.getFirstItem(config.specWeapon());
                if (specWeapon != null && canSpec())
                {
                    if (EquipmentUtils.getWepSlotItem() != null)
                    {
                        nonSpecWeaponId = EquipmentUtils.getWepSlotItem().getId();
                    }

                    if (EquipmentUtils.getShieldSlotItem() != null)
                    {
                        offhandWeaponID = EquipmentUtils.getShieldSlotItem().getId();
                    }

                    lastTarget = client.getLocalPlayer().getInteracting();

                    if (lastTarget != null)
                    {
                        if (InventoryUtils.itemHasAction(client, specWeapon.getId(), "Wield"))
                        {
                            InventoryUtils.itemInteract(specWeapon.getId(), "Wield");
                            equippedItem = true;
                        }
                        else if (InventoryUtils.itemHasAction(client, specWeapon.getId(), "Wear"))
                        {
                            InventoryUtils.itemInteract(specWeapon.getId(), "Wear");
                            equippedItem = true;
                        }
                    }
                }
            }
        }
        else
        {
            if (client.getLocalPlayer().getInteracting() != null || lastTarget != null)
            {
                equippedItem = true;
            }
        }

        if (equippedItem && isSpeccing && !CombatUtils.isSpecEnabled())
        {
            CombatUtils.toggleSpec();
            return true;
        }

        return false;
    }

    private boolean canStartSpeccing()
    {
        final int spec = CombatUtils.getSpecEnergy(client);
        return spec >= config.minSpec() && spec >= config.specNeeded();
    }

    private boolean canSpec()
    {
        final int spec = CombatUtils.getSpecEnergy(client);
        return spec >= config.specNeeded();
    }

    private boolean handleLooting()
    {
        if (!autoCombatRunning)
        {
            return false;
        }

        if (nextLootAttempt == 0)
        {
            nextLootAttempt = client.getTickCount();
        }

        if (ticksUntilNextLootAttempt() > 0)
        {
            return false;
        }

        boolean ignoringTargetLimitation = ticksUntilNextLootAttempt() < -config.maxTicksBetweenLooting();

        if (config.onlyLootWithNoTarget() && !(targetDeadOrNoTargetIgnoreAttackingUs() || ignoringTargetLimitation))
        {
            return false;
        }

        List<TileItem> lootableItems = getLootableItems();

        if (config.stackableOnly())
        {
            lootableItems.removeIf(loot -> {

                if (config.buryScatter())
                {
                    return (!isStackable(loot.getId()) && !canBuryOrScatter(loot.getId())) || (loot.getId() == ItemID.CURVED_BONE || loot.getId() == ItemID.LONG_BONE);
                }

                return !isStackable(loot.getId());
            });
        }

        if (InventoryUtils.getFreeSlots() == 0)
        {
            lootableItems.removeIf(loot -> !isStackable(loot.getId()) || (isStackable(loot.getId()) && InventoryUtils.count(loot.getId()) == 0));
        }

        TileItem nearest = nearestTileItem(lootableItems);

        if (config.enableLooting() && nearest != null)
        {
            if (client.getLocalPlayer().getInteracting() != null)
            {
                lastTarget = client.getLocalPlayer().getInteracting();
            }

            InteractionUtils.interactWithTileItem(nearest, "Take");
            lastLootedTile = nearest.getLocalLocation();

            if (!client.getLocalPlayer().getLocalLocation().equals(nearest.getLocalLocation()))
            {
                if (config.onlyLootWithNoTarget())
                {
                    if (ignoringTargetLimitation && lootableItems.size() <= 1)
                    {
                        nextLootAttempt = client.getTickCount() + 2;
                    }
                }
                else
                {
                    nextLootAttempt = client.getTickCount() + 2;
                }
            }
            else
            {
                if (config.onlyLootWithNoTarget())
                {
                    if (ignoringTargetLimitation && lootableItems.size() <= 1)
                    {
                        nextLootAttempt = client.getTickCount() + 2;
                    }
                }
            }

            secondaryStatus = "Looting!";
            return true;
        }


        if (config.buryScatter())
        {
            List<SlottedItem> itemsToBury = InventoryUtils.getAll(item -> {
                ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                return Arrays.asList(composition.getInventoryActions()).contains("Bury") &&
                        !(composition.getName().contains("Long") || composition.getName().contains("Curved"));
            });

            List<SlottedItem> itemsToScatter = InventoryUtils.getAll(item -> {
                ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                return Arrays.asList(composition.getInventoryActions()).contains("Scatter");
            });

            if (!itemsToBury.isEmpty())
            {
                SlottedItem itemToBury = itemsToBury.get(0);

                if (itemToBury != null)
                {
                    InventoryUtils.itemInteract(itemToBury.getItem().getId(), "Bury");
                    nextReactionTick = client.getTickCount() + randomIntInclusive(1, 3);
                    return true;
                }
            }

            if (!itemsToScatter.isEmpty())
            {
                SlottedItem itemToScatter = itemsToScatter.get(0);

                if (itemToScatter != null)
                {
                    InventoryUtils.itemInteract(itemToScatter.getItem().getId(), "Scatter");
                    nextReactionTick = client.getTickCount() + randomIntInclusive(1, 3);
                    return true;
                }
            }
        }

        return false;
    }

    private List<TileItem> getLootableItems()
    {
        return InteractionUtils.getAllTileItems(tileItem -> {
            ItemComposition composition = client.getItemComposition(tileItem.getId());

            if (composition.getName() == null)
            {
                return false;
            }

            boolean inWhitelist = nameInLootWhiteList(composition.getName());
            boolean inBlacklist = nameInLootBlackList(composition.getName());
            boolean isValuable = (itemManager.getItemPrice(composition.getId()) * tileItem.getQuantity()) >= config.lootAbovePrice();

            boolean antiLureActivated = false;

            if (config.antilureProtection())
            {
                antiLureActivated = InteractionUtils.distanceTo2DHypotenuse(tileItem.getWorldLocation(), startLocation) > (config.maxRange() + 3);
            }

            boolean inAnExpectedLocation = (config.lootGoblin() || expectedLootLocations.containsKey(tileItem.getLocalLocation()));

            return (!inBlacklist && (inWhitelist || isValuable)) && inAnExpectedLocation &&
                    InteractionUtils.distanceTo2DHypotenuse(tileItem.getWorldLocation(), client.getLocalPlayer().getWorldLocation()) <= config.lootRange() &&
                    !antiLureActivated;
        });
    }

    private boolean nameInLootWhiteList(String name)
    {
        if (config.lootNames().trim().isEmpty())
        {
            return true;
        }

        for (String itemName : config.lootNames().split(","))
        {
            itemName = itemName.trim();

            if (name.length() > 0 && name.contains(itemName))
            {
                return true;
            }
        }

        return false;
    }

    private boolean nameInLootBlackList(String name)
    {
        if (config.lootBlacklist().trim().isEmpty())
        {
            return false;
        }

        for (String itemName : config.lootBlacklist().split(","))
        {
            itemName = itemName.trim();

            if (name.length() > 0 && name.contains(itemName))
            {
                return true;
            }
        }

        return false;
    }

    private TileItem nearestTileItem(List<TileItem> items)
    {
        TileItem nearest = null;
        float nearestDist = 999;

        for (TileItem tileItem : items)
        {
            final float dist = InteractionUtils.distanceTo2DHypotenuse(tileItem.getWorldLocation(), client.getLocalPlayer().getWorldLocation());
            if (dist < nearestDist)
            {
                nearest = tileItem;
                nearestDist = dist;
            }
        }

        return nearest;
    }

    private boolean handleThralls()
    {
        if (!config.enableThralls())
        {
            return false;
        }

        if (client.getTickCount() - lastThrallTick < 5 || GameObjectUtils.nearest(ObjectID.PORTAL_4525) != null)
        {
            return false;
        }

        if (client.getVarbitValue(Varbits.RESURRECT_THRALL_COOLDOWN) > 0 || client.getVarbitValue(Varbits.RESURRECT_THRALL) == 1)
        {
            return false;
        }

        if (getInactiveTicks() > 25 || !hasRunesForThrall() || !InventoryUtils.contains("Book of the dead"))
        {
            return false;
        }

        WidgetInfo spellInfo2 = config.thrallType().getThrallSpell().getWidget();
        if (spellInfo2 == null)
        {
            return false;
        }

        int bookId = client.getVarbitValue(Varbits.SPELLBOOK_SWAP);

        if (bookId != 3)
        {
            return false;
        }

        Widget widget = client.getWidget(spellInfo2.getPackedId());
        if (widget == null)
        {
            return false;
        }

        lastThrallTick = client.getTickCount();
        MousePackets.queueClickPacket();
        Static.getClient().invokeMenuAction("", "", 1, MenuAction.CC_OP.getId(), -1, spellInfo2.getPackedId());

        return true;
    }

    private boolean hasRunesForThrall()
    {
        final int air = totalCount(ItemID.AIR_RUNE);
        final int dust = totalCount(ItemID.DUST_RUNE);
        final int smoke = totalCount(ItemID.SMOKE_RUNE);
        final int mist = totalCount(ItemID.MIST_RUNE);
        final int earth = totalCount(ItemID.EARTH_RUNE);
        final int mud = totalCount(ItemID.MUD_RUNE);
        final int lava = totalCount(ItemID.LAVA_RUNE);
        final int steam = totalCount(ItemID.STEAM_RUNE);
        final int fire = totalCount(ItemID.FIRE_RUNE);
        final int mind = totalCount(ItemID.MIND_RUNE);
        final int death = totalCount(ItemID.DEATH_RUNE);
        final int blood = totalCount(ItemID.BLOOD_RUNE);
        final int cosmic = totalCount(ItemID.COSMIC_RUNE);

        final int totalAir = air + dust + smoke + mist;
        final int totalEarth = earth + dust + mud + lava;
        final int totalFire = fire + lava + smoke + steam;

        switch (config.thrallType())
        {
            case LESSER_GHOST:
            case LESSER_SKELETON:
            case LESSER_ZOMBIE:
                return totalAir >= 10 && cosmic >= 1 && mind >= 5;
            case SUPERIOR_GHOST:
            case SUPERIOR_SKELETON:
            case SUPERIOR_ZOMBIE:
                return totalEarth >= 10 && cosmic >= 1 && death >= 5;
            case GREATER_GHOST:
            case GREATER_SKELETON:
            case GREATER_ZOMBIE:
                return totalFire >= 10 && cosmic >= 1 && blood >= 5;
        }
        return false;
    }

    private boolean handleAutoCombat()
    {
        if (!autoCombatRunning)
        {
            return false;
        }

        if (config.useSafespot() && !startLocation.equals(client.getLocalPlayer().getWorldLocation()) && !isMoving())
        {
            InteractionUtils.walk(startLocation);
            nextReactionTick = client.getTickCount() + getReaction();
            return false;
        }

        if (!canReact() || isMoving())
        {
            return false;
        }

        if (ticksUntilNextLootAttempt() > 0)
        {
            return false;
        }

        if (config.alchStuff())
        {
            if (handleAlching())
            {
                return false;
            }
        }

        secondaryStatus = "Combat";

        if (targetDeadOrNoTarget())
        {
            NPC target = getEligibleTarget();
            if (target != null)
            {
                NpcUtils.interact(target, "Attack");
                nextReactionTick = client.getTickCount() + getReaction();
                secondaryStatus = "Attacking " + target.getName();

                if (getInactiveTicks() > 2)
                {
                    lastTickActive = client.getTickCount();
                }

                return true;
            }
            else
            {
                secondaryStatus = "Nothing to murder";
                nextReactionTick = client.getTickCount() + getReaction();
                return false;
            }
        }
        else
        {
            if (getEligibleNpcInteractingWithUs() != null && client.getLocalPlayer().getInteracting() == null)
            {
                if (isNpcEligible(getEligibleNpcInteractingWithUs()))
                {
                    NpcUtils.interact(getEligibleNpcInteractingWithUs(), "Attack");
                    nextReactionTick = client.getTickCount() + getReaction();
                    secondaryStatus = "Re-attacking " + getEligibleNpcInteractingWithUs().getName();
                }

                if (getInactiveTicks() > 2)
                {
                    lastTickActive = client.getTickCount();
                }
                return true;
            }
        }

        secondaryStatus = "Idle";
        nextReactionTick = client.getTickCount() + getReaction();
        return false;
    }

    private boolean handleAlching()
    {
        if (lastAlchTick == 0)
        {
            lastAlchTick = client.getTickCount() + 5;
        }

        if (client.getTickCount() - lastAlchTick < 5)
        {
            return false;
        }

        List<SlottedItem> alchableItems = getAlchableItems();

        if (alchableItems.isEmpty())
        {
            return false;
        }

        boolean hasRunes = (isHighAlching() && hasAlchRunes(true)) || (!isHighAlching() && hasAlchRunes(false));
        if (!hasRunes)
        {
            MessageUtils.addMessage(client, "Need to alch but not enough runes");
            return false;
        }

        if (client.getVarbitValue(4070) != 0)
        {
            MessageUtils.addMessage(client, "Need to alch but not on normal spellbook");
            return false;
        }

        SlottedItem itemToAlch = alchableItems.get(0);

        if (client.getLocalPlayer().getInteracting() != null)
        {
            lastTarget = client.getLocalPlayer().getInteracting();
        }

        InventoryUtils.castAlchemyOnItem(itemToAlch.getItem().getId(), isHighAlching());
        lastAlchTick = client.getTickCount();
        secondaryStatus = "Alching";
        return true;
    }

    private int totalCount(int itemId)
    {
        int count = InventoryUtils.count(itemId);

        if (!hasRunePouchInInventory())
        {
            return count;
        }

        int runeIndex = Runes.getVarbitIndexForItemId(itemId);

        if (idInRunePouch1() == runeIndex)
        {
            count += amountInRunePouch1();
        }

        if (idInRunePouch2() == runeIndex)
        {
            count += amountInRunePouch2();
        }

        if (idInRunePouch3() == runeIndex)
        {
            count += amountInRunePouch3();
        }

        if (idInRunePouch4() == runeIndex)
        {
            count += amountInRunePouch4();
        }

        return count;
    }

    private boolean isHighAlching()
    {
        return client.getBoostedSkillLevel(Skill.MAGIC) >= 55;
    }

    private int idInRunePouch1()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_RUNE1);
    }

    private int amountInRunePouch1()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT1);
    }

    private int idInRunePouch2()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_RUNE2);
    }

    private int amountInRunePouch2()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT2);
    }

    private int idInRunePouch3()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_RUNE3);
    }

    private int amountInRunePouch3()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT3);
    }

    private int idInRunePouch4()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_RUNE4);
    }

    private int amountInRunePouch4()
    {
        return client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT4);
    }

    private boolean hasAlchRunes(boolean highAlch)
    {
        int natCount = totalCount(ItemID.NATURE_RUNE);

        int fireRunes = totalCount(ItemID.FIRE_RUNE);
        int lavaRunes = totalCount(ItemID.LAVA_RUNE);
        int steamRunes = totalCount(ItemID.STEAM_RUNE);
        int smokeRunes = totalCount(ItemID.SMOKE_RUNE);
        int total = (fireRunes + lavaRunes + smokeRunes + steamRunes);

        boolean hasFireRunes = total >= (highAlch ? 5 : 3);
        boolean hasNatures = natCount >= 1;
        boolean hasTome = EquipmentUtils.contains(ItemID.TOME_OF_FIRE);

        return hasNatures && (hasFireRunes || hasTome);
    }

    private boolean hasRunePouchInInventory()
    {
        return InventoryUtils.contains("Rune pouch") || InventoryUtils.contains("Divine rune pouch");
    }


    private List<SlottedItem> getAlchableItems()
    {
        if (config.alchNames().trim().isEmpty())
        {
            return List.of();
        }

        return InventoryUtils.getAll(item ->
        {
            ItemComposition composition = client.getItemComposition(item.getItem().getId());
            boolean nameContains = false;
            for (String itemName : config.alchNames().split(","))
            {
                itemName = itemName.trim();

                if (composition.getName() != null && composition.getName().contains(itemName))
                {
                    nameContains = true;
                    break;
                }
            }

            boolean inBlacklist = false;
            if (!config.lootBlacklist().trim().isEmpty())
            {
                for (String itemName : config.alchBlacklist().split(","))
                {
                    itemName = itemName.trim();

                    if (itemName.length() < 2)
                    {
                        continue;
                    }

                    if (composition.getName() != null && composition.getName().contains(itemName))
                    {
                        inBlacklist = true;
                        break;
                    }
                }
            }

            return nameContains && !inBlacklist;
        });
    }

    public int getReaction()
    {
        int min = config.autocombatStyle().getLowestDelay();
        int max = config.autocombatStyle().getHighestDelay();

        int delay = randomIntInclusive(min, max);

        if (config.autocombatStyle() == PlayStyle.ROBOTIC)
        {
            delay = 0;
        }

        int randomMinDelay = Math.max(0, randomStyle().getLowestDelay());
        int randomMaxDelay = Math.max(randomMinDelay, randomStyle().getHighestDelay());

        int randomDeterminer = randomIntInclusive(0, 49);

        if (config.reactionAntiPattern())
        {
            boolean fiftyFifty = randomIntInclusive(0, 1) == 0;
            int firstNumber = (fiftyFifty ? 5 : 18);
            int secondNumber = (fiftyFifty ? 24 : 48);
            if (randomDeterminer == firstNumber || randomDeterminer == secondNumber)
            {
                delay = randomIntInclusive(randomMinDelay, randomMaxDelay);
                random = new Random();
            }
        }

        return delay;
    }

    public PlayStyle randomStyle()
    {
        return PlayStyle.values()[randomIntInclusive(0, PlayStyle.values().length - 1)];
    }

    public int randomIntInclusive(int min, int max)
    {
        return random.nextInt((max - min) + 1) + min;
    }


    private boolean canReact()
    {
        return ticksUntilNextInteraction() <= 0;
    }

    public int ticksUntilNextInteraction()
    {
        return nextReactionTick - client.getTickCount();
    }


    private NPC getEligibleTarget()
    {
        if (config.npcToFight().isEmpty())
        {
            return null;
        }

        return NpcUtils.getNearest(npc ->
            (npc.getName() != null && (isNameInNpcsToFight(npc.getName()) && !idInNpcBlackList(npc.getId()))) &&
            (((npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)) ||
            (npc.getInteracting() == null && noPlayerFightingNpc(npc)) ||
            (npc.getInteracting() instanceof NPC && noPlayerFightingNpc(npc))) &&

            Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
            (config.allowUnreachable() || (!config.allowUnreachable() && InteractionUtils.isWalkable(npc.getWorldLocation()))) &&
            InteractionUtils.distanceTo2DHypotenuse(npc.getWorldLocation(), startLocation) <= config.maxRange()
        );
    }

    private boolean isNpcEligible(NPC npc)
    {
        if (npc == null)
        {
            return false;
        }

        if (npc.getComposition().getActions() == null)
        {
            return false;
        }

        return (npc.getName() != null && (isNameInNpcsToFight(npc.getName()) && !idInNpcBlackList(npc.getId()))) &&
                (((npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0)) ||
                (npc.getInteracting() == null && noPlayerFightingNpc(npc)) ||
                (npc.getInteracting() instanceof NPC && noPlayerFightingNpc(npc))) &&
                Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
                (config.allowUnreachable() || (!config.allowUnreachable() && InteractionUtils.isWalkable(npc.getWorldLocation()))) &&
                InteractionUtils.distanceTo2DHypotenuse(npc.getWorldLocation(), startLocation) <= config.maxRange();
    }

    private boolean isPlayerEligible(Player player)
    {
        return Arrays.asList(player.getActions()).contains("Attack");
    }

    private boolean noPlayerFightingNpc(NPC npc)
    {
        return PlayerUtils.getNearest(player -> player != client.getLocalPlayer() && player.getInteracting() == npc || npc.getInteracting() == player) == null;
    }

    private boolean targetDeadOrNoTarget()
    {
        NPC interactingWithUs = getEligibleNpcInteractingWithUs();

        if (client.getLocalPlayer().getInteracting() == null && interactingWithUs == null)
        {
            return true;
        }

        if (interactingWithUs != null)
        {
            return false;
        }

        if (client.getLocalPlayer().getInteracting() instanceof NPC)
        {
            NPC npcTarget = (NPC) client.getLocalPlayer().getInteracting();
            int ratio = npcTarget.getHealthRatio();

            return ratio == 0;
        }

        return false;
    }

    private boolean targetDeadOrNoTargetIgnoreAttackingUs()
    {
        if (client.getLocalPlayer().getInteracting() == null)
        {
            return true;
        }

        if (client.getLocalPlayer().getInteracting() instanceof NPC)
        {
            NPC npcTarget = (NPC) client.getLocalPlayer().getInteracting();
            int ratio = npcTarget.getHealthRatio();

            return ratio == 0;
        }

        return false;
    }

    private NPC getEligibleNpcInteractingWithUs()
    {
        return NpcUtils.getNearest((npc) ->
            (npc.getName() != null  && (isNameInNpcsToFight(npc.getName()) && !idInNpcBlackList(npc.getId()))) &&
            (npc.getInteracting() == client.getLocalPlayer() && npc.getHealthRatio() != 0) &&
            Arrays.asList(npc.getComposition().getActions()).contains("Attack") &&
            (config.allowUnreachable() || (!config.allowUnreachable() && InteractionUtils.isWalkable(npc.getWorldLocation()))) &&
            InteractionUtils.distanceTo2DHypotenuse(npc.getWorldLocation(), startLocation) <= config.maxRange()
        );
    }

    @Subscribe
    private void onNpcLootReceived(NpcLootReceived event)
    {
        boolean match = false;
        for (NPC killed : npcsKilled)
        {
            if (event.getNpc() == killed)
            {
                match = true;
                break;
            }
        }

        if (!match)
        {
            return;
        }

        npcsKilled.remove(event.getNpc());

        if (event.getItems().size() > 0)
        {
            List<ItemStack> itemStacks = new ArrayList<>(event.getItems());
            for (ItemStack itemStack : itemStacks)
            {
                if (expectedLootLocations.getOrDefault(itemStack.getLocation(), null) == null)
                {
                    expectedLootLocations.put(itemStack.getLocation(), client.getTickCount());
                }
            }
        }
    }

    private void updatePluginVars()
    {
        if (config.cannonOnCompletion() && !InventoryUtils.contains("Cannon base") && taskEnded)
        {
            if (client.getTickCount() - lastCannonAttempt > 2)
            {
                if (InventoryUtils.getFreeSlots() < 3)
                {
                    List<SlottedItem> food = getFoodItemsNotInBlacklist();
                    List<SlottedItem> karams = InventoryUtils.getAll(karambwanFilter);
                    if (karams != null)
                    {
                        food.addAll(karams);
                    }

                    int slotsNeeded = 4 - InventoryUtils.getFreeSlots();
                    if (food.size() >= slotsNeeded)
                    {
                        for (int i = 0; i < slotsNeeded; i++)
                        {
                            InventoryUtils.interactSlot(food.get(i).getSlot(), "Drop");
                        }
                    }
                    else if (config.teletabOnCompletion() && !tabbed)
                    {
                        SlottedItem teletab = InventoryUtils.getAll(item -> {
                            ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                            return Arrays.asList(composition.getInventoryActions()).contains("Break") && composition.getName().toLowerCase().contains("teleport");
                        }).stream().findFirst().orElseGet(null);

                        if (teletab != null)
                        {
                            InventoryUtils.itemInteract(teletab.getItem().getId(), "Break");
                            tabbed = true;
                        }
                    }
                }
                else
                {
                    TileObject cannon = GameObjectUtils.nearest("Dwarf multicannon");
                    if (cannon != null && !tabbed)
                    {
                        GameObjectUtils.interact(cannon, "Pick-up");
                        lastCannonAttempt = client.getTickCount();
                    }
                }
            }
        }

        if (config.teletabOnCompletion() && taskEnded && !tabbed)
        {
            if ((config.cannonOnCompletion() && InventoryUtils.contains("Cannon base")) || !config.cannonOnCompletion())
            {
                SlottedItem teletab = InventoryUtils.getAll(item -> {
                    ItemComposition composition = client.getItemDefinition(item.getItem().getId());
                    return Arrays.asList(composition.getInventoryActions()).contains("Break") && composition.getName().toLowerCase().contains("teleport");
                }).stream().findFirst().orElseGet(null);

                if (teletab != null)
                {
                    InventoryUtils.itemInteract(teletab.getItem().getId(), "Break");
                    tabbed = true;
                }
            }
        }

        if (client.getLocalPlayer().getAnimation() != -1)
        {
            if ((config.stopUpkeepOnTaskCompletion() && !taskEnded) || !config.stopUpkeepOnTaskCompletion())
            {
                lastTickActive = client.getTickCount();
            }
        }

        if (nextHpToRestoreAt <= 0)
        {
            nextHpToRestoreAt = Math.max(1, config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0));
        }

        if (nextPrayerLevelToRestoreAt <= 0)
        {
            nextPrayerLevelToRestoreAt = Math.max(1, config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0));
        }
    }

    private boolean restorePrimaries()
    {
        boolean ateFood = false;
        boolean restoredPrayer = false;
        boolean brewed = false;
        boolean karambwanned = false;

        if (config.enableHpRestore() && needToRestoreHp())
        {
            final List<SlottedItem> foodItems = getFoodItemsNotInBlacklist();
            if (!foodItems.isEmpty() && canRestoreHp())
            {
                if (!eatingToMaxHp && config.restoreHpToMax())
                {
                    eatingToMaxHp = true;
                }

                final SlottedItem firstItem = foodItems.get(0);
                InventoryUtils.itemInteract(firstItem.getItem().getId(), "Eat");

                ateFood = true;
            }

            if ((!ateFood || config.enableTripleEat()) && canPotUp())
            {
                if (!eatingToMaxHp && config.restoreHpToMax())
                {
                    eatingToMaxHp = true;
                }

                final Item saraBrew = getLowestDosePotion("Saradomin brew");
                if (saraBrew != null)
                {
                    InventoryUtils.itemInteract(saraBrew.getId(), "Drink");
                    brewed = true;
                }
            }
        }

        if (config.enablePrayerRestore() && !brewed && needToRestorePrayer() && canPotUp())
        {
            if (!drinkingToMaxPrayer && config.restorePrayerToMax())
            {
                drinkingToMaxPrayer = true;
            }

            final Item prayerRestore = getLowestDosePrayerRestore();
            if (prayerRestore != null)
            {
                InventoryUtils.itemInteract(prayerRestore.getId(), "Drink");
                restoredPrayer = true;
            }
        }

        if (!restoredPrayer && needToRestoreHp() && canKarambwan())
        {
            boolean shouldEat = false;
            if ((config.enableDoubleEat() || config.enableTripleEat()) && ateFood)
            {
                shouldEat = true;
            }

            if (config.enableHpRestore() && !ateFood && getFoodItemsNotInBlacklist().isEmpty())
            {
                shouldEat = true;
            }

            final SlottedItem karambwan = InventoryUtils.getAll(karambwanFilter).stream().findFirst().orElse(null);

            if (karambwan != null && shouldEat)
            {
                if (!ateFood && !eatingToMaxHp && config.restoreHpToMax())
                {
                    eatingToMaxHp = true;
                }

                InventoryUtils.itemInteract(karambwan.getItem().getId(), "Eat");
                karambwanned = true;
            }
        }

        if (config.stopIfNoFood() && config.enableHpRestore() && needToRestoreHp() && !ateFood && !brewed && !karambwanned)
        {
            if (autoCombatRunning)
            {
                secondaryStatus = "Ran out of food";
                autoCombatRunning = false;
            }
        }

        if (ateFood)
        {
            nextSolidFoodTick = client.getTickCount() + 3;
            nextHpToRestoreAt = config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0);
        }

        if (restoredPrayer)
        {
            nextPotionTick = client.getTickCount() + 3;
            nextPrayerLevelToRestoreAt = config.prayerPointsMin() + (config.prayerRestoreBuffer() > 0 ? random.nextInt(config.prayerRestoreBuffer() + 1) : 0);
        }

        if (brewed)
        {
            nextPotionTick = client.getTickCount() + 3;
            timesBrewedDown++;
            nextHpToRestoreAt = config.minHp() + (config.minHpBuffer() > 0 ? random.nextInt(config.minHpBuffer() + 1) : 0);
        }

        if (karambwanned)
        {
            nextKarambwanTick = client.getTickCount() + 2;
        }

        return ateFood || restoredPrayer || brewed || karambwanned;
    }

    private boolean needToRestoreHp()
    {
        final int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        return currentHp < nextHpToRestoreAt || eatingToMaxHp;
    }

    private boolean hpFull()
    {
        final int maxHp = client.getRealSkillLevel(Skill.HITPOINTS);
        final int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        return currentHp >= (maxHp - config.maxHpBuffer());
    }


    private boolean needToRestorePrayer()
    {
        final int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        return currentPrayer < nextPrayerLevelToRestoreAt || drinkingToMaxPrayer;
    }

    private boolean prayerFull()
    {
        final int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        final int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        return currentPrayer >= (maxPrayer - config.maxPrayerBuffer());
    }

    private boolean restoreStats()
    {
        if (timesBrewedDown > 2 && canPotUp())
        {
            Item restore = getLowestDoseRestore();
            if (restore != null)
            {
                InventoryUtils.itemInteract(restore.getId(), "Drink");

                nextPotionTick = client.getTickCount() + 3;

                timesBrewedDown -= 3;

                if (timesBrewedDown < 0)
                {
                    timesBrewedDown = 0;
                }

                return true;
            }
        }

        return false;
    }

    private boolean restoreBoosts()
    {
        boolean meleeBoosted = false;
        boolean rangedBoosted = false;
        boolean magicBoosted = false;

        final int attackBoost = client.getBoostedSkillLevel(Skill.ATTACK) - client.getRealSkillLevel(Skill.ATTACK);
        final int strengthBoost = client.getBoostedSkillLevel(Skill.STRENGTH) - client.getRealSkillLevel(Skill.STRENGTH);
        final int defenseBoost = client.getBoostedSkillLevel(Skill.DEFENCE) - client.getRealSkillLevel(Skill.DEFENCE);

        Item meleePotionToUse = null;

        final Item combatBoostPotion = getCombatBoostingPotion();

        if (attackBoost < config.minMeleeBoost())
        {
            final Item attackBoostingItem = getAttackBoostingItem();

            if (attackBoostingItem != null)
            {
                meleePotionToUse = attackBoostingItem;
            }
            else if (combatBoostPotion != null)
            {
                meleePotionToUse = combatBoostPotion;
            }
        }
        else if (strengthBoost < config.minMeleeBoost())
        {
            final Item strengthBoostingItem = getStrengthBoostingItem();
            if (strengthBoostingItem != null)
            {
                meleePotionToUse = strengthBoostingItem;
            }
            else if (combatBoostPotion != null)
            {
                meleePotionToUse = combatBoostPotion;
            }
        }
        else if (defenseBoost < config.minMeleeBoost())
        {
            final Item defenseBoostingItem = getDefenseBoostingItem();
            if (defenseBoostingItem != null)
            {
                meleePotionToUse = defenseBoostingItem;
            }
            else if (combatBoostPotion != null)
            {
                meleePotionToUse = combatBoostPotion;
            }
        }

        if (config.enableMeleeUpkeep() && meleePotionToUse != null && canPotUp())
        {
            InventoryUtils.itemInteract(meleePotionToUse.getId(), "Drink");
            nextPotionTick = client.getTickCount() + 3;
            meleeBoosted = true;
        }

        final int rangedBoost = client.getBoostedSkillLevel(Skill.RANGED) - client.getRealSkillLevel(Skill.RANGED);
        if (rangedBoost < config.minRangedBoost() && !meleeBoosted)
        {
            Item rangedPotion = getRangedBoostingItem();
            if (config.enableRangedUpkeep() && rangedPotion != null && canPotUp())
            {
                InventoryUtils.itemInteract(rangedPotion.getId(), "Drink");
                nextPotionTick = client.getTickCount() + 3;
                rangedBoosted = true;
            }
        }

        final int magicBoost = client.getBoostedSkillLevel(Skill.MAGIC) - client.getRealSkillLevel(Skill.MAGIC);
        if (magicBoost < config.minMagicBoost() && !meleeBoosted && !rangedBoosted)
        {
            Item magicPotion = getMagicBoostingPotion();
            Item imbuedHeart = InventoryUtils.getFirstItem("Imbued heart");
            Item saturatedHeart = InventoryUtils.getFirstItem("Saturated heart");
            Item heart = imbuedHeart != null ? imbuedHeart : saturatedHeart;

            if (config.enableMagicUpkeep() && magicPotion != null && canPotUp())
            {
                InventoryUtils.itemInteract(magicPotion.getId(), "Drink");
                nextPotionTick = client.getTickCount() + 3;
                magicBoosted = true;
            }
            else if (config.enableMagicUpkeep() && imbuedHeartTicksLeft() == 0 && heart != null)
            {
                InventoryUtils.itemInteract(heart.getId(), "Invigorate");
                magicBoosted = true;
            }
        }

        return meleeBoosted || rangedBoosted || magicBoosted;
    }

    private boolean canRestoreHp()
    {
        return client.getTickCount() > nextSolidFoodTick;
    }

    private boolean canPotUp()
    {
        return client.getTickCount() > nextPotionTick;
    }

    private boolean canKarambwan()
    {
        return client.getTickCount() > nextKarambwanTick;
    }

    private List<SlottedItem> getFoodItemsNotInBlacklist()
    {
        return InventoryUtils.getAll(foodFilterNoBlacklistItems);
    }

    private Item getAttackBoostingItem()
    {
        Item itemToUse = null;

        final Item attackPot = getLowestDosePotion("Attack potion");
        final Item superAttackPot = getLowestDosePotion("Super attack");
        final Item divineSuperAttack = getLowestDosePotion("Divine super attack potion");

        if (attackPot != null)
        {
            itemToUse = attackPot;
        }
        else if (superAttackPot != null)
        {
            itemToUse = superAttackPot;
        }
        else if (divineSuperAttack != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineSuperAttack;
        }

        return itemToUse;
    }

    private Item getStrengthBoostingItem()
    {
        Item itemToUse = null;

        final Item strengthPot = getLowestDosePotion("Strength potion");
        final Item superStrengthPot = getLowestDosePotion("Super strength");
        final Item divineSuperStrength = getLowestDosePotion("Divine super strength potion");

        if (strengthPot != null)
        {
            itemToUse = strengthPot;
        }
        else if (superStrengthPot != null)
        {
            itemToUse = superStrengthPot;
        }
        else if (divineSuperStrength != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineSuperStrength;
        }

        return itemToUse;
    }

    private Item getDefenseBoostingItem()
    {
        Item itemToUse = null;

        final Item defensePot = getLowestDosePotion("Defense potion");
        final Item superDefensePot = getLowestDosePotion("Super defense");
        final Item divineSuperDefense = getLowestDosePotion("Divine super defense potion");

        if (defensePot != null)
        {
            itemToUse = defensePot;
        }
        else if (superDefensePot != null)
        {
            itemToUse = superDefensePot;
        }
        else if (divineSuperDefense != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineSuperDefense;
        }

        return itemToUse;
    }

    private Item getRangedBoostingItem()
    {
        Item itemToUse = null;

        final Item rangingPot = getLowestDosePotion("Ranging potion");
        final Item divineRangingPot = getLowestDosePotion("Divine ranging potion");
        final Item bastionPot = getLowestDosePotion("Bastion potion");
        final Item divineBastionPot = getLowestDosePotion("Divine bastion potion");

        if (rangingPot != null)
        {
            itemToUse = rangingPot;
        }
        else if (divineRangingPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineRangingPot;
        }
        else if (bastionPot != null)
        {
            itemToUse = bastionPot;
        }
        else if (divineBastionPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineBastionPot;
        }

        return itemToUse;
    }

    private Item getMagicBoostingPotion()
    {
        Item itemToUse = null;

        final Item magicEssence = getLowestDosePotion("Magic essence");
        final Item magicPot = getLowestDosePotion("Magic potion");
        final Item divineMagicPot = getLowestDosePotion("Divine magic potion");
        final Item battleMagePot = getLowestDosePotion("Battlemage potion");
        final Item divineBattleMagePot = getLowestDosePotion("Divine battlemage potion");

        if (magicEssence != null)
        {
            itemToUse = magicEssence;
        }
        else if (magicPot != null)
        {
            itemToUse = magicPot;
        }
        else if (divineMagicPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineMagicPot;
        }
        else if (battleMagePot != null)
        {
            itemToUse = battleMagePot;
        }
        else if (divineBattleMagePot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineBattleMagePot;
        }

        return itemToUse;
    }

    private int imbuedHeartTicksLeft()
    {
        return client.getVarbitValue(Varbits.IMBUED_HEART_COOLDOWN) * 10;
    }

    private Item getCombatBoostingPotion()
    {
        Item itemToUse = null;

        final Item combatPot = getLowestDosePotion("Combat potion");
        final Item superCombatPot = getLowestDosePotion("Super combat potion");
        final Item divineCombatPot = getLowestDosePotion("Divine super combat potion");

        if (combatPot != null)
        {
            itemToUse = combatPot;
        }
        else if (superCombatPot != null)
        {
            itemToUse = superCombatPot;
        }
        else if (divineCombatPot != null && client.getBoostedSkillLevel(Skill.HITPOINTS) > 10)
        {
            itemToUse = divineCombatPot;
        }

        return itemToUse;
    }

    private Item getLowestDosePotion(String name)
    {
        for (int i = 1; i < 5; i++)
        {
            final String fullName = name + "(" + i + ")";

            if (config.foodBlacklist().contains(fullName))
            {
                continue;
            }

            final Item b = InventoryUtils.getFirstItem(fullName);
            if (b != null)
            {
                final ItemComposition itemComposition = client.getItemDefinition(b.getId());
                if ((Arrays.asList(itemComposition.getInventoryActions()).contains("Drink")))
                {
                    return b;
                }
            }
        }
        return null;
    }

    private Item getLowestDoseRestore()
    {
        for (int i = 1; i < 5; i++)
        {
            final String fullName = "Super restore(" + i + ")";

            if (config.foodBlacklist().contains(fullName))
            {
                continue;
            }

            final Item b = InventoryUtils.getFirstItem(fullName);
            if (b != null)
            {
                final ItemComposition itemComposition = client.getItemDefinition(b.getId());
                if ((Arrays.asList(itemComposition.getInventoryActions()).contains("Drink")))
                {
                    return b;
                }
            }
        }
        return null;
    }

    private Item getLowestDosePrayerRestore()
    {
        for (int i = 1; i < 5; i++)
        {
            for (String restoreItem : prayerRestoreNames)
            {
                String fullName = restoreItem + "(" + i + ")";

                if (config.foodBlacklist().contains(fullName))
                {
                    continue;
                }

                Item r = InventoryUtils.getFirstItem(fullName);
                if (r != null)
                {
                    ItemComposition itemComposition = client.getItemDefinition(r.getId());
                    if ((Arrays.asList(itemComposition.getInventoryActions()).contains("Drink")))
                    {
                        return r;
                    }
                }
            }
        }
        return null;
    }

    public int getInactiveTicks()
    {
        return client.getTickCount() - lastTickActive;
    }

    public int ticksUntilNextLootAttempt()
    {
        return nextLootAttempt - client.getTickCount();
    }

    public float getDistanceToStart()
    {
        if (startLocation == null)
        {
            return 0;
        }

        return InteractionUtils.distanceTo2DHypotenuse(startLocation, client.getLocalPlayer().getWorldLocation());
    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        if (config.autocombatHotkey().matches(e))
        {
            clientThread.invoke(() -> {
                lastTickActive = client.getTickCount();
                autoCombatRunning = !autoCombatRunning;
                expectedLootLocations.clear();
                npcsKilled.clear();
                tabbed = false;
                taskEnded = false;

                if (autoCombatRunning)
                {
                    startLocation = client.getLocalPlayer().getWorldLocation();
                }
                else
                {
                    startLocation = null;
                }
            });
        }
    }

    private boolean isStackable(int id)
    {
        ItemComposition composition = client.getItemComposition(id);
        return composition.isStackable();
    }

    private boolean canBuryOrScatter(int id)
    {
        ItemComposition composition = client.getItemComposition(id);
        return Arrays.asList(composition.getInventoryActions()).contains("Bury") || Arrays.asList(composition.getInventoryActions()).contains("Scatter");
    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }
}
