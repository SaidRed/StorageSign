package wacky.storagesign;

import com.github.teruteru128.logger.Logger;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.plugin.java.JavaPlugin;
import wacky.storagesign.event.*;
import wacky.storagesign.signdefinition.SignDefinition;

public class StorageSignCore extends JavaPlugin implements Listener {

  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(StorageSignCore.class);
  static BannerMeta ominousBannerMeta;
  public Logger logger;
  private boolean _fallingBlockSS;

  @Override
  public void onEnable() {
    // ConfigLoader初期化
    ConfigLoader.setup(this);

    // ロガーの初期設定
    String logLevel = ConfigLoader.getLogLevel();
    // ログ取得
    Logger.register(this, logLevel);
    logger = Logger.getInstance(this);

    logger.debug("★onEnable:Start");
//		logger.fatal("serverLog");
//		logger.error("errorLog");
//		logger.warn("warnLog");
//		logger.info("infoLog");
//		logger.debug("debugLog");
//		logger.trace("traceLog");

    //鯖別レシピが実装されたら
    logger.trace("hardrecipe:" + ConfigLoader.getHardRecipe());
//    Iterator<Material> it = SignDefinition.sign_materials.iterator();

//    while(it.hasNext()){
//      Material mat = it.next();
    for(Material mat : SignDefinition.signs){
      logger.trace("signRecipi name:" + mat);

      ShapedRecipe storageSignRecipe = new ShapedRecipe(
              new NamespacedKey(this, "ssr" + mat.toString()), StorageSignV2.emptyStorageSign(mat,logger)
      );
      //ShapedRecipe storageSignRecipe = new ShapedRecipe(StorageSignV2.emptyStorageSign(mat,logger));
      storageSignRecipe.shape("CCC", "CSC", "CHC");
      storageSignRecipe.setIngredient('C', Material.CHEST);
      storageSignRecipe.setIngredient('S', mat);

      storageSignRecipe.setIngredient('H', ConfigLoader.getHardRecipe() ? Material.ENDER_CHEST : Material.CHEST);

      storageSignRecipe.setCategory(CraftingBookCategory.BUILDING);
      storageSignRecipe.setGroup("StorageSign");

      getServer().addRecipe(storageSignRecipe);
      logger.trace(mat + "StorageSign Recipe added.");
    }

    logger.trace("setEvent");
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(new StorageSignPlayerEvent(this), this);
    getServer().getPluginManager().registerEvents(new StorageSignBlockPlaceBreak(this), this);
    getServer().getPluginManager().registerEvents(new StorageSignPickupItemEvent(this), this);
    getServer().getPluginManager().registerEvents(new StorageSignItemMoveEvent(this), this);
    logger.trace("no-bud:" + ConfigLoader.getNoBud());
    if (ConfigLoader.getNoBud()) {
      logger.trace("no-bud is True.");
      new SignPhysicsEvent(this, logger);
    }
    _fallingBlockSS = ConfigLoader.getFallingBlockItemSs();

    logger.debug("★onEnable:End");
  }

  @Override
  public void onDisable() {
  }




  public boolean isHorseEgg(ItemStack item) {
    logger.debug("isHorseEgg: Start");
    if (item.getType() != Material.GHAST_SPAWN_EGG) {
      logger.debug("This item isn't HorseEgg.");
      return false;
    }
    if (item.getItemMeta().hasLore()) {
      logger.debug("This item is HorseEgg.");
      return true;
    }

    logger.debug("This item isn't HorseEgg.");
    return false;
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    logger.debug("★onSignChangeEvent:Start");

    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }
    Sign sign = (Sign) event.getBlock().getState();

    logger.trace("sign.getSide(Side.FRONT).getLine(0):" + sign.getSide(Side.FRONT).getLine(0));
    if (sign.getSide(Side.FRONT).getLine(0).matches("StorageSign"))/*変更拒否*/ {
      event.setLine(0, sign.getSide(Side.FRONT).getLine(0));
      event.setLine(1, sign.getSide(Side.FRONT).getLine(1));
      event.setLine(2, sign.getSide(Side.FRONT).getLine(2));
      event.setLine(3, sign.getSide(Side.FRONT).getLine(3));
      sign.update();
    } else if (event.getLine(0).equalsIgnoreCase("storagesign"))/*書き込んで生成禁止*/ {
      logger.debug("Line 0 Str is storagesign");

      logger.trace("event.getPlayer().hasPermission(\"storagesign.create\")" + event.getPlayer().hasPermission("storagesign.create"));
      if (event.getPlayer().hasPermission("storagesign.create")) {
        logger.debug("This user has Permission.storagesign.create. make StorageSign.");
        event.setLine(0, "StorageSign");
        sign.update();
      } else {
        logger.debug("This user hasn't Permission.storagesign.create");
        event.getPlayer().sendMessage(ChatColor.RED + ConfigLoader.getNoPermission());
        event.setCancelled(true);
      }
    }
    logger.debug("★onSignChangeEvent:End");
  }

  @EventHandler
  public void onPlayerSignOpen(PlayerSignOpenEvent event){
    logger.debug("★onPlayerSignOpen:Start");

    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }

    Block block = event.getSign().getBlock();
    boolean isStorageSign = StorageSignV2.isStorageSign(block);
    logger.trace("isStorageSign: " + isStorageSign);
    if (isStorageSign) {
      logger.debug("StorageSignEdit Cancel.");
      event.setCancelled(true);
    }

    logger.debug("★onPlayerSignOpen:End");
  }


  @EventHandler
  public void onPlayerCraft(CraftItemEvent event) {
    logger.debug("★onPlayerCraft:Start");
    logger.trace("isStorageSign(event.getCurrentItem()): " + StorageSign.isStorageSign(event.getCurrentItem(), logger));
    logger.trace("!event.getWhoClicked().hasPermission(\"storagesign.craft\")" + !event.getWhoClicked().hasPermission("storagesign.craft"));
    if (StorageSignV2.isStorageSign(event.getCurrentItem()) &&
            !event.getWhoClicked().hasPermission("storagesign.craft")) {
      logger.debug("This user hasn't Permission. storagesign.craft.");
      ((CommandSender) event.getWhoClicked()).sendMessage(
          ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
    }
    logger.debug("★onPlayerCraft:End");
  }

}