package com.mcsrranked.client.anticheat.replay.render;

import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class CustomRenderPhase extends RenderPhase {
   public CustomRenderPhase(String name, Runnable beginAction, Runnable endAction) {
      super(name, beginAction, endAction);
   }

   public static RenderLayer getRenderLayerThroughWall(Identifier texture, boolean overlay) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(ONE_TENTH_ALPHA).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).overlay(overlay ? ENABLE_OVERLAY_COLOR : DISABLE_OVERLAY_COLOR).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).depthTest(LEQUAL_DEPTH_TEST).writeMaskState(ALL_MASK).target(OUTLINE_TARGET).build(true);
      return RenderLayer.of("ghost_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, multiPhaseParameters);
   }

   public static RenderLayer getRenderLayer(Identifier texture, boolean overlay, @Nullable ReplayPlayerEntity.DisplayType displayType) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(ONE_TENTH_ALPHA).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).overlay(overlay ? ENABLE_OVERLAY_COLOR : DISABLE_OVERLAY_COLOR).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).writeMaskState(ALL_MASK).target(displayType != null && displayType != ReplayPlayerEntity.DisplayType.NORMAL ? OUTLINE_TARGET : MAIN_TARGET).build(true);
      return RenderLayer.of("ghost_entity_translucent_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, false, true, multiPhaseParameters);
   }

   public static RenderLayer getBootsRenderLayer(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).overlay(ENABLE_OVERLAY_COLOR).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(ENABLE_LIGHTMAP).writeMaskState(COLOR_MASK).target(OUTLINE_TARGET).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).depthTest(LEQUAL_DEPTH_TEST).alpha(ONE_TENTH_ALPHA).cull(DISABLE_CULLING).build(true);
      return RenderLayer.of("ghost_entity_boots", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, multiPhaseParameters);
   }

   public static RenderLayer getBoatRenderLayer(Identifier texture) {
      RenderLayer.MultiPhaseParameters multiPhaseParameters = RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false, false)).alpha(ONE_TENTH_ALPHA).diffuseLighting(ENABLE_DIFFUSE_LIGHTING).transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).lightmap(ENABLE_LIGHTMAP).depthTest(LEQUAL_DEPTH_TEST).writeMaskState(ALL_MASK).target(MAIN_TARGET).build(true);
      return RenderLayer.of("ghost_entity_boat", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, multiPhaseParameters);
   }
}
