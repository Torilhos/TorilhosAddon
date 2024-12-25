package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.feature.base.BaseItemRangeFeature;
import mdsol.torilhosaddon.util.Items;
import net.minecraft.item.ItemStack;

public class AbilityRangeFeature extends BaseItemRangeFeature {

    public AbilityRangeFeature() {
        super(TorilhosAddon.CONFIG.keys.showAbilityRange);
    }

    @Override
    protected ItemStack getItem() {
        return Items.getCurrentPlayerAbility();
    }
}
