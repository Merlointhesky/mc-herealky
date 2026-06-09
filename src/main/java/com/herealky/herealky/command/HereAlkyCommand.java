package com.herealky.herealky.command;

import com.herealky.herealky.HereAlkyPlugin;
import com.herealky.herealky.auraskills.AuraSkillsHelper;
import com.herealky.herealky.config.BrewConfigManager;
import com.herealky.herealky.config.BrewConfigUI;
import com.herealky.herealky.config.BrewRecipe;
import com.herealky.herealky.config.PlayerBrewConfig;
import com.herealky.herealky.selection.SelectionManager;
import com.herealky.herealky.setup.SetupConfiguration;
import com.herealky.herealky.setup.SetupManager;
import com.herealky.herealky.task.BrewTask;
import com.herealky.herealky.task.BrewTaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HereAlkyCommand implements CommandExecutor {

    private final SelectionManager selectionManager;
    private final SetupManager setupManager;
    private final BrewConfigManager configManager;
    private final BrewTaskManager taskManager;
    private final AuraSkillsHelper auraSkillsHelper;
    private final SetupWizardCommand setupWizardCommand;
    private final BrewConfigUI configUI;

    public HereAlkyCommand(SelectionManager selectionManager, SetupManager setupManager, BrewConfigManager configManager,
                           BrewTaskManager taskManager, AuraSkillsHelper auraSkillsHelper,
                           SetupWizardCommand setupWizardCommand, BrewConfigUI configUI) {
        this.selectionManager = selectionManager;
        this.setupManager = setupManager;
        this.configManager = configManager;
        this.taskManager = taskManager;
        this.auraSkillsHelper = auraSkillsHelper;
        this.setupWizardCommand = setupWizardCommand;
        this.configUI = configUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "start" -> handleStart(player);
            case "stop" -> handleStop(player);
            case "select" -> handleSelect(player);
            case "config" -> handleConfig(player);
            case "setup" -> handleSetup(player);
            case "info" -> handleInfo(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleStart(Player player) {
        UUID uuid = player.getUniqueId();

        // 1. Setup Validation
        if (!setupManager.hasSetupConfig(uuid)) {
            player.sendMessage(Component.text("❌ Setup incomplete! Run '/ha setup' to map containers and water source first.")
                    .color(NamedTextColor.RED));
            return;
        }

        SetupConfiguration setupConfig = setupManager.getSetupConfig(uuid);
        PlayerBrewConfig playerConfig = configManager.getPlayerConfig(uuid);
        BrewRecipe recipe = playerConfig.getSelectedRecipe();

        // 2. Recipe Selection Validation
        if (recipe == null) {
            player.sendMessage(Component.text("❌ No recipe selected! Run '/ha config' to choose a potion first.")
                    .color(NamedTextColor.RED));
            return;
        }

        // 3. Bounding Stand Scan / Register Validation
        if (!selectionManager.hasCompleteSelection(uuid)) {
            selectionManager.setSelectionMode(uuid, true);
            player.sendMessage(Component.text("❌ Stands missing! Selection Mode auto-enabled.")
                    .color(NamedTextColor.RED)
                    .append(Component.text("\nHold Empty Glass Bottle and Shift-Right-Click two blocks to set Point A and Point B surrounding your brewing stands.").color(NamedTextColor.GREEN)));
            return;
        }

        Location pA = selectionManager.getPointA(uuid);
        Location pB = selectionManager.getPointB(uuid);

        if (!pA.getWorld().equals(pB.getWorld())) {
            player.sendMessage(Component.text("❌ Bounding points must be in the same world!").color(NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Scanning region for brewing stands...").color(NamedTextColor.GOLD));
        List<Location> stands = scanStands(pA, pB);

        if (stands.isEmpty()) {
            player.sendMessage(Component.text("❌ No brewing stands found inside selection region! Place stands, then restart.").color(NamedTextColor.RED));
            return;
        }

        // Save stands in player configuration
        playerConfig.setRegisteredStands(stands);
        configManager.saveConfiguration(uuid);

        // Terminate any previous runs
        if (taskManager.isBrewing(player)) {
            taskManager.stopTask(player, "Starting a new batch run.", NamedTextColor.YELLOW);
        }

        // Launch BrewTask loop
        BrewTask task = new BrewTask(HereAlkyPlugin.getInstance(), player, playerConfig, setupConfig);
        taskManager.startTask(player, task);

        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("     Brewing Batch Initiated!     ").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text(" ✔ Potion Recipe: " + recipe.getDisplayName()).color(NamedTextColor.GREEN));
        player.sendMessage(Component.text(" ✔ Bounding stands registered: " + stands.size()).color(NamedTextColor.GREEN));
        player.sendMessage(Component.text(" ✔ Mode: In-Place batch brewing (Manual fuel required)").color(NamedTextColor.GREEN));
        player.sendMessage(Component.text(" Type '/ha stop' at any time to pause and show stats.").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
    }

    private void handleStop(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Stop active wizard
        if (setupManager.isInSetup(uuid)) {
            setupManager.cancelSetup(uuid);
            setupWizardCommand.removeActivity(uuid);
            player.sendMessage(Component.text("Setup wizard cancelled.").color(NamedTextColor.YELLOW));
            return;
        }

        // Stop active brewing task
        if (!taskManager.isBrewing(player)) {
            player.sendMessage(Component.text("Auto-brewing is not currently active.").color(NamedTextColor.RED));
            return;
        }

        taskManager.stopTask(player, "Manual stop request.", NamedTextColor.YELLOW);
    }

    private void handleSelect(Player player) {
        UUID uuid = player.getUniqueId();
        boolean current = selectionManager.isSelectionMode(uuid);
        selectionManager.setSelectionMode(uuid, !current);

        if (!current) {
            player.sendMessage(Component.text("Selection Mode ENABLED! Hold Empty Glass Bottle and Shift-Right-Click blocks to set Point A and Point B.")
                    .color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Selection Mode DISABLED.").color(NamedTextColor.YELLOW));
        }
    }

    private void handleConfig(Player player) {
        configUI.openMainMenu(player);
    }

    private void handleSetup(Player player) {
        setupWizardCommand.onCommand(player);
    }

    private void handleInfo(Player player) {
        UUID uuid = player.getUniqueId();
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("       HereAlky Potion Stats       ").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));

        // AuraSkills check
        if (auraSkillsHelper != null && auraSkillsHelper.isAvailable()) {
            player.sendMessage(Component.text(" ★ Alchemy Level: " + auraSkillsHelper.getAlchemyLevel(player)).color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text(" ★ Alchemy Skill XP: " + String.format("%.1f", auraSkillsHelper.getAlchemyXp(player))).color(NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text(" ★ AuraSkills plugin: Inactive").color(NamedTextColor.RED));
        }

        PlayerBrewConfig pConfig = configManager.getPlayerConfig(uuid);
        player.sendMessage(Component.text(" ✔ Potion Configured: " + (pConfig.getSelectedRecipe() != null ? pConfig.getSelectedRecipe().getDisplayName() : "None")).color(NamedTextColor.GRAY));
        player.sendMessage(Component.text(" ✔ Bounding stands: " + pConfig.getRegisteredStands().size()).color(NamedTextColor.GRAY));

        if (setupManager.hasSetupConfig(uuid)) {
            SetupConfiguration s = setupManager.getSetupConfig(uuid);
            player.sendMessage(Component.text(" ✔ Ingredient Box: " + formatLoc(s.getIngredientBox())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text(" ✔ Empty Bottle Box: " + formatLoc(s.getEmptyBottleBox())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text(" ✔ Output Box: " + formatLoc(s.getOutputBox())).color(NamedTextColor.GRAY));
            player.sendMessage(Component.text(" ✔ Water Source: " + formatLoc(s.getWaterSource())).color(NamedTextColor.GRAY));
        } else {
            player.sendMessage(Component.text(" ✔ Setup Status: Incomplete (Run '/ha setup')").color(NamedTextColor.RED));
        }
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
    }

    private List<Location> scanStands(Location pA, Location pB) {
        List<Location> stands = new ArrayList<>();
        World world = pA.getWorld();
        int minX = Math.min(pA.getBlockX(), pB.getBlockX());
        int maxX = Math.max(pA.getBlockX(), pB.getBlockX());
        int minY = Math.min(pA.getBlockY(), pB.getBlockY());
        int maxY = Math.max(pA.getBlockY(), pB.getBlockY());
        int minZ = Math.min(pA.getBlockZ(), pB.getBlockZ());
        int maxZ = Math.max(pA.getBlockZ(), pB.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.BREWING_STAND) {
                        stands.add(block.getLocation());
                    }
                }
            }
        }
        return stands;
    }

    private String formatLoc(Location loc) {
        if (loc == null) return "None";
        return "[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]";
    }

    private void sendHelp(Player player) {
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text("       HereAlky Command Help       ").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
        player.sendMessage(Component.text(" /ha start - Start batch-brewing inside selection bounds").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" /ha stop - Stop active brewing task or setup wizard").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" /ha select - Toggle bottle selection mode (Point A & B bounds)").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" /ha config - Open potion recipe GUI").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" /ha setup - Run 4-step container mapping wizard").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text(" /ha info - Display alchemy level, recipe, and setup configurations").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("-----------------------------------").color(NamedTextColor.GRAY));
    }
}
