package info.u_team.gltf_minecraft_loader.api;

import net.minecraft.resources.ResourceLocation;

public interface GlTFApi {
	
	void load(ResourceLocation location);
	
	void render(RenderInfo info);
	
}
