package seedu.address;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.stage.Stage;
import seedu.address.commons.core.Config;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Version;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.ConfigUtil;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AnalyticModel;
import seedu.address.model.ExpenseTracker;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyExpenseTracker;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.util.SampleExpenseTracker;
import seedu.address.storage.ExpenseTrackerStorage;
import seedu.address.storage.JsonExpenseTrackerStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.Storage;
import seedu.address.storage.StorageManager;
import seedu.address.storage.UserPrefsStorage;
import seedu.address.ui.Ui;
import seedu.address.ui.UiManager;

/**
 * Runs the application.
 */
public class MainApp extends Application {

    public static final Version VERSION = new Version(1, 2, 1, true);

    private static final Logger logger = LogsCenter.getLogger(MainApp.class);

    protected Ui ui;
    protected Logic logic;
    protected Storage storage;
    protected Model dataModel;
    protected AnalyticModel analyticModel;
    protected Config config;

    @Override
    public void init() throws Exception {
        logger.info("=============================[ Initializing ExpenseTracker ]===========================");
        super.init();

        AppParameters appParameters = AppParameters.parse(getParameters());
        config = initConfig(appParameters.getConfigPath());

        UserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(config.getUserPrefsFilePath());
        UserPrefs userPrefs = initPrefs(userPrefsStorage);
        ExpenseTrackerStorage expenseTrackerStorage = new JsonExpenseTrackerStorage(
                userPrefs.getExpenseTrackerFilePath());
        storage = new StorageManager(expenseTrackerStorage, userPrefsStorage);

        initLogging(config);

        dataModel = initModelManager(storage, userPrefs);

        logic = new LogicManager(dataModel, storage);

        ui = new UiManager(logic);
    }

    /**
     * Returns a {@code ModelManager} with the data from {@code storage}'s expense
     * tracker and {@code userPrefs}. <br>
     * The data from the sample expense tracker will be used instead if
     * {@code storage}'s expense tracker is not found,
     * or an empty expense tracker will be used instead if errors occur when reading
     * {@code storage}'s expense tracker.
     */
    private Model initModelManager(Storage storage, ReadOnlyUserPrefs userPrefs) {
        Optional<ReadOnlyExpenseTracker> expenseTrackerOptional;
        ReadOnlyExpenseTracker initialData;
        try {
            expenseTrackerOptional = storage.readExpenseTracker();
            if (!expenseTrackerOptional.isPresent()) {
                logger.info("Data file not found. Will be starting with a sample ExpenseTracker");
            }
            // TODO update sample data
            initialData = expenseTrackerOptional.orElseGet(SampleExpenseTracker::getSampleExpenseTracker);
            // initialData =
            // addressBookOptional.orElseGet(SampleDataUtil::getSampleCategoryData);
            // initialData =
            // addressBookOptional.orElseGet(SampleDataUtil::getSampleAddressBook);
        } catch (DataConversionException e) {
            logger.warning("Data file not in the correct format. Will be starting with an empty ExpenseTracker");
            initialData = new ExpenseTracker();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty ExpenseTracker");
            initialData = new ExpenseTracker();
        }

        return new ModelManager(initialData, userPrefs);
    }

    private void initLogging(Config config) {
        LogsCenter.init(config);
    }

    /**
     * Returns a {@code Config} using the file at {@code configFilePath}. <br>
     * The default file path {@code Config#DEFAULT_CONFIG_FILE} will be used instead
     * if {@code configFilePath} is null.
     */
    protected Config initConfig(Path configFilePath) {
        Config initializedConfig;
        Path configFilePathUsed;

        configFilePathUsed = Config.DEFAULT_CONFIG_FILE;

        if (configFilePath != null) {
            logger.info("Custom Config file specified " + configFilePath);
            configFilePathUsed = configFilePath;
        }

        logger.info("Using config file : " + configFilePathUsed);

        try {
            Optional<Config> configOptional = ConfigUtil.readConfig(configFilePathUsed);
            initializedConfig = configOptional.orElse(new Config());
        } catch (DataConversionException e) {
            logger.warning("Config file at " + configFilePathUsed + " is not in the correct format. "
                    + "Using default config properties");
            initializedConfig = new Config();
        }

        // Update config file in case it was missing to begin with or there are
        // new/unused fields
        try {
            ConfigUtil.saveConfig(initializedConfig, configFilePathUsed);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }
        return initializedConfig;
    }

    /**
     * Returns a {@code UserPrefs} using the file at {@code storage}'s user prefs
     * file path,
     * or a new {@code UserPrefs} with default configuration if errors occur when
     * reading from the file.
     */
    protected UserPrefs initPrefs(UserPrefsStorage storage) {
        Path prefsFilePath = storage.getUserPrefsFilePath();
        logger.info("Using prefs file : " + prefsFilePath);

        UserPrefs initializedPrefs;
        try {
            Optional<UserPrefs> prefsOptional = storage.readUserPrefs();
            initializedPrefs = prefsOptional.orElse(new UserPrefs());
        } catch (DataConversionException e) {
            logger.warning("UserPrefs file at " + prefsFilePath + " is not in the correct format. "
                    + "Using default user prefs");
            initializedPrefs = new UserPrefs();
        } catch (IOException e) {
            logger.warning("Problem while reading from the file. Will be starting with an empty ExpenseTracker");
            initializedPrefs = new UserPrefs();
        }

        // Update prefs file in case it was missing to begin with or there are
        // new/unused fields
        try {
            storage.saveUserPrefs(initializedPrefs);
        } catch (IOException e) {
            logger.warning("Failed to save config file : " + StringUtil.getDetails(e));
        }

        return initializedPrefs;
    }

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting ExpenseTracker " + MainApp.VERSION);
        ui.start(primaryStage);
    }

    @Override
    public void stop() {
        logger.info("============================ [ Stopping Expense Tracker ] =============================");
        try {
            storage.saveUserPrefs(dataModel.getUserPrefs());
        } catch (IOException e) {
            logger.severe("Failed to save preferences " + StringUtil.getDetails(e));
        }
    }
}
