package wacky.storagesign;

import com.github.teruteru128.logger.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class StorageSignPlayerEvent implements Listener {

  private final Logger logger;
  private Player player;

  private Block clickBlock;
  private StorageSignV2 clickBlockSSData;

  private ItemStack itemMainHand;
  private StorageSignV2 itemMainHandSSData;

  private PlayerInventory playerInventory;

  public StorageSignPlayerEvent(StorageSignCore plugin) {
    this.logger = plugin.logger;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    logger.debug("★onPlayerInteract: Start");
    player = event.getPlayer();

    logger.trace("player.getGameMode() == GameMode.SPECTATOR:" + (player.getGameMode() == GameMode.SPECTATOR));
    if (player.getGameMode() == GameMode.SPECTATOR) return;

    clickBlock = event.getClickedBlock();
    logger.trace("event.useInteractedBlock() == Result.DENY :" + (event.useInteractedBlock() == Event.Result.DENY));
    logger.trace("event.getAction() == Action.RIGHT_CLICK_AIR:" + (event.getAction() == Action.RIGHT_CLICK_AIR));
    //手持ちがブロックだと叩いた看板を取得できないことがあるとか
    if (event.useInteractedBlock() == Event.Result.DENY && event.getAction() == Action.RIGHT_CLICK_AIR) {
      try {
        logger.debug("try getTargetBlock.");
        clickBlock = player.getTargetBlock((Set) null, 3);
      } catch (IllegalStateException ex) {
        java.util.logging.Logger log = logger.getLogger();
        log.log(Level.SEVERE, "★getTargetBlock is Error. Trace", ex);
        return;
      }
    } else {
      logger.debug("ClickedBlock :" + event.getClickedBlock());
      clickBlock = event.getClickedBlock();
    }
    logger.trace("block == null:" + (clickBlock == null));
    if (clickBlock == null) {
      logger.debug("★block is null.");
      return;
    }

    logger.trace("event.getAction():" + event.getAction());
    if (List.of(Action.RIGHT_CLICK_BLOCK,Action.RIGHT_CLICK_AIR).contains(event.getAction())) {
      logger.debug("UserAction is RIGHT_CLICK_BLOCK or RIGHT_CLICK_AIR");

      // 対象が StorageSign ではない場合スキップ
      if (!StorageSignV2.isStorageSign(clickBlock)) return;

      if (event.getHand() == EquipmentSlot.OFF_HAND) {
        logger.debug("★This hand is OFF_HAND.");
        //オフハンドでスニーク動作でSSを触り、かつ持ってるアイテムがSSのときは、看板が張り付かないようにイベントをキャンセルする
        logger.trace("player.isSneaking(): " + player.isSneaking());
        if (player.isSneaking() && StorageSignV2.isStorageSign(event.getItem())) {
          event.setUseItemInHand(Event.Result.DENY);
          event.setUseInteractedBlock(Event.Result.DENY);
        }
        return;
      }
      event.setUseItemInHand(Event.Result.DENY);
      event.setUseInteractedBlock(Event.Result.DENY);

      logger.trace("!player.hasPermission(\"storagesign.use\"):  " + !player.hasPermission("storagesign.use"));
      if (!player.hasPermission("storagesign.use")) {
        logger.debug("★This User hasn't permission.storagesign.use");
        player.sendMessage(ChatColor.BLACK + ConfigLoader.getNoPermission());
        event.setCancelled(true);
        return;
      }

      clickBlockSSData = new StorageSignV2(clickBlock, logger);
      itemMainHand = event.getItem();

      logger.debug("Check Event Type.");
      logger.trace("storageSign: " + clickBlockSSData);
      logger.trace("itemMainHand: " + itemMainHand);
      logger.debug("check Item Regist.");

      // 手持ちアイテムが StorageSign かで分岐
      if(StorageSignV2.isStorageSign(itemMainHand)) {
        // 手持ちが StorageSign である
        itemMainHandSSData = new StorageSignV2(itemMainHand, logger);

        if(itemMainHandSSData.isEmpty()){
          if(clickBlockSSData.isEmpty()){
            // Block SS Empty <- Item SS Empty
            // 手持ちStorageSign を StorageSign に登録
            clickBlockSSData.entryContent(itemMainHand);

          } else if (clickBlockSSData.isSimilar(itemMainHandSSData)) {
            // Block SS Content == Item SS Contents
            // 手持ちStorageSign の中身を StorageSign に入庫
            if(clickBlockSSData.importContentItem(itemMainHand)){
              playerInventory.clear(playerInventory.getHeldItemSlot());

            }

          }else {
            // Block SS In Item -> Item SS Empty
            // 手持ちStorageSign に StorageSign Contents を分割
            clickBlockSSData.SSExchangeExport(itemMainHandSSData, itemMainHand, player.isSneaking());

          }

        }

        //空看板収納
        /*  logger.debug("Empty Sign store.");
          logger.trace("player.isSneaking()" + player.isSneaking());

          if(! targetStorageSign.importItemStack(itemMainHand))return;

          player.getInventory().clear(player.getInventory().getHeldItemSlot());

          if (! player.isSneaking()){
            for (int i = 0; i < player.getInventory().getSize(); i++){
              ItemStack item = player.getInventory().getItem(i);
              if (StorageSignV2.isStorageSign())
            }
          }*/
//        clickBlockSSData.isContentItemEquals()
      }else{
        // 手持ちが StorageSign ではない
        if(clickBlockSSData.empty){
          if(itemMainHand == null) {
            logger.debug("★User MainHand is Null.");
            //何もしない
            return;
          }

          //アイテム登録
          logger.debug("main hand has " + itemMainHand.getType());
          clickBlockSSData.entryContent(itemMainHand);
          //clickBlockSSData.setStorageData(clickBlock);

        } else if(clickBlockSSData.isContentItemEquals(itemMainHand)) {
          //入庫
          logger.debug("StorageSign Content import.");
          playerInventory = player.getInventory();
          if(clickBlockSSData.importContentItem(itemMainHand)){
            playerInventory.clear(playerInventory.getHeldItemSlot());
            //clickBlockSSData.setStorageData(clickBlock);
          }
        } else {
          //出庫
          logger.debug("Export StorageSign Item.");

          boolean isDye = itemMainHand != null && isDye(itemMainHand);
          boolean isSac = itemMainHand != null && isSac(itemMainHand);

          logger.trace("itemMainHand:" + itemMainHand);
          logger.trace("isDye:" + isDye);
          logger.trace("isSac:" + isSac);
          //染料の場合、放出せずに看板に色がつく
          if (isDye) {
            logger.debug("★Set SignColor.");
            event.setUseItemInHand(Event.Result.ALLOW);
            //最初にDENYにしてたので戻す、同色染料が使えない。
            event.setUseInteractedBlock(Event.Result.ALLOW);
            return;
          } else if (isSac) {
            logger.debug("★Set Sac.");
            event.setUseItemInHand(Event.Result.ALLOW);
            event.setUseInteractedBlock(Event.Result.ALLOW);
            return;
          }

          logger.trace("player.isSneaking(): " + player.isSneaking());
          ItemStack item = clickBlockSSData.outputContentItem(player.isSneaking());

          logger.debug("drop Item.");
          Location loc = player.getLocation();
          loc.setY(loc.getY() + 0.5);
          player.getWorld().dropItem(loc, item);
        }
      }
      logger.debug("SignTextUpdate.");
      clickBlockSSData.setStorageData(clickBlock);

      logger.debug("★ItemRegist: End");
      player.updateInventory();
      return;

    }

//      if (clickBlockSSData.isEmpty()){
//        logger.debug("SS Material Regist.");
//        logger.trace("itemMainHand:" + itemMainHand);
//        logger.trace("itemMainHand == null: " + (itemMainHand == null));





/*        mat = itemMainHand.getType();
//        boolean mainHandIsSS = StorageSign.isStorageSign(itemMainHand, logger);
//        boolean mainHandIsHE = isHorseEgg(itemMainHand);
//        logger.trace("isStorageSign(itemMainHand): " + mainHandIsSS);
//        logger.trace("isHorseEgg(itemMainHand): " + mainHandIsHE);
//        logger.trace("mat: " + mat);
        if (StorageSign.isStorageSign(itemMainHand, logger)) {*/

//          logger.debug("main hand has StorageSign.");
//          storageSign.setMaterial(mat);
//          storageSign.setDamage((short) 1);
//        } else if (mainHandIsHE) {
//          logger.debug("main hand has HorseEgg.");
//          storageSign.setMaterial(Material.END_PORTAL);
//          storageSign.setDamage((short) 1);
//        } else if (mat == Material.STONE_SLAB) {
//          logger.debug("main hand has STONE_SLAB.");
//          storageSign.setMaterial(mat);
//          storageSign.setDamage((short) 1);
//        } else if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION) {
//          logger.debug("main hand has PotionSeries.");
//          storageSign.info = new Potion(itemMainHand,logger);
//        } else if (mat == Material.OMINOUS_BOTTLE) {
//          logger.debug("main hand has OMINOUS_BOTTLE.");
//          storageSign.setMaterial(mat);
//          storageSign.setDamage(OmniousBottleInfo.GetAmplifierWithMeta(itemMainHand.getItemMeta()));

//          storageSign.info = new OminousBottle(itemMainHand, logger);
//        } else if (mat == Material.ENCHANTED_BOOK) {
//          logger.debug("main hand has EnchantedBook.");

//          EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) itemMainHand.getItemMeta();
//          if (enchantMeta.getStoredEnchants().size() == 1) {
//            storageSign.info = new EnchantedBook(itemMainHand, logger);
//          }
//        } else if (mat == Material.FIREWORK_ROCKET) {
/*          logger.debug("main hand has FireRocket.");
          storageSign.setMaterial(mat);
          FireworkMeta fireworkMeta = (FireworkMeta) itemMainHand.getItemMeta();
          storageSign.setDamage((short) fireworkMeta.getPower());*/
//          storageSign.info = new FireworkRocket(itemMainHand, logger);
//        } else if (mat == Material.WHITE_BANNER) {
//          logger.debug("main hand has WhiteBanner.");
//          storageSign.setMaterial(mat);
//          BannerMeta bannerMeta = (BannerMeta) itemMainHand.getItemMeta();
//          if (bannerMeta.getPatterns().size() == 8) {
//            ominousBannerMeta = bannerMeta;//襲撃バナー登録
//            storageSign.setDamage((short) 8);
//          }
//        } else {
//          logger.debug("main hand has " + mat);
//          storageSign.info = new NormalInformation(itemMainHand, logger);
//          storageSign.setMaterial(mat);
//          var meta = itemMainHand.getItemMeta();

//          logger.trace("meta instanceof Damageable dam" + (meta instanceof Damageable dam));
//          if (meta instanceof Damageable dam) {
//            logger.debug("This Item has Damage.damage:" + dam.getDamage());
//            storageSign.setDamage((short) dam.getDamage());
//          }

//        }
//


      //boolean isStorageSign = StorageSign.isStorageSign(itemMainHand, logger);
      //logger.trace("isStorageSign:" + isStorageSign);

//      if (StorageSignV2.isStorageSign(itemMainHand)) {
        //看板合成
//        logger.debug("Item move to User StorageSign");

        //StorageSign itemSign = new StorageSign(itemMainHand, logger);

//        itemMainHandSSData = new StorageSignV2(itemMainHand,logger);

//        logger.trace("itemSign:" + itemSign);
//        logger.trace("storageSign.getContents().isSimilar(itemSign.getContents()) && config.getBoolean(\n"
//            + "            \"manual-import\"):" + (storageSign.getContents().isSimilar(itemSign.getContents()) && ConfigLoader.getManualImport()));
//        logger.trace("itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()\n"
//            + "            && config.getBoolean(\"manual-export\")" + (itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()
//            && ConfigLoader.getManualExport()));

//        if (ConfigLoader.getManualImport()) return ;

//        if (clickBlockSSData.isSimilar(itemMainHandSSData)) {
//          logger.debug("Sign store Items.");
//          storageSign.addAmount(itemSign.getAmount() * itemSign.getStackSize());
//          itemSign.setAmount(0);
//          player.getInventory().setItemInMainHand(itemSign.getStorageSign());
//          itemMainHandSS.setStorageSignData(itemMainHand);

//        } else if (itemSign.isEmpty() && storageSign.getMaterial() == itemSign.getSmat()

//        } else if (itemMainHandSSData.isEmpty() && StorageSignV2.isStorageSign(itemMainHand)) {
          //空看板収納
        /*  logger.debug("Empty Sign store.");
          logger.trace("player.isSneaking()" + player.isSneaking());

          if(! targetStorageSign.importItemStack(itemMainHand))return;

          player.getInventory().clear(player.getInventory().getHeldItemSlot());

          if (! player.isSneaking()){
            for (int i = 0; i < player.getInventory().getSize(); i++){
              ItemStack item = player.getInventory().getItem(i);
              if (StorageSignV2.isStorageSign())
            }
          }*/
//					if (player.isSneaking()) {
//            logger.debug("Player is Sneaking.");
//						storageSign.addAmount(itemMainHand.getAmount());
//						player.getInventory().clear(player.getInventory().getHeldItemSlot());
//					} else {
//            logger.debug("store all Empty Sign.SearchPlayer Inventory.");
//            // アイテム内でサインを探す
//						for (int i = 0; i < player.getInventory().getSize(); i++) {
//							ItemStack item = player.getInventory().getItem(i);
//              boolean isSimilar = storageSign.isSimilar(item);
//              logger.trace("storageSign.isSimilar(item)" + isSimilar);
//							if (isSimilar) {
//                logger.debug("find Empty Sign.");
//                storageSign.addAmount(item.getAmount());
//                player.getInventory().clear(i);
//							}
//						}
//					}
//        } else if (itemSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()
//            && ConfigLoader.getManualExport()) {

          //} else if (targetStorageSign.isEmpty() && storageSign.getAmount() > itemMainHand.getAmount()) {
          //中身分割機能
/*          logger.debug("Export Item to Empty Sign.");
          targetStorageSign.isStorageItemEquals(itemMainHandSS.getStorageItem()).setMaterial(storageSign.getMaterial());
          itemSign.setDamage(storageSign.getDamage());
          //itemSign.setEnchant(storageSign.getEnchant());
          //itemSign.setInfo(StorageSign.getInfo());
          //itemSign.setPotion(storageSign.getPotion());

          int limit;
          if (player.isSneaking()){
            limit = ConfigLoader.getSneakDivideLimit();
          } else {
            limit = ConfigLoader.getDivideLimit();
          }

          logger.trace("limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)" + (limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)));
					if (limit > 0 && storageSign.getAmount() > limit * (itemSign.getStackSize() + 1)) {
            logger.debug("Item Export EmptySign divide-limit.");
						itemSign.setAmount(limit);
					} else {
            logger.debug("Item Export EmptySign Equality divide.");
						itemSign.setAmount(storageSign.getAmount() / (itemSign.getStackSize() + 1));
					}
          player.getInventory().setItemInMainHand(itemSign.getStorageSign());
          //余りは看板に引き受けてもらう
          storageSign.setAmount(storageSign.getAmount() - (itemSign.getStackSize()
              * itemSign.getAmount()));*/
//        }
/*        logger.debug("Update StorageSign.");
				for (int i = 0; i < 4; i++) {
          logger.trace("set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
					sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
				}
        sign.update();*/
//        logger.debug("★Item move to User StorageSign: End");
//        return;
//      }

      //ここから搬入
//      boolean isMainSimilar = storageSign.isSimilar(itemMainHand);
//      logger.debug("check manual Import.");
//      logger.trace("storageSign.isSimilar(itemMainHand): " + isMainSimilar);
//      logger.trace("config.getBoolean(\"manual-import\"): " + ConfigLoader.getManualImport());
//      logger.trace("config.getBoolean(\"manual-export\"): " + ConfigLoader.getManualExport());


//      else if (StorageSignV2.isStorageSign(itemMainHand)) {
//        logger.debug("StorageSign Import.");

//        logger.trace("!config.getBoolean(\"manual-import\"): " + !ConfigLoader.getManualImport());
//        if (!ConfigLoader.getManualImport()) {
//          logger.debug("★option:manual-import is False!");
//          return;
//        }
//        logger.trace("player.isSneaking():" + player.isSneaking());

//        clickBlockSSData.importContentItem(itemMainHand);

/*				if (player.isSneaking()) {
          logger.debug("player is Sneaking.Push 1 Item.");
          targetStorageSign.importItemStack(itemMainHand);
//					storageSign.addAmount(itemMainHand.getAmount());
					player.getInventory().clear(player.getInventory().getHeldItemSlot());

          logger.trace("isDye(itemMainHand):" + isDye(itemMainHand));
          logger.trace("isSac(itemMainHand):" + isSac(itemMainHand));
					if (isDye(itemMainHand)) {
            logger.debug("mainHandItem is Dye.");
						sign.getSide(Side.FRONT).setColor(getDyeColor(itemMainHand)); //同色用
					}
					if (isSac(itemMainHand)) {
            logger.debug("mainHandItem is Sac.");
						sign.getSide(Side.FRONT).setGlowingText(isGlowSac(itemMainHand)); //イカスミ用
					}
				} else {
          logger.debug("push Stack Item.");

					for (int i = 0; i < player.getInventory().getSize(); i++) {
						ItemStack item = player.getInventory().getItem(i);
            boolean itemSimilar = storageSign.isSimilar(item);
            logger.trace(" item: " + item);
            logger.trace(" storageSign.isSimilar(item): " + itemSimilar);
						if (itemSimilar) {
              logger.debug(" same Item Found.Push Item to StorageSign.");
							storageSign.addAmount(item.getAmount());
							player.getInventory().clear(i);
						}
					}
          logger.debug("push Stack Item.End.");
				}*/

//        player.updateInventory();
//      } else if (ConfigLoader.getManualExport()) {
//        //放出
//        logger.debug("Export StorageSign Item.");
//
//        boolean isDye = false;
//        boolean isSac = false;
//        if (itemMainHand != null){
//          isDye = isDye(itemMainHand);
//          isSac = isSac(itemMainHand);
//        }
//        logger.trace("itemMainHand:" + itemMainHand);
//        logger.trace("isDye:" + isDye);
//        logger.trace("isSac:" + isSac);
//        //染料の場合、放出せずに看板に色がつく
//        if (isDye) {
//          logger.debug("★Set SignColor.");
//          event.setUseItemInHand(Event.Result.ALLOW);
//          //最初にDENYにしてたので戻す、同色染料が使えない。
//          event.setUseInteractedBlock(Event.Result.ALLOW);
//          return;
//        } else if (isSac) {
//          logger.debug("★Set Sac.");
//          event.setUseItemInHand(Event.Result.ALLOW);
//          event.setUseInteractedBlock(Event.Result.ALLOW);
//          return;
//        } else if (clickBlockSSData.isEmpty()) {
//          logger.debug("★StorageSign is Empty.");
//          return;
//        }

//        ItemStack item = storageSign.getContents();
//        int max = item.getMaxStackSize();

//        logger.trace("player.isSneaking(): " + player.isSneaking());

//        ItemStack item = clickBlockSSData.outputContentItem(player.isSneaking());

//        logger.trace("storageSign.getAmount() > max: " + (storageSign.getAmount() > max));
/*				if (player.isSneaking()) {
          logger.debug("Player is Sneaking.Get 1 Item.");
					storageSign.addAmount(-1);
				} else if (storageSign.getAmount() > max) {
          logger.debug("Sign Items bigger than 1Stack.Get 1Stack Items.");
					item.setAmount(max);
					storageSign.addAmount(-max);
				} else {
          logger.debug("Get 1Stack Items.");
					item.setAmount(storageSign.getAmount());
					storageSign.setAmount(0);
				}*/

//        logger.debug("drop Item.");
//        Location loc = player.getLocation();
//        loc.setY(loc.getY() + 0.5);
//        player.getWorld().dropItem(loc, item);
//      }

//      logger.debug("SetSignText.");
      /*
			for (int i = 0; i < 4; i++) {
        logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
				sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
			}*/
//      clickBlockSSData.setStorageData(clickBlock);

//      logger.debug("update Sign.");
      //sign.update();
//    }

//    logger.debug("★onPlayerInteract: End");
  }

  private boolean isDye(ItemStack item) {
    logger.debug(" isDye: Start");
    Material mat = item.getType();

    logger.trace(" mat: " + mat);
    return switch (mat) {
      case WHITE_DYE, ORANGE_DYE, MAGENTA_DYE, LIGHT_BLUE_DYE, YELLOW_DYE,
           LIME_DYE, PINK_DYE, GRAY_DYE, LIGHT_GRAY_DYE, CYAN_DYE,
           PURPLE_DYE, BLUE_DYE, BROWN_DYE, GREEN_DYE, RED_DYE,
           BLACK_DYE -> true;
      default -> false;
    };
  }

  private boolean isSac(ItemStack item) {
    logger.debug(" isSac: Start");
    Material mat = item.getType();

    logger.trace(" mat: " + mat);
    return switch (mat) {
      case INK_SAC, GLOW_INK_SAC -> true;
      default -> false;
    };
  }

}
