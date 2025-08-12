package mdsol.torilhosaddon.feature.bosstracker;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.event.HandledScreenRemovedCallback;
import mdsol.torilhosaddon.event.NetworkHandlerOnGameJoin;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import mdsol.torilhosaddon.ui.QuickBossTeleportScreen;
import mdsol.torilhosaddon.ui.hud.TrackedBossesComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class EventBossTrackerHudFeature extends BaseToggleableFeature {
    private static final KeyBinding OPEN_QUICK_BOSS_TELEPORT_KEY = new KeyBinding(
            "key.torilhos-addon.openQuickBossTeleport",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            TorilhosAddon.MOD_CATEGORY);

    private static final Identifier ID = TorilhosAddon.id("hud/event_boss_tracker");

    private static final Pattern BOSS_DEFEATED_MESSAGE_PATTERN = Pattern.compile("^(\\w+) has been defeated");
    private static final Pattern BOSS_SPAWNED_MESSAGE_PATTERN =
            Pattern.compile("^(\\w+) has spawned at ([0-9.-]+), ([0-9.-]+), ([0-9.-]+)");
    private static final Pattern POTENTIAL_BOSS_MESSAGE_PATTERN = Pattern.compile("^\\[(\\w+)]");
    private static final Pattern BOSS_ITEM_NAME_PATTERN = Pattern.compile("^» \\[(\\w+)] «");

    private final Map<String, TrackedBoss> trackedBosses = new HashMap<>();
    private final Map<TrackedBoss.State, Set<TrackedBoss>> trackedBossesByState = new HashMap<>();
    private final TrackedBossesComponent trackedBossesComponent = new TrackedBossesComponent(trackedBossesByState);
    private int distanceUpdateTimer = 0;

    public EventBossTrackerHudFeature() {
        super(TorilhosAddonClient.config::isEventBossTrackerEnabled);

        for (var state : TrackedBoss.State.values()) {
            trackedBossesByState.put(state, new HashSet<>());
        }
    }

    @Override
    public void init() {
        super.init();
        KeyBindingHelper.registerKeyBinding(OPEN_QUICK_BOSS_TELEPORT_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        ClientReceiveMessageEvents.GAME.register(this::onGameMessage);
        NetworkHandlerOnGameJoin.EVENT.register(this::onGameJoin);
        HandledScreenRemovedCallback.EVENT.register(this::onScreenClosed);

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ID, (context, tickCounter) -> {
            if (!isEnabled()) {
                return;
            }

            trackedBossesComponent.draw(context, tickCounter);
        });
    }

    public TrackedBoss[] getBossesWithCalledPlayers() {
        return trackedBosses.values().stream()
                .filter(trackedBoss -> trackedBoss.getState() != TrackedBoss.State.DEFEATED
                        && trackedBoss.getCalledPlayerName().isPresent())
                .toArray(TrackedBoss[]::new);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        clearTrackedBosses();
    }

    private void tick(MinecraftClient client) {
        if (!isEnabledAndInWorld()) {
            return;
        }

        while (OPEN_QUICK_BOSS_TELEPORT_KEY.wasPressed()) {
            client.setScreen(new QuickBossTeleportScreen(this));
        }

        distanceUpdateTimer = ++distanceUpdateTimer % 20;

        for (var boss : trackedBosses.values()) {
            if (boss.getState() == TrackedBoss.State.DEFEATED_PORTAL_ACTIVE && boss.decrementPortalTimer() < 0) {
                setTrackedBossState(boss, TrackedBoss.State.DEFEATED);
            }

            if (boss.getState() != TrackedBoss.State.DEFEATED && distanceUpdateTimer == 0 && client.player != null) {
                boss.setDistanceMarkerValue(Math.floor(
                        Vec3d.of(client.player.getBlockPos()).distanceTo(Vec3d.of(boss.getData().spawnPosition))
                                * 0.008));
            }
        }
    }

    private void onGameMessage(Text message, boolean overlay) {
        if (!isEnabledAndInWorld()) {
            return;
        }

        var messageString = message.getString();

        // Skip messages that do not contain relevant information.
        if (!messageString.matches("^(\\w|\\[|\\().+")) {
            return;
        }

        // Try to match potential boss messages in the format: "[<boss-name>] <message>".
        var potentialBossName = getSingleMatchResult(POTENTIAL_BOSS_MESSAGE_PATTERN, messageString);
        if (potentialBossName.isPresent()) {
            var bossInfo = updateTrackedBossByName(potentialBossName.get(), TrackedBoss.State.ALIVE);

            if (bossInfo.isEmpty()) {
                // Not a valid boss name.
                return;
            }

            var playerCallPattern = bossInfo.get().getData().playerCallPattern;
            var bossCallMatcher = playerCallPattern.matcher(messageString);

            if (bossCallMatcher.find()) {
                bossInfo.get().setCalledPlayerName(bossCallMatcher.group(1));
            }

            // Return early given there is no more new information to extract from this message.
            return;
        }

        var defeatedBossName = getSingleMatchResult(BOSS_DEFEATED_MESSAGE_PATTERN, messageString);
        if (defeatedBossName.isPresent()) {
            updateTrackedBossByName(defeatedBossName.get(), TrackedBoss.State.DEFEATED_PORTAL_ACTIVE)
                    .ifPresent(TrackedBoss::resetPortalTimer);
            return;
        }

        var spawnedBossName = getSingleMatchResult(BOSS_SPAWNED_MESSAGE_PATTERN, messageString);
        if (spawnedBossName.isPresent()) {
            var trackedBoss = trackedBosses.get(spawnedBossName.get());

            if (trackedBoss != null) {
                trackedBoss.setCalledPlayerName(null);
            }

            updateTrackedBossByName(spawnedBossName.get(), TrackedBoss.State.ALIVE);
        }
    }

    private void onGameJoin() {
        if (isEnabledAndInWorld()) {
            clearTrackedBosses();
        }
    }

    private void onScreenClosed(Screen screen) {
        if (!isEnabledAndInWorld()) {
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
            var bossName = getSingleMatchResult(BOSS_ITEM_NAME_PATTERN, stackName);

            if (bossName.isEmpty()) {
                continue;
            }

            var loreData = stack.getComponents().get(DataComponentTypes.LORE);

            if (loreData == null) {
                continue;
            }

            var loreString = loreData.lines().stream().map(Text::getString).collect(Collectors.joining(" "));

            if (loreString.contains("This boss is alive")) {
                updatedBosses.put(bossName.get(), TrackedBoss.State.ALIVE);
            } else if (loreString.contains("This boss has been defeated")) {
                updatedBosses.put(bossName.get(), TrackedBoss.State.DEFEATED);
            } else if (loreString.contains("This boss has not spawned")) {
                removeTrackedBossByName(bossName.get());
            }
        }

        if (!updatedBosses.isEmpty()) {
            updatedBosses.forEach(this::updateTrackedBossByName);

            if (client.player != null) {
                client.player.sendMessage(Text.translatable("text.torilhos-addon.bossTrackerUpdated"), true);
            }
        }
    }

    private Optional<String> getSingleMatchResult(Pattern pattern, String input) {
        var matcher = pattern.matcher(input);

        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.ofNullable(matcher.group(1));
    }

    private void clearTrackedBosses() {
        trackedBosses.clear();

        for (var state : TrackedBoss.State.values()) {
            trackedBossesByState.put(state, new HashSet<>());
        }
    }

    private void addBossToState(TrackedBoss trackedBoss, TrackedBoss.State state) {
        var bossesByState = trackedBossesByState.get(state);

        if (bossesByState == null) {
            return;
        }

        bossesByState.add(trackedBoss);
    }

    private void removeBossFromState(TrackedBoss trackedBoss, TrackedBoss.State state) {
        var bossesByState = trackedBossesByState.get(state);

        if (bossesByState == null) {
            return;
        }

        bossesByState.remove(trackedBoss);
    }

    private void setTrackedBossState(TrackedBoss trackedBoss, TrackedBoss.State newState) {
        removeBossFromState(trackedBoss, trackedBoss.getState());
        addBossToState(trackedBoss, newState);
        trackedBoss.setState(newState);
    }

    private void addTrackedBoss(TrackedBoss trackedBoss) {
        trackedBosses.put(trackedBoss.getData().label, trackedBoss);
        addBossToState(trackedBoss, trackedBoss.getState());
    }

    private void removeTrackedBossByName(String trackedBossName) {
        var removedBoss = trackedBosses.remove(trackedBossName);

        if (removedBoss == null) {
            return;
        }

        removeBossFromState(removedBoss, removedBoss.getState());
    }

    private Optional<TrackedBoss> updateTrackedBossByName(String bossName, TrackedBoss.State state) {
        var existing = trackedBosses.get(bossName);

        if (existing != null) {
            setTrackedBossState(existing, state);
            return Optional.of(existing);
        }

        var newBoss = BossData.fromString(bossName).map(bossData -> new TrackedBoss(bossData, state));
        newBoss.ifPresent(this::addTrackedBoss);

        return newBoss;
    }
}
