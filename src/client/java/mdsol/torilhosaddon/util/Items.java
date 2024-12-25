package mdsol.torilhosaddon.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Items {

    private static final int CMDC_NEXUS = 300;

    public static boolean isWeapon(@NotNull ItemStack stack) {
        return stack.getItem().toString().endsWith("_shovel");
    }

    public static boolean isAbility(@NotNull ItemStack stack) {
        return stack.getItem().toString().endsWith("_hoe");
    }

    public static boolean isNexus(@NotNull ItemStack stack) {
        var customModelDataComponent = stack.getComponents().get(DataComponentTypes.CUSTOM_MODEL_DATA);

        if (customModelDataComponent == null) {
            return false;
        }

        return customModelDataComponent.value() == CMDC_NEXUS;
    }

    public static ItemStack getCurrentPlayerWeapon() {
        return getCurrentPlayerItemOfType(Items::isWeapon);
    }

    public static ItemStack getCurrentPlayerAbility() {
        return getCurrentPlayerItemOfType(Items::isAbility);
    }

    public static float getCurrentPlayerAbilityCooldown() {
        var stack = getCurrentPlayerAbility();
        var client = MinecraftClient.getInstance();

        if (stack.isEmpty() || client.player == null) {
            return -1f;
        }

        return client.player.getItemCooldownManager().getCooldownProgress(stack, 0f);
    }

    public static float getItemCooldown(@NotNull ItemStack stack) {
        var client = MinecraftClient.getInstance();

        if (stack.isEmpty() || client.player == null) {
            return -1f;
        }

        return client.player.getItemCooldownManager().getCooldownProgress(stack, 0f);
    }

    private static ItemStack getCurrentPlayerItemOfType(Function<ItemStack, Boolean> typeChecker) {
        var client = MinecraftClient.getInstance();

        if (client.player == null) {
            return ItemStack.EMPTY;
        }

        var mainHandStack = client.player.getMainHandStack();
        var stackToUse = typeChecker.apply(mainHandStack) ? mainHandStack : client.player.getOffHandStack();

        if (typeChecker.apply(stackToUse)) {
            return stackToUse;
        }

        return ItemStack.EMPTY;
    }
}
