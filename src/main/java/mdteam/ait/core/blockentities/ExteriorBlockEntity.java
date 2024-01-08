package mdteam.ait.core.blockentities;

import mdteam.ait.AITMod;
import mdteam.ait.tardis.animation.ExteriorAnimation;
import mdteam.ait.compat.DependencyChecker;
import mdteam.ait.core.AITBlockEntityTypes;
import mdteam.ait.core.blocks.ExteriorBlock;
import mdteam.ait.core.item.SiegeTardisItem;
import mdteam.ait.registry.ExteriorRegistry;
import mdteam.ait.tardis.exterior.CapsuleExterior;
import mdteam.ait.tardis.exterior.ExteriorSchema;
import mdteam.ait.tardis.util.TardisUtil;
import mdteam.ait.core.item.KeyItem;
import mdteam.ait.tardis.*;
import mdteam.ait.tardis.handler.DoorHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.wrapper.client.manager.ClientTardisManager;
import mdteam.ait.tardis.wrapper.server.manager.ServerTardisManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static mdteam.ait.tardis.TardisTravel.State.*;
import static mdteam.ait.tardis.util.TardisUtil.findTardisByPosition;
import static mdteam.ait.tardis.util.TardisUtil.isClient;

public class ExteriorBlockEntity extends BlockEntity implements BlockEntityTicker<ExteriorBlockEntity> { // fixme copy tardishandler and refactor to use uuids instead, this is incredibly inefficient and the main cause of lag.
    private UUID tardisId;
    public int animationTimer = 0;
    public final AnimationState DOOR_STATE = new AnimationState();
    private ExteriorAnimation animation;

    public ExteriorBlockEntity(BlockPos pos, BlockState state) {
        super(AITBlockEntityTypes.EXTERIOR_BLOCK_ENTITY_TYPE, pos, state);
    }

    public void useOn(ServerWorld world, boolean sneaking, PlayerEntity player) {
        if (player == null)
            return;
        if (this.getTardis().isGrowth())
            return;

        if (player.getMainHandStack().getItem() instanceof KeyItem && !getTardis().isSiegeMode() && !getTardis().getHandlers().getInteriorChanger().isGenerating()) {
            ItemStack key = player.getMainHandStack();
            NbtCompound tag = key.getOrCreateNbt();
            if (!tag.contains("tardis")) {
                return;
            }
            if (Objects.equals(this.getTardis().getUuid().toString(), tag.getString("tardis"))) {
                DoorHandler.toggleLock(this.getTardis(), (ServerPlayerEntity) player);
            } else {
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 1F, 0.2F);
                player.sendMessage(Text.literal("TARDIS does not identify with key"), true);
            }
            return;
        }

        if (sneaking && getTardis().isSiegeMode() && !getTardis().isSiegeBeingHeld()) {
            SiegeTardisItem.pickupTardis(getTardis(), (ServerPlayerEntity) player);
            return;
        }

