package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.config.HealthBarConfig;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import mdsol.torilhosaddon.render.CustomRenderLayers;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HealthBarFeature extends BaseToggleableFeature {

    private final VertexConsumerProvider.Immediate immediateConsumer =
            VertexConsumerProvider.immediate(new BufferAllocator(1536));

    public HealthBarFeature() {
        super(TorilhosAddonClient.config.getHealthBarConfig()::isEnabled);
    }

    @Override
    public void init() {
        super.init();
        WorldRenderEvents.LAST.register(this::render);
    }

    private void render(WorldRenderContext context) {
        var player = client.player;

        if (!isEnabledAndInWorld()
                || player == null
                || client.options.getPerspective().isFirstPerson()) {
            return;
        }

        var config = TorilhosAddonClient.config.getHealthBarConfig();
        var healthPercentage = player.getHealth() / player.getMaxHealth();

        if (healthPercentage == 1f && config.hideWhenFull()) {
            return;
        }

        var matrixStack = context.matrixStack();

        if (matrixStack == null) {
            return;
        }

        var tickDelta = context.tickCounter().getTickProgress(false);
        var playerPos = new Vec3d(
                MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()),
                MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()),
                MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()));
        var camera = context.camera();
        var renderPos = playerPos.subtract(camera.getPos());
        var borderWidth = config.getBorderWidth();
        var innerWidth = config.getWidth() - borderWidth * 2;
        var innerHeight = config.getHeight() - borderWidth * 2;
        var scaledInnerWidth = config.getOrientation() == HealthBarConfig.Orientation.HORIZONTAL
                ? innerWidth * healthPercentage
                : innerWidth;
        var scaledInnerHeight = config.getOrientation() == HealthBarConfig.Orientation.VERTICAL
                ? innerHeight * healthPercentage
                : innerHeight;
        var altColor1Tshd = config.getAltColor1Threshold() / 100f;
        var altColor2Tshd = config.getAltColor2Threshold() / 100f;
        var healthBarColor = healthPercentage <= altColor2Tshd
                ? config.getAltColor2()
                : healthPercentage <= altColor1Tshd ? config.getAltColor1() : config.getDefaultColor();

        if (config.getPositionAnchor() == HealthBarConfig.PositionAnchor.PLAYER_CENTER) {
            renderPos = renderPos.add(0, 1, 0);
        }

        if (config.getPositionAnchor() == HealthBarConfig.PositionAnchor.PLAYER_HEAD) {
            renderPos = renderPos.add(0, 2.4, 0);
        }

        matrixStack.push();

        // Set the center of ratation based on the anchor config.
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);

        // Billboarding
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw() + 180));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));

        // Apply the position offset.
        matrixStack.translate(config.getPositionOffsetX(), config.getPositionOffsetY(), config.getPositionOffsetZ());

        var matrix4f = matrixStack.peek().getPositionMatrix();

        drawRectCentered(
                matrix4f, innerWidth + borderWidth * 2, innerHeight + borderWidth * 2, config.getBorderColor(), 0);
        drawRectCentered(matrix4f, innerWidth, innerHeight, config.getBgColor(), 0);
        drawRectStart(matrix4f, scaledInnerWidth, innerWidth, scaledInnerHeight, innerHeight, healthBarColor);

        if (config.showNumber()) {
            var textScale = config.getTextScale();
            matrixStack.scale(textScale, -textScale, textScale);
            drawTextCentered(matrixStack.peek().getPositionMatrix(), config, String.valueOf((int) player.getHealth()));
        }

        matrixStack.pop();
    }

    private void drawRectCentered(Matrix4f matrix, float width, float height, int argb, float z) {
        var halfWidth = width * 0.5f;
        var halfHeight = height * 0.5f;
        var buffer = immediateConsumer.getBuffer(CustomRenderLayers.QUADS);

        // Order is top right, top left, bottom left, bottom right
        buffer.vertex(matrix, halfWidth, halfHeight, z).color(argb);
        buffer.vertex(matrix, -halfWidth, halfHeight, z).color(argb);
        buffer.vertex(matrix, -halfWidth, -halfHeight, z).color(argb);
        buffer.vertex(matrix, halfWidth, -halfHeight, z).color(argb);

        // @todo Need to figure out depth flickering issue to draw all rectangles at once.
        immediateConsumer.draw();
    }

    private void drawRectStart(
            Matrix4f matrix, float width, float fullWidth, float height, float fullHeight, int argb) {
        var halfWidth = fullWidth * 0.5f;
        var halfHeight = fullHeight * 0.5f;
        var top = height - halfHeight;
        var right = width - halfWidth;
        var buffer = immediateConsumer.getBuffer(CustomRenderLayers.QUADS);

        buffer.vertex(matrix, right, top, 0f).color(argb);
        buffer.vertex(matrix, -halfWidth, top, 0f).color(argb);
        buffer.vertex(matrix, -halfWidth, -halfHeight, 0f).color(argb);
        buffer.vertex(matrix, right, -halfHeight, 0f).color(argb);

        immediateConsumer.draw();
    }

    private void drawTextCentered(Matrix4f matrix, HealthBarConfig config, String text) {
        var textRenderer = client.textRenderer;

        textRenderer.draw(
                text,
                -textRenderer.getWidth(text) * 0.45f,
                -textRenderer.fontHeight * 0.4f,
                config.getTextColor(),
                config.drawTextShadow(),
                matrix,
                immediateConsumer,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.pack(15, 15));

        immediateConsumer.draw();
    }
}
