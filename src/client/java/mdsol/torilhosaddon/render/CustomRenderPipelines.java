package mdsol.torilhosaddon.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;
import mdsol.torilhosaddon.TorilhosAddon;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.VertexFormats;

public class CustomRenderPipelines {
    private CustomRenderPipelines() {}

    public static final RenderPipeline QUADS =
            RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withLocation(TorilhosAddon.id("pipeline/quads"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build());

    public static final RenderPipeline TRIANGLES =
            RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
                    .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLE_STRIP)
                    .withLocation(TorilhosAddon.id("pipeline/triangles"))
                    .build());
}
