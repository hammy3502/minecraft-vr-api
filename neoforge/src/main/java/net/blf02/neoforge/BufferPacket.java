package net.blf02.neoforge;

import net.blf02.vrapi.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class BufferPacket {

    private final FriendlyByteBuf buffer;

    public BufferPacket(FriendlyByteBuf buffer) {
        this.buffer = buffer;
    }

    public static void encode(BufferPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.buffer.readableBytes());
        buffer.writeBytes(message.buffer);
        message.buffer.resetReaderIndex();
    }

    public static BufferPacket decode(FriendlyByteBuf buffer) {
        return new BufferPacket(new FriendlyByteBuf(buffer.readBytes(buffer.readInt())));
    }

    public static void handle(BufferPacket message, NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Network.CHANNEL.doReceive(ctx.getSender(), message.buffer);
        });
    }

}
