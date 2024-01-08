package mdteam.ait.tardis.handler;

import mdteam.ait.AITMod;
import mdteam.ait.core.item.TardisItemBuilder;
import mdteam.ait.core.managers.DeltaTimeManager;
import mdteam.ait.tardis.TardisDesktopSchema;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Random;
import java.util.UUID;

public class InteriorChangingHandler extends TardisLink {
    private static final int WARN_TIME = 10 * 40;
    public static final String IS_REGENERATING = "is_regenerating";
    public static final String GENERATING_TICKS = "generating_ticks";
    public static final String QUEUED_INTERIOR = "queued_interior";
    public static final Identifier CHANGE_DESKTOP = new Identifier(AITMod.MOD_ID, "change_desktop");
    private static Random random;

    public InteriorChangingHandler(UUID tardisId) {
        super(tardisId, "interior_changing");
    }

    private void setGenerating(boolean var) {
        PropertiesHandler.set(tardis().getHandlers().getProperties(), IS_REGENERATING, var);
        tardis().markDirty();
    }
    public boolean isGenerating() {
        return PropertiesHandler.getBool(tardis().getHandlers().getProperties(), IS_REGENERATING);
    }

    private void setTicks(int var) {
        PropertiesHandler.set(tardis().getHandlers().getProperties(), GENERATING_TICKS, var);
        tardis().markDirty();
    }
    private void addTick() {
        setTicks(getTicks() + 1);
    }
    public int getTicks() {
        return PropertiesHandler.getInt(tardis().getHandlers().getProperties(), GENERATING_TICKS);
    }
    public boolean hasReachedMax() {
        return getTicks() >= WARN_TIME;
    }

    private void setQueuedInterior(TardisDesktopSchema schema) {
        PropertiesHandler.setDesktop(tardis().getHandlers().getProperties(), QUEUED_INTERIOR, schema);
        tardis().markDirty();
    }
    public TardisDesktopSchema getQueuedInterior() {
        return PropertiesHandler.getDesktop(tardis().getHandlers().getProperties(), QUEUED_INTERIOR);
    }

    public void queueInteriorChange(TardisDesktopSchema schema) {
        if (!tardis().hasPower()) return;
        if (tardis().isGrowth() && tardis().hasGrowthExterior())
            tardis().getExterior().setType(TardisItemBuilder.findRandomExterior());
        if (tardis().getHandlers().getFuel().getFuel() < 5000 && !(tardis().isGrowth() && tardis().hasGrowthDesktop())) {
            for (PlayerEntity player : TardisUtil.getPlayersInInterior(tardis())) {
                player.sendMessage(Text.translatable("tardis.message.interiorchange.not_enough_fuel").formatted(Formatting.RED), true);
                return;
            }
        }
        setQueuedInterior(schema);
        setTicks(0);
        setGenerating(true);
        DeltaTimeManager.createDelay("interior_change-" + tardis().getUuid().toString(), 100L);
        tardis().getHandlers().getAlarms().enable();
        tardis().getDesktop().setConsolePos(null);
        if (!(tardis().hasGrowthDesktop()))
            tardis().removeFuel(5000);
        tardis().markDirty();
    }

    private void onCompletion() {
        setGenerating(false);
        clearedOldInterior = false;
        tardis().getHandlers().getAlarms().disable();
        DoorHandler.lockTardis(PropertiesHandler.getBool(tardis().getHandlers().getProperties(), PropertiesHandler.PREVIOUSLY_LOCKED), tardis(), null, false);
    }

    private void warnPlayers() {
        for (PlayerEntity player : TardisUtil.getPlayersInInterior(tardis())) {
            player.sendMessage(Text.translatable("tardis.message.interiorchange.warning").formatted(Formatting.RED), true);
        }
    }
    private boolean isInteriorEmpty() {
        return TardisUtil.getPlayersInInterior(tardis()).isEmpty();
    }

    public static Random random() {
        if (random == null)
            random = new Random();

        return random;
    }

    private boolean clearedOldInterior = false;

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);
        if (TardisUtil.isClient()) return;
        if (!isGenerating()) return;
        if(tardis() == null) return;
        if (DeltaTimeManager.isStillWaitingOnDelay("interior_change-" + tardis().getUuid().toString())) return;
        if (tardis().getTravel().getState() == TardisTravel.State.FLIGHT) {
            tardis().getTravel().crash();
        }
        if (isGenerating()) {
            if (!tardis().getHandlers().getAlarms().isEnabled()) tardis().getHandlers().getAlarms().enable();
        }


        if (!tardis().hasPower()) {
            setGenerating(false);
            tardis().getHandlers().getAlarms().disable();
            return;
        }

        if (!isInteriorEmpty()) {
            warnPlayers();
            return;
        }

        if (isInteriorEmpty() && !tardis().getDoor().locked()) {
            DoorHandler.lockTardis(true, tardis(), null, true);
        }
        if (isInteriorEmpty() && !clearedOldInterior) {
            tardis().getDesktop().clearOldInterior(getQueuedInterior());
            DeltaTimeManager.createDelay("interior_change-" + tardis().getUuid().toString(), 15000L);
            clearedOldInterior = true;
            return;
        }
        if (isInteriorEmpty() && clearedOldInterior) {
            tardis().getDesktop().changeInterior(getQueuedInterior());
            onCompletion();
        }

    }
}
