package mdsol.torilhosaddon.ui.hud;

import java.util.*;
import mdsol.torilhosaddon.feature.bosstracker.TrackedBoss;
import mdsol.torilhosaddon.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.GlobalPos;

public class TrackedBossesComponent extends BaseComponent {
    private static final int GAP_SECTION = 4;
    private static final int GAP_LINE = 2;
    private static final int GAP_ITEMS = 1;
    private static final int PADDING = 6;
    private static final int X = 4;
    private static final int Y = 4;
    private static final int COLOR_BOX_BG = 0xA0101010;
    private static final int COLOR_BOX_BORDER = 0xA0AA00AA;
    private static final int ITEM_SIDE = 16;
    private static final String DISTANCE_MARKER = "♦";
    private final Map<TrackedBoss.State, Set<TrackedBoss>> trackedBossesByState;
    private final Map<String, BossLine> bossLines = new HashMap<>();

    public TrackedBossesComponent(Map<TrackedBoss.State, Set<TrackedBoss>> trackedBossesByState) {
        super();
        this.trackedBossesByState = trackedBossesByState;
    }

    @Override
    public void draw(DrawContext context, RenderTickCounter tickCounter) {
        var aliveBosses = trackedBossesByState.get(TrackedBoss.State.ALIVE);
        var bossesWithPortal = trackedBossesByState.get(TrackedBoss.State.DEFEATED_PORTAL_ACTIVE);
        var defeatedBosses = trackedBossesByState.get(TrackedBoss.State.DEFEATED);

        if (aliveBosses == null || bossesWithPortal == null || defeatedBosses == null) {
            return;
        }

        var totalLineCount = aliveBosses.size() + bossesWithPortal.size() + defeatedBosses.size();
        var activeBossLineCount = aliveBosses.size() + bossesWithPortal.size();

        if (totalLineCount == 0) {
            return;
        }

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        var headingSectionHeight = getSectionHeight(1, false);
        var activeBossesSectionHeight = getSectionHeight(activeBossLineCount, ITEM_SIDE, true);
        var defeatedBossesSectionHeight = getSectionHeight(defeatedBosses.size(), true);
        var contentHeight = headingSectionHeight + activeBossesSectionHeight + defeatedBossesSectionHeight;
        var headingText = Text.literal("Bosses %d/10".formatted(defeatedBosses.size()));
        var contentWidth = textRenderer.getWidth(headingText);

        contentWidth = Math.max(contentWidth, buildBossGroup(aliveBosses));
        contentWidth = Math.max(contentWidth, buildBossGroup(bossesWithPortal));
        contentWidth = Math.max(contentWidth, buildBossGroup(defeatedBosses));

        var boxWidth = contentWidth + PADDING * 2;
        var boxHeight = contentHeight + PADDING * 2;

        context.fill(X, Y, X + boxWidth, Y + boxHeight, COLOR_BOX_BG);
        context.drawBorder(X, Y, boxWidth, boxHeight, COLOR_BOX_BORDER);

        var lineY = Y + PADDING;

        context.drawText(textRenderer, headingText, X + PADDING, lineY, COLOR_BOX_BORDER, true);
        lineY += textRenderer.fontHeight;

        if (activeBossLineCount > 0) {
            lineY += GAP_SECTION;
        }

        for (var boss : aliveBosses) {
            lineY = drawNonDefeatedBossLine(context, boss, lineY);
        }

        for (var boss : bossesWithPortal) {
            lineY = drawNonDefeatedBossLine(context, boss, lineY);
        }

        if (!defeatedBosses.isEmpty()) {
            lineY += GAP_SECTION - GAP_LINE;

            for (var boss : defeatedBosses) {
                var bossLine = getBossLine(boss);
                context.drawText(textRenderer, bossLine.text, X + PADDING, lineY, 0xFFFFFFFF, true);
                lineY += textRenderer.fontHeight + GAP_LINE;
            }
        }
    }

    private int getSectionHeight(int lineCount, boolean hasGap) {
        return getSectionHeight(lineCount, MinecraftClient.getInstance().textRenderer.fontHeight, hasGap);
    }

    private int getSectionHeight(int lineCount, int lineHeight, boolean hasGap) {
        var contentHeight = lineCount * lineHeight + Math.max((lineCount - 1) * GAP_LINE, 0);

        if (contentHeight > 0 && hasGap) {
            return contentHeight + GAP_SECTION;
        }

        return contentHeight;
    }

