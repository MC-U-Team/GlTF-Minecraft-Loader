package info.u_team.gltf_minecraft_loader.resource_pack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

import info.u_team.gltf_minecraft_loader.GlTFLoaderMod;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public class GlTFResourcesResourcePack implements PackResources {
	
	public static final String NAMESPACE = GlTFLoaderMod.MODID + "_resources";
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
		if (serializer == PackMetadataSection.SERIALIZER) {
			return (T) new PackMetadataSection(new TextComponent("Textures and metadata for the gltf resources"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion()));
			
		}
		return null;
	}
	
	@Override
	public Set<String> getNamespaces(PackType type) {
		if (type == PackType.CLIENT_RESOURCES) {
			return Collections.singleton(NAMESPACE);
		} else {
			return Collections.emptySet();
		}
	}
	
	@Override
	public InputStream getRootResource(String fileName) throws IOException {
		return null;
	}
	
	@Override
	public InputStream getResource(PackType type, ResourceLocation location) throws IOException {
		return null;
	}
	
	@Override
	public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, int maxDepth, Predicate<String> filter) {
		return Collections.emptyList();
	}
	
	@Override
	public boolean hasResource(PackType type, ResourceLocation location) {
		return false;
	}
	
	@Override
	public String getName() {
		return "GlTF Resources";
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public boolean isHidden() {
		return false; // TODO change to true
	}
	
}
