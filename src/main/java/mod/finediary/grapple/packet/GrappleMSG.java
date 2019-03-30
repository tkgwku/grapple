package mod.finediary.grapple.packet;

import java.util.function.Supplier;

import mod.finediary.grapple.GrappleMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.network.NetworkEvent;

public class GrappleMSG {
	int data;

	public GrappleMSG(int data){
		this.data = data;
	}

	public static void encode(GrappleMSG msg, PacketBuffer buf){
		buf.writeInt(msg.data);
	}

	public static GrappleMSG decode(PacketBuffer buf)
	{
		return new GrappleMSG(buf.readInt());
	}

	public static class Handler
	{
		public static void handle(final GrappleMSG msg, Supplier<NetworkEvent.Context> ctx)
		{
			ctx.get().enqueueWork(() -> {
				if (msg.data == EnumGrappleMessage.SWING_MAINHAND.data){
					EntityPlayer player = ctx.get().getSender();
					player.swingArm(EnumHand.MAIN_HAND);
				} else if (msg.data == EnumGrappleMessage.SWING_OFFHAND.data) {
					EntityPlayer player = ctx.get().getSender();
					player.swingArm(EnumHand.OFF_HAND);
				} else if (msg.data == EnumGrappleMessage.NULLIZE_FISHENTITY.data) {
					EntityPlayer player = GrappleMod.proxy.getEntityPlayerInstance();
					if (player != null) {
						player.fishEntity = null;
					}
				}
			});

			ctx.get().setPacketHandled(true);
		}
	}

	public static enum EnumGrappleMessage {
		SWING_MAINHAND(0),
		SWING_OFFHAND(1),
		NULLIZE_FISHENTITY(2);

		int data;

		EnumGrappleMessage(int data){
			this.data = data;
		}

		public void sendToServer(){
			PacketHandler.HANDLER.sendToServer(new GrappleMSG(data));
		}
	}
}

