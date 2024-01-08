package mdteam.ait.tardis.handler;

import mdteam.ait.AITMod;
import mdteam.ait.api.tardis.TardisEvents;
import mdteam.ait.core.interfaces.RiftChunk;
import mdteam.ait.core.managers.DeltaTimeManager;
import mdteam.ait.datagen.datagen_providers.AITLanguageProvider;
import mdteam.ait.tardis.Exclude;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class FuelHandler extends TardisLink {
    @Exclude
    public static final double TARDIS_MAX_FUEL = 25000;
    public static final String FUEL_COUNT = "fuel_count";
    public static final String REFUELING = "refueling";

    public FuelHandler(UUID tardisId) {
        super(tardisId, "fuel");
    }

    public double getFuel() {
        if (PropertiesHandler.get(tardis().getHandlers().getProperties(), FUEL_COUNT) == null) {
            AITMod.LOGGER.warn("Fuel count was null, setting to 0");
            this.setFuelCount(0);
        }

        return (double) PropertiesHandler.get(tardis().getHandlers().getProperties(), FUEL_COUNT);
    }

    public boolean isOutOfFuel() {
        return getFuel() <= 0;
    }

    public void setFuelCount(double fuel) {
        double prev = getFuel();

        PropertiesHandler.set(tardis().getHandlers().getProperties(), FUEL_COUNT, fuel);
        tardis().markDirty();

        // fire the event if ran out of fuel
        // this may get ran multiple times though for some reason
        if (isOutOfFuel() && prev != 0) {
            TardisEvents.OUT_OF_FUEL.invoker().onNoFuel(tardis());
        }
    }

    public double addFuel(double fuel) {
        double currentFuel = this.getFuel();
        this.setFuelCount(this.getFuel() <= TARDIS_MAX_FUEL ? this.getFuel() + fuel : TARDIS_MAX_FUEL);
        if(this.getFuel() == TARDIS_MAX_FUEL)
            return fuel - (TARDIS_MAX_FUEL - currentFuel);
        return 0;
    }

    public void removeFuel(double fuel) {
        if (this.getFuel() - fuel < 0) {
            this.setFuelCount(0);
            return;
        }
        this.setFuelCount(getFuel() - fuel);
    }

    public void setRefueling(boolean isRefueling) {
        PropertiesHandler.setBool(tardis().getHandlers().getProperties(), REFUELING, isRefueling);
        tardis().markDirty();
    }

    // is always true for now
    public boolean isRefueling() {
        return PropertiesHandler.getBool(tardis().getHandlers().getProperties(), REFUELING);
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);

        // creativious i moved your code here
        RiftChunk riftChunk = (RiftChunk) this.tardis().getTravel().getExteriorPos().getChunk();
        if (tardis().getTravel().getState() == TardisTravel.State.LANDED && this.isRefueling() && riftChunk.getArtronLevels() > 0 && this.getFuel() < FuelHandler.TARDIS_MAX_FUEL && (!DeltaTimeManager.isStillWaitingOnDelay("tardis-" + tardis().getUuid().toString() + "-refueldelay"))) {
            if(riftChunk.isRiftChunk()) {
                riftChunk.setArtronLevels(riftChunk.getArtronLevels() - 1); // we shouldn't need to check how much it has because we can't even get here if don't have atleast one artron in the chunk
                addFuel(5);
            } else {
                addFuel(1);
            }
            DeltaTimeManager.createDelay("tardis-" + tardis().getUuid().toString() + "-refueldelay", 250L);
        }
        if ((tardis().getTravel().getState() == TardisTravel.State.DEMAT || tardis().getTravel().getState() == TardisTravel.State.MAT) && !DeltaTimeManager.isStillWaitingOnDelay("tardis-" + tardis().getUuid().toString() + "-fueldraindelay")) {
            DeltaTimeManager.createDelay("tardis-" + tardis().getUuid().toString() + "-fueldraindelay", 500L);
            removeFuel(5);
        }
        if (tardis().getTravel().getState() == TardisTravel.State.FLIGHT && !DeltaTimeManager.isStillWaitingOnDelay("tardis-" + tardis().getUuid().toString() + "-fueldraindelay")) {
            DeltaTimeManager.createDelay("tardis-" + tardis().getUuid().toString() + "-fueldraindelay", 500L);
            removeFuel(4^(this.tardis().getTravel().getSpeed()));
        }
        if (tardis().getTravel().getState() == TardisTravel.State.LANDED && !isRefueling() && !DeltaTimeManager.isStillWaitingOnDelay("tardis-" + tardis().getUuid().toString() + "-fueldraindelay")) {
            DeltaTimeManager.createDelay("tardis-" + tardis().getUuid().toString() + "-fueldraindelay", 500L);
            removeFuel(0.25);
        }
        if (tardis().getTravel().getState() == TardisTravel.State.FLIGHT && !this.tardis().hasPower()) {
            tardis().getTravel().crash(); // hehe force land if you don't have enough fuel
        }
    }
}
