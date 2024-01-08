package mdteam.ait.tardis.handler;

import mdteam.ait.core.item.WaypointItem;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.FlightUtil;
import mdteam.ait.tardis.util.Waypoint;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class WaypointHandler extends TardisLink {
    private Waypoint current; // The current waypoint in the slot ( tried to make it optional, but that caused a gson crash )
    private boolean hasCartridge;

    public WaypointHandler(UUID tardisId) {
        super(tardisId, "waypoint");
    }

    public boolean hasCartridge() {
        return this.hasCartridge;
    }
    public void markHasCartridge() {
        this.hasCartridge = true;
    }

    // todo summon a new waypoint item at the console if spawnItem is true
    /**
     * Sets the new waypoint
     * @param var
     * @return The optional of the previous waypoiint
     */
    public Optional<Waypoint> set(Waypoint var, boolean spawnItem) {
        Optional<Waypoint> prev = Optional.ofNullable(this.current);
        // System.out.println(var);
        // System.out.println(this.current);
        this.current = var;
        this.tardis().markDirty();

        if (spawnItem && prev.isPresent()) {
            this.spawnItem(prev.get());
        }

        return prev;
    }

    public Waypoint get() {
        return this.current;
    }
    public boolean hasWaypoint() {
        return this.current != null;
    }
    public void clear(boolean spawnItem) {
        this.set(null, spawnItem);
    }

    public void gotoWaypoint() {
        if (!this.hasWaypoint()) return; // todo move this check to the DEMAT event so the fail to takeoff happens

        PropertiesHandler.setAutoPilot(this.tardis().getHandlers().getProperties(), true);
        FlightUtil.travelTo(tardis(), this.get());
    }
    public void setDestination() {
        if (!this.hasWaypoint()) return;

        this.tardis().getTravel().setDestination(this.get(), true);
    }

    public void spawnItem() {
        if (!this.hasWaypoint()) return;

        spawnItem(this.get());
        this.clear(false);
    }

    public void spawnItem(Waypoint waypoint) {
        if (!this.hasCartridge) return;

        spawnItem(waypoint, this.tardis().getDesktop().getConsolePos());
        this.hasCartridge = false;
    }

    public static ItemStack createWaypointItem(Waypoint waypoint) {
        return WaypointItem.create(waypoint);
    }
    public static ItemEntity spawnItem(Waypoint waypoint, AbsoluteBlockPos pos) {
        ItemEntity entity = new ItemEntity(pos.getWorld(), pos.getX(), pos.getY(), pos.getZ(), createWaypointItem(waypoint));
        pos.getWorld().spawnEntity(entity);
        return entity;
    }
}
