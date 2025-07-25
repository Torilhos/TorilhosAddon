package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import mdsol.torilhosaddon.util.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class AbilityCooldownHudFeature extends BaseToggleableFeature {

    private static final Identifier ID = TorilhosAddon.id("hud/ability_cooldown");
    private static final int BAR_COLOR_COOLING_DOWN = 0x60FFFFFF;
    private static final int BAR_COLOR_READY = 0xA0FF55FF;
    private static final int BAR_COLOR_BORDER = 0xA0FFFFFF;
    private static final int BAR_COLOR_BG = 0xA0101010;
    private static final int BAR_WIDTH = 5;
    private static final int BAR_HEIGHT = 14;
    private static final int BAR_LEFT_OFFSET = 18;
    private ItemStack trackedAbility = ItemStack.EMPTY;
    private ItemStack displayedAbility = ItemStack.EMPTY;
    private float cooldownProgress = 0;

    public AbilityCooldownHudFeature() {
        super(TorilhosAddonClient.config::isAbilityCooldownEnabled);
    }

    @Override
    public void init() {
        super.init();
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, ID, this::renderHud);
    }

    private void tick(MinecraftClient client) {
        var player = client.player;

        if (!isEnabledAndInGame() || player == null) {
            return;
        }

        cooldownProgress = player.getItemCooldownManager().getCooldownProgress(trackedAbility, 0);

        var heldAbility = ItemUtils.getCurrentPlayerAbility();

        if (heldAbility.isEmpty()) {
            displayedAbility = heldAbility;
            return;
        }

        displayedAbility = heldAbility;

        if (!heldAbility.equals(trackedAbility)) {
            // If stack has changed, we update it in the hud.
            trackedAbility = heldAbility;
        }
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        if (!isEnabledAndInGame()) {
            return;
        }

        var windowWidth = context.getScaledWindowWidth();
        var windowHeight = context.getScaledWindowHeight();
        var halfWindowWidth = windowWidth / 2;
        var halfWindowHeight = windowHeight / 2;
        var barTopOffset = BAR_HEIGHT / -2;
        var barColor = cooldownProgress > 0 ? BAR_COLOR_COOLING_DOWN : BAR_COLOR_READY;
        var scaledHeight = MathHelper.ceil((BAR_HEIGHT - 2) * cooldownProgress);
        var x1 = BAR_LEFT_OFFSET + halfWindowWidth;
        var y1 = barTopOffset + halfWindowHeight;

        context.drawBorder(x1, y1, BAR_WIDTH, BAR_HEIGHT, BAR_COLOR_BORDER);

        var x2 = x1 + BAR_WIDTH - 1;
        var y2 = y1 + BAR_HEIGHT - 1;
        x1 += 1;
        y1 += 1;

        context.fill(x1, y1, x2, y2, BAR_COLOR_BG);
        context.fill(x1, y1 + scaledHeight, x2, y2, barColor);
        context.drawItem(displayedAbility, x1 + BAR_WIDTH + 1, y1);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        trackedAbility = ItemStack.EMPTY;
        cooldownProgress = 0;
    }
}
