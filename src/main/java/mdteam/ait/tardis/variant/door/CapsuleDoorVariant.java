package mdteam.ait.tardis.variant.door;

import mdteam.ait.AITMod;
import mdteam.ait.client.models.doors.CapsuleDoorModel;
import mdteam.ait.client.models.doors.ClassicDoorModel;
import mdteam.ait.client.models.doors.DoorModel;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CapsuleDoorVariant extends DoorSchema {
    public static final Identifier REFERENCE = new Identifier(AITMod.MOD_ID, "door/capsule");

    public CapsuleDoorVariant() {
        super(REFERENCE);
    }

    @Override
    public boolean isDouble() {
        return true;
    }
    @Override
    public SoundEvent openSound() {
        return SoundEvents.BLOCK_IRON_DOOR_OPEN;
    }

    @Override
    public SoundEvent closeSound() {
        return SoundEvents.BLOCK_IRON_DOOR_CLOSE;
    }
    @Override
    public Vec3d adjustPortalPos(Vec3d pos, Direction direction) {
        return switch (direction) {
            case DOWN, UP -> pos;
            case NORTH -> pos.add(0,0.1,0.35);
            case SOUTH -> pos.add(0,0.1,-0.35);
            case WEST -> pos.add(0.35,0.1,0);
            case EAST -> pos.add(-0.35,0.1,0);
        };
    }
}
