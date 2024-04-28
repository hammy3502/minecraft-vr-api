package net.blf02.vrapi.common.network;

import dev.architectury.networking.NetworkManager;
import net.blf02.vrapi.VRAPIMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class S2CPacket<T> implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(VRAPIMod.MOD_ID, "s2c");
    public static final Type<S2CPacket<?>> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPacket<?>> CODEC = CustomPacketPayload.codec(S2CPacket::write, S2CPacket::new);

    private final T payload;
    private final NetworkChannel.PacketHandlerInfo<T> handlerInfo;

    public S2CPacket(T payload) {
        this.payload = payload;
        this.handlerInfo = Network.CHANNEL.getPacketHandler(payload);
    }

    public S2CPacket(RegistryFriendlyByteBuf buffer) {
        this.handlerInfo = Network.CHANNEL.getPacketHandler(buffer.readInt());
        this.payload = this.handlerInfo.decoder().apply(buffer);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.handlerInfo.intId());
        this.handlerInfo.encoder().accept(this.payload, buffer);
    }

    public static <P> void receiveClient(S2CPacket<P> packet, NetworkManager.PacketContext context) {
        packet.handlerInfo.handler().accept(packet.payload, () -> context);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
