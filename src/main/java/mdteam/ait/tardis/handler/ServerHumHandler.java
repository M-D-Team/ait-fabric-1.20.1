package mdteam.ait.tardis.handler;

import mdteam.ait.AITMod;
import mdteam.ait.core.AITSounds;
import mdteam.ait.registry.HumsRegistry;
import mdteam.ait.tardis.handler.TardisLink;
import mdteam.ait.tardis.sound.HumSound;
import mdteam.ait.tardis.util.TardisUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.UUID;

// Loqor, if you dont understand DONT TOUCH or ask me! - doozoo
public class ServerHumHandler extends TardisLink {
    public static final Identifier SEND = new Identifier(AITMod.MOD_ID, "send_hum");
    public static final Identifier RECEIVE = new Identifier(AITMod.MOD_ID, "receive_hum");
    private HumSound current;
    public ServerHumHandler(UUID tardisId) {
        super(tardisId, "hum");
    }

    public HumSound getHum() {
        if (current == null) {
            this.current = HumsRegistry.TOYOTA;
        }

        return this.current;
    }
    public void setHum(HumSound hum) {
        this.current = hum;

        this.updateClientHum();
        tardis().markDirty(); // should b ok here its not gonna spam like the door did
    }

    private void updateClientHum() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(this.current.sound().getId());

        for (PlayerEntity player : TardisUtil.getPlayersInInterior(this.tardis())) { // is bad? fixme
            ServerPlayNetworking.send((ServerPlayerEntity) player, SEND, buf);
        }
    }
}
