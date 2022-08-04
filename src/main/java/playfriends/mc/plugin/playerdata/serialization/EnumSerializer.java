package playfriends.mc.plugin.playerdata.serialization;

public class EnumSerializer implements TypeSerializer<Enum<?>> {
	/** The enum constants. */
	private final Enum<?>[] constants;

	public EnumSerializer(Class<?> enumClass) {
		if (!enumClass.isEnum()) {
			throw new IllegalArgumentException("Constructor parameter must be an enum class.");
		}
		constants = (Enum<?>[]) enumClass.getEnumConstants();
	}

	@Override
	public Enum<?> deserialize(String string) {
		for (Enum<?> constant : constants) {
			if (constant.name().equalsIgnoreCase(string)) {
				return constant;
			}
		}
		return null;
	}

	@Override
	public String serialize(Enum<?> value) {
		return value.name();
	}
}
