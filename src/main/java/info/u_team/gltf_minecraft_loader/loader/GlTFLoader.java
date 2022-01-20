package info.u_team.gltf_minecraft_loader.loader;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public class GlTFLoader implements IModelLoader<GlTFModel> {
	
	public static final GlTFLoader INSTANCE = new GlTFLoader();
	
	private ResourceManager resourceManager;
	private final Map<GlTFModelSettings, GlTFModel> modelCache = new HashMap<>();
	
	protected GlTFLoader() {
		resourceManager = Minecraft.getInstance().getResourceManager();
	}
	
	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
		modelCache.clear();
	}
	
	@Override
	public GlTFModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
		if (!modelContents.has("model")) {
			throw new RuntimeException("Missing model property for a gltf model");
		}
		
		final ResourceLocation modelLocation = new ResourceLocation(modelContents.get("model").getAsString());
		return loadModel(new GlTFModelSettings(modelLocation));
	}
	
	private GlTFModel loadModel(GlTFModelSettings settings) {
		return modelCache.computeIfAbsent(settings, unused -> {
			try (final Resource resource = resourceManager.getResource(settings.getModel())) {
				return new GlTFModel(resource, settings);
			} catch (final Exception ex) {
				throw new RuntimeException("Could not read GlTF model", ex);
			}
		});
	}
	
	public static class GlTFModelSettings {
		
		private final ResourceLocation model;
		
		public GlTFModelSettings(ResourceLocation model) {
			this.model = model;
		}
		
		public ResourceLocation getModel() {
			return model;
		}
	}
}
