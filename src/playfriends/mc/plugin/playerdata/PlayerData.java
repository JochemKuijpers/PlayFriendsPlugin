package playfriends.mc.plugin.playerdata;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;

public class PlayerData {
    /** The player UUID */
    private final UUID uuid;

    /** Whether or not the data is equivalent to what is on disk. */
    private boolean isDirty;

    /** Whether or not the player plays on peaceful. */
    private boolean peaceful;

    /** How many times has this person visited the server. */
    private long timesSeen;

    /** Real clock time when the player moved last (Unix milliseconds), not saved. */
    private long lastMove;

    /** Whether or not the player is considered AFK, not saved. */
    private boolean isAfk;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.lastMove = System.currentTimeMillis();
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

    public void loadFromLines(List<String> lines) {
        for (String line : lines) {
            final String[] parts = line.split("=", 2);
            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();

            switch (key) {
                case "peaceful":
                    peaceful = value.toLowerCase().equals("true");
                    break;
                case "times-seen":
                    try {
                        timesSeen = Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        timesSeen = 0;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public List<String> saveAsLines() {
        return Lists.newArrayList(
                "peaceful = " + (peaceful ? "true" : "false"),
                "times-seen = " + timesSeen
        );
    }

    public long getLastMove() {
        return lastMove;
    }

    public void setLastMove(long lastMove) {
        this.lastMove = lastMove;
    }

    public boolean isAfk() {
        return isAfk;
    }

    public void setAfk(boolean afk) {
        isAfk = afk;
    }
}
