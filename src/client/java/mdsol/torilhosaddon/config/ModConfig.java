package mdsol.torilhosaddon.config;

import mdsol.torilhosaddon.TorilhosAddon;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = TorilhosAddon.MOD_ID)
public class ModConfig implements ConfigData {
    boolean enableHoldToSwing = true;
    boolean enableHealthBar = true;
    boolean enableWeaponRange = true;
    boolean enableAbilityRange = false;
    boolean enableAbilityCooldown = false;
    boolean enableEventBossTracker = true;
    boolean enableAutomaticPerspective = false;

    public boolean isHoldToSwingEnabled() {
        return enableHoldToSwing;
    }

    public boolean isHealthBarEnabled() {
        return enableHealthBar;
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
