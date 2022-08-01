package playfriends.mc.plugin.playerdata.serialization;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** The instant serializer. */
public class InstantSerializer implements TypeSerializer<Instant> {
	/** The formatter used for serialization and deserialization. */
	public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public Instant deserialize(String string) {
		LocalDateTime utcDateTime = LocalDateTime.parse(string, FORMATTER);
		return utcDateTime.toInstant(ZoneOffset.UTC);
	}

	@Override
	public String serialize(Instant value) {
		LocalDateTime dateTime = LocalDateTime.ofInstant(value, ZoneOffset.UTC);
		return dateTime.format(FORMATTER);
	}
}
