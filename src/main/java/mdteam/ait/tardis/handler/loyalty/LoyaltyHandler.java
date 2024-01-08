package mdteam.ait.tardis.handler.loyalty;

import mdteam.ait.tardis.handler.TardisLink;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class LoyaltyHandler extends TardisLink { // todo currently will be useless as will only be finished when all features have been added.
    private HashMap<UUID, Loyalty> data;

    public LoyaltyHandler(UUID tardisId, HashMap<UUID, Loyalty> data) {
        super(tardisId, "loyalty");
        this.data = data;
    }

    public LoyaltyHandler(UUID tardis) {
        this(tardis, new HashMap<>());
    }

    public HashMap<UUID, Loyalty> data() {
        return this.data;
    }

    public void add(ServerPlayerEntity player) {
        this.add(player, Loyalty.NONE);
    }

    public void add(ServerPlayerEntity player, Loyalty loyalty) {
        this.data().put(player.getUuid(), loyalty);

        tardis().markDirty();
    }

    public Loyalty get(ServerPlayerEntity player) {
        return this.data().get(player.getUuid());
    }
}
