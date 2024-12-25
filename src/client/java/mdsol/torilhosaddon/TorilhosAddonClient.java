package mdsol.torilhosaddon;

import mdsol.torilhosaddon.events.SetPerspectiveCallback;
import mdsol.torilhosaddon.feature.*;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.option.Perspective;

public class TorilhosAddonClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        new AbilityCooldownHudFeature();
        new AbilityRangeFeature();
        new EventBossTrackerHudFeature();
        new FullBrightFeature();
        new HealthBarFeature();
        new HoldToSwingFeature();
        new IngameConfigFeature();
        new NexusKeyFeature();
        new NightVisionFeature();
        new WeaponRangeFeature();

        SetPerspectiveCallback.EVENT.register(perspective -> {
            // TODO: Refactor to add dedicated hotkey instead and move into a feature.
            // Disable front view perspective.
            if (TorilhosAddon.CONFIG.disableFrontViewPerspective() && perspective == Perspective.THIRD_PERSON_FRONT) {
                return Perspective.FIRST_PERSON;
            }

            return perspective;
        });
    }
}