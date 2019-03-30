package mod.finediary.grapple.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy{
	@Override
	public EntityPlayer getEntityPlayerInstance(){
		return Minecraft.getInstance().player;
	}
}
