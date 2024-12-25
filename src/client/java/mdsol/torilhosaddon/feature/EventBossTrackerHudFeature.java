package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.events.ClientJoinWorldCallback;
import mdsol.torilhosaddon.events.HandledScreenRemovedCallback;
import mdsol.torilhosaddon.events.NetworkHandlerOnGameJoin;
import mdsol.torilhosaddon.feature.base.BaseHudFeature;
import mdsol.torilhosaddon.feature.model.BossData;
import mdsol.torilhosaddon.feature.model.TrackedBoss;
import mdsol.torilhosaddon.ui.hud.BossTrackerHud;
import mdsol.torilhosaddon.ui.screen.QuickBossTeleportScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventBossTrackerHudFeature extends BaseHudFeature {

    private static final KeyBinding OPEN_QUICK_BOSS_TELEPORT_KEY = new KeyBinding(
            "key.torilhos-addon.openQuickBossTeleport",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "category.torilhos-addon"
    );

    private static final Pattern BOSS_DEFEATED_MESSAGE_PATTERN = Pattern.compile("^(\\w+) has been defeated");
    private static final Pattern BOSS_SPAWNED_MESSAGE_PATTERN = Pattern.compile("^(\\w+) has spawned at ([0-9.-]+), ([0-9.-]+), ([0-9.-]+)");
    private static final Pattern ONYX_PORTAL_OPEN_MESSAGE_PATTERN = Pattern.compile("^A portal to Onyx's Castle has opened");
    private static final Pattern POTENTIAL_BOSS_MESSAGE_PATTERN = Pattern.compile("^\\[(\\w+)]");
    private static final Pattern BOSS_ITEM_NAME_PATTERN = Pattern.compile("^» \\[(\\w+)] «");

    private final Map<String, TrackedBoss> trackedBosses = new HashMap<>();
    private final BossTrackerHud bossTrackerHudItem;
    private int distanceUpdateTimer = 0;

    public EventBossTrackerHudFeature() {
        super(TorilhosAddon.CONFIG.keys.showEventBossTracker, new BossTrackerHud());
        bossTrackerHudItem = (BossTrackerHud) getHudItem();

        KeyBindingHelper.registerKeyBinding(OPEN_QUICK_BOSS_TELEPORT_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ClientJoinWorldCallback.EVENT.register(this::onWorldJoin);
        ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
        NetworkHandlerOnGameJoin.EVENT.register(this::onGameJoin);
        HandledScreenRemovedCallback.EVENT.register(this::onScreenClosed);
    }

    public TrackedBoss[] getAliveBosses() {
        return trackedBosses.values().stream()
                .filter(bossInfo -> bossInfo.getState() != TrackedBoss.State.DEFEATED)
                .toArray(TrackedBoss[]::new);
    }

    @Override
    protected void onDisable() {
        super.onDisable();

        if (trackedBosses != null) {
            trackedBosses.clear();
        }
    }

    private void tick(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        while (OPEN_QUICK_BOSS_TELEPORT_KEY.wasPressed()) {
            client.setScreen(new QuickBossTeleportScreen(this));
        }

        distanceUpdateTimer = ++distanceUpdateTimer % 20;

        for (var boss : trackedBosses.values()) {
            if (boss.getState() == TrackedBoss.State.JUST_DEFEATED && boss.decrementPortalTimer() < 0) {
                boss.setState(TrackedBoss.State.DEFEATED);
            }

            if (boss.getState() != TrackedBoss.State.DEFEATED && distanceUpdateTimer == 0 && client.player != null) {
                boss.setDistanceMarkerValue(
                        Math.floor(
                                Vec3d.of(client.player.getBlockPos())
                                        .distanceTo(Vec3d.of(boss.getData().spawnPosition)) * 0.008
                        )
                );
            }
        }
    }

    private void onGameMessage(@NotNull Text message, boolean overlay) {
        if (!isEnabled()) {
            return;
        }

        var messageString = message.getString();

        // Skip messages that do not contain relevant information.
        if (!messageString.matches("^(\\w|\\[|\\().+")) {
            return;
        }

        // Try to match potential boss messages in the format: "[<boss-name>] <message>".
        var bossNameMatcher = POTENTIAL_BOSS_MESSAGE_PATTERN.matcher(messageString);
        if (bossNameMatcher.find()) {
            var potentialBossName = bossNameMatcher.group(1);
            var bossInfoOpt = updateTrackedBoss(potentialBossName, TrackedBoss.State.ALIVE);

            if (bossInfoOpt.isEmpty()) {
                // Not a valid boss name.
                return;
            }

            var bossInfo = bossInfoOpt.get();
            var playerCallPattern = bossInfo.getData().playerCallPattern;

            if (playerCallPattern == null) {
                return;
            }

            var bossCallMatcher = playerCallPattern.matcher(messageString);

            if (bossCallMatcher.find()) {
                bossInfo.setCalledPlayerName(bossCallMatcher.group(1));
            }

            // Return early given there is no more new information to extract from this message.
            return;
        }

        var bossDefeatedMatcher = BOSS_DEFEATED_MESSAGE_PATTERN.matcher(messageString);
        if (bossDefeatedMatcher.find()) {
            updateTrackedBoss(bossDefeatedMatcher.group(1), TrackedBoss.State.JUST_DEFEATED)
                    .ifPresent(TrackedBoss::resetPortalTimer);

            return;
        }

        var bossSpawnedMatcher = BOSS_SPAWNED_MESSAGE_PATTERN.matcher(messageString);
        if (bossSpawnedMatcher.find()) {
            updateTrackedBoss(bossSpawnedMatcher.group(1), TrackedBoss.State.ALIVE);
            return;
        }

        var onyxPortalOpenMatcher = ONYX_PORTAL_OPEN_MESSAGE_PATTERN.matcher(messageString);
        if (onyxPortalOpenMatcher.find()) {
            clearTrackedBosses();
        }
    }

    private void onGameJoin() {
        clearTrackedBosses();
    }

    private void onWorldJoin(ClientWorld world) {
        bossTrackerHudItem.onWorldJoin(world);
    }

    private void onScreenClosed(Screen screen) {
        if (!isEnabled()) {
            return;
        }

        if (!(screen instanceof GenericContainerScreen containerScreen)) {
            return;
        }

        var inventory = containerScreen.getScreenHandler().getInventory();

        if (!(inventory instanceof SimpleInventory simpleInventory)) {
            return;
        }

        var updatedBosses = new HashMap<String, TrackedBoss.State>();

        for (var stack : simpleInventory.getHeldStacks()) {
            var stackName = stack.getName().getString();
            var bossItemNameMatcher = BOSS_ITEM_NAME_PATTERN.matcher(stackName);

            // Match boss items.
            if (!bossItemNameMatcher.find()) {
                continue;
            }

            var loreData = stack.getComponents().get(DataComponentTypes.LORE);

            if (loreData == null) {
                continue;
            }

            var loreString = loreData
                    .lines()
                    .stream()
                    .map(Text::getString)
                    .collect(Collectors.joining(" "));

            if (loreString.contains("This boss is alive")) {
                updatedBosses.put(bossItemNameMatcher.group(1), TrackedBoss.State.ALIVE);
            } else if (loreString.contains("This boss has been defeated")) {
                updatedBosses.put(bossItemNameMatcher.group(1), TrackedBoss.State.DEFEATED);
            } else if (loreString.contains("This boss has not spawned")) {
                trackedBosses.remove(bossItemNameMatcher.group(1));
            }
        }

        if (!updatedBosses.isEmpty()) {
            updatedBosses.forEach(this::updateTrackedBoss);

            if (client.player != null) {
                client.player.sendMessage(Text.translatable("text.torilhos-addon.bossTrackerUpdated"), true);
            }
        }
    }

    private void clearTrackedBosses() {
        trackedBosses.clear();
        bossTrackerHudItem.clear();
    }

    private @NotNull Optional<TrackedBoss> updateTrackedBoss(String bossName, TrackedBoss.State state) {
        var existing = trackedBosses.get(bossName);

        if (existing != null) {
            existing.setState(state);
            return Optional.of(existing);
        }

        var newBoss = BossData.fromString(bossName).map(bossData -> new TrackedBoss(bossData, state));
        newBoss.ifPresent(boss -> {
            trackedBosses.put(boss.getData().label, boss);
            bossTrackerHudItem.addTrackedBoss(boss);
        });

        return newBoss;
    }
}