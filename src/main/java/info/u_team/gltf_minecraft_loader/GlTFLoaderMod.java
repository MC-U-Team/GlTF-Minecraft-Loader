package info.u_team.gltf_minecraft_loader;

import info.u_team.gltf_minecraft_loader.api.GlTFApi;
import info.u_team.gltf_minecraft_loader.init.GlTFLoaderClientInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(GlTFLoaderMod.MODID)
public class GlTFLoaderMod {
	
	public static final String MODID = "gltfloader";
	
	public static GlTFApi API;
	
	public GlTFLoaderMod() {
		API = new GlTFApiImpl();
		
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> GlTFLoaderClientInit::init);
	}
	
}
