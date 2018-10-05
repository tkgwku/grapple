package mod.finediary.grapple.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class GrappleMessage implements IMessage {

    public byte data;

    public GrappleMessage(){}

    public GrappleMessage(byte par1) {
        this.data= par1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.data= buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.data);
    }
}