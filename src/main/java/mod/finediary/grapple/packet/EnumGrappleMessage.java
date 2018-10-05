package mod.finediary.grapple.packet;

public enum EnumGrappleMessage {
	SWING((byte)0);

	byte data;

	EnumGrappleMessage(byte data){
		this.data = data;
	}

	public void sendToServer(){
		PacketHandler.INSTANCE.sendToServer(new GrappleMessage(data));
	}
}
