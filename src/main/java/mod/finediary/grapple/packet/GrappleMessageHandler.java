package mod.finediary.grapple.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GrappleMessageHandler implements IMessageHandler<GrappleMessage, IMessage> {

    @Override
    public IMessage onMessage(GrappleMessage message, MessageContext ctx) {
        if (message.data == 0){
            EntityPlayer player = ctx.getServerHandler().player;
            player.swingArm(EnumHand.MAIN_HAND);
        } else if (message.data == 1) {
            EntityPlayer player = ctx.getServerHandler().player;
            player.swingArm(EnumHand.OFF_HAND);
        }
        return null;
    }
}
