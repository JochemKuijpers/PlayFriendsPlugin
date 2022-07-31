package playfriends.mc.plugin.playerdata.serialization;

import java.util.UUID;

/** The UUID serializer. */
public class UuidSerializer implements TypeSerializer<UUID> {
	@Override
	public UUID deserialize(String string) {
		return UUID.fromString(string);
	}

	@Override
	public String serialize(UUID value) {
		return value.toString();
	}
}
