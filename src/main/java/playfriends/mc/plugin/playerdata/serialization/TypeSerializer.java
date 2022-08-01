package playfriends.mc.plugin.playerdata.serialization;

/** The read/write class for a specific type, to serialize and deserialize player data. */
public interface TypeSerializer<T> {
	/**
	 * @param string the string representation of the value
	 * @return the deserialized value
	 */
	T deserialize(String string);

	/**
	 * @param value the value to serialize
	 * @return the serialized string representation of the value
	 */
	String serialize(T value);
}
