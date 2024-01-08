package mdteam.ait.tardis.handler.properties;

import mdteam.ait.tardis.handler.TardisLink;

import java.util.HashMap;
import java.util.UUID;

public class PropertiesHolder extends TardisLink {
    private final HashMap<String, Object> data; // might replace the generic object with a property class that has impls eg Property.Boolean, etc

    public PropertiesHolder(UUID tardisId, HashMap<String, Object> data) {
        super(tardisId, "properties");
        this.data = data;
    }

    public PropertiesHolder(UUID tardis) {
        this(tardis, PropertiesHandler.createDefaultProperties());
    }

    public HashMap<String, Object> getData() {
        return this.data;
    }
}
