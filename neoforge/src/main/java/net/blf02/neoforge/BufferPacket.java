package net.blf02.neoforge;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class BufferPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(VRAPIMod.MOD_ID, "network");

    private final FriendlyByteBuf buffer;

    public BufferPacket(FriendlyByteBuf buffer) {
        this.buffer = buffer;
    }

    public static BufferPacket read(FriendlyByteBuf buffer) {
        int numBytes = buffer.readInt();
        return new BufferPacket(new FriendlyByteBuf(buffer.readBytes(numBytes)));
    }

    public void handle(PlayPayloadContext ctx) {
        ServerPlayer player = ctx.player().orElse(null) instanceof ServerPlayer sp ? sp : null;
        ctx.workHandler().execute(() -> Network.CHANNEL.doReceive(player, this.buffer));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.buffer.readableBytes());
        buffer.writeBytes(this.buffer);
        this.buffer.resetReaderIndex();
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
