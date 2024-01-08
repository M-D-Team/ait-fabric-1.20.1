package mdteam.ait.tardis.handler;

import mdteam.ait.tardis.*;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.SerialDimension;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.UUID;

// todo rename all "Handler" to "Data" - eg FuelHander -> FuelData, makes more sense, no?
public abstract class TardisLink extends TardisSyncable implements TardisTickable {

    protected UUID tardisId;
    protected final String header;

    public TardisLink(UUID tardisId, String header) {
        this.tardisId = tardisId;
        this.header = header;
    }

    @Override
    public String getHeader() {
        return this.header;
    }

    @Override
    public void updateTardis(World world) {
        if (world.isClient()) return;

        ServerTardisManager.getInstance().sendToSubscribers(this);
    }

    public Tardis tardis() {
        if (isClient()) {
            return ClientTardisManager.getInstance().getLookup().get(tardisId);
        }
        return ServerTardisManager.getInstance().getTardis(tardisId);
    }

    @Override
    public void tick(ServerWorld world) {
        // Implementation of the server-side tick logic
    }

    @Override
    public void tick(MinecraftServer server) {
        // Implementation of the server-side tick logic
    }

    @Override
    public void tick(MinecraftClient client) {
        // Implementation of the client-side tick logic
    }

    @Override
    public void startTick(MinecraftServer server) {
        // Implementation of the server-side tick logic when it starts
    }

    public AbsoluteBlockPos.Directed getDoorPos() {
        Tardis tardis = tardis();
        return tardis != null && tardis.getDesktop() != null ?
                tardis.getDesktop().getInteriorDoorPos() :
                new AbsoluteBlockPos.Directed(0, 0, 0, new SerialDimension(World.OVERWORLD.getValue().toString()), Direction.NORTH);
    }

    public AbsoluteBlockPos.Directed getExteriorPos() {
        Tardis tardis = tardis();
        return tardis != null && tardis.getTravel() != null ?
                tardis.getTravel().getPosition() :
                new AbsoluteBlockPos.Directed(0, 0, 0, new SerialDimension(World.OVERWORLD.getValue().toString()), Direction.NORTH);
    }

    public static boolean isClient() {
        return TardisUtil.isClient();
    }

    public static boolean isServer() {
        return TardisUtil.isServer();
    }
}
