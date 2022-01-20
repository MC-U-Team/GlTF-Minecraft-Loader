package info.u_team.gltf_minecraft_loader;

import info.u_team.gltf_minecraft_loader.api.GlTFModelLoaderApi;
import net.minecraftforge.fml.common.Mod;

@Mod(GlTFMinecraftLoaderMod.MODID)
public class GlTFMinecraftLoaderMod {
	
	public static final String MODID = "gltfminecraftloader";
	
	public static GlTFModelLoaderApi API;
	
	public GlTFMinecraftLoaderMod() {
		API = new GlTFModelLoader();
	}
	
}
