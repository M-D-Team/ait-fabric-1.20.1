package mdteam.ait.tardis;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mdteam.ait.api.tardis.ILinkable;
import mdteam.ait.core.events.BlockEntityPreLoadEvent;
import mdteam.ait.tardis.console.ConsoleSchema;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.util.Corners;
import mdteam.ait.tardis.variant.console.ConsoleVariantSchema;
import mdteam.ait.tardis.variant.door.DoorSchema;
import mdteam.ait.tardis.variant.exterior.ExteriorVariantSchema;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.World;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class TardisManager {
    protected final Map<UUID, Tardis> lookup = new HashMap<>();
    protected final Gson gson;

    public TardisManager() {
        GsonBuilder builder = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes field) {
                        return field.getAnnotation(Exclude.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                }).registerTypeAdapter(TardisDesktopSchema.class, TardisDesktopSchema.serializer())
                .registerTypeAdapter(ExteriorVariantSchema.class, ExteriorVariantSchema.serializer())
                .registerTypeAdapter(DoorSchema.class, DoorSchema.serializer())
                .registerTypeAdapter(ExteriorSchema.class, ExteriorSchema.serializer())
                .registerTypeAdapter(ConsoleSchema.class, ConsoleSchema.serializer())
                .registerTypeAdapter(ConsoleVariantSchema.class, ConsoleVariantSchema.serializer())
                .registerTypeAdapter(Corners.class, Corners.serializer());
        builder = this.init(builder);
        this.gson = builder.create();
    }

    public static void init() {
        // nicked this off theo

        // this will re-register the client tardis manager on every join (that includes local worlds as well)
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayConnectionEvents.INIT.register((handler, client) -> ClientTardisManager.init());
        }

        // this is a race between what happens first:
        // if it's a world with tardises - then we need to initialize server tardis manager before any of the block entities load
        BlockEntityPreLoadEvent.LOAD.register(() -> {
            if (ServerTardisManager.getInstance() == null) {
                ServerTardisManager.init();
            }
        });

        // if it's a brand-new world without any tardises - then there's no need to run ahead of the train and break things
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (ServerTardisManager.getInstance() == null) {
                ServerTardisManager.init();
            }
        });
    }

    public GsonBuilder init(GsonBuilder builder) {
        return builder;
    }

    public static TardisManager getInstance() {
//        return FabricLauncherBase.getLauncher().getEnvironmentType() == EnvType.SERVER ? ServerTardisManager.getInstance() : ClientTardisManager.getInstance();
        return TardisUtil.isServer() ? ServerTardisManager.getInstance() : ClientTardisManager.getInstance();
    }

    public static TardisManager getInstance(boolean isServer) {
        return isServer ? ServerTardisManager.getInstance() : ClientTardisManager.getInstance();
    }
    public static TardisManager getInstance(World world) {
        return getInstance(!world.isClient());
    }

    public void getTardis(UUID uuid, Consumer<Tardis> consumer) {
        if (this.lookup.containsKey(uuid)) {
            consumer.accept(this.lookup.get(uuid));
            return;
        }

        this.loadTardis(uuid, consumer);
    }

    public void link(UUID uuid, ILinkable linkable) {
        this.getTardis(uuid, linkable::setTardis);
    }

    public abstract void loadTardis(UUID uuid, Consumer<Tardis> consumer);

    public void reset() {
        this.lookup.clear();
    }

    public Map<UUID, Tardis> getLookup() {
        return this.lookup;
    }
}
