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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.java.JavaPlugin;
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
    Iterator<Material> it = SignDefinition.sign_materials.iterator();
    while(it.hasNext()){
      Material mat = it.next();
      logger.trace("signRecipi name:" + mat);

      ShapedRecipe storageSignRecipe = new ShapedRecipe(
              new NamespacedKey(this, "ssr" + mat.toString()), StorageSign.emptySign(mat)
      );
      //ShapedRecipe storageSignRecipe = new ShapedRecipe(StorageSign.emptySign());
      storageSignRecipe.shape("CCC", "CSC", "CHC");
      storageSignRecipe.setIngredient('C', Material.CHEST);
      storageSignRecipe.setIngredient('S', mat);

      storageSignRecipe.setIngredient('H', ConfigLoader.getHardRecipe() ? Material.ENDER_CHEST : Material.CHEST);
      getServer().addRecipe(storageSignRecipe);
      logger.trace(mat + "StorageSign Recipe added.");
    }

    logger.trace("setEvent");
    getServer().getPluginManager().registerEvents(this, this);
    getServer().getPluginManager().registerEvents(new StorageSignPlayerEvent(this), this);

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

  @EventHandler(priority = EventPriority.HIGH)
  public void onBlockBreak(BlockBreakEvent event) {
    logger.debug("★onBlockBreak:Start");

    logger.trace("event.isCancelled(): " + event.isCancelled());
		if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
      return;
    }
    logger.trace("!event.getPlayer().hasPermission(\"storagesign.break\"): " + !event.getPlayer().hasPermission("storagesign.break"));
    if (!event.getPlayer().hasPermission("storagesign.break")) {
      logger.debug("★This user hasn't Permission. storagesign.break.");
      event.getPlayer().sendMessage(ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
      return;
    }

    dropRelativeSign(event.getBlock());

    logger.debug("★onBlockBreak:End.");
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event) {
    logger.debug("★onBlockPlace: Start");

    boolean handItemIsSS = StorageSign.isStorageSign(event.getItemInHand(), logger);
    logger.trace("event.isCancelled(): " + event.isCancelled());
    logger.trace("!handItemIsSS: " + !handItemIsSS);
		if (event.isCancelled() || !handItemIsSS) {
      logger.debug("★this Event is Cancelled!");
			return;
		}
    Player player = event.getPlayer();
    logger.trace("!player.hasPermission(\"storagesign.place\"): " + !player.hasPermission("storagesign.place"));
    if (!player.hasPermission("storagesign.place")) {
      logger.debug("★This user hasn't Permission. storagesign.place.");
      player.sendMessage(ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
      return;
    }

    logger.debug("StorageSign Placed in World.");
    StorageSign storageSign = new StorageSign(event.getItemInHand(), logger);
    Sign sign = (Sign) event.getBlock().getState();
		for (int i = 0; i < 4; i++) {
      logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
			sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
		}

    logger.trace("storageSign.getSmat() == Material.DARK_OAK_SIGN: " + (storageSign.getSmat() == Material.DARK_OAK_SIGN));
    if (storageSign.getSmat() == Material.DARK_OAK_SIGN) {
      logger.debug("This Sign is DarkOak Sign.0");
      sign.getSide(Side.FRONT).setColor(DyeColor.WHITE);//文字色を白くする
    }

    logger.debug("UpdateSign.");
    sign.update();

    //時差発動が必要らしい
    player.closeInventory();
    logger.debug("★onBlockPlace: End");
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
    boolean isStorageSign = StorageSign.isStorageSign(block, logger);
    logger.trace("isStorageSign: " + isStorageSign);
    if (isStorageSign) {
      logger.debug("StorageSignEdit Cancel.");
      event.setCancelled(true);
    }

    logger.debug("★onPlayerSignOpen:End");
  }


  @EventHandler
  public void onItemMove(InventoryMoveItemEvent event) {
    logger.debug("★onItemMove: Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
		if (event.isCancelled()) {
      logger.debug("★this Event is Cancelled!");
			return;
		}

    logger.debug("ItemMoveEvent check");
    BlockState[] blockInventory = new BlockState[2];
    Boolean flag = false;
    Sign sign = null;
    StorageSign storageSign = null;
    ItemStack item = event.getItem();
    logger.trace("config.getBoolean(\"auto-import\"): " + ConfigLoader.getAutoImport());
    if (ConfigLoader.getAutoImport()) {
      logger.debug("auto-import Start");
      logger.trace("event.getDestination().getLocation(): " + event.getDestination().getLocation());
      logger.trace("event.getDestination().getHolder(): " + event.getDestination().getHolder());
      if (event.getDestination().getLocation() == null){
        logger.debug("This Event is Temp Inventory.");
        //コンポスター用に生成された一時インベントリ
      } else if (event.getDestination().getHolder() instanceof Minecart){
        //何もしない
        logger.debug("This Event is Minecart.");
      } else if (event.getDestination().getHolder() instanceof DoubleChest) {
        logger.debug("This Event is DoubleChest.");
        DoubleChest lc = (DoubleChest) event.getDestination().getHolder();
        blockInventory[0] = (BlockState) lc.getLeftSide();
        blockInventory[1] = (BlockState) lc.getRightSide();
      } else if (!(event.getDestination().getHolder() instanceof BlockState)){
        //ブロック情報が取得できない場合も何もしない
        logger.debug("This Event not get BlockState.");
      } else {
        logger.debug("BlockState Set.");
        blockInventory[0] = (BlockState) event.getDestination().getHolder();
        logger.trace("blockInventory[0]: " + blockInventory[0]);
      }

      logger.debug("importLoop Start.");
      importLoop:
      for (int j = 0; j < 2; j++) {
        logger.trace("j: " + j);
        logger.trace("blockInventory[j]: " + blockInventory[j]);
				if (blockInventory[j] == null) {
          logger.debug("This Inventory is NULL.");
					break;
				}
        logger.debug("BlockFaceCheck Start.");
        for (int i = 0; i < 5; i++) {
          BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
              BlockFace.WEST};
          Block block = blockInventory[j].getBlock().getRelative(face[i]);
          boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
          boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          logger.trace("blockInventory[j].getBlock(): "+ blockInventory[j].getBlock());
          logger.trace("i: " + i);
          logger.trace("face[i]: " + face[i]);
          logger.trace("block: " + block);
          logger.trace("relIsSignPost: " + relIsSignPost);
          logger.trace("relIsStorageSign: " + relIsStorageSign);
          logger.trace("relIsWallSign: " + relIsWallSign);
          if (relIsWallSign) {
            logger.trace("((WallSign) block.getBlockData()).getFacing(): "
                + ((WallSign) block.getBlockData()).getFacing());
          }
          if (i == 0 && relIsSignPost && relIsStorageSign) {
            if (item.getType() == Material.WHITE_BANNER) {
              //
              //襲撃バナー用
              //
            }
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              flag = true;
              break importLoop;
            }
          } else if (i != 0 && relIsWallSign
              && ((WallSign) block.getBlockData()).getFacing() == face[i] && relIsStorageSign) {
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              flag = true;
              break importLoop;
            }
          }
        }
      }
      //搬入先が見つかった(搬入するとは言ってない)
			if (flag) {
        logger.debug("Import Sign.");
				importSign(sign, storageSign, item, event.getDestination());
			}
    }

    //搬出用にリセット
    logger.debug("config.getBoolean(\"auto-export\"): " + ConfigLoader.getAutoExport());
    if (ConfigLoader.getAutoExport()) {
      blockInventory[0] = null;
      blockInventory[1] = null;
      flag = false;
      logger.trace("event.getSource(): " + event.getSource());
      logger.trace("event.getSource().getLocation(): " + event.getSource().getLocation());
      logger.trace("event.getSource().getHolder(): " + event.getSource().getHolder());
      if (event.getSource().getLocation() == null){
        //一時インベントリ
        logger.debug("This Event is Temp Inventory.");
      } else if (event.getSource().getHolder() instanceof Minecart){
        logger.debug("This Event is Minecart.");
      } else if (event.getSource().getHolder() instanceof DoubleChest) {
        logger.debug("This Event is DoubleChest.");
        DoubleChest lc = (DoubleChest) event.getSource().getHolder();
        blockInventory[0] = (BlockState) lc.getLeftSide();
        blockInventory[1] = (BlockState) lc.getRightSide();
      } else if (!(event.getSource().getHolder() instanceof BlockState)){
        //ブロック情報が取得できない時も何もしない
        logger.debug("This Event not get BlockState.");
      } else {
        logger.debug("BlockState Set.");
        blockInventory[0] = (BlockState) event.getSource().getHolder();
        logger.trace("blockInventory[0]: " + blockInventory[0]);
      }

      exportLoop:
      for (int j = 0; j < 2; j++) {
        logger.trace("j: " + j);
        logger.trace("blockInventory[j]: " + blockInventory[j]);
				if (blockInventory[j] == null) {
					break;
				}
        for (int i = 0; i < 5; i++) {
          BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
              BlockFace.WEST};
          Block block = blockInventory[j].getBlock().getRelative(face[i]);
          boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
          boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          logger.trace("blockInventory[j].getBlock(): "+ blockInventory[j].getBlock());
          logger.trace("i: " + i);
          logger.trace("face[i]: " + face[i]);
          logger.trace("block: " + block);
          logger.trace("relIsSignPost: " + relIsSignPost);
          logger.trace("relIsStorageSign: " + relIsStorageSign);
          logger.trace("relIsWallSign: " + relIsWallSign);
          if (relIsWallSign) {
            logger.trace("((WallSign) block.getBlockData()).getFacing(): "
                + ((WallSign) block.getBlockData()).getFacing());
          }
          if (i == 0 && relIsSignPost && relIsStorageSign) {
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              flag = true;
              break exportLoop;
            }
          } else if (i != 0 && relIsWallSign
              && ((WallSign) block.getBlockData()).getFacing() == face[i] && relIsStorageSign) {
            sign = (Sign) block.getState();
            storageSign = new StorageSign(sign, block.getType(), logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              flag = true;
              break exportLoop;
            }
          }
        }
      }
			if (flag) {
        logger.debug("Export Sign.");
				exportSign(sign, storageSign, item, event.getSource(), event.getDestination());
			}
    }
    logger.debug("★onItemMove:End.");
  }

  private void importSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv) {
    logger.debug("importSign:Start");
    //搬入　条件　1スタック以上アイテムが入っている
    logger.trace("inv.containsAtLeast(item, item.getMaxStackSize()): " + inv.containsAtLeast(item, item.getMaxStackSize()));
    if (inv.containsAtLeast(item, item.getMaxStackSize())) {
      logger.debug("Item is more 1s.Import Item.");
      inv.removeItem(item);
      storageSign.addAmount(item.getAmount());
    }
		for (int i = 0; i < 4; i++) {
      logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
			sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
		}
    logger.debug("update Sign.");
    sign.update();
  }

  //搬出先ブロックに枠指定があると事故る
  private void exportSign(Sign sign, StorageSign storageSign, ItemStack item, Inventory inv,
      Inventory dest) {
    logger.debug("exportSign:Start.");
    logger.trace("item: " + item);
    logger.trace("!inv.containsAtLeast(item, item.getMaxStackSize(): " + !inv.containsAtLeast(item, item.getMaxStackSize()));
    logger.trace("storageSign.getAmount(): " + storageSign.getAmount());
    logger.trace("item.getAmount(): " + item.getAmount());
    if (!inv.containsAtLeast(item, item.getMaxStackSize())
        && storageSign.getAmount() >= item.getAmount()) {
      int stacks = 0;
      int amount = 0;
      ItemStack[] contents = dest.getContents();

      logger.trace("dest.getType(): " + dest.getType());
      logger.trace("item.getType(): " + item.getType());
      // TODO 1.21でアイテム追加。
      if (dest.getType() == InventoryType.BREWING) {
        switch (item.getType()) {
          case NETHER_WART://上
          case SUGAR:
          case REDSTONE:
          case GLOWSTONE_DUST:
          case GUNPOWDER:
          case RABBIT_FOOT:
          case GLISTERING_MELON_SLICE:
          case GOLDEN_CARROT:
          case MAGMA_CREAM:
          case GHAST_TEAR:
          case SPIDER_EYE:
          case FERMENTED_SPIDER_EYE:
          case DRAGON_BREATH:
          case PUFFERFISH:
          case TURTLE_HELMET:
          case PHANTOM_MEMBRANE:
            //上から搬入
            logger.debug("This item is PHANTOM_MEMBRANE.");
            logger.trace("inv.getLocation().getBlockY()" + inv.getLocation().getBlockY());
            logger.trace("dest.getLocation().getBlockY()" + dest.getLocation().getBlockY());
            logger.trace("inv.getLocation().getBlockY() > dest.getLocation().getBlockY()" + (inv.getLocation().getBlockY() > dest.getLocation().getBlockY()));
            logger.trace("contents[3]: " + contents[3]);
						if (inv.getLocation().getBlockY() > dest.getLocation().getBlockY()) {
              logger.debug("This item import at up.");
							if (contents[3] != null && !item.isSimilar(contents[3])) {
                //他のアイテムが詰まってる
                logger.debug("This item is clog.");
								return;
							} else {
                logger.debug("This item Export.");
								break;
							}
						} else {
              logger.debug("This item import not.");
							return;
						}

          case BLAZE_POWDER:
            //横or上
            logger.debug("This item is BLAZE_POWDER.");
            logger.trace("inv.getLocation().getBlockY()" + inv.getLocation().getBlockY());
            logger.trace("dest.getLocation().getBlockY()" + dest.getLocation().getBlockY());
            logger.trace("inv.getLocation().getBlockY() > dest.getLocation().getBlockY()" + (inv.getLocation().getBlockY() > dest.getLocation().getBlockY()));
            logger.trace("inv.getLocation().getBlockY() == dest.getLocation().getBlockY(): " + (inv.getLocation().getBlockY() == dest.getLocation().getBlockY()));
            if (inv.getLocation().getBlockY() > dest.getLocation().getBlockY()) {//上から搬入
              logger.debug("This item import at up.");
              logger.trace("contents[3]: " + contents[3]);
							if (contents[3] != null && !item.isSimilar(contents[3])) {
                //他のアイテムが詰まってる
                logger.debug("This item is clog.");
								return;
							} else {
                logger.debug("This item import not.");
								break;
							}
            } else if (inv.getLocation().getBlockY() == dest.getLocation().getBlockY()) {//横
              logger.debug("This item import at beside.");
              logger.trace("contents[4]: " + contents[4]);
							if (contents[4] != null && contents[4].getAmount() == 64) {
                //パウダー詰まり
                logger.debug("This item is clog.");
								return;
							} else {
                logger.debug("This item Export.");
								break;
							}
						} else {
              logger.debug("This item import not.");
							return;
						}

          case POTION:
          case SPLASH_POTION:
          case LINGERING_POTION:
            //横or下
            logger.debug("This item is POTIONS.");
            logger.trace("inv.getLocation().getBlockY()" + inv.getLocation().getBlockY());
            logger.trace("dest.getLocation().getBlockY()" + dest.getLocation().getBlockY());
            logger.trace("inv.getLocation().getBlockY() <= dest.getLocation().getBlockY()" + (inv.getLocation().getBlockY() <= dest.getLocation().getBlockY()));
						if (inv.getLocation().getBlockY() <= dest.getLocation().getBlockY()) {
              logger.debug("This item import at beside or down.");
              logger.trace("contents[0]: " + contents[0]);
              logger.trace("contents[1]: " + contents[1]);
              logger.trace("contents[2]: " + contents[2]);
							if (contents[0] != null && contents[1] != null && contents[2] != null) {
                logger.debug("This item is clog.");
								return;
							} else {
                logger.debug("This item Export.");
								break;
							}
						} else {
              logger.debug("This item import not.");
							return;
						}
          default://ロスト回避
            logger.debug("This Item not support BREW Item.");
            return;
        }
      } else if (dest.getType() == InventoryType.FURNACE
          || dest.getType() == InventoryType.BLAST_FURNACE
          || dest.getType() == InventoryType.SMOKER) {
        logger.debug("This Type is Furnace Series.");
        logger.trace("inv.getLocation().getBlockY()" + inv.getLocation().getBlockY());
        logger.trace("dest.getLocation().getBlockY()" + dest.getLocation().getBlockY());
        logger.trace("inv.getLocation().getBlockY() > dest.getLocation().getBlockY(): " + (inv.getLocation().getBlockY() > dest.getLocation().getBlockY()));
        if (inv.getLocation().getBlockY() > dest.getLocation().getBlockY()) {//上から搬入
          logger.debug("This item import at up.");
          logger.trace("contents[0]: " + contents[0]);
					if (contents[0] != null && !item.isSimilar(contents[0])) {
            //他のアイテムが詰まってる
            logger.debug("This item is clog.");
						return;
					}
        } else {
          //横から(下から)
          logger.debug("This item import at beside or down.");
          logger.trace("contents[1]: " + contents[1]);
					if (!item.getType().isFuel() || contents[1] != null && !item.isSimilar(contents[1])) {
            //燃料以外 or 他のアイテムが詰まってる
            logger.debug("This item is clog or not Fuel.");
						return;
					}
        }
      }

      logger.debug("Export Item");
      //PANPANによるロスト回避
      for (int i = 0; i < contents.length; i++) {
        logger.trace("i: " + i);
        logger.trace("contents[i]: " + contents[i]);
        if (item.isSimilar(contents[i])) {
          stacks++;
          amount += contents[i].getAmount();
          logger.trace(" stacks: " + stacks);
          logger.trace(" amount: " + amount);
        }
      }
      logger.trace("amount == stacks * item.getMaxStackSize(): " + (amount
          == stacks * item.getMaxStackSize()));
      logger.trace("dest.firstEmpty() == -1: " + (dest.firstEmpty() == -1));
      if (amount == stacks * item.getMaxStackSize() && dest.firstEmpty() == -1) {
        logger.debug("Item less than Stack.not Export.");
        return;
      }

      logger.debug("Export Item to Inventory.");
      ItemStack cItem = item.clone();
      inv.addItem(cItem);
      storageSign.addAmount(-cItem.getAmount());
    }
    for (int i = 0; i < 4; i++) {
      logger.trace("set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
    }
    sign.update();
    logger.debug("ExportSign:End");
  }

  @EventHandler
  public void onPlayerCraft(CraftItemEvent event) {
    logger.debug("★onPlayerCraft:Start");
    logger.trace("isStorageSign(event.getCurrentItem()): " + StorageSign.isStorageSign(event.getCurrentItem(), logger));
    logger.trace("!event.getWhoClicked().hasPermission(\"storagesign.craft\")" + !event.getWhoClicked().hasPermission("storagesign.craft"));
    if (StorageSign.isStorageSign(event.getCurrentItem(), logger) && !event.getWhoClicked()
        .hasPermission("storagesign.craft")) {
      logger.debug("This user hasn't Permission. storagesign.craft.");
      ((CommandSender) event.getWhoClicked()).sendMessage(
          ChatColor.RED + ConfigLoader.getNoPermission());
      event.setCancelled(true);
    }
    logger.debug("★onPlayerCraft:End");
  }

  @EventHandler
  public void onInventoryPickup(InventoryPickupItemEvent event) {//ホッパーに投げ込まれたとき
    logger.debug("★onInventoryPickup:Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
    logger.trace("!config.getBoolean(\"auto-import\"): " + !ConfigLoader.getAutoImport());
		if (event.isCancelled() || !ConfigLoader.getAutoImport()) {
      logger.debug("★eventCancelled or not set permission.auto-import.");
			return;
		}

    InventoryHolder holder = event.getInventory().getHolder();
    logger.trace("holder: " + holder);
    if (holder instanceof BlockState) {
      logger.debug("holder has BlockState.");

      Sign sign = null;
      StorageSign storageSign = null;
      boolean flag = false;
      for (int i = 0; i < 5; i++) {
        BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
            BlockFace.WEST};
        Block block = ((BlockState) holder).getBlock().getRelative(face[i]);
        boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
        boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
        boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
        logger.trace(" i: " + i);
        logger.trace(" relIsSignPost: " + relIsSignPost);
        logger.trace(" relIsStorageSign: " + relIsStorageSign);
        logger.trace(" relIsWallSign: " + relIsWallSign);
        logger.trace(" face[i]: " + face[i]);
        if(relIsWallSign) {
          logger.trace(" ((WallSign) block.getBlockData()).getFacing() == face[i]: " + (
              ((WallSign) block.getBlockData()).getFacing() == face[i]));
        }
        if (i == 0 && relIsSignPost && relIsStorageSign) {
          logger.debug(" This block is StorageSign.");
          sign = (Sign) block.getState();
          storageSign = new StorageSign(sign, block.getType(), logger);
          logger.trace(" storageSign.isSimilar(event.getItem().getItemStack()): " + storageSign.isSimilar(event.getItem().getItemStack()));
          if (storageSign.isSimilar(event.getItem().getItemStack())) {
            logger.debug(" this Item is Import Item.");
            flag = true;
            break;
          }
        } else if (i != 0 && relIsWallSign
            && ((WallSign) block.getBlockData()).getFacing() == face[i] && relIsStorageSign) {
          //BlockFaceに変更？(めんどい)
          logger.debug(" This block is WallStorageSign.");
          sign = (Sign) block.getState();
          storageSign = new StorageSign(sign, block.getType(), logger);
          logger.trace(" storageSign.isSimilar(event.getItem().getItemStack()): " + storageSign.isSimilar(event.getItem().getItemStack()));
          if (storageSign.isSimilar(event.getItem().getItemStack())) {
            logger.debug(" this Item is Import Item.");
            flag = true;
            break;
          }
        }
      }
			if (flag) {
        logger.debug("Import Item.");
				importSign(sign, storageSign, event.getItem().getItemStack(), event.getInventory());
			}
    }
    logger.debug("★onInventoryPickup:End");
  }

  @EventHandler
  public void onPlayerPickupItem(EntityPickupItemEvent event) {
    logger.debug("★onPlayerPickupItem:Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
		if (event.isCancelled()) {
      logger.debug("★Event Cancelled.");
			return;
		}

    logger.trace("event.getEntityType(): " + event.getEntityType());
    logger.trace("config.getBoolean(\"autocollect\"): " + ConfigLoader.getAutoCollect());
    if (event.getEntityType() == EntityType.PLAYER && ConfigLoader.getAutoCollect()) {
      Player player = (Player) event.getEntity();
      PlayerInventory playerInv = player.getInventory();
      ItemStack item = event.getItem().getItemStack();
      StorageSign storagesign = null;

      logger.trace("!player.hasPermission(\"storagesign.autocollect\"): " + !player.hasPermission("storagesign.autocollect"));
      //ここでは、エラーを出さずに無視する
			if (!player.hasPermission("storagesign.autocollect")) {
        logger.debug("★This user hasn't Permission. storagesign.autocollect.");
				return;
			}

      boolean mainHandhasSS = StorageSign.isStorageSign(playerInv.getItemInMainHand(), logger);
      boolean offHandhasSS = StorageSign.isStorageSign(playerInv.getItemInOffHand(), logger);
      logger.trace("mainHandhasSS: " + mainHandhasSS);
      logger.trace("offHandisSS: " + offHandhasSS);
      logger.debug("check mainHand has SS.");
      if (mainHandhasSS) {
        logger.debug("MainHand has StorageSign.");
        storagesign = new StorageSign(playerInv.getItemInMainHand(), logger);

        logger.trace("storagesign.getContents(): " + storagesign.getContents());
        if (storagesign.getContents() != null) {
          logger.debug("This SS is not Empty");

          logger.trace("storagesign.isSimilar(item): " + storagesign.isSimilar(item));
          logger.trace("playerInv.containsAtLeast(item, item.getMaxStackSize()): " + playerInv.containsAtLeast(item, item.getMaxStackSize()));
          logger.trace("storagesign.getStackSize(): " + storagesign.getStackSize());
          if (storagesign.isSimilar(item) && playerInv.containsAtLeast(item, item.getMaxStackSize())
              && storagesign.getStackSize() == 1) {
            logger.debug("★Pickup Item to StorageSign.");
            storagesign.addAmount(item.getAmount());

            //1.9,10ではバグる？
            playerInv.removeItem(item);
            playerInv.setItemInMainHand(storagesign.getStorageSign());
            player.updateInventory();
            //event.getItem().remove();
            //player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.5f);
            //event.setCancelled(true);
            return;
          }
        }
      }
      logger.debug("check offHand has SS.");
      if (offHandhasSS) {//メインハンドで回収されなかった時
        logger.debug("OffHand has StorageSign.");
        storagesign = new StorageSign(playerInv.getItemInOffHand(), logger);
        if (storagesign.getContents() != null) {
          logger.debug("This SS is not Empty");

          logger.trace("storagesign.isSimilar(item): " + storagesign.isSimilar(item));
          logger.trace("playerInv.containsAtLeast(item, item.getMaxStackSize()): " + playerInv.containsAtLeast(item, item.getMaxStackSize()));
          logger.trace("storagesign.getStackSize(): " + storagesign.getStackSize());
          if (storagesign.isSimilar(item) && playerInv.containsAtLeast(item, item.getMaxStackSize())
              && storagesign.getStackSize() == 1) {
            logger.debug("★Pickup Item to StorageSign.");
            storagesign.addAmount(item.getAmount());
            playerInv.removeItem(item);
            playerInv.setItemInOffHand(storagesign.getStorageSign());
            player.updateInventory();
            //event.getItem().remove();
            //player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.5f);
            //event.setCancelled(true);
            return;
          }
        }
      }
    }
    //SSをプレイヤー以外拾えなくする
    if (event.getEntityType() != EntityType.PLAYER) {
      logger.debug("This Entity isn't Player.");
      Item item = event.getItem();

      boolean isStorageSign = StorageSign.isStorageSign(item.getItemStack(), logger);
      logger.trace("isStorageSign(item.getItemStack()): " + isStorageSign);
      if (isStorageSign) {
        logger.debug("Delay Picked Entity StorageSign.");
        item.setPickupDelay(20);//毎tickキャンセルしてたら重そう
        event.setCancelled(true);
      }
    }
    logger.debug("★onPlayerPickupItem:End.");
  }

  @EventHandler
  public void onEntityChangeBlock(EntityChangeBlockEvent event) {
    if(_fallingBlockSS){
      logger.debug("★onEntityChangeBlock:Start");
      logger.trace("event.getEntity(): " + event.getEntity());
      if (event.getEntity() instanceof FallingBlock fallingBlock) {
        logger.debug("check Falling relativeBlock.");
        dropRelativeSign(event.getBlock());
      }
      logger.debug("★onEntityChangeBlock:End");
    }
  }

  private void dropRelativeSign(Block block) {
    logger.debug(" dropRelativeSign:Start");
    Map<Location, StorageSign> breakSignMap = new HashMap<>();
    boolean isStorageSign = StorageSign.isStorageSign(block, logger);
    logger.trace(" isStorageSign: " + isStorageSign);
    if (isStorageSign) {
      logger.debug(" breakItem is StorageSign.");
      breakSignMap.put(block.getLocation(),
          new StorageSign((Sign) block.getState(), block.getType(), logger));
    }

    for (int i = 0; i < 5; i++) {//東西南北で判定
      logger.trace("  i: " + i);
      BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST,
          BlockFace.WEST};
      Block relBlock = block.getRelative(face[i]);

      boolean relIsSignPost = SignDefinition.sign_materials.contains(relBlock.getType());
      boolean relIsStorageSign = StorageSign.isStorageSign(relBlock, logger);
      boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(relBlock.getType());
      logger.trace("  relBlock: " + relBlock);
      logger.trace("  relIsSignPost: " + relIsSignPost);
      logger.trace("  relIsStorageSign: " + relIsStorageSign);
      logger.trace("  relIsWallSign: " + relIsWallSign);
      if (i == 0 && relIsSignPost && relIsStorageSign) {
        logger.debug("  This Block is StorageSign.");
        breakSignMap.put(relBlock.getLocation(),
            new StorageSign((Sign) relBlock.getState(), relBlock.getType(), logger));
      } else if (relIsWallSign && ((WallSign) relBlock.getBlockData()).getFacing() == face[i]
          && relIsStorageSign) {
        logger.debug("  This Block is WallStorageSign.");
        breakSignMap.put(relBlock.getLocation(),
            new StorageSign((Sign) relBlock.getState(), relBlock.getType(), logger));
      }
    }

    logger.trace(" breakSignMap.isEmpty(): " + breakSignMap.isEmpty());
    if (breakSignMap.isEmpty()) {
      logger.debug(" This Block isn't block StorageSign.");
      return;
    }

    logger.debug(" Break StorageSign Set Item in World.");
    for (Location loc : breakSignMap.keySet()) {
      StorageSign sign = breakSignMap.get(loc);
      logger.trace("  loc: " + loc);
      logger.trace("  sign:" + sign);

      Location loc2 = loc;
      loc2.add(0.5, 0.5, 0.5);//中心にドロップさせる
      loc.getWorld().dropItem(loc2, sign.getStorageSign());
      loc.getBlock().setType(Material.AIR);
      logger.debug("  Storage Sign drop in World.");
    }
  }

  private DyeColor getDyeColor(ItemStack item) {
    logger.debug("getDyeColor: Start");
    Material mat = item.getType();

    logger.trace("mat: " + mat);
    switch (mat) {
      case WHITE_DYE:
        return DyeColor.WHITE;
      case ORANGE_DYE:
        return DyeColor.ORANGE;
      case MAGENTA_DYE:
        return DyeColor.MAGENTA;
      case LIGHT_BLUE_DYE:
        return DyeColor.LIGHT_BLUE;
      case YELLOW_DYE:
        return DyeColor.YELLOW;
      case LIME_DYE:
        return DyeColor.LIME;
      case PINK_DYE:
        return DyeColor.PINK;
      case GRAY_DYE:
        return DyeColor.GRAY;
      case LIGHT_GRAY_DYE:
        return DyeColor.LIGHT_GRAY;
      case CYAN_DYE:
        return DyeColor.CYAN;
      case PURPLE_DYE:
        return DyeColor.PURPLE;
      case BLUE_DYE:
        return DyeColor.BLUE;
      case BROWN_DYE:
        return DyeColor.BROWN;
      case GREEN_DYE:
        return DyeColor.GREEN;
      case RED_DYE:
        return DyeColor.RED;
      case BLACK_DYE:
        return DyeColor.BLACK;
      default:
    }
    return null;
  }

  private boolean isGlowSac(ItemStack item) {
    logger.debug(" isSac: Start");
    Material mat = item.getType();

    logger.trace(" mat: " + mat);
    switch (mat) {
      case INK_SAC:
        return false;
      case GLOW_INK_SAC:
        return true;
      default:
    }
    return false;
  }
}