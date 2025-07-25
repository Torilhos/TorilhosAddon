package mdsol.torilhosaddon.render;

import mdsol.torilhosaddon.TorilhosAddon;
import net.minecraft.client.render.RenderLayer;

public class CustomRenderLayers {
    private CustomRenderLayers() {}

    public static final RenderLayer.MultiPhase QUADS = RenderLayer.of(
            TorilhosAddon.id("quads").toString(),
            1536,
            false,
            true,
            CustomRenderPipelines.QUADS,
            RenderLayer.MultiPhaseParameters.builder().build(false));

    public static final RenderLayer.MultiPhase TRIANGLES = RenderLayer.of(
            TorilhosAddon.id("triangles").toString(),
            1536,
            false,
            true,
            CustomRenderPipelines.TRIANGLES,
            RenderLayer.MultiPhaseParameters.builder().build(false));
}
