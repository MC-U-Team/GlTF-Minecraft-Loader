package info.u_team.gltf_minecraft_loader;

import static info.u_team.gltf_parser.GlTFUtil.GLTF_COMPONENT_TYPE_UINT;
import static info.u_team.gltf_parser.GlTFUtil.equalsOptional;
import static info.u_team.gltf_parser.GlTFUtil.getOptionalEntry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;

import info.u_team.gltf_parser.GlTFParser;
import info.u_team.gltf_parser.generated.gltf.Accessor;
import info.u_team.gltf_parser.generated.gltf.GlTF;
import info.u_team.gltf_parser.generated.gltf.Node;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

public class GlTFModel implements IModelGeometry<GlTFModel> {
	
	private final GlTFParser parser;
	private final GlTF gltf;
	private final Collection<Material> materials;
	private final Node node;
	
	private static final HashMap<String, Integer> STRING_TO_ELEMENT = new HashMap<>();
	private static final HashMap<String, Integer> STRING_TO_SIZE = new HashMap<>();
	private static final HashMap<String, float[]> STRING_TO_DEFAULT = new HashMap<>();
	private static final int POSITION;
	
	public GlTFModel(GlTFParser parser, GlTF gltf, Collection<Material> materials) {
		this(parser, gltf, materials, null);
	}
	
	public GlTFModel(GlTFParser parser, GlTF gltf, Collection<Material> materials, Node node) {
		this.parser = parser;
		this.materials = materials;
		this.gltf = gltf;
		this.node = node;
	}
	
	static {
		final HashMap<String, String> elements = Maps.newHashMap();
		elements.put("Position", "POSITION");
		elements.put("UV0", "TEXCOORD_0");
		elements.put("UV1", "TEXCOORD_1");
		elements.put("UV2", "TEXCOORD_1");
		elements.put("Normal", "NORMAL");
		elements.put("Color", "COLOR");
		elements.put("Padding", "__UNUSED");
		
		STRING_TO_SIZE.put("POSITION", 3);
		STRING_TO_SIZE.put("TEXCOORD_0", 2);
		STRING_TO_SIZE.put("NORMAL", 3);
		STRING_TO_SIZE.put("COLOR", 4);
		STRING_TO_SIZE.put("TEXCOORD_1", 2);
		STRING_TO_SIZE.put("__UNUSED", 0);
		
		STRING_TO_DEFAULT.put("POSITION", new float[] { 0f, 0f, 0f, 1f });
		STRING_TO_DEFAULT.put("TEXCOORD_0", new float[] { 0f, 0f, 0f, 0f });
		STRING_TO_DEFAULT.put("NORMAL", new float[] { 0f, 0f, 0f, 1f });
		STRING_TO_DEFAULT.put("COLOR", new float[] { 1f, 1f, 1f, 1f });
		STRING_TO_DEFAULT.put("TEXCOORD_1", new float[] { 0f, 0f, 0f, 0f });
		STRING_TO_DEFAULT.put("__UNUSED", new float[] { 0f, 0f, 0f, 0f });
		
		final ImmutableList<String> formats = DefaultVertexFormat.BLOCK.getElementAttributeNames();
		for (int j = 0; j < formats.size(); j++) {
			final String element = formats.get(j);
			if (elements.containsKey(element))
				STRING_TO_ELEMENT.put(elements.get(element), j);
		}
		POSITION = STRING_TO_ELEMENT.get("POSITION");
		STRING_TO_ELEMENT.remove("POSITION");
	}
	
