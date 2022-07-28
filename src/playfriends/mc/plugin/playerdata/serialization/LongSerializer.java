package playfriends.mc.plugin.playerdata.serialization;

/** The long serializer. */
public class LongSerializer implements TypeSerializer<Long> {
	@Override
	public Long deserialize(String string) {
		return Long.parseLong(string);
	}

	@Override
	public String serialize(Long value) {
		return String.valueOf(value);
	}
}
