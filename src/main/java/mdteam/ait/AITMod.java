package mdteam.ait;

import com.neptunedevelopmentteam.neptunelib.core.init_handlers.NeptuneInitHandler;
import com.neptunedevelopmentteam.neptunelib.core.itemgroup.NeptuneItemGroup;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import mc.craig.software.regen.util.Platform;
import mdteam.ait.api.tardis    .TardisEvents;
import mdteam.ait.compat.DependencyChecker;
import mdteam.ait.compat.immersive.PortalsHandler;
import mdteam.ait.compat.regen.RegenHandler;
import mdteam.ait.core.*;
import mdteam.ait.core.blockentities.ConsoleBlockEntity;
import mdteam.ait.core.blockentities.ExteriorBlockEntity;
import mdteam.ait.core.commands.*;
import mdteam.ait.core.components.block.radio.RadioNBTComponent;
import mdteam.ait.core.entities.ConsoleControlEntity;
import mdteam.ait.core.managers.RiftChunkManager;
import mdteam.ait.core.util.AITConfig;
import mdteam.ait.registry.*;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisManager;
import mdteam.ait.tardis.advancement.TardisCriterions;
import mdteam.ait.tardis.handler.InteriorChangingHandler;
import mdteam.ait.tardis.handler.ServerHumHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.sound.HumSound;
import mdteam.ait.tardis.util.FlightUtil;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AITMod implements ModInitializer {
    public static final String MOD_ID = "ait";
    public static final Logger LOGGER = LoggerFactory.getLogger("ait");
    public static final Boolean DEBUG = true;

    public static final AITConfig AIT_CONFIG = AITConfig.createAndLoad(); // if this doesnt exist for you run data gen
    public static final NeptuneItemGroup AIT_ITEM_GROUP = new NeptuneItemGroup(new Identifier(AITMod.MOD_ID, "item_group"), AITItems.TARDIS_ITEM.getDefaultStack());
    public static final ComponentKey<RadioNBTComponent> RADIONBT =
            ComponentRegistry.getOrCreate(new Identifier(AITMod.MOD_ID, "radionbt"), RadioNBTComponent.class);

    @Override
    public void onInitialize() {
        ConsoleRegistry.init();
        DesktopRegistry.init();
        ExteriorRegistry.init();
        HumsRegistry.init();
        CreakRegistry.init();
        SequenceRegistry.init();

        // These 3 have client registries which also need registering to.
        ConsoleVariantRegistry.init();
        ExteriorVariantRegistry.init();
        DoorRegistry.init();

        NeptuneInitHandler.register(AITItems.class, MOD_ID);
        NeptuneInitHandler.register(AITBlocks.class, MOD_ID);
        NeptuneInitHandler.register(AITSounds.class, MOD_ID);
        NeptuneInitHandler.register(AITBlockEntityTypes.class, MOD_ID);
        NeptuneInitHandler.register(AITEntityTypes.class, MOD_ID);


        TardisUtil.init();
        TardisManager.getInstance();
        TardisManager.init();
        RiftChunkManager.init();
        TardisCriterions.init();

        entityAttributeRegister();

        // ip support
        if (DependencyChecker.hasPortals())
            PortalsHandler.init();

        if (DependencyChecker.hasRegeneration())
            RegenHandler.init();

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            TeleportInteriorCommand.register(dispatcher);
            UnlockInteriorsCommand.register(dispatcher);
            SummonTardisCommand.register(dispatcher);
            SetLockedCommand.register(dispatcher);
            // SetHumCommand.register(dispatcher);
            SetFuelCommand.register(dispatcher);
            ToggleHumCommand.register(dispatcher);
            ToggleAlarmCommand.register(dispatcher);
            ToggleSiegeModeCommand.register(dispatcher);
            RiftChunkCommand.register(dispatcher);
            //RealWorldCommand.register(dispatcher);
        }));

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(((blockEntity, world) -> {
            // fixme this doesnt seem to run??
            if (blockEntity instanceof ConsoleBlockEntity console) {
                console.markNeedsSyncing();
            }
        }));

        TardisEvents.LANDED.register((tardis -> {
            // stuff for resetting the ExteriorAnimation
            if (tardis.getTravel().getPosition().getWorld().getBlockEntity(tardis.getTravel().getExteriorPos()) instanceof ExteriorBlockEntity entity) {
                entity.getAnimation().setupAnimation(tardis.getTravel().getState());
            }
        }));

        TardisEvents.DEMAT.register((tardis -> {
            if (tardis.isGrowth() || tardis.getHandlers().getInteriorChanger().isGenerating() || PropertiesHandler.getBool(tardis.getHandlers().getProperties(), PropertiesHandler.HANDBRAKE) || PropertiesHandler.getBool(tardis.getHandlers().getProperties(), PropertiesHandler.IS_FALLING) || tardis.isRefueling())
                return true; // cancelled

            if (tardis.getDoor().isOpen() /*|| !tardis.getDoor().locked()*/)
                return true;

            for (PlayerEntity player : TardisUtil.getPlayersInInterior(tardis)) {
                TardisCriterions.TAKEOFF.trigger((ServerPlayerEntity) player);
            }
            return false;
        }));

        TardisEvents.MAT.register((tardis -> {
            // Check that the tardis has finished flight
            boolean flightDone = tardis.getHandlers().getFlight().hasFinishedFlight();

            // Check if the Tardis is on cooldown
            boolean isCooldown = FlightUtil.isMaterialiseOnCooldown(tardis);

            // Check if the destination is already occupied
            boolean isDestinationOccupied = !tardis.getTravel().getPosition().equals(tardis.getTravel().getDestination()) && !tardis.getTravel().checkDestination();

            return /*!flightDone ||*/ isCooldown || isDestinationOccupied;
        }));

        TardisEvents.CRASH.register((tardis -> {
            for (PlayerEntity player : TardisUtil.getPlayersInInterior(tardis)) {
                TardisCriterions.CRASH.trigger((ServerPlayerEntity) player);
            }
        }));

        TardisEvents.OUT_OF_FUEL.register(Tardis::disablePower);
        TardisEvents.LOSE_POWER.register((tardis -> {
            if (tardis.getDesktop().getConsolePos() != null) {
                TardisUtil.getTardisDimension().playSound(null, tardis.getDesktop().getConsolePos(), AITSounds.SHUTDOWN, SoundCategory.AMBIENT, 10f, 1f);
            }

            // disabling protocols
            PropertiesHandler.setBool(tardis.getHandlers().getProperties(), PropertiesHandler.AUTO_LAND, false);
            PropertiesHandler.setBool(tardis.getHandlers().getProperties(), PropertiesHandler.ANTIGRAVS_ENABLED, false);
            PropertiesHandler.setBool(tardis.getHandlers().getProperties(), PropertiesHandler.HAIL_MARY, false);
            PropertiesHandler.setBool(tardis.getHandlers().getProperties(), PropertiesHandler.HADS_ENABLED, false);
        }));
        TardisEvents.REGAIN_POWER.register((tardis -> {
            if (tardis.getDesktop().getConsolePos() != null) {
                TardisUtil.getTardisDimension().playSound(null, tardis.getDesktop().getConsolePos(), AITSounds.POWERUP, SoundCategory.AMBIENT, 10f, 1f);
            }
        }));

        ServerPlayNetworking.registerGlobalReceiver(ConsoleBlockEntity.ASK, ((server, player, handler, buf, responseSender) -> {
            if (player.getServerWorld().getRegistryKey() != AITDimensions.TARDIS_DIM_WORLD) return;

            BlockPos consolePos = buf.readBlockPos();
            // fixme the gotten block entity is always null, shit.
            if (player.getServerWorld().getBlockEntity(consolePos) instanceof ConsoleBlockEntity console)
                console.markNeedsSyncing();
        }));

        ServerPlayNetworking.registerGlobalReceiver(InteriorChangingHandler.CHANGE_DESKTOP, ((server, player, handler, buf, responseSender) -> {
            Tardis tardis = ServerTardisManager.getInstance().getTardis(buf.readUuid());
            TardisDesktopSchema desktop = DesktopRegistry.get(buf.readIdentifier());

            if (tardis == null || desktop == null) return;

            tardis.getHandlers().getInteriorChanger().queueInteriorChange(desktop);
        }));

        ServerPlayNetworking.registerGlobalReceiver(ServerHumHandler.RECEIVE, ((server, player, handler, buf, responseSender) -> {
            Tardis tardis = ServerTardisManager.getInstance().getTardis(buf.readUuid());
            HumSound hum = HumSound.fromName(buf.readString(), buf.readString());

            if (tardis == null || hum == null) return;

            tardis.getHandlers().getHum().setHum(hum);
        }));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            DesktopRegistry.syncToClient(handler.getPlayer());
            // ServerTardisManager.getInstance().addSubscriberToAll(handler.getPlayer());
        });

        AIT_ITEM_GROUP.initialize();
    }

    public void entityAttributeRegister() {
        FabricDefaultAttributeRegistry.register(AITEntityTypes.CONTROL_ENTITY_TYPE, ConsoleControlEntity.createControlAttributes());
    }

    public static final Identifier OPEN_SCREEN_TARDIS = new Identifier(AITMod.MOD_ID, "open_screen_tardis"); // fixes "AITModClient in env type SERVER"

    public static void openScreen(ServerPlayerEntity player, int id, UUID tardis) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(id);
        buf.writeUuid(tardis);
        ServerPlayNetworking.send(player, OPEN_SCREEN_TARDIS, buf);
    }
}