package mod.finediary.grapple.packet;

public enum EnumGrappleMessage {
	SWING_MAINHAND((byte)0),
    SWING_OFFHAND((byte)1);

	byte data;

	EnumGrappleMessage(byte data){
		this.data = data;
	}

	public void sendToServer(){
		PacketHandler.INSTANCE.sendToServer(new GrappleMessage(data));
	}
}
