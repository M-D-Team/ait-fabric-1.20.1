package mdteam.ait.api.tardis;

import the.mdteam.ait.Tardis;
import the.mdteam.ait.TardisDesktop;
import the.mdteam.ait.TardisTravel;

public interface ILinkable {

    Tardis getTardis();
    void setTardis(Tardis tardis);

    default TardisDesktop getDesktop() { return this.getTardis().getDesktop(); }
    default void setDesktop(TardisDesktop desktop) { }

    /**
     * This method forces the {@link ILinkable} to update its desktop!
     */
    default void linkDesktop() {
        if (this.getTardis() == null)
            return;

        TardisDesktop desktop = this.getTardis().getDesktop();

        if (desktop != null)
            this.setDesktop(desktop);
    }

    default TardisTravel getTravel() { return this.getTardis().getTravel(); }
    default void setTravel(TardisTravel travel) { }

    /**
     * This method forces the {@link ILinkable} to update its travel!
     */
    default void linkTravel() {
        if (this.getTardis() == null)
            return;

        TardisTravel travel = this.getTardis().getTravel();

        if (travel != null)
            this.setTravel(travel);
    }

    /**
     * If false, calling {@link ILinkable#setTardis(Tardis)} might throw an exception!
     */
    default boolean linkable() {
        return true;
    }
}
