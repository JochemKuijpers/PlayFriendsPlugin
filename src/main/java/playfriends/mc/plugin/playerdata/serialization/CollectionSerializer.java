package playfriends.mc.plugin.playerdata.serialization;

import java.util.Collection;
import java.util.StringJoiner;

public class CollectionSerializer implements TypeSerializer<Collection<?>> {
	@Override
	public Collection<?> deserialize(String string) {
		return null;
	}

	@Override
	public String serialize(Collection<?> value) {
		final StringJoiner joiner = new StringJoiner(",");
		for (Object entry : value) {
			final TypeSerializer<Object> serializer = (TypeSerializer<Object>) Serializers.getSerializer(entry.getClass());
			joiner.add(serializer.serialize(entry));
		}
		return "[" + joiner + "]";
	}
}
