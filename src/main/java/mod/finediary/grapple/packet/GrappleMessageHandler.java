package mod.finediary.grapple.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.entity.player.EntityPlayer;

public class GrappleMessageHandler implements IMessageHandler<GrappleMessage, IMessage> {

    @Override
    public IMessage onMessage(GrappleMessage message, MessageContext ctx) {
        if (message.data == 0){
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            player.swingItem();
        }
        return null;
    }
}
