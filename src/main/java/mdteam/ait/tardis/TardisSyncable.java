package mdteam.ait.tardis;


import net.minecraft.world.World;

public abstract class TardisSyncable {
    public abstract String getHeader();
    public abstract void updateTardis(World world);
    public abstract Tardis tardis();
}
