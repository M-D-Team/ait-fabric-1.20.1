package mdteam.ait.client.registry.exterior.impl.growth;

import mdteam.ait.AITMod;
import mdteam.ait.client.models.coral.CoralGrowthExteriorModel;
import mdteam.ait.client.models.coral.CoralGrowthModel;
import mdteam.ait.client.models.exteriors.ExteriorModel;
import mdteam.ait.client.models.exteriors.TardimExteriorModel;
import mdteam.ait.client.registry.exterior.ClientExteriorVariantSchema;
import mdteam.ait.client.renderers.coral.CoralRenderer;
import net.minecraft.util.Identifier;

public class ClientGrowthVariant extends ClientExteriorVariantSchema {
    public ClientGrowthVariant() {
        super(new Identifier(AITMod.MOD_ID, "exterior/coral_growth"));
    }


    @Override
    public ExteriorModel model() {
        return new CoralGrowthExteriorModel(CoralGrowthExteriorModel.getTexturedModelData().createModel());
    }
    @Override
    public Identifier texture() {
        return CoralRenderer.CORAL_GROWTH_TEXTURE;
    }

    @Override
    public Identifier emission() {
        return null;
    }
}