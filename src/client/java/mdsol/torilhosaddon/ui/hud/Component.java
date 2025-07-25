package mdsol.torilhosaddon.ui.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public interface Component {

    void draw(DrawContext context, RenderTickCounter tickCounter);
}
