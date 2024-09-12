package net.blf02.neoforge;

import net.blf02.vrapi.VRAPIMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.connection.ConnectionType;

public record BufferPacket(RegistryFriendlyByteBuf buffer) implements CustomPacketPayload {

    public static final Type<BufferPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(VRAPIMod.MOD_ID, "network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BufferPacket> CODEC =
            CustomPacketPayload.codec(BufferPacket::write, BufferPacket::read);

    public static BufferPacket read(RegistryFriendlyByteBuf buffer) {
        return new BufferPacket(new RegistryFriendlyByteBuf(buffer.readBytes(buffer.readInt()), buffer.registryAccess(), ConnectionType.NEOFORGE));
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.buffer.readableBytes());
        buffer.writeBytes(this.buffer);
        this.buffer.resetReaderIndex();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
