package mod.finediary.grapple;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityScoutHook extends EntityFishHook{
    //private static final DataParameter<Integer> DATA_HOOKED_ENTITY = EntityDataManager.<Integer>createKey(EntityFishHook.class, DataSerializers.VARINT);
    private boolean inGround;
    private int ticksInGround = 0;
    private EntityPlayer angler;
    private int ticksInAir;
    private int ticksCatchable;
    private int ticksCaughtDelay;
    private int ticksCatchableDelay;
    private float fishApproachAngle;
    //public Entity caughtEntity;
    private EntityScoutHook.State currentState = EntityScoutHook.State.FLYING;
    private int field_191518_aw;
    private int field_191519_ax;
    static double maxLength;

    public EntityScoutHook(World worldIn, EntityPlayer fishingPlayer) {
        super(worldIn, fishingPlayer);
        this.init(fishingPlayer);
        this.shoot();
    }

    public EntityScoutHook(World worldIn, EntityPlayer p_i47290_2_, double x, double y, double z) {
        super(worldIn, p_i47290_2_, x, y, z);
        this.init(p_i47290_2_);
        this.setPosition(x, y, z);
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
    }

    private void init(EntityPlayer p_190626_1_)
    {
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
        this.angler = p_190626_1_;
        this.angler.fishEntity = this;
    }

    private void shoot()
    {
        this.setLocationAndAngles(angler.posX, angler.posY + (double)angler.getEyeHeight(), angler.posZ, angler.rotationYaw, angler.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw * 0.017453292F) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f);
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f);
        this.motionY = (double)(-MathHelper.sin(this.rotationPitch * 0.017453292F) * f);
        this.handleHookCasting(this.motionX, this.motionY, this.motionZ, 1.5F, 1.0F);
    }

    @Override
    public void onUpdate()
    {
        //super.onUpdate();

        //ok
        if (this.angler == null)
        {
            this.setDead();
        }

        //ok
        else if (this.world.isRemote || !this.shouldStopFishing())
        {
            //ok
            if (this.inGround)
            {
                ++this.ticksInGround;

                if (this.ticksInGround >= 1200)
                {
                    this.setDead();
                    return;
                }
            }

            //depth of liquid (?)
            float f = 0.0F;
            BlockPos blockpos = new BlockPos(this);
            IBlockState iblockstate = this.world.getBlockState(blockpos);

            if (iblockstate.getMaterial() == Material.WATER)
            {
                f = BlockLiquid.getBlockLiquidHeight(iblockstate, this.world, blockpos);
            }

            if (this.currentState == EntityScoutHook.State.FLYING)
            {
                //if in liquid
                if (f > 0.0F)
                {
                    //decrement of the velocity
                    this.motionX *= 0.3D;
                    this.motionY *= 0.2D;
                    this.motionZ *= 0.3D;
                    this.currentState = EntityScoutHook.State.BOBBING;
                    return;
                }

                if (!this.world.isRemote)
                {
                    //[TODO]: check
                    this.checkCollision();
                }

                //ok
                if (!this.inGround && !this.onGround && !this.isCollidedHorizontally)
                {
                    ++this.ticksInAir;
                }
                else
                {
                    this.ticksInAir = 0;
                    this.motionX = 0.0D;
                    this.motionY = 0.0D;
                    this.motionZ = 0.0D;
                }
            }
            else
            {
                if (this.currentState == EntityScoutHook.State.BOBBING)
                {
                    //velocity decrement (2)
                    this.motionX *= 0.9D;
                    this.motionZ *= 0.9D;
                    double d0 = this.posY + this.motionY - (double)blockpos.getY() - (double)f;

                    if (Math.abs(d0) < 0.01D)
                    {
                        d0 += Math.signum(d0) * 0.1D;
                    }

                    this.motionY -= d0 * (double)this.rand.nextFloat() * 0.2D;

                    if (!this.world.isRemote && f > 0.0F)
                    {
                        this.catchingFish(blockpos);
                    }
                }
            }

            //velocity decrement by liquid
            if (iblockstate.getMaterial() != Material.WATER)
            {
                this.motionY -= 0.03D;
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.updateRotation();
            double d1 = 0.92D;
            this.motionX *= 0.92D;
            this.motionY *= 0.92D;
            this.motionZ *= 0.92D;
            this.setPosition(this.posX, this.posY, this.posZ);
        }
    }

    private void handleHookCasting(double p_146035_1_, double p_146035_3_, double p_146035_5_, float p_146035_7_, float p_146035_8_)
    {
        float f = MathHelper.sqrt(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_);
        p_146035_1_ = p_146035_1_ / (double)f;
        p_146035_3_ = p_146035_3_ / (double)f;
        p_146035_5_ = p_146035_5_ / (double)f;
        p_146035_1_ = p_146035_1_ + this.rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_3_ = p_146035_3_ + this.rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_5_ = p_146035_5_ + this.rand.nextGaussian() * 0.007499999832361937D * (double)p_146035_8_;
        p_146035_1_ = p_146035_1_ * (double)p_146035_7_;
        p_146035_3_ = p_146035_3_ * (double)p_146035_7_;
        p_146035_5_ = p_146035_5_ * (double)p_146035_7_;
        this.motionX = p_146035_1_;
        this.motionY = p_146035_3_;
        this.motionZ = p_146035_5_;
        float f1 = MathHelper.sqrt(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_);
        this.prevRotationYaw = this.rotationYaw = (float)(MathHelper.atan2(p_146035_1_, p_146035_5_) * (180D / Math.PI));
        this.prevRotationPitch = this.rotationPitch = (float)(MathHelper.atan2(p_146035_3_, (double)f1) * (180D / Math.PI));
        this.ticksInGround = 0;
    }
    //ok
    private boolean shouldStopFishing()
    {
        ItemStack itemstack = this.angler.getHeldItemMainhand();
        ItemStack itemstack1 = this.angler.getHeldItemOffhand();
        boolean flag = itemstack.getItem() instanceof ItemScoutRod;
        boolean flag1 = itemstack1.getItem() instanceof ItemScoutRod;

        if (!this.angler.isDead && this.angler.isEntityAlive()
                && (flag || flag1) && this.getDistanceSqToEntity(this.angler) <= maxLength * maxLength)
        {
            return false;
        }
        else
        {
            this.setDead();
            return true;
        }
    }


    private void catchingFish(BlockPos p_190621_1_)
    {
        WorldServer worldserver = (WorldServer)this.world;
        int i = 1;
        BlockPos blockpos = p_190621_1_.up();

        if (this.rand.nextFloat() < 0.25F && this.world.isRainingAt(blockpos))
        {
            ++i;
        }

        if (this.rand.nextFloat() < 0.5F && !this.world.canSeeSky(blockpos))
        {
            --i;
        }

        if (this.ticksCatchable > 0)
        {
            --this.ticksCatchable;

            if (this.ticksCatchable <= 0)
            {
                this.ticksCaughtDelay = 0;
                this.ticksCatchableDelay = 0;
            }
            else
            {
                this.motionY -= 0.2D * (double)this.rand.nextFloat() * (double)this.rand.nextFloat();
            }
        }
        else if (this.ticksCatchableDelay > 0)
        {
            this.ticksCatchableDelay -= i;

            if (this.ticksCatchableDelay > 0)
            {
                this.fishApproachAngle = (float)((double)this.fishApproachAngle + this.rand.nextGaussian() * 4.0D);
                float f = this.fishApproachAngle * 0.017453292F;
                float f1 = MathHelper.sin(f);
                float f2 = MathHelper.cos(f);
                double d0 = this.posX + (double)(f1 * (float)this.ticksCatchableDelay * 0.1F);
                double d1 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                double d2 = this.posZ + (double)(f2 * (float)this.ticksCatchableDelay * 0.1F);
                Block block = worldserver.getBlockState(new BlockPos(d0, d1 - 1.0D, d2)).getBlock();

                if (block == Blocks.WATER || block == Blocks.FLOWING_WATER)
                {
                    if (this.rand.nextFloat() < 0.15F)
                    {
                        worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d0, d1 - 0.10000000149011612D, d2, 1, (double)f1, 0.1D, (double)f2, 0.0D, new int[0]);
                    }

                    float f3 = f1 * 0.04F;
                    float f4 = f2 * 0.04F;
                    worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d0, d1, d2, 0, (double)f4, 0.01D, (double)(-f3), 1.0D, new int[0]);
                    worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, d0, d1, d2, 0, (double)(-f4), 0.01D, (double)f3, 1.0D, new int[0]);
                }
            }
            else
            {
                this.motionY = (double)(-0.4F * MathHelper.nextFloat(this.rand, 0.6F, 1.0F));
                this.playSound(SoundEvents.ENTITY_BOBBER_SPLASH, 0.25F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
                double d3 = this.getEntityBoundingBox().minY + 0.5D;
                worldserver.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, d3, this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, new int[0]);
                worldserver.spawnParticle(EnumParticleTypes.WATER_WAKE, this.posX, d3, this.posZ, (int)(1.0F + this.width * 20.0F), (double)this.width, 0.0D, (double)this.width, 0.20000000298023224D, new int[0]);
                this.ticksCatchable = MathHelper.getInt(this.rand, 20, 40);
            }
        }
        else if (this.ticksCaughtDelay > 0)
        {
            this.ticksCaughtDelay -= i;
            float f5 = 0.15F;

            if (this.ticksCaughtDelay < 20)
            {
                f5 = (float)((double)f5 + (double)(20 - this.ticksCaughtDelay) * 0.05D);
            }
            else if (this.ticksCaughtDelay < 40)
            {
                f5 = (float)((double)f5 + (double)(40 - this.ticksCaughtDelay) * 0.02D);
            }
            else if (this.ticksCaughtDelay < 60)
            {
                f5 = (float)((double)f5 + (double)(60 - this.ticksCaughtDelay) * 0.01D);
            }

            if (this.rand.nextFloat() < f5)
            {
                float f6 = MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * 0.017453292F;
                float f7 = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
                double d4 = this.posX + (double)(MathHelper.sin(f6) * f7 * 0.1F);
                double d5 = (double)((float)MathHelper.floor(this.getEntityBoundingBox().minY) + 1.0F);
                double d6 = this.posZ + (double)(MathHelper.cos(f6) * f7 * 0.1F);
                Block block1 = worldserver.getBlockState(new BlockPos((int)d4, (int)d5 - 1, (int)d6)).getBlock();

                if (block1 == Blocks.WATER || block1 == Blocks.FLOWING_WATER)
                {
                    worldserver.spawnParticle(EnumParticleTypes.WATER_SPLASH, d4, d5, d6, 2 + this.rand.nextInt(2), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D, new int[0]);
                }
            }

            if (this.ticksCaughtDelay <= 0)
            {
                this.fishApproachAngle = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
                this.ticksCatchableDelay = MathHelper.getInt(this.rand, 20, 80);
            }
        }
        else
        {
            this.ticksCaughtDelay = MathHelper.getInt(this.rand, 100, 600);
            this.ticksCaughtDelay -= this.field_191519_ax * 20 * 5;
        }
    }


    private void checkCollision()
    {
        Vec3d vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d, vec3d1, false, true, false);
        vec3d = new Vec3d(this.posX, this.posY, this.posZ);
        vec3d1 = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        if (raytraceresult != null)
        {
            vec3d1 = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y,
                    raytraceresult.hitVec.z);
        }

        Entity entity = null;
        //List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expandXyz(1.0D));
        double d0 = 0.0D;

        if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            this.inGround = true;
        }
    }


    private void updateRotation()
    {

        float f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(MathHelper.atan2(this.motionX, this.motionZ) * (180D / Math.PI));

        for (this.rotationPitch = (float)(MathHelper.atan2(this.motionY, (double)f2) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float f3 = 0.92F;

        if (this.onGround || this.isCollidedHorizontally)
        {
            f3 = 0.5F;
        }
    }

    @Override
    public int handleHookRetraction()
    {
        if (!this.world.isRemote && this.angler != null)
        {
            int i = 0;

            if (this.ticksCatchable > 0)
            {
                LootContext.Builder lootcontext$builder = new LootContext.Builder((WorldServer)this.world);
                lootcontext$builder.withLuck((float)this.field_191518_aw + this.angler.getLuck());

                for (ItemStack itemstack : this.world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING).generateLootForPools(this.rand, lootcontext$builder.build()))
                {
                    EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY, this.posZ, itemstack);
                    double d0 = this.angler.posX - this.posX;
                    double d1 = this.angler.posY - this.posY;
                    double d2 = this.angler.posZ - this.posZ;
                    double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    double d4 = 0.1D;
                    entityitem.motionX = d0 * 0.1D;
                    entityitem.motionY = d1 * 0.1D + (double)MathHelper.sqrt(d3) * 0.08D;
                    entityitem.motionZ = d2 * 0.1D;
                    this.world.spawnEntity(entityitem);
                    this.angler.world.spawnEntity(new EntityXPOrb(this.angler.world, this.angler.posX, this.angler.posY + 0.5D, this.angler.posZ + 0.5D, this.rand.nextInt(6) + 1));
                    Item item = itemstack.getItem();

                    if (item == Items.FISH || item == Items.COOKED_FISH)
                    {
                        this.angler.addStat(StatList.FISH_CAUGHT, 1);
                    }
                }

                i = 1;
            }

            if (this.inGround)
            {
                i = 2;
            }

            this.setDead();
            return i;
        }
        else
        {
            return 0;
        }
    }

    GrappleState getGrappleReadyState(){
        double _d = posY - Math.floor(posY);
        BlockPos position = new BlockPos(posX, posY, posZ);
        IBlockState state = this.world.getBlockState(position);
        IBlockState upstate = this.world.getBlockState(position.up());
        IBlockState downstate = this.world.getBlockState(position.down(1));
        IBlockState down2state = this.world.getBlockState(position.down(2));

        if (state.getBlock().equals(Blocks.LADDER)
                || state.getBlock().equals(Blocks.VINE)
                || state.getBlock().equals(Blocks.IRON_BARS)) {
            return GrappleState.LADDER;
        }

        if (state.getMaterial().isLiquid() && state.getBlock() != Blocks.LAVA){
        	if (isGrapplable(downstate) && _d < 0.8D){
        		return GrappleState.GROUND;
            }
        } else {
            if (isGrapplable(downstate) || (_d < 0.5D && downstate.getBlock() == Blocks.AIR && isGrapplable(down2state))) {
                return GrappleState.GROUND;
            }
        }

        if (isGrapplable(upstate) || isGrapplable(state)){
            return GrappleState.ROOF;
        }

        return GrappleState.NOT_READY;
    }

    private boolean isGrapplable(IBlockState state){
        Block b = state.getBlock();
        if (state.getMaterial().isLiquid() || state.getBlock() == Blocks.AIR || b instanceof BlockWeb) return false;
        return true;
    }

    static enum State
    {
        FLYING,
        //HOOKED_IN_ENTITY,
        BOBBING;
    }

    static enum GrappleState
    {
        NOT_READY,
        ROOF,
        GROUND,
        LADDER
    }
}
