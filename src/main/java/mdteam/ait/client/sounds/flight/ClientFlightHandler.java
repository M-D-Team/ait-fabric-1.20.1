package mdteam.ait.client.sounds.flight;

import mdteam.ait.client.sounds.LoopingSound;
import mdteam.ait.client.sounds.PlayerFollowingLoopingSound;
import mdteam.ait.client.sounds.PositionedLoopingSound;
import mdteam.ait.client.util.ClientShakeUtil;
import mdteam.ait.client.util.ClientTardisUtil;
import mdteam.ait.core.AITDimensions;
import mdteam.ait.core.AITSounds;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.TardisTravel;
import mdteam.ait.tardis.handler.properties.PropertiesHandler;
import mdteam.ait.tardis.util.SoundHandler;
import mdteam.ait.tardis.util.TardisUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

// All this is CLIENT ONLY!!
// Loqor, if you dont understand DONT TOUCH or ask me! - doozoo
// todo create a ServerFlightHandler if necessary eg in future when we do more of the stuff on the trello to do with flight sounds.
public class ClientFlightHandler extends SoundHandler {
    public static LoopingSound FLIGHT;
    protected ClientFlightHandler() {}

    public LoopingSound getFlightLoop() {
        if (FLIGHT == null) FLIGHT = new FlightSound(AITSounds.FLIGHT_LOOP, SoundCategory.BLOCKS, tardis().getDesktop().getConsolePos(), 2.5f); // should this be positioned at the console pos or global?

        return FLIGHT;
    }
    public static ClientFlightHandler create() {
        if (MinecraftClient.getInstance().player == null) return null;

        ClientFlightHandler handler = new ClientFlightHandler();
        handler.generate();
        return handler;
    }

    private void generate() {
        if (tardis() == null || tardis().getDesktop().getConsolePos() == null) return;

        if (FLIGHT == null) FLIGHT = new FlightSound(AITSounds.FLIGHT_LOOP, SoundCategory.BLOCKS, tardis().getDesktop().getConsolePos(), 2.5f);

        this.sounds = new ArrayList<>();
        this.sounds.add(
                FLIGHT
        );
    }

    public boolean isPlayerInATardis() {
        if (MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().world.getRegistryKey() != AITDimensions.TARDIS_DIM_WORLD) return false;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Tardis found = TardisUtil.findTardisByInterior(player.getBlockPos());

        return found != null;
    }

    public Tardis tardis() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return null;
        Tardis found = TardisUtil.findTardisByInterior(player.getBlockPos());
        return found;
    }

    private void playFlightSound() {
        if (!isPlaying(getFlightLoop())) {
            // ensures it plays at the right place
            ((PositionedLoopingSound) getFlightLoop()).setPosition(tardis().getDesktop().getConsolePos());
        }
        this.startIfNotPlaying(this.getFlightLoop());
    }

    private boolean shouldPlaySounds() {
        return inFlight() || hasThrottleAndHandbrakeDown();
    }

    private boolean inFlight() {
        return (isPlayerInATardis() && tardis().getTravel().getState() == TardisTravel.State.FLIGHT);
    }
    public boolean hasThrottleAndHandbrakeDown() {
        return (isPlayerInATardis() && tardis().getTravel().getSpeed() > 0 && PropertiesHandler.getBool(tardis().getHandlers().getProperties(), PropertiesHandler.HANDBRAKE));
    }

    public void tick(MinecraftClient client) {
        if (this.sounds == null) this.generate();

        if (shouldPlaySounds()) {
            this.playFlightSound();
        }
        else {
            this.stopSounds();
        }
    }
}
