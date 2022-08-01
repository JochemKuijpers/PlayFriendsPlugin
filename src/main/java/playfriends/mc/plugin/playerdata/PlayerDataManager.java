package playfriends.mc.plugin.playerdata;

import playfriends.mc.plugin.playerdata.serialization.Serializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PlayerDataManager {
    private static final String FOLDER_NAME = "player-data";
    private static final String FILENAME_EXT = "txt";

    private static final String LOG_DIR_EXISTS_AS_FILE =
            "Unable to create player-data folder, a file with this name already exists!";
    private static final String LOG_WARN_NO_FILESYSTEM_BACKING =
            "Player data is not backed by the file system and will therefore reset when the server restarts.";
    private static final String LOG_COULD_NOT_CREATE_DIRS =
            "Unable to create the player data directory.";
    private static final String LOG_FILESYSTEM_ERROR =
            "Something went wrong while accessing or writing files.";
    private static final String LOG_CREATED_DIRS =
            "Created player data directory successfully!";
    public static final String LOG_SAVE_DIRTY_FILES =
            "Saving dirty player data files.";

    private static final String LOG_LOADED_X_FILES =
            "Successfully loaded %d player data files.";
    private static final String LOG_SKIPPED_X_FILES =
            "Skipped loading from %d player data files due to errors.";
    private static final String LOG_COULD_NOT_SAVE =
            "Could not save player data to file '%s' due to an IO Exception: %s";

    private final File dataFolder;
    private final Logger logger;
    private final Clock clock;

    private final boolean backedByFileSystem;
    private final Map<UUID, PlayerData> dataByUUID;

    public PlayerDataManager(File dataFolder, Logger logger, Clock clock) {
        this.dataFolder = new File(dataFolder.getPath() + "/" + FOLDER_NAME);
        this.logger = logger;
        this.clock = clock;

        this.backedByFileSystem = prepareDataDirectory();
        this.dataByUUID = new HashMap<>();

        if (!backedByFileSystem) {
            logger.log(Level.WARNING, LOG_WARN_NO_FILESYSTEM_BACKING);
        }
    }

    private boolean prepareDataDirectory() {
        if (dataFolder.isFile()) {
            logger.log(Level.SEVERE, LOG_DIR_EXISTS_AS_FILE);
            return false;
        }

        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                logger.log(Level.SEVERE, LOG_COULD_NOT_CREATE_DIRS);
                return false;
            } else {
                logger.log(Level.INFO, LOG_CREATED_DIRS);
            }
        }

        return true;
    }

    public PlayerData getPlayerData(UUID uuid) {
        final Instant now = clock.instant();
        synchronized (dataByUUID) {
            return dataByUUID.computeIfAbsent(uuid, k -> new PlayerData(k, now));
        }
    }

    public void savePlayerData(UUID uuid) {
        savePlayerData(getPlayerData(uuid));
    }

    public void savePlayerData(PlayerData playerData) {
        synchronized (dataByUUID) {
            dataByUUID.put(playerData.getUUID(), playerData);
        }
        if (!playerData.isDirty()) { return; }

        final String filename = dataFolder.getPath() + "/" + playerData.getUUID().toString() + "." + FILENAME_EXT;
        try {
            Files.writeString(Paths.get(filename), Serializers.serialize(playerData));
            playerData.resetDirty();
        } catch (IOException | IllegalAccessException e) {
            logger.log(Level.SEVERE, String.format(LOG_COULD_NOT_SAVE, filename, e.getMessage()));
        }
    }

    public void loadAll() {
        if (!backedByFileSystem) { return; }

        final Pattern pattern = Pattern.compile("([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\." + FILENAME_EXT + ")");
        final File[] fileNames = dataFolder.listFiles((dir, name) -> pattern.matcher(name).find());
        if (fileNames == null) {
            logger.log(Level.SEVERE, LOG_FILESYSTEM_ERROR);
            return;
        }

        long numFiles = 0;
        long numSuccess = 0;

        final Instant now = clock.instant();
        synchronized (dataByUUID) {
            for (File dataFile : fileNames) {
                numFiles += 1;
                final String dataFileName = dataFile.getName();
                final String fileUUID = dataFileName.replace("." + FILENAME_EXT, "");

                try {
                    final UUID playerUUID = UUID.fromString(fileUUID);
                    final PlayerData playerData = dataByUUID.computeIfAbsent(playerUUID, uuid -> new PlayerData(uuid, now));
                    Serializers.deserialize(Files.readString(dataFile.toPath()), playerData);

                    numSuccess += 1;
                } catch (Exception ignored) {
                    // logged below
                }
            }
        }

        if (numFiles != numSuccess) {
            logger.log(Level.WARNING, String.format(LOG_SKIPPED_X_FILES, numFiles - numSuccess));
        }
        if (numSuccess > 0) {
            logger.log(Level.INFO, String.format(LOG_LOADED_X_FILES, numSuccess));
        }
    }

    public void saveAll() {
        if (!backedByFileSystem) { return; }

        logger.log(Level.INFO, LOG_SAVE_DIRTY_FILES);

        synchronized (dataByUUID) {
            for (UUID uuid: dataByUUID.keySet()) {
                savePlayerData(uuid);
            }
        }
    }
}
