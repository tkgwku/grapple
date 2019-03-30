package mod.finediary.grapple.packet;

import mod.finediary.grapple.GrappleMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = Integer.toString(1);

    static final SimpleChannel HANDLER = net.minecraftforge.fml.network.NetworkRegistry.ChannelBuilder.named(new ResourceLocation(GrappleMod.MODID, "main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    static int disc = 0;

    public static void register(){
        HANDLER.registerMessage(disc++, GrappleMSG.class, GrappleMSG::encode, GrappleMSG::decode, GrappleMSG.Handler::handle);
    }
}