package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.feature.base.BaseItemRangeFeature;
import mdsol.torilhosaddon.util.Items;
import net.minecraft.item.ItemStack;

public class WeaponRangeFeature extends BaseItemRangeFeature {

    public WeaponRangeFeature() {
        super(TorilhosAddon.CONFIG.keys.showWeaponRange);
        setActiveColor(0xFF0000);
        setInactiveColor(0xFF7070);
    }

    @Override
    protected ItemStack getItem() {
        return Items.getCurrentPlayerWeapon();
    }
}