        DoorHandler.useDoor(this.getTardis(), (ServerWorld) this.getWorld(), this.getPos(), (ServerPlayerEntity) player);
        // fixme maybe this is required idk the doorhandler already marks the tardis dirty || tardis().markDirty();
        if (sneaking)
            return;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        if (this.getTardis() == null) {
            AITMod.LOGGER.error("this.tardis() is null! Is " + this + " invalid? BlockPos: " + "(" + this.getPos().toShortString() + ")");
        }
        super.writeNbt(nbt);
        if (tardisId != null)
            nbt.putString("tardis", this.tardisId.toString());
        nbt.putFloat("alpha", this.getAlpha());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("tardis")) {
            this.tardisId = UUID.fromString(nbt.getString("tardis"));
        }
        //if (this.getAnimation() != null)
        //    this.getAnimation().setAlpha(nbt.getFloat("alpha"));
        if(this.getTardis() != null)
            this.getTardis().markDirty();
    }

    public void onEntityCollision(Entity entity) {
        if (!(entity instanceof ServerPlayerEntity player))
            return;

        if (this.getTardis() != null && this.getTardis().getDoor().isOpen()) {
            if (!this.getTardis().getLockedTardis())
                if (!DependencyChecker.hasPortals() || !getTardis().getExterior().getType().hasPortals())
                    TardisUtil.teleportInside(this.getTardis(), player);
        }
    }

    public Tardis getTardis() {
        if (this.tardisId == null) {
            //AITMod.LOGGER.warn("Exterior at " + this.getPos() + " is finding TARDIS!");
            this.findTardisFromPosition();
        }

        if (isClient()) {
            return ClientTardisManager.getInstance().getLookup().get(this.tardisId);
        }

        return ServerTardisManager.getInstance().getTardis(this.tardisId);
    }

    public void setTardis(Tardis tardis) {
        this.tardisId = tardis.getUuid();
    }

    private void findTardisFromPosition() { // should only be used if tardisId is null so we can hopefully refind the tardis
        Tardis found = findTardisByPosition(this.getPos());

        if (found == null) return;

        this.tardisId = found.getUuid();
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState blockState, ExteriorBlockEntity blockEntity) {
        if (this.animation != null && this.getTardis().getTravel().getState() != LANDED)
            this.getAnimation().tick();

        if(world.isClient()) {
            this.checkAnimations();
        }

        if (this.getTardis() == null) return;

        // Should be when tardis is set to landed / position is changed instead. fixme
        if (!world.isClient() && (blockState.getBlock() instanceof ExteriorBlock)) {
            // For checking falling
            ((ExteriorBlock) blockState.getBlock()).tryFall(blockState, (ServerWorld) world, pos);
        }

        if (!world.isClient() && this.getTardis() != null && !PropertiesHandler.getBool(this.getTardis().getHandlers().getProperties(), PropertiesHandler.PREVIOUSLY_LOCKED) && this.getTardis().getTravel().getState() == MAT && this.getAlpha() >= 0.9f) {
            for (ServerPlayerEntity entity : world.getEntitiesByClass(ServerPlayerEntity.class, new Box(this.getPos()).expand(0, 1, 0), EntityPredicates.EXCEPT_SPECTATOR)) {
                TardisUtil.teleportInside(this.getTardis(), entity); // fixme i dont like how this works you can just run into peoples tardises while theyre landing
            }
        }

        // ensures we dont exist during flight
        if (!world.isClient() && this.getTardis().getTravel().getState() == FLIGHT) {
            world.removeBlock(this.getPos(), false);
        }
    }

    // es caca
    public void verifyAnimation() {
        if (this.animation != null || this.getTardis() == null || this.getTardis().getExterior() == null)
            return;

        this.animation = this.getTardis().getExterior().getVariant().animation(this);
        AITMod.LOGGER.warn("Created new ANIMATION for " + this);
        this.animation.setupAnimation(this.getTardis().getTravel().getState());

        if (this.getWorld() != null) {
            if (!this.getWorld().isClient()) {
                this.animation.tellClientsToSetup(this.getTardis().getTravel().getState());
            }
        }
    }

    public void checkAnimations() {
    // DO NOT RUN THIS ON SERVER!!
        if(getTardis() == null) return;
        animationTimer++;
//        if (!DOOR_STATE.isRunning()) {
//            DOOR_STATE.startIfNotRunning(animationTimer);
//        }
        if(getTardis().getHandlers().getDoor().getAnimationExteriorState() == null) return;;
        if (getTardis().getHandlers().getDoor().getAnimationExteriorState() == null || !(getTardis().getHandlers().getDoor().getAnimationExteriorState().equals(getTardis().getDoor().getDoorState()))) {
            DOOR_STATE.start(animationTimer);
            getTardis().getHandlers().getDoor().tempExteriorState = getTardis().getDoor().getDoorState();
        }
    }

    public ExteriorAnimation getAnimation() {
        this.verifyAnimation();

        return this.animation;
    }

    public ExteriorSchema getExteriorType() {
        if(this.getTardis() == null) return ExteriorRegistry.REGISTRY.get(CapsuleExterior.REFERENCE);
        return this.getTardis().getExterior().getType();
    }

    public float getAlpha() {
        if (this.getAnimation() == null) {
            return 1f;
        }

        return this.getAnimation().getAlpha();
    }

    public void onBroken() {
        if(this.getTardis() != null)
            this.getTardis().getTravel().setState(TardisTravel.State.FLIGHT);
    }
}