package mod.finediary.grapple.packet;

import mod.finediary.grapple.ModContainer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ModContainer.MODID);

    public static void init() {
        INSTANCE.registerMessage(GrappleMessageHandler.class, GrappleMessage.class, 0, Side.SERVER);
    }
}