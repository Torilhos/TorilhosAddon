package mdsol.torilhosaddon.ui;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.feature.bosstracker.EventBossTrackerHudFeature;
import mdsol.torilhosaddon.feature.bosstracker.TrackedBoss;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class QuickBossTeleportScreen extends Screen {
    private static final Text TITLE_TEXT =
            Text.translatable("text.quick-boss-teleport-screen.torilhos-addon.label.teleportTo");
    private static final Text NO_PLAYERS_TEXT =
            Text.translatable("text.quick-boss-teleport-screen.torilhos-addon.label.noPlayersFound");
    private static final int BUTTON_WIDTH = 120;
    private static final int BUTTON_HEIGHT = 20;
    private static final int TEXT_BOTTOM_MARGIN = 6;
    private final EventBossTrackerHudFeature eventBossTrackerHudFeature;
    private int contentHeight = 0;
    private int contentWidth = 0;
    private boolean hasAvailablePlayers = false;

    public QuickBossTeleportScreen(EventBossTrackerHudFeature eventBossTrackerHudFeature) {
        super(Text.literal(TITLE_TEXT.getString()));
        this.eventBossTrackerHudFeature = eventBossTrackerHudFeature;
    }

    @Override
    protected void init() {
        super.init();

        var availableBosses = eventBossTrackerHudFeature.getBossesWithCalledPlayers();
        var buttonGap = 2;
        var textContentHeight = textRenderer.fontHeight + TEXT_BOTTOM_MARGIN;
        var playerButtonsContentHeight =
                availableBosses.length * 20 + Math.max(availableBosses.length - 1, 0) * buttonGap;
        hasAvailablePlayers = availableBosses.length > 0;

        contentWidth = Math.max(textRenderer.getWidth(TITLE_TEXT), BUTTON_WIDTH);
        contentHeight = textContentHeight + playerButtonsContentHeight + BUTTON_HEIGHT;

        if (!hasAvailablePlayers) {
            contentHeight += textContentHeight;
        }

        var y = height / 2 - contentHeight / 2 + textContentHeight;
        var x = width / 2 - contentWidth / 2;

        for (var boss : availableBosses) {
            var playerName = boss.getCalledPlayerName();

            if (playerName.isEmpty()) {
                continue;
            }

            this.addDrawableChild(createTpButton(x, y, boss, playerName.get()));

            y += BUTTON_HEIGHT + buttonGap;
        }

        if (!hasAvailablePlayers) {
            y += textRenderer.fontHeight + TEXT_BOTTOM_MARGIN;
        }

        var cancelButton = ButtonWidget.builder(Text.translatable("gui.cancel"), btn -> MinecraftClient.getInstance()
                        .setScreen(null))
                .dimensions(x, y, 60, BUTTON_HEIGHT)
                .build();
        this.addDrawableChild(cancelButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        var x = width / 2 - contentWidth / 2;
        var y = height / 2 - contentHeight / 2;

        context.drawText(textRenderer, TITLE_TEXT, x, y, 0xFFFFFFFF, true);

        if (!hasAvailablePlayers) {
            y += textRenderer.fontHeight + TEXT_BOTTOM_MARGIN;
            context.drawText(textRenderer, NO_PLAYERS_TEXT, x, y, 0xFF888888, true);
        }
    }

    private ButtonWidget createTpButton(int x, int y, TrackedBoss trackedBoss, String playerName) {
        var bossState = trackedBoss.getState();
        var displayedPlayerName = playerName.length() > 8 ? playerName.substring(0, 8) + "..." : playerName;
        var textColor = bossState == TrackedBoss.State.ALIVE ? Formatting.GREEN : Formatting.RED;
        var buttonText = Text.literal(trackedBoss.getData().label.substring(0, 3) + " > " + displayedPlayerName)
                .formatted(textColor);

        return ButtonWidget.builder(buttonText, btn -> this.sendTpCommand(playerName))
                .dimensions(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
    }

    private void sendTpCommand(String playerName) {
        var client = MinecraftClient.getInstance();
        var networkHandler = client.getNetworkHandler();

        if (networkHandler == null) {
            TorilhosAddon.LOGGER.error("Network Handler is null");
            return;
        }

        client.setScreen(null);
        networkHandler.sendChatCommand("tp " + playerName);
    }
}
