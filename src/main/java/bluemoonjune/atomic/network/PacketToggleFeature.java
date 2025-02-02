package bluemoonjune.atomic.network;

import bluemoonjune.atomic.Atomic;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketToggleFeature extends Packet {

	private int featureID;
	private boolean state;

	public PacketToggleFeature(String feature, boolean state) {
		this.featureID = Atomic.FEATURE_IDS.get(feature);
		this.state = state;
	}

	@Override
	public void read(DataInputStream dataInputStream) throws IOException {
		state = dataInputStream.readByte() == 1;
		featureID = dataInputStream.readInt();
	}

	@Override
	public void write(DataOutputStream dataOutputStream) throws IOException {
		dataOutputStream.writeByte(state ? 1 : 0);
		dataOutputStream.writeInt(featureID);
	}

	@Override
	public void handlePacket(PacketHandler packetHandler) {
		Atomic.FEATURES.put(Atomic.ID_FEATURES.get(featureID), state);
	}

	@Override
	public int getEstimatedSize() {
		return 5;
	}
}
