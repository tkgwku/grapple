package mod.finediary.grapple.packet;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import mod.finediary.grapple.ModContainer;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModContainer.MODID);

    public static void init() {
        INSTANCE.registerMessage(GrappleMessageHandler.class, GrappleMessage.class, 0, Side.SERVER);
    }
}