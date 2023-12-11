package mdteam.ait.client.models.exteriors;

import mdteam.ait.AITMod;
import mdteam.ait.core.blockentities.door.ExteriorBlockEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CapsuleExteriorModel extends ExteriorModel {

	public static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID, ("textures/blockentities/exteriors/capsule.png"));
	private final ModelPart body;
	public CapsuleExteriorModel(ModelPart root) {
		super(RenderLayer::getEntityCutoutNoCull);
		this.body = root.getChild("body");
	}
	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData body = modelPartData.addChild("body", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 3.0F, 0.0F));

		ModelPartData top = body.addChild("top", ModelPartBuilder.create().uv(45, 92).cuboid(-4.9706F, -42.0F, -12.0F, 9.9411F, 8.0F, 24.0F, new Dilation(0.0F))
				.uv(73, 1).cuboid(-12.0F, -42.1F, -12.0F, 24.0F, 0.0F, 24.0F, new Dilation(0.0F))
				.uv(0, 25).cuboid(-12.0F, -33.89F, -12.0F, 24.0F, 0.0F, 24.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 21.0F, 0.0F));

		ModelPartData octagon_r1 = top.addChild("octagon_r1", ModelPartBuilder.create().uv(73, 26).cuboid(-4.9706F, -42.0F, -12.0F, 9.9411F, 8.0F, 24.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData octagon_r2 = top.addChild("octagon_r2", ModelPartBuilder.create().uv(73, 59).cuboid(-4.9706F, -42.0F, -12.0F, 9.9411F, 8.0F, 24.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		ModelPartData octagon_r3 = top.addChild("octagon_r3", ModelPartBuilder.create().uv(0, 75).cuboid(-4.9706F, -42.0F, -12.0F, 9.9411F, 8.0F, 24.0F, new Dilation(0.01F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		ModelPartData middle = body.addChild("middle", ModelPartBuilder.create().uv(26, 135).cuboid(-4.7635F, -34.0F, 9.5F, 9.5269F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 21.0F, 0.0F));

		ModelPartData octagon_r4 = middle.addChild("octagon_r4", ModelPartBuilder.create().uv(120, 128).cuboid(-4.7635F, -34.0F, 9.5F, 9.5269F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		ModelPartData octagon_r5 = middle.addChild("octagon_r5", ModelPartBuilder.create().uv(50, 135).cuboid(-2.2365F, -34.0F, 9.5F, 7.0F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, -0.7854F, 3.1416F));

		ModelPartData octagon_r6 = middle.addChild("octagon_r6", ModelPartBuilder.create().uv(95, 128).cuboid(-4.7635F, -34.0F, 9.5F, 9.5269F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData octagon_r7 = middle.addChild("octagon_r7", ModelPartBuilder.create().uv(70, 128).cuboid(-4.7635F, -34.0F, 9.5F, 9.5269F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		ModelPartData octagon_r8 = middle.addChild("octagon_r8", ModelPartBuilder.create().uv(144, 128).cuboid(-4.7635F, -34.0F, 9.5F, 7.0F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -3.1416F, 0.7854F, 3.1416F));

		ModelPartData octagon_r9 = middle.addChild("octagon_r9", ModelPartBuilder.create().uv(1, 135).cuboid(-4.7635F, -34.0F, 9.5F, 9.5269F, 32.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		ModelPartData bottom = body.addChild("bottom", ModelPartBuilder.create().uv(118, 68).cuboid(-4.9706F, -2.0F, -12.0F, 9.9411F, 2.0F, 24.0F, new Dilation(0.0F))
				.uv(0, 50).cuboid(-12.0F, 0.01F, -12.0F, 24.0F, 0.0F, 24.0F, new Dilation(0.0F))
				.uv(0, 0).cuboid(-12.0F, -2.1F, -12.0F, 24.0F, 0.0F, 24.0F, new Dilation(0.0F)), ModelTransform.pivot(0.0F, 21.0F, 0.0F));

		ModelPartData octagon_r10 = bottom.addChild("octagon_r10", ModelPartBuilder.create().uv(90, 101).cuboid(-4.9706F, -2.0F, -12.0F, 9.9411F, 2.0F, 24.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		ModelPartData octagon_r11 = bottom.addChild("octagon_r11", ModelPartBuilder.create().uv(118, 35).cuboid(-4.9706F, -2.0F, -12.0F, 9.9411F, 2.0F, 24.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		ModelPartData octagon_r12 = bottom.addChild("octagon_r12", ModelPartBuilder.create().uv(0, 108).cuboid(-4.9706F, -2.0F, -12.0F, 9.9411F, 2.0F, 24.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		ModelPartData doors = body.addChild("doors", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, -2.0F, 0.0F));

		ModelPartData right_door = doors.addChild("right_door", ModelPartBuilder.create().uv(162, 162).cuboid(0.4706F, -11.0F, -0.5F, 6.0F, 32.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(-6.5F, 0.0F, -8.5F));

		ModelPartData left_door = doors.addChild("left_door", ModelPartBuilder.create().uv(161, 95).cuboid(-6.5294F, -11.0F, -0.5F, 6.0F, 32.0F, 1.0F, new Dilation(0.0F)), ModelTransform.pivot(6.5F, 0.0F, -8.5F));
		return TexturedModelData.of(modelData, 256, 256);
	}

	@Override
	public void renderWithAnimations(ExteriorBlockEntity exterior, ModelPart root, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float pAlpha) {
		matrices.push();
		matrices.translate(0, -1.5f, 0);

		this.body.getChild("doors").getChild("left_door").yaw = exterior.getDoor().getState().getLeft();
		this.body.getChild("doors").getChild("right_door").yaw = -exterior.getDoor().getState().getRight();

		super.renderWithAnimations(exterior, root, matrices, vertices, light, overlay, red, green, blue, pAlpha);
		matrices.pop();
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}

	@Override
	public Identifier getTexture() {
		return TEXTURE;
	}

	@Override
	public Identifier getEmission() {
		return null;
	}

	@Override
	public ModelPart getPart() {
		return this.body;
	}
}