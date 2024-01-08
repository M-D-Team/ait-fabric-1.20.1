package mdteam.ait.tardis.handler;

import mdteam.ait.tardis.Exclude;
import mdteam.ait.tardis.TardisTickable;
import mdteam.ait.tardis.control.sequences.SequenceHandler;
import mdteam.ait.tardis.handler.loyalty.LoyaltyHandler;
import mdteam.ait.tardis.handler.properties.PropertiesHolder;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TardisHandlersManager extends TardisLink {
    @Exclude
    private List<TardisLink> tickables = new ArrayList<>();
    // Yup
    private DoorHandler door;
    private PropertiesHolder properties;
    private WaypointHandler waypoints;
    private LoyaltyHandler loyalties;
    private OvergrownHandler overgrown;
    private ServerHumHandler hum = null;
    private ServerAlarmHandler alarms;
    private InteriorChangingHandler interior;
    private FuelHandler fuel;
    private HADSHandler hads;
    private FlightHandler flight;
    private SiegeModeHandler siege;
    private CloakHandler cloak;
    // private final SequenceHandler sequence;

    public TardisHandlersManager(UUID tardisId) {
        super(tardisId, "handlers");

        this.door = new DoorHandler(tardisId);
        this.properties = new PropertiesHolder(tardisId);
        this.waypoints = new WaypointHandler(tardisId);
        this.loyalties = new LoyaltyHandler(tardisId);
        this.overgrown = new OvergrownHandler(tardisId);
        this.hum = new ServerHumHandler(tardisId);
        alarms = new ServerAlarmHandler(tardisId);
        interior = new InteriorChangingHandler(tardisId);
        this.fuel = new FuelHandler(tardisId);
        this.hads = new HADSHandler(tardisId);
        this.flight = new FlightHandler(tardisId);
        this.siege = new SiegeModeHandler(tardisId);
        this.cloak = new CloakHandler(tardisId);
        // this.sequence = new SequenceHandler(tardisId);

        generateTickables();
    }

    private void generateTickables() {
        if (tickables == null) tickables = new ArrayList<>();

        tickables.clear();

        addTickable(getDoor());
        addTickable(getProperties());
        addTickable(getWaypoints());
        addTickable(getLoyalties());
        addTickable(getOvergrownHandler());
        addTickable(getHum());
        addTickable(getAlarms());
        addTickable(getInteriorChanger());
        addTickable(getFuel());
        addTickable(getHADS());
        addTickable(getFlight());
        addTickable(getSiege());
        // addTickable(getSequencing()); // todo sequences
    }

    protected void addTickable(TardisLink var) {
        tickables.add(var);
    }

    /**
     * Called on the END of a servers tick
     * @param server
     */
    public void tick(MinecraftServer server) {
        if (tickables == null) generateTickables();

        for (TardisTickable ticker : tickables) {
            if (ticker == null) {
                generateTickables();
                return;
            } // RAHHH I DONT CARE ABOUT PERFORMACNE, REGERNEATE IT ALLLL
            ticker.tick(server);
        }
    }

    /**
     * Called on the START of a servers tick
     * @param server
     */
    public void startTick(MinecraftServer server) {
        if (tickables == null) generateTickables();

        for (TardisTickable ticker:  tickables) {
            if (ticker == null) {
                generateTickables();
                return;
            }
            ticker.startTick(server);
        }
    }

    public PropertiesHolder getProperties() {
        if (this.properties == null) {
            this.properties = new PropertiesHolder(this.tardisId);
        }
        return properties;
    }

    public WaypointHandler getWaypoints() {
        if (this.waypoints == null) {
            this.waypoints = new WaypointHandler(this.tardisId);
        }
        return waypoints;
    }

    public LoyaltyHandler getLoyalties() {
        if (this.loyalties == null) {
            this.loyalties = new LoyaltyHandler(this.tardisId);
        }
        return loyalties;
    }
    public DoorHandler getDoor() {
        if (this.door == null) {
            this.door = new DoorHandler(this.tardisId);
        }
        return door;
    }
    public OvergrownHandler getOvergrownHandler() {
        if (this.overgrown == null) {
            this.overgrown = new OvergrownHandler(this.tardisId);
        }
        return overgrown;
    }
    public ServerHumHandler getHum() {
        if (this.hum == null) {
            this.hum = new ServerHumHandler(this.tardisId);
        }

        return this.hum;
    }

    public ServerAlarmHandler getAlarms() {
        if (this.alarms == null) {
            this.alarms = new ServerAlarmHandler(this.tardisId);
        }
        return alarms;
    }
    public InteriorChangingHandler getInteriorChanger() {
        if (this.interior == null) {
            this.interior = new InteriorChangingHandler(this.tardisId);
        }
        return interior;
    }

    public FuelHandler getFuel() {
        if (this.fuel == null) {
            this.fuel = new FuelHandler(this.tardisId);
        }
        return fuel;
    }

    public HADSHandler getHADS() {
        if (this.hads == null) {
            this.hads = new HADSHandler(this.tardisId);
        }
        return hads;
    }
    public FlightHandler getFlight() {
        if (this.flight == null) {
            this.flight = new FlightHandler(this.tardisId);
        }

        return flight;
    }
    public SiegeModeHandler getSiege() {
        if (this.siege == null) {
            this.siege = new SiegeModeHandler(this.tardisId);
        }
        return this.siege;
    }
    public CloakHandler getCloak() {
        if (this.cloak == null) {
            this.cloak = new CloakHandler(this.tardisId);
        }
        return this.cloak;
    }
    // public SequenceHandler getSequencing() {return this.sequence;}
}
