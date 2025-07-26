package mdsol.torilhosaddon.config;

import mdsol.torilhosaddon.TorilhosAddon;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = TorilhosAddon.MOD_ID)
public class ModConfig implements ConfigData {
    boolean enableHoldToSwing = true;
    boolean enableWeaponRange = true;
    boolean enableAbilityRange = false;
    boolean enableAbilityCooldown = false;
    boolean enableEventBossTracker = true;
    boolean enableAutomaticPerspective = false;

    @ConfigEntry.Gui.CollapsibleObject
    HealthBarConfig healthBarConfig = new HealthBarConfig();

    public boolean isHoldToSwingEnabled() {
        return enableHoldToSwing;
    }

    public HealthBarConfig getHealthBarConfig() {
        return healthBarConfig;
    }

    public boolean isWeaponRangeEnabled() {
        return enableWeaponRange;
    }

    public boolean isAbilityRangeEnabled() {
        return enableAbilityRange;
    }

    public boolean isAbilityCooldownEnabled() {
        return enableAbilityCooldown;
    }

    public boolean isEventBossTrackerEnabled() {
        return enableEventBossTracker;
    }

    public boolean isAutomaticPerspectiveEnabled() {
        return enableAutomaticPerspective;
    }
}
