package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.feature.base.BaseItemRangeFeature;
import mdsol.torilhosaddon.util.ItemUtils;
import net.minecraft.item.ItemStack;

public class WeaponRangeFeature extends BaseItemRangeFeature {

    public WeaponRangeFeature() {
        super(TorilhosAddonClient.config::isWeaponRangeEnabled);
        activeColor = 0xFF0000;
        inactiveColor = 0xFF7070;
    }

    @Override
    protected ItemStack getItem() {
        return ItemUtils.getCurrentPlayerWeapon();
    }
}
