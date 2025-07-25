package mdsol.torilhosaddon.feature;

import java.util.List;
import mdsol.torilhosaddon.config.ModConfig;
import mdsol.torilhosaddon.feature.base.Feature;
import mdsol.torilhosaddon.feature.bosstracker.EventBossTrackerHudFeature;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.util.ActionResult;

public class Features {
    public static final List<Feature> FEATURES = List.of(
            new ConfigScreenFeature(),
            new HoldToSwingFeature(),
            new HealthBarFeature(),
            new WeaponRangeFeature(),
            new AbilityRangeFeature(),
            new AbilityCooldownHudFeature(),
            new PerspectiveFeature(),
            new EventBossTrackerHudFeature());

    private Features() {}

    public static ActionResult onConfigChanged(ConfigHolder<ModConfig> ignoredConfigHolder, ModConfig modConfig) {
        for (var feature : FEATURES) {
            feature.onConfigChanged(modConfig);
        }

        return ActionResult.SUCCESS;
    }
}
