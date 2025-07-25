package mdsol.torilhosaddon.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class WorldUtils {
    public static final RegistryKey<World> realmRegistryKey =
            RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("realm2"));
}
