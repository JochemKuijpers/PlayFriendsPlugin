package playfriends.mc.plugin.playerdata.serialization;

/** The string serializer, which is a no-op. */
public class StringSerializer implements TypeSerializer<String> {
	@Override
	public String deserialize(String string) {
		return string;
	}

	@Override
	public String serialize(String value) {
		return value;
	}
}
