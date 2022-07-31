package playfriends.mc.plugin.playerdata;


import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

public class PlayerData {
    /** The player UUID */
    @Persistent("uuid")
    private final UUID uuid;

    /** Whether the player plays on peaceful. */
    @Persistent("peaceful")
    private boolean peaceful;

    /** How many times has this person visited the server. */
    @Persistent("times-seen")
    private long timesSeen;

    /** Real clock time when the player moved last (Unix milliseconds), not saved. */
    @Persistent("last-move")
    private Instant lastMove;

    /** The player name. */
    @Persistent("player-name")
    private String playerName;

    /** Whether the player has AFK detection enabled. */
    @Persistent("afk-enabled")
    private boolean isAfkEnabled;

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

    public boolean isPeaceful() {
        return peaceful;
    }

    public long getTimesSeen() {
        return timesSeen;
    }

    public Instant getLastMove() {
        return lastMove;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isAfkEnabled() {
        return isAfkEnabled;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void resetDirty() {
        isDirty = false;
    }

    public boolean isAfk() {
        return isAfk;
    }

    public void setPeaceful(boolean peaceful) {
        isDirty = isDirty || (this.peaceful != peaceful);
        this.peaceful = peaceful;
    }

    public void setTimesSeen(long timesSeen) {
        isDirty = isDirty || (this.timesSeen != timesSeen);
        this.timesSeen = timesSeen;
    }

    public void setLastMove(Instant lastMove) {
        isDirty = isDirty || (!this.lastMove.equals(lastMove));
        this.lastMove = lastMove;
    }

    public void setPlayerName(String playerName) {
        isDirty = isDirty || (!playerName.equals(this.playerName));
        this.playerName = playerName;
    }

    public void setAfkEnabled(boolean afkEnabled) {
        isDirty = isDirty || (this.isAfkEnabled != afkEnabled);
        this.isAfkEnabled = afkEnabled;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }
}
