package net.blf02.vrapi.common.network;

import dev.architectury.networking.NetworkManager;
import net.blf02.vrapi.VRAPIMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class C2SPacket<T> implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(VRAPIMod.MOD_ID, "c2s");
    public static final Type<C2SPacket<?>> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPacket<?>> CODEC = CustomPacketPayload.codec(C2SPacket::write, C2SPacket::new);

    private final T payload;
    private final NetworkChannel.PacketHandlerInfo<T> handlerInfo;

    public C2SPacket(T payload) {
        this.payload = payload;
        this.handlerInfo = Network.CHANNEL.getPacketHandler(payload);
    }

    public C2SPacket(RegistryFriendlyByteBuf buffer) {
        this.handlerInfo = Network.CHANNEL.getPacketHandler(buffer.readInt());
        this.payload = this.handlerInfo.decoder().apply(buffer);
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.handlerInfo.intId());
        this.handlerInfo.encoder().accept(this.payload, buffer);
    }

    public static <P> void receiveServer(C2SPacket<P> packet, NetworkManager.PacketContext context) {
        packet.handlerInfo.handler().accept(packet.payload, () -> context);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
