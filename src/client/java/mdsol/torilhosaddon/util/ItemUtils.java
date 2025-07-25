package mdsol.torilhosaddon.util;

import java.util.function.Function;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class ItemUtils {

    public static boolean isWeapon(ItemStack stack) {
        return stack.getItem().toString().endsWith("_shovel");
    }

    public static boolean isAbility(ItemStack stack) {
        return stack.getItem().toString().endsWith("_hoe");
    }

    public static ItemStack getCurrentPlayerWeapon() {
        return getCurrentPlayerItemOfType(ItemUtils::isWeapon);
    }

    public static ItemStack getCurrentPlayerAbility() {
        return getCurrentPlayerItemOfType(ItemUtils::isAbility);
    }

    public static float getItemCooldown(ItemStack stack) {
        var client = MinecraftClient.getInstance();

        if (stack.isEmpty() || client.player == null) {
            return -1f;
        }

        return client.player.getItemCooldownManager().getCooldownProgress(stack, 0f);
    }

    private static ItemStack getCurrentPlayerItemOfType(Function<ItemStack, Boolean> typeChecker) {
        var client = MinecraftClient.getInstance();
        var player = client.player;

        if (player == null) {
            return ItemStack.EMPTY;
        }

        var mainHandStack = player.getMainHandStack();
        var stackToUse = typeChecker.apply(mainHandStack) ? mainHandStack : player.getOffHandStack();

        if (typeChecker.apply(stackToUse)) {
            return stackToUse;
        }

        return ItemStack.EMPTY;
    }
}