    private String getFormattedBossName(String bossName) {
        return bossName.substring(0, 3);
    }

    private String getFormattedPlayerName(String playerName, boolean truncate) {
        return " [" + (truncate ? playerName.substring(0, 3) : playerName) + "]";
    }

    private String getFormattedTimer(int timer) {
        return " (%d)".formatted(timer / 20);
    }

    private Text getDistanceMarkerText(double distance) {
        var labelColor = distance <= 2
                ? Formatting.GREEN
                : distance <= 4
                        ? Formatting.YELLOW
                        : distance <= 6 ? Formatting.GOLD : distance <= 8 ? Formatting.RED : Formatting.DARK_RED;
        return Text.literal(DISTANCE_MARKER).formatted(labelColor);
    }

    private int buildBossLine(TrackedBoss boss) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        var isChatFocused = client.inGameHud.getChatHud().isChatFocused();
        var bossState = boss.getState();
        var lineWidth = 0;
        var bossLabel = Text.empty();
        var style = Style.EMPTY;

        if (bossState != TrackedBoss.State.DEFEATED) {
            bossLabel.append(getDistanceMarkerText(boss.getDistanceMarkerValue()));
            bossLabel.append(" ").append(getFormattedBossName(boss.getData().label));

            var calledPlayerName = boss.getCalledPlayerName();
            if (calledPlayerName.isPresent()) {
                bossLabel.append(getFormattedPlayerName(calledPlayerName.get(), !isChatFocused));
                style = style.withFormatting(Formatting.GREEN);
            }

            lineWidth += (ITEM_SIDE + GAP_ITEMS) * 2;
        }

        if (bossState == TrackedBoss.State.DEFEATED_PORTAL_ACTIVE) {
            bossLabel.append(getFormattedTimer(boss.getPortalTimer()));
            style = style.withFormatting(Formatting.RED);
        }

        if (bossState == TrackedBoss.State.DEFEATED) {
            bossLabel.append(boss.getData().label);
            style = style.withFormatting(Formatting.DARK_RED);
        }

        var bossLine = getBossLine(boss);
        bossLine.text = bossLabel.setStyle(style);

        lineWidth += textRenderer.getWidth(bossLabel);

        return lineWidth;
    }

    private int buildBossGroup(Set<TrackedBoss> trackedBosses) {
        var contentWidth = 0;

        for (var boss : trackedBosses) {
            var lineWidth = buildBossLine(boss);

            if (lineWidth > contentWidth) {
                contentWidth = lineWidth;
            }
        }

        return contentWidth;
    }

    private int drawNonDefeatedBossLine(DrawContext context, TrackedBoss boss, int y) {
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        var bossLine = getBossLine(boss);
        var lineX = X + PADDING;

        context.drawItem(bossLine.bossIconItem, lineX, y);
        lineX += ITEM_SIDE + GAP_ITEMS;
        context.drawItem(bossLine.compassItem, lineX, y);
        lineX += ITEM_SIDE + GAP_ITEMS;
        context.drawText(
                textRenderer, bossLine.text, lineX, y + ITEM_SIDE / 2 - textRenderer.fontHeight / 2, 0xFFFFFFFF, true);

        return y + ITEM_SIDE + GAP_LINE;
    }

    private BossLine getBossLine(TrackedBoss trackedBoss) {
        var bossLine = bossLines.get(trackedBoss.getData().label);

        if (bossLine != null) {
            return bossLine;
        }

        bossLine = new BossLine(trackedBoss);
        bossLines.put(trackedBoss.getData().label, bossLine);
        return bossLine;
    }

    private static class BossLine {
        public final ItemStack compassItem = new ItemStack(Items.COMPASS);
        public final ItemStack bossIconItem = new ItemStack(Items.CARROT_ON_A_STICK);
        public Text text = Text.empty();

        public BossLine(TrackedBoss trackedBoss) {
            this.bossIconItem.set(DataComponentTypes.ITEM_MODEL, trackedBoss.getData().modelIdentifier);
            var bossGlobalPos = new GlobalPos(WorldUtils.realmRegistryKey, trackedBoss.getData().spawnPosition);
            var lodestoneComponent = new LodestoneTrackerComponent(Optional.of(bossGlobalPos), false);
            this.compassItem.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneComponent);
            this.compassItem.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        }
    }
}
