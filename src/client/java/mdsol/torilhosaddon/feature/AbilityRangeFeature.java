package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.feature.base.BaseItemRangeFeature;
import mdsol.torilhosaddon.util.ItemUtils;
import net.minecraft.item.ItemStack;

public class AbilityRangeFeature extends BaseItemRangeFeature {

    public AbilityRangeFeature() {
        super(TorilhosAddonClient.config::isAbilityRangeEnabled);
    }

    @Override
    protected ItemStack getItem() {
        return ItemUtils.getCurrentPlayerAbility();
    }
}