	private void loadFromIndex(final Map<String, ByteBuffer> attributes, final Supplier<IVertexConsumer> vertexConsumerSupplier, final ByteBuffer vertexBuffer, final int indexCount, final IntSupplier indexBuffer) {
		final int rest = indexCount % 6;
		final int divider = (indexCount - rest) / 6;
		for (int j = 0; j < divider; j++) {
			final IVertexConsumer vertexConsumer = vertexConsumerSupplier.get();
			final int[] indices = new int[6];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = indexBuffer.getAsInt();
			}
			final int position = vertexBuffer.position();
			Arrays.stream(indices).distinct().forEachOrdered(index -> {
				final int streamPos = index * 3 + position;
				vertexConsumer.put(POSITION, vertexBuffer.get(streamPos), vertexBuffer.get(streamPos + 1), vertexBuffer.get(streamPos + 2), 1f);
			});
			applyAttributes(attributes, vertexConsumer, indices);
			continue;
		}
		// TODO Interpolate for non-powers of 6
		for (int i = 0; i < rest; i++) {
			indexBuffer.getAsInt();
		}
	}
	
	private void loadFromVertex(final Map<String, ByteBuffer> attributes, final Supplier<IVertexConsumer> vertexConsumerSupplier, final ByteBuffer buffer, final int vertexCount) {
		final int rest = vertexCount % 4;
		final int divider = (vertexCount - rest) / 4;
		for (int j = 0; j < divider; j++) {
			final IVertexConsumer vertexConsumer = vertexConsumerSupplier.get();
			for (int i = 0; i < 4; i++) {
				vertexConsumer.put(POSITION, buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), 1f);
			}
			final int offset = j * 4;
			applyAttributes(attributes, vertexConsumer, new int[] { 0 + offset, 1 + offset, 2 + offset, 3 + offset });
			continue;
		}
		// TODO Interpolate for non-powers of 4
		for (int i = 0; i < rest; i++) {
			buffer.getFloat();
			buffer.getFloat();
			buffer.getFloat();
		}
	}
	
	private void applyAttributes(final Map<String, ByteBuffer> attributes, IVertexConsumer consumer, int[] indexBuffer) {
		Arrays.stream(indexBuffer).distinct().forEachOrdered(index -> {
			STRING_TO_ELEMENT.forEach((name, element) -> {
				if (!attributes.containsKey(name)){
					consumer.put(element, STRING_TO_DEFAULT.get(name));
					return;
				}
				final ByteBuffer buffer = attributes.get(name);
				float[] data = new float[4];
				final int readLength = STRING_TO_SIZE.get(name);
				final int offset = index * readLength * 4 + buffer.position();
				for (int i = 0; i < readLength; i++) {
					data[i] = buffer.getFloat(offset + i);
				}
				for (int i = readLength; i < data.length; i++) {
					data[i] = 0;
				}
				consumer.put(element, data);
			});
		});
		return;
	}
	
	private void addNode(final Node node, final Supplier<IVertexConsumer> vertexConsumerSupplier) {
		getOptionalEntry(this.gltf.getMeshes(), node.getMesh()).ifPresent(mesh -> {
			mesh.getPrimitives().forEach(primitive -> {
				final Map<String, Number> attributes = (Map<String, Number>) primitive.getAttributes();
				final Accessor accessor = getOptionalEntry(gltf.getAccessors(), attributes.get("POSITION")).get();
				final ByteBuffer buffer = this.parser.getData(accessor);
				final Optional<Accessor> indices = getOptionalEntry(gltf.getAccessors(), primitive.getIndices());
				final Map<String, ByteBuffer> attributesBuffer = new HashMap<>();
				attributes.forEach((name, id) -> getOptionalEntry(gltf.getAccessors(), id).ifPresent(access -> attributesBuffer.put(name, parser.getData(access))));
				if (indices.isPresent()) {
					final Accessor indexAccess = indices.get();
					final ByteBuffer indexBuffer = parser.getData(indexAccess);
					loadFromIndex(attributesBuffer, vertexConsumerSupplier, buffer, indexAccess.getCount(), equalsOptional(GLTF_COMPONENT_TYPE_UINT, indexAccess.getComponentType()) ? indexBuffer::getInt : indexBuffer::getShort);
				} else {
					loadFromVertex(attributesBuffer, vertexConsumerSupplier, buffer, accessor.getCount());
				}
			});
		});
	}
	
	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
		final ArrayList<BakedQuadBuilder> builderList = new ArrayList<>();
		final Supplier<IVertexConsumer> consumer = () -> {
			final BakedQuadBuilder bqb = new BakedQuadBuilder();
			builderList.add(bqb);
			return bqb;
		};
		if (node != null) {
			addNode(node, consumer);
		} else {
			this.gltf.getNodes().forEach(node -> addNode(node, consumer));
		}
		final ImmutableList.Builder<BakedQuad> bakedBuilder = ImmutableList.builder();
		builderList.forEach(builder -> bakedBuilder.add(builder.build()));
		return new GlTFBakedModel(bakedBuilder.build());
	}
	
	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return this.materials;
	}
	
}
