package info.u_team.gltf_minecraft_loader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.io.CharsWrapper.Builder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;

import info.u_team.gltf_parser.GlTFParser;
import info.u_team.gltf_parser.generated.gltf.Accessor;
import info.u_team.gltf_parser.generated.gltf.GlTF;
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

	private GlTFParser parser;
	private GlTF gltf;
	private Collection<Material> materials;

	private static final HashMap<String, Integer> STRING_TO_ELEMENT = new HashMap<>();
	private static final HashMap<String, Integer> STRING_TO_SIZE = new HashMap<>();
	private static final int POSITION;

	public GlTFModel(GlTFParser parser, GlTF gltf, Collection<Material> materials) {
		this.parser = parser;
		this.materials = materials;
	}

	static {
		final HashMap<VertexFormatElement.Usage, String> elements = Maps.newHashMap();
		elements.put(VertexFormatElement.Usage.POSITION, "POSITION");
		elements.put(VertexFormatElement.Usage.UV, "TEXCOORD_0");
		elements.put(VertexFormatElement.Usage.NORMAL, "NORMAL");
		elements.put(VertexFormatElement.Usage.COLOR, "COLOR");

		STRING_TO_SIZE.put("POSITION", 3);
		STRING_TO_SIZE.put("TEXCOORD_0", 2);
		STRING_TO_SIZE.put("NORMAL", 3);
		STRING_TO_SIZE.put("COLOR", 4);

		final ImmutableList<VertexFormatElement> formats = DefaultVertexFormat.BLOCK.getElements();
		for (int j = 0; j < formats.size(); j++) {
			final VertexFormatElement element = formats.get(j);
			if (elements.containsKey(element.getUsage()))
				STRING_TO_ELEMENT.put(elements.get(element.getUsage()), j);
		}
		POSITION = STRING_TO_ELEMENT.get("POSITION");
		STRING_TO_ELEMENT.remove("POSITION");
	}

	private static <T> Optional<T> of(List<T> list, Object indexObj) {
		final int index = ((Number) indexObj).intValue();
		if (index < 0)
			return Optional.empty();
		return Optional.of(list.get(index));
	}

	private void preProcess(final Map<String, ByteBuffer> attributes, final List<BakedQuadBuilder> builder,
			final ByteBuffer buffer, final int vertexCount, final Supplier<BakedQuadBuilder> consumer) {
		final int rest = vertexCount % 4;
		final int divider = (vertexCount - rest) / 4;
		// TODO Interpolate for non-powers of 4
		for (int j = 0; j < divider; j++) {
			final BakedQuadBuilder vertexConsumer = consumer.get();
			for (int i = 0; i < 4; i++) {
				vertexConsumer.put(POSITION, buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
			}
			applyAttributes(attributes, vertexConsumer);
			builder.add(vertexConsumer);
		}
		for (int i = 0; i < rest; i++) {
			buffer.getFloat();
			buffer.getFloat();
			buffer.getFloat();
		}
	}

	private void loadFromVertex(final Map<String, ByteBuffer> attributes, final List<BakedQuadBuilder> builder,
			final Supplier<BakedQuadBuilder> consumer, final ByteBuffer buffer, final int vertexCount) {
		if (vertexCount == 4) {
			final BakedQuadBuilder vertexConsumer = consumer.get();
			for (int i = 0; i < 4; i++) {
				vertexConsumer.put(POSITION, buffer.getFloat(), buffer.getFloat(), buffer.getFloat());
			}
			applyAttributes(attributes, vertexConsumer);
			builder.add(vertexConsumer);
		} else {
			preProcess(attributes, builder, buffer, vertexCount, consumer);
		}
	}

	private void applyAttributes(final Map<String, ByteBuffer> attributes, IVertexConsumer consumer) {
		STRING_TO_ELEMENT.forEach((name, element) -> {
			final ByteBuffer buffer = attributes.get(name);
			float[] data = new float[STRING_TO_SIZE.get(name)];
			for (int i = 0; i < data.length; i++) {
				data[i] = buffer.getFloat();
			}
			consumer.put(element, data);
		});
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery,
			Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides,
			ResourceLocation modelLocation) {
		final ArrayList<BakedQuadBuilder> builder = new ArrayList<>();
		this.gltf.getNodes().forEach(node -> {
			of(this.gltf.getMeshes(), node.getMesh()).ifPresent(mesh -> {
				mesh.getPrimitives().forEach(primitive -> {
					final Map<String, Integer> attributes = (Map<String, Integer>) primitive.getAttributes();
					final Accessor accessor = of(gltf.getAccessors(), attributes.get("POSITION")).get();
					final ByteBuffer buffer = this.parser.getData(accessor);
					final Optional<Accessor> indices = of(gltf.getAccessors(), primitive.getIndices());
					final Supplier<BakedQuadBuilder> consumer = BakedQuadBuilder::new;
					final Map<String, ByteBuffer> attributesBuffer = new HashMap<>();
					attributes.forEach((name, id) -> of(gltf.getAccessors(), id)
							.ifPresent(access -> attributesBuffer.put(name, parser.getData(access))));
					if (!indices.isPresent()) {
						loadFromVertex(attributesBuffer, builder, consumer, buffer, accessor.getCount());
					} else {
					}
				});
			});
		});
		ImmutableList.Builder<BakedQuad> bakedBuilder = ImmutableList.builder();
		builder.forEach(consumer -> bakedBuilder.add(consumer.build()));
		return new GlTFBakedModel(bakedBuilder.build());
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner,
			Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		return this.materials;
	}

}
