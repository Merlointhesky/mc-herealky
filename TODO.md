# HereAlky - Plugin Technical Design Document

## 1. Overview
**Plugin Name:** HereAlky (Prefix: `herealky`)
**Core Concept:** A semi-automated alchemy plugin that streamlines multi-stage potion brewing while maintaining player engagement and ensuring experience point (XP) collection. It utilizes a guided UI and physical container mapping to process potions from empty bottles to fully modified brews.

## 2. Core Commands
* `/herealky info`: Displays the player's current alchemy level and stats.
* `/herealky select`: Toggles a dedicated selection mode to prevent hijacking normal gameplay interactions.
* `/herealky setup`: Initiates the setup process to map out physical locations (ingredient boxes, empty bottle box, output box, and water source).
* `/herealky config`: Opens an interactive GUI for the player to select the desired potion to brew and sets up the current batch.
* `/herealky start`: Initiates the brewing loop once all ingredients and bottles are prepared.

## 3. Selection Mechanics & Tools
* **Selection Item:** An **Empty Glass Bottle**. The player must hold this in their main hand while in `/herealky select` mode to register blocks.
* **Brewing Stand Validation:** When clicking brewing stands to assign them to a stage row, the plugin will validate the block type to ensure only brewing stands are registered.
* **Smart Water Source Detection:** Because players cannot directly click liquid blocks easily, the plugin will feature a smart scan. When a block is clicked during the water source setup, the algorithm will check adjacent blocks (up, down, north, south, east, west) to locate a valid water source (e.g., water source block, waterlogged block, or cauldron).

## 4. The Brewing Workflow
The plugin breaks down potion making into a structured, multi-stage batch process:

### Phase A: Setup & Configuration
1.  **Station Setup:** The player uses `/herealky setup` and the Empty Glass Bottle to define:
    * The input box for Empty Glass Bottles.
    * The water source for filling.
    * Row 1 of brewing stands (Stage 1) + its ingredient box.
    * Row 2 of brewing stands (Stage 2) + its ingredient box.
    * Row 3 of brewing stands (Stage 3) + its ingredient box.
    * The final output box for finished potions.
2.  **Batch Configuration:** The player runs `/herealky config`, opening an inventory GUI containing a comprehensive list of potion recipes. 
3.  **Player Guidance:** Upon selecting a potion (e.g., Extended Potion of Slow Falling), the plugin outputs chat messages instructing the player where to place ingredients. *(Example: "Place Nether Wart in Box 1. Place Phantom Membrane in Box 2. Place Redstone in Box 3.")*

### Phase B: Execution
1.  **Activation:** Once boxes are stocked, the player types `/herealky start`.
2.  **Automation Loop:** * The system facilitates picking up empty bottles from the input box, "filling" them at the designated water source, and routing them to the Stage 1 brewing stands.
    * The plugin listens for brewing completion events via the Spigot/Paper API.
    * Once a stage finishes, the plugin programmatically moves the output potions to the next stage's brewing stands, applying the next box's ingredients.
    * *Implementation Note for IDE:* Special care must be taken regarding the Spigot/Paper API to handle the transfer of potions and ensure the player is still credited with the appropriate brewing XP when the final potions drop into the collection box.