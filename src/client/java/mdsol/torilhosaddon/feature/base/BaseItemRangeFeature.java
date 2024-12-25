package mdsol.torilhosaddon.feature.base;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.config.Option;
import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.util.Items;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class BaseItemRangeFeature extends BaseToggleableFeature {

    private static final Pattern ITEM_RANGE_PATTERN = Pattern.compile("Range: (\\d+(\\.\\d+)?)");
    private ItemStack previousStack = ItemStack.EMPTY;
    private float range = -1;
    private boolean isHoldingItem = false;
    private int colorOpacity = 0x80000000;
    private int inactiveColor = 0xFFFFFF;
    private int activeColor = 0xAA00AA;

    protected BaseItemRangeFeature(Option.Key configKey) {
        super(configKey);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        WorldRenderEvents.LAST.register(this::render);
    }

    public void setColorOpacity(int colorOpacity) {
        this.colorOpacity = colorOpacity;
    }

    public void setInactiveColor(int inactiveColor) {
        this.inactiveColor = inactiveColor;
    }

    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
    }

    protected void tick(MinecraftClient client) {
        if (!isEnabled()) {
            return;
        }

        var stack = getItem();

        if (stack.isEmpty()) {
            isHoldingItem = false;
            return;
        }

        isHoldingItem = true;

        if (stack.equals(previousStack)) {
            return;
        }

        TorilhosAddon.LOGGER.info(stack.getName().toString());

        previousStack = stack;
        range = getItemRange(stack);
    }

    protected void render(WorldRenderContext context) {
        if (!isEnabled()
                || !isHoldingItem
                || range < 0
                || client.player == null
                || client.player.hasVehicle()
        ) {
            return;
        }

        var matrixStack = context.matrixStack();

        if (matrixStack == null) {
            return;
        }

        var camera = context.camera();
        var tickDelta = context.tickCounter().getTickDelta(false);
        var playerPos = new Vec3d(
                MathHelper.lerp(tickDelta, client.player.lastRenderX, client.player.getX()),
                MathHelper.lerp(tickDelta, client.player.lastRenderY, client.player.getY()),
                MathHelper.lerp(tickDelta, client.player.lastRenderZ, client.player.getZ())
        );
        var renderPos = playerPos.subtract(camera.getPos());
        var color = Items.getItemCooldown(previousStack) > 0 ? inactiveColor : activeColor;

        matrixStack.push();

        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        var tessellator = Tessellator.getInstance();
        var circleBuffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        var positionMatrix = matrixStack.peek().getPositionMatrix();
        var angleCount = 6f;

        for (var i = 0; i <= angleCount; i++) {
            var angle = MathHelper.PI * 0.4f + MathHelper.PI * 0.2f * (i / angleCount) - MathHelper.PI * 0.5f;
            var vx = range * MathHelper.sin(angle);
            var vz = range * MathHelper.cos(angle);
            var opacity = colorOpacity;

            if (i == 0 || i == angleCount) {
                opacity = 0;
            }

            circleBuffer.vertex(positionMatrix, vx, 0, vz).color(color + opacity);
            circleBuffer.vertex(positionMatrix, vx, 0.2f, vz).color(color + opacity);
        }

        BufferRenderer.drawWithGlobalProgram(circleBuffer.end());
        RenderSystem.disableBlend();
        // We intentionally do not call .disableDepthTest() here because it caused weird rendering bugs sometimes

        matrixStack.pop();
    }

    protected abstract ItemStack getItem();

    private float getItemRange(@NotNull ItemStack stack) {
        var loreComponent = stack.getComponents().get(DataComponentTypes.LORE);

        if (loreComponent == null) {
            return -1;
        }

        var rangeMatcher = ITEM_RANGE_PATTERN.matcher("");

        for (var line : loreComponent.lines()) {
            rangeMatcher.reset(line.getString());

            if (rangeMatcher.find()) {
                return Float.parseFloat(rangeMatcher.group(1));
            }
        }

        return -1;
    }
}
