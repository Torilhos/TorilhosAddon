package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import mdsol.torilhosaddon.render.CustomRenderLayers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HealthBarFeature extends BaseToggleableFeature {

    private final VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(new BufferAllocator(1536));

    public HealthBarFeature() {
        super(TorilhosAddonClient.config::isHealthBarEnabled);
    }

    @Override
    public void init() {
        super.init();
        WorldRenderEvents.LAST.register(this::render);
    }

    private void render(WorldRenderContext context) {
        var player = client.player;

        if (!isEnabledAndInGame()
                || player == null
                || client.options.getPerspective().isFirstPerson()) {
            return;
        }

        var healthPercentage = player.getHealth() / player.getMaxHealth();

        if (healthPercentage == 1f) {
            return;
        }

        var matrixStack = context.matrixStack();

        if (matrixStack == null) {
            return;
        }

        var camera = context.camera();
        var tickDelta = context.tickCounter().getTickProgress(false);
        var playerPos = new Vec3d(
                MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()),
                MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()),
                MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()));
        var renderPos = playerPos.subtract(camera.getPos());
        var barWidth = 1.2f;
        var barHeight = 0.22f;
        var barBorder = 0.02f;
        var scaledBarWidth = barWidth * healthPercentage;
        var healthBarColor =
                healthPercentage >= 0.75f ? 0xB040CC40 : healthPercentage <= 0.4f ? 0xB0CC3030 : 0xB0FFCC40;

        matrixStack.push();

        // Use the player position as the center of rotation
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);

        // Billboarding
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw() + 180));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));

        // Place bar slightly below the player
        matrixStack.translate(0, -0.4f, 0);

        var matrix = matrixStack.peek().getPositionMatrix();

        drawRectCentered(matrix, barWidth, barHeight, 0xB0FFFFFF, -0.0011f);
        drawRectCentered(matrix, barWidth - barBorder * 2, barHeight - barBorder * 2, 0xB0000000, -0.001f);
        drawRectLeft(
                matrix,
                scaledBarWidth - barBorder * 2,
                barWidth - barBorder * 2,
                barHeight - barBorder * 2,
                healthBarColor);
        drawTextCentered(matrixStack, client.textRenderer, String.valueOf((int) player.getHealth()));

        matrixStack.pop();
    }

    private void drawRectCentered(Matrix4f matrix, float width, float height, int argb, float z) {
        var halfWidth = width * 0.5f;
        var halfHeight = height * 0.5f;
        var buffer = vcp.getBuffer(CustomRenderLayers.QUADS);

        // Order is top right, top left, bottom left, bottom right
        buffer.vertex(matrix, halfWidth, halfHeight, z).color(argb);
        buffer.vertex(matrix, -halfWidth, halfHeight, z).color(argb);
        buffer.vertex(matrix, -halfWidth, -halfHeight, z).color(argb);
        buffer.vertex(matrix, halfWidth, -halfHeight, z).color(argb);

        // @todo Need to figure out depth flickering issue to draw all rectangles at once.
        vcp.draw();
    }

    private void drawRectLeft(Matrix4f matrix, float width, float fullWidth, float height, int argb) {
        var halfWidth = fullWidth * 0.5f;
        var halfHeight = height * 0.5f;
        var buffer = vcp.getBuffer(CustomRenderLayers.QUADS);

        buffer.vertex(matrix, width - halfWidth, halfHeight, 0f).color(argb);
        buffer.vertex(matrix, -halfWidth, halfHeight, 0f).color(argb);
        buffer.vertex(matrix, -halfWidth, -halfHeight, 0f).color(argb);
        buffer.vertex(matrix, width - halfWidth, -halfHeight, 0f).color(argb);

        vcp.draw();
    }

    private void drawTextCentered(MatrixStack matrixStack, TextRenderer textRenderer, String text) {
        matrixStack.push();
        matrixStack.scale(0.02f, -0.02f, 0.02f);

        textRenderer.draw(
                text,
                -textRenderer.getWidth(text) * 0.5f,
                -textRenderer.fontHeight * 0.4f,
                0xFFFFFFFF,
                false,
                matrixStack.peek().getPositionMatrix(),
                this.vcp,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.pack(15, 15));

        this.vcp.draw();

        matrixStack.pop();
    }
}
