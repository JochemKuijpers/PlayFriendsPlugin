package playfriends.mc.plugin.playerdata;


import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class PlayerData {
    /** The player UUID */
    @Persistent("uuid")
    private final UUID uuid;

    /** Whether or not the player plays on peaceful. */
    @Persistent("peaceful")
    private boolean peaceful;

    /** How many times has this person visited the server. */
    @Persistent("times-seen")
    private long timesSeen;

    /** Real clock time when the player moved last (Unix milliseconds), not saved. */
    @Persistent("last-move")
    private Instant lastMove;

    /** An alias the player can register for themselves */
    @Persistent("alias")
    private String alias;

    /** The player name. */
    @Persistent("player-name")
    private String playerName;

    /** Whether the data is equivalent to what is on disk. */
    private boolean isDirty;

    /** Whether the player is considered AFK, not saved. */
    private boolean isAfk;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.lastMove = Clock.systemUTC().instant();
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void resetDirty() {
        isDirty = false;
    }

    public boolean isPeaceful() {
        return peaceful;
    }

    public void setPeaceful(boolean peaceful) {
        isDirty = isDirty || (this.peaceful != peaceful);
        this.peaceful = peaceful;
    }

    public long getTimesSeen() {
        return timesSeen;
    }

    public void setTimesSeen(long timesSeen) {
        isDirty = isDirty || (this.timesSeen != timesSeen);
        this.timesSeen = timesSeen;
    }

    public Instant getLastMove() {
        return lastMove;
    }

    public void setLastMove(Instant lastMove) {
        this.lastMove = lastMove;
    }

    public boolean isAfk() {
        return isAfk;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        if (alias == null) {
            this.isDirty = true;
            this.alias = null;
            return;
        }

        if (alias.contains("\n")) {
            throw new IllegalArgumentException("Alias contains illegal characters");
        }
        if (alias.length() > 16) {
            throw new IllegalArgumentException("Alias is too long");
        }

        this.isDirty = true;
        this.alias = alias;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.isDirty = (!playerName.equals(this.playerName));
        this.playerName = playerName;
    }
}
