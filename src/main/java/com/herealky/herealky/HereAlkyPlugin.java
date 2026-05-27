package com.herealky.herealky;

import com.herealky.herealky.auraskills.AuraSkillsHelper;
import com.herealky.herealky.command.HereAlkyCommand;
import com.herealky.herealky.command.SetupWizardCommand;
import com.herealky.herealky.config.BrewConfigListener;
import com.herealky.herealky.config.BrewConfigManager;
import com.herealky.herealky.config.BrewConfigUI;
import com.herealky.herealky.listener.BrewListener;
import com.herealky.herealky.listener.SelectionListener;
import com.herealky.herealky.listener.SetupWizardListener;
import com.herealky.herealky.selection.SelectionManager;
import com.herealky.herealky.setup.SetupManager;
import com.herealky.herealky.task.BrewTaskManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class HereAlkyPlugin extends JavaPlugin {

    private static HereAlkyPlugin instance;
    
    private SelectionManager selectionManager;
    private SetupManager setupManager;
    private BrewConfigManager brewConfigManager;
    private BrewConfigUI brewConfigUI;
    private AuraSkillsHelper auraSkillsHelper;
    private BrewTaskManager brewTaskManager;
    private SetupWizardCommand setupWizardCommand;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.selectionManager = new SelectionManager(this);
        this.setupManager = new SetupManager(this);
        this.brewConfigManager = new BrewConfigManager(this);
        this.brewConfigUI = new BrewConfigUI(this.brewConfigManager);
        this.auraSkillsHelper = new AuraSkillsHelper();
        this.brewTaskManager = new BrewTaskManager();

        this.auraSkillsHelper.init();

        // Register Setup Wizard Command Helper
        this.setupWizardCommand = new SetupWizardCommand(this.setupManager, this);

        // Register Main Command
        getCommand("herealky").setExecutor(new HereAlkyCommand(
                this.selectionManager,
                this.setupManager,
                this.brewConfigManager,
                this.brewTaskManager,
                this.auraSkillsHelper,
                this.setupWizardCommand,
                this.brewConfigUI
        ));

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new SelectionListener(this.selectionManager), this);
        getServer().getPluginManager().registerEvents(new SetupWizardListener(this.setupManager, this.setupWizardCommand), this);
        getServer().getPluginManager().registerEvents(new BrewConfigListener(this.brewConfigUI, this.brewConfigManager), this);
        getServer().getPluginManager().registerEvents(new BrewListener(this), this);

        // Start Setup Timeout Task Loop (every second)
        new BukkitRunnable() {
            @Override
            public void run() {
                setupWizardCommand.checkTimeouts();
            }
        }.runTaskTimer(this, 0L, 20L);

        getLogger().info("HereAlky enabled!");
    }

    @Override
    public void onDisable() {
        if (brewTaskManager != null) {
            brewTaskManager.stopAllTasks();
        }
        getLogger().info("HereAlky disabled!");
    }

    public static HereAlkyPlugin getInstance() {
        return instance;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public SetupManager getSetupManager() {
        return setupManager;
    }

    public BrewConfigManager getBrewConfigManager() {
        return brewConfigManager;
    }

    public BrewConfigUI getBrewConfigUI() {
        return brewConfigUI;
    }

    public AuraSkillsHelper getAuraSkillsHelper() {
        return auraSkillsHelper;
    }

    public BrewTaskManager getBrewTaskManager() {
        return brewTaskManager;
    }
}
