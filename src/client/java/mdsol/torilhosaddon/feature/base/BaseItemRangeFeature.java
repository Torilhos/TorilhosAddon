package mdsol.torilhosaddon.feature.base;

import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import mdsol.torilhosaddon.render.CustomRenderLayers;
import mdsol.torilhosaddon.util.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public abstract class BaseItemRangeFeature extends BaseToggleableFeature {

    private static final Pattern ITEM_RANGE_PATTERN = Pattern.compile("Range: (\\d+(\\.\\d+)?)");
    private final VertexConsumerProvider.Immediate vcp = VertexConsumerProvider.immediate(new BufferAllocator(1536));
    protected int inactiveColor = 0xFFFFFF;
    protected int activeColor = 0xAA00AA;
    private ItemStack previousStack = ItemStack.EMPTY;
    private float range = -1;
    private boolean isHoldingItem = false;
    private int colorOpacity = 0x80000000;

    protected BaseItemRangeFeature(BooleanSupplier configToggleSupplier) {
        super(configToggleSupplier);
    }

    @Override
    public void init() {
        super.init();
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        WorldRenderEvents.LAST.register(this::render);
    }

    protected void tick(MinecraftClient client) {
        if (!isEnabledAndInWorld()) {
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

        previousStack = stack;
        range = getItemRange(stack);
    }

    protected void render(WorldRenderContext context) {
        var player = client.player;

        if (!isEnabledAndInWorld() || !isHoldingItem || range < 0 || player == null || player.hasVehicle()) {
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
        var color = ItemUtils.getItemCooldown(previousStack) > 0 ? inactiveColor : activeColor;

        matrixStack.push();
        matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));

        var positionMatrix = matrixStack.peek().getPositionMatrix();
        var angleCount = 6f;
        var buffer = vcp.getBuffer(CustomRenderLayers.TRIANGLES);

        for (var i = 0; i <= angleCount; i++) {
            var angle = MathHelper.PI * 0.4f + MathHelper.PI * 0.2f * (i / angleCount) - MathHelper.PI * 0.5f;
            var vx = range * MathHelper.sin(angle);
            var vz = range * MathHelper.cos(angle);
            var opacity = colorOpacity;

            if (i == 0 || i == angleCount) {
                opacity = 0;
            }

            buffer.vertex(positionMatrix, vx, 0, vz).color(color + opacity);
            buffer.vertex(positionMatrix, vx, 0.2f, vz).color(color + opacity);
        }

        vcp.draw();

        matrixStack.pop();
    }

    protected abstract ItemStack getItem();

    private float getItemRange(ItemStack stack) {
        var loreComponent = stack.getComponents().get(DataComponentTypes.LORE);

        if (loreComponent == null) {
            return -1;
        }

        var rangeMatcher = ITEM_RANGE_PATTERN.matcher("");

        for (var line : loreComponent.lines()) {
            rangeMatcher.reset(line.getString());

            if (rangeMatcher.find()) {
                var rangeString = rangeMatcher.group(1);
                return rangeString != null ? Float.parseFloat(rangeString) : -1;
            }
        }

        return -1;
    }
}
