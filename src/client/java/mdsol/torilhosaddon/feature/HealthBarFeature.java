package mdsol.torilhosaddon.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class HealthBarFeature extends BaseToggleableFeature {

    // TODO: search for other uses of .immediate for good practices
    private final VertexConsumerProvider.Immediate vertexConsumer = VertexConsumerProvider.immediate(new BufferAllocator(1536));

    public HealthBarFeature() {
        super(TorilhosAddon.CONFIG.keys.showHealthBar);
        WorldRenderEvents.LAST.register(this::render);
    }

    private void render(WorldRenderContext context) {
        if (!isEnabled()
                || client.player == null
                || client.options.getPerspective().isFirstPerson()) {
            return;
        }

        var healthPercentage = client.player.getHealth() / client.player.getMaxHealth();

        if (healthPercentage == 1f) {
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
        var barWidth = 1.2f;
        var barHeight = 0.22f;
        var barBorder = 0.02f;
        var scaledBarWidth = barWidth * healthPercentage;
        var healthBarColor = healthPercentage >= 0.75f
                             ? 0xB040CC40
                             : healthPercentage <= 0.4f
                               ? 0xB0CC3030
                               : 0xB0FFCC40;

        matrixStack.push();

        // Use the player position as the center of rotation
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);

        // Billboarding
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw() + 180));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-camera.getPitch()));

        // Place bar slightly below the player
        matrixStack.translate(0, -0.4f, 0);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        var matrix = matrixStack.peek().getPositionMatrix();
        drawRectCentered(matrix, barWidth, barHeight, 0xB0FFFFFF);
        drawRectCentered(matrix, barWidth - barBorder * 2, barHeight - barBorder * 2, 0xB0000000);
        drawRectLeft(matrix, scaledBarWidth - barBorder * 2, barWidth - barBorder * 2, barHeight - barBorder * 2, healthBarColor);

        drawTextCentered(matrixStack, client.textRenderer, String.valueOf((int) client.player.getHealth()));

        matrixStack.pop();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private void drawRectCentered(Matrix4f matrix, float width, float height, int argb) {
        var halfWidth = width * 0.5f;
        var halfHeight = height * 0.5f;
        // TODO: Try Quad draw mode?
        var buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        // Order is top right, top left,  bottom left, bottom right
        buffer.vertex(matrix, halfWidth, halfHeight, 0).color(argb);
        buffer.vertex(matrix, -halfWidth, halfHeight, 0).color(argb);
        buffer.vertex(matrix, -halfWidth, -halfHeight, 0).color(argb);
        buffer.vertex(matrix, halfWidth, -halfHeight, 0).color(argb);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawRectLeft(Matrix4f matrix, float width, float fullWidth, float height, int argb) {
        var halfWidth = fullWidth * 0.5f;
        var halfHeight = height * 0.5f;
        var buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, width - halfWidth, halfHeight, 0).color(argb);
        buffer.vertex(matrix, -halfWidth, halfHeight, 0).color(argb);
        buffer.vertex(matrix, -halfWidth, -halfHeight, 0).color(argb);
        buffer.vertex(matrix, width - halfWidth, -halfHeight, 0).color(argb);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private void drawTextCentered(@NotNull MatrixStack matrixStack, @NotNull TextRenderer textRenderer, String text) {
        matrixStack.push();
        matrixStack.scale(0.02f, -0.02f, 0.02f);

        textRenderer.draw(
                text,
                -textRenderer.getWidth(text) * 0.5f,
                -textRenderer.fontHeight * 0.4f,
                0xFFFFFF,
                false,
                matrixStack.peek().getPositionMatrix(),
                this.vertexConsumer,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.pack(15, 15)
        );

        this.vertexConsumer.draw();

        matrixStack.pop();
    }
}
