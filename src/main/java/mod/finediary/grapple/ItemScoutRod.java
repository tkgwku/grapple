package mod.finediary.grapple;

import java.util.Iterator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mod.finediary.grapple.EntityScoutHook.GrappleState;
import mod.finediary.grapple.packet.EnumGrappleMessage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemScoutRod extends ItemFishingRod{
	@SideOnly(Side.CLIENT)
	private IIcon theIcon;
	private static final String __OBFID = "CL_00000034";

	// config
	static int durability;
	static int damageMode;
	static boolean goForward;

	public ItemScoutRod(){
		this.setUnlocalizedName("grapple");
		if (durability > 0) this.setMaxDamage(durability);
		this.setCreativeTab(CreativeTabs.tabTools);
		this.setTextureName("fishing_rod");
	}

	/*
	 * ON SCOUT CASTED OR TOOK IN
	 * @see net.minecraft.item.ItemFishingRod#onItemRightClick()
	 */
	public ItemStack onItemRightClick(ItemStack itemstackIn, World worldIn, EntityPlayer playerIn){
		if (playerIn.fishEntity != null){
			handleScoutPull(itemstackIn, worldIn, playerIn);
			int i = playerIn.fishEntity.func_146034_e();
			itemstackIn.damageItem(i, playerIn);
			playerIn.swingItem();
			EnumGrappleMessage.SWING.sendToServer();
		} else {
			worldIn.playSoundAtEntity(playerIn, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

			if (!worldIn.isRemote){
				worldIn.spawnEntityInWorld(new EntityScoutHook(worldIn, playerIn));
			} else {
				playerIn.swingItem();
				EnumGrappleMessage.SWING.sendToServer();
			}
		}

		return itemstackIn;
	}

	public void handleScoutPull(ItemStack itemstackIn, World worldIn, EntityPlayer playerIn) {
		if (playerIn.fishEntity instanceof EntityScoutHook){
			boolean isVelocityChanged = true;
			float dx = (float) (playerIn.fishEntity.posX - playerIn.posX);
			float dy = (float) (playerIn.fishEntity.posY - playerIn.posY);
			float dz = (float) (playerIn.fishEntity.posZ - playerIn.posZ);
			GrappleState gstate = ((EntityScoutHook)playerIn.fishEntity).getGrappleReadyState();
			if (gstate == GrappleState.NOT_READY){
				playerIn.addChatMessage(new ChatComponentTranslation("unable.to.grapple"));
				isVelocityChanged = false;
			} else if (!GrappleEventHandler.canHook(playerIn)){
				playerIn.addChatMessage(new ChatComponentTranslation("grapple.not.ready"));
				isVelocityChanged = false;
			}

			if (isVelocityChanged){
				float c_xz = 0.15F + 0.000015F * MathHelper.clamp_float(dy * dy, 0, 64);

				double xz_incr =c_xz - (c_xz/Math.cosh(dx*dx+dz*dz));
				double y_incr = 0.1 + Math.signum(dy) * Math.log(Math.cosh(0.225D*dy));

				if (dy > 0 && dy <= 8.1F) {
					y_incr *= 2.0F - 0.12333F * dy;
				} else if (dy <= -2.0F) {
					y_incr *= 0.8F;
					xz_incr *= 1.2F;
				}

				if (goForward && dy < -5.0F){
					y_incr *= 1 / Math.abs(dy);
					xz_incr *= 1.6F;
				}

				Iterator<PotionEffect> iterator = playerIn.getActivePotionEffects().iterator();
				while (iterator.hasNext()) {
					PotionEffect effect = iterator.next();
					if (effect.getPotionID() == Potion.moveSpeed.id) {
						int a = MathHelper.clamp_int(effect.getAmplifier(), 1, 5);
						xz_incr *= 0.16F * (a + 6);
					}
				}

				double newMotionX = xz_incr * dx;
				double newMotionY = y_incr;
				double newMotionZ = xz_incr * dz;

				setMotion(playerIn, newMotionX, newMotionY, newMotionZ);

				GrappleEventHandler.addCooltime(playerIn);
				GrappleEventHandler.addPullPlayerTask(new PullPlayerTask(playerIn, 1, newMotionX, newMotionY, newMotionZ));

				switch (damageMode){
				case 0:
					playerIn.fallDistance -= Math.abs(dy);
					break;
				case 1:
					if (dy < -3.0D || playerIn.fallDistance > 8.0F) {
						playerIn.fallDistance = 0.7F * playerIn.fallDistance - 0.5F * Math.abs(dy);
					}
					break;
				case 2:
					playerIn.fallDistance -= 3F + 0.5F * Math.abs(dy);
					break;
				case 3:
					playerIn.fallDistance -= 111;
					break;
				}

				worldIn.playSoundAtEntity(playerIn, "mob.zombie.infect", 0.6F, 1.6F);
				itemstackIn.damageItem(1, playerIn);
			}
		}
	}

	static void setMotion(EntityPlayer player, double x, double y, double z){
		player.motionX = x;
		player.motionY = y;
		player.motionZ = z;
		player.velocityChanged = true;
	}
}
