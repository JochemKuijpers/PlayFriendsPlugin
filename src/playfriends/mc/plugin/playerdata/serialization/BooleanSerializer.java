package playfriends.mc.plugin.playerdata.serialization;

/** The boolean serializer. */
public class BooleanSerializer implements TypeSerializer<Boolean> {
	@Override
	public Boolean deserialize(String string) {
		return Boolean.parseBoolean(string);
	}

	@Override
	public String serialize(Boolean value) {
		return String.valueOf(value);
	}
}
