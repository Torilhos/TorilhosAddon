package mdsol.torilhosaddon.ui.hud;

import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.ui.hud.base.BaseHudItem;
import mdsol.torilhosaddon.util.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class AbilityCooldownHudItem extends BaseHudItem {

    private static final Surface SURFACE_COOLING_DOWN = Surface.flat(0xA0101010).and(Surface.outline(0x90FFFFFF));
    private static final Surface SURFACE_READY = Surface.flat(0xA0FF55FF).and(Surface.outline(0x90FFFFFF));

    private final int cooldownBarHeight = 12;
    private FlowLayout cooldownContainer;
    private ItemComponent itemComponent;
    private BoxComponent cooldownBox;
    private ItemStack trackedAbility;
    private float previousCooldownProgress;

    public AbilityCooldownHudItem() {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL, TorilhosAddon.id("hud_off_hand_cooldown"));
    }

    @Override
    public void enable() {
        super.enable();
        positioning(Positioning.relative(50, 50));
        padding(Insets.left(40));
        allowOverflow(true);
        trackedAbility = ItemStack.EMPTY;
        previousCooldownProgress = -1;
    }

    @Override
    public void disable() {
        super.disable();
        trackedAbility = ItemStack.EMPTY;
        previousCooldownProgress = -1;
    }

    @Override
    protected void parentUpdate(float delta, int mouseX, int mouseY) {
        super.parentUpdate(delta, mouseX, mouseY);

        var player = client.player;

        if (player == null) {
            return;
        }

        var cooldownProgress = client.player.getItemCooldownManager().getCooldownProgress(trackedAbility, 0);

        if (cooldownProgress != previousCooldownProgress) {
            previousCooldownProgress = cooldownProgress;
            cooldownContainer.surface(cooldownProgress > 0 ? SURFACE_COOLING_DOWN : SURFACE_READY);
            cooldownBox.verticalSizing(Sizing.fixed(MathHelper.floor(cooldownBarHeight * cooldownProgress)));
        }

        var heldAbility = Items.getCurrentPlayerAbility();

        if (heldAbility.isEmpty()) {
            // Show no item in the hud.
            itemComponent.stack(heldAbility);
            return;
        }

        if (!heldAbility.equals(trackedAbility)) {
            // If stack has changed, we update it in the hud.
            trackedAbility = heldAbility;
            itemComponent.stack(heldAbility);
        }
    }

    @Override
    public void init() {
        var player = client.player;

        if (player == null) {
            return;
        }

        cooldownBox = Components.box(Sizing.fixed(4), Sizing.fixed(16))
                .fill(true)
                .color(Color.ofArgb(0x60FFFFFF));

        var padding = 1;

        cooldownContainer = Containers.verticalFlow(Sizing.content(), Sizing.fixed(cooldownBarHeight + padding * 2));
        cooldownContainer.verticalAlignment(VerticalAlignment.BOTTOM);
        cooldownContainer.surface(SURFACE_READY);
        cooldownContainer.padding(Insets.of(padding));
        cooldownContainer.child(cooldownBox);

        itemComponent = Components.item(ItemStack.EMPTY);
        itemComponent.sizing(Sizing.fixed(10)).margins(Insets.left(1));

        child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(cooldownContainer)
                        .child(itemComponent)
        );
    }
}
