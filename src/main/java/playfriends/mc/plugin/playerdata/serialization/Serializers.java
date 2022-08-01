package playfriends.mc.plugin.playerdata.serialization;

import playfriends.mc.plugin.playerdata.Persistent;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;

/** The serializers for data fields. */
public class Serializers {
	/** The serializer map. */
	private static final Map<Class<?>, TypeSerializer<?>> serializerMap;

	static {
		serializerMap = new HashMap<>();
		serializerMap.put(Boolean.class,	new BooleanSerializer());
		serializerMap.put(String.class,		new StringSerializer());
		serializerMap.put(Long.class,		new LongSerializer());
		serializerMap.put(UUID.class,		new UuidSerializer());
		serializerMap.put(Instant.class,	new InstantSerializer());
		serializerMap.put(Collection.class,	new CollectionSerializer());
	}

	/** Separation string between key/values in the serialized string. */
	public static final String VALUE_SEPARATOR = ": ";

	/** Separation string between fields in the serialized string. */
	public static final String FIELD_SEPARATOR = "\n";

	/**
	 * @param cls the class to get a serializer for
	 * @return the serializer
	 * @throws NoSuchElementException if no such serializer exists
	 */
	public static TypeSerializer<?> getSerializer(Class<?> cls) throws NoSuchElementException {
		if (cls.isPrimitive()) {
			cls = getBoxedClassFromPrimitive(cls);
		}
		final TypeSerializer<?> serializer = serializerMap.get(cls);
		if (serializer == null) {
			throw new NoSuchElementException("Could not find serializer for class '" + cls.getSimpleName() + "'.");
		}
		return serializer;
	}

	public static <T> String serialize(T value) throws IllegalAccessException {
		final StringBuilder builder = new StringBuilder();
		final Field[] fields = value.getClass().getDeclaredFields();
		for (Field field : fields) {
			final Persistent persistentAnnotation = field.getAnnotation(Persistent.class);
			if (persistentAnnotation == null) {
				continue;
			}

			final TypeSerializer<Object> serializer = (TypeSerializer<Object>) getSerializer(field.getType());
			field.setAccessible(true);

			final Object fieldValue = field.get(value);
			if (fieldValue == null) {
				continue;
			}

			builder.append(persistentAnnotation.value());
			builder.append(VALUE_SEPARATOR);
			builder.append(serializer.serialize(fieldValue));
			builder.append(FIELD_SEPARATOR);
		}
		return builder.toString();
	}

	public static <T> void deserialize(String value, T into) throws IllegalAccessException {
		final Map<String, String> fieldMap = new HashMap<>();
		final String[] lines = value.split("\n");
		for (String line : lines) {
			String[] parts = line.split(VALUE_SEPARATOR, 2);
			if (parts.length != 2) {
				throw new RuntimeException("Not enough parts");
			}
			fieldMap.put(parts[0].strip(), parts[1].strip());
		}

		final Field[] fields = into.getClass().getDeclaredFields();
		for (Field field : fields) {
			final Persistent persistentAnnotation = field.getAnnotation(Persistent.class);
			if (persistentAnnotation == null) {
				continue;
			}

			final TypeSerializer<Object> serializer = (TypeSerializer<Object>) getSerializer(field.getType());
			field.setAccessible(true);
			field.set(into, serializer.deserialize(fieldMap.get(persistentAnnotation.value())));
		}
	}

	private static Class<?> getBoxedClassFromPrimitive(Class<?> cls) {
		if (cls == Integer.TYPE)
			return Integer.class;
		if (cls == Long.TYPE)
			return Long.class;
		if (cls == Boolean.TYPE)
			return Boolean.class;
		if (cls == Byte.TYPE)
			return Byte.class;
		if (cls == Character.TYPE)
			return Character.class;
		if (cls == Float.TYPE)
			return Float.class;
		if (cls == Double.TYPE)
			return Double.class;
		if (cls == Short.TYPE)
			return Short.class;
		return Void.class;
	}
}
