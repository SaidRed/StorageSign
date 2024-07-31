package wacky.storagesign.event;

import com.github.teruteru128.logger.Logger;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import wacky.storagesign.ConfigLoader;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.StorageSignV2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class StorageSignPlayerEvent implements Listener {

  private final Logger logger;

  public StorageSignPlayerEvent(StorageSignCore plugin) {
    this.logger = plugin.logger;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    logger.debug("★onPlayerInteract: Start");
    Player player = event.getPlayer();

    logger.trace("player.getGameMode() == GameMode.SPECTATOR:" + (player.getGameMode() == GameMode.SPECTATOR));
    if (player.getGameMode() == GameMode.SPECTATOR) return;

    Block clickBlock = event.getClickedBlock();
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

      StorageSignV2 clickBlockSSData = new StorageSignV2(clickBlock, logger);
      ItemStack itemMainHand = event.getItem();
      PlayerInventory playerInventory = player.getInventory();

      logger.debug("Check Event Type.");
      logger.trace("storageSign: " + clickBlockSSData);
      logger.trace("itemMainHand: " + itemMainHand);
      logger.debug("check Item Regist.");

      // 手持ちアイテムが StorageSign かで分岐
      if(StorageSignV2.isStorageSign(itemMainHand)) {
        // 手持ちが StorageSign である
        StorageSignV2 itemMainHandSSData = new StorageSignV2(itemMainHand, logger);

        if(itemMainHandSSData.isEmpty()){
          if(clickBlockSSData.isEmpty()){
            // Block SS Empty <- Item SS Empty
            // 手持ちStorageSign を StorageSign に登録
            clickBlockSSData.entryContent(itemMainHand);

          } else if (clickBlockSSData.isSimilar(itemMainHand) &&
                  ConfigLoader.getManualImport()) {
            // Block SS Content == Item SS Contents
            // 手持ちStorageSign の中身を StorageSign に入庫
            if(player.isSneaking()) {
              if (clickBlockSSData.importContentItem(itemMainHand)) {
                playerInventory.clear(playerInventory.getHeldItemSlot());

              }
            }else{
              for(ItemStack item:playerInventory.getContents()){
                if (item != null && clickBlockSSData.importContentItem(item)){
                  playerInventory.remove(item);
                }
              }
            }

          } else if(ConfigLoader.getManualExport()){
            // Block SS In Item -> Item SS Empty
            // 手持ちStorageSign に StorageSign Contents を分割
            clickBlockSSData.SSExchangeExport(itemMainHand, player.isSneaking());

          }

        }else if(ConfigLoader.getManualImport()){
          // Block SS In Item is StorageSign <- import SS ItemStack
          // 手持ちSS の SS内入庫
          if(clickBlockSSData.SSExchangeExport(itemMainHandSSData,itemMainHand)){
            clickBlockSSData.setStorageData(clickBlock);

          }

        }

      }else{
        // 手持ちが StorageSign ではない
        if(clickBlockSSData.isEmpty()){
          if(itemMainHand == null) {
            logger.debug("★User MainHand is Null.");
            //何もしない
            return;
          }

          //アイテム登録
          logger.debug("main hand has " + itemMainHand.getType());
          clickBlockSSData.entryContent(itemMainHand);

        } else if(clickBlockSSData.isContentItemEquals(itemMainHand) &&
                ConfigLoader.getManualImport()) {
          //入庫
          boolean isDye = itemMainHand != null && isDye(itemMainHand);
          boolean isSac = itemMainHand != null && isSac(itemMainHand);

          logger.trace("itemMainHand:" + itemMainHand);
          logger.trace("isDye:" + isDye);
          logger.trace("isSac:" + isSac);

          logger.debug("StorageSign Content import.");
          if(player.isSneaking()) {
            if (clickBlockSSData.importContentItem(itemMainHand)) {
              playerInventory.clear(playerInventory.getHeldItemSlot());

            }
          }else{
            /*
            for(int i = 0; i < playerInventory.getSize(); i++) {
              if (playerInventory.getItem(i) != null && clickBlockSSData.importContentItem(playerInventory.getItem(i))) {
                playerInventory.clear(i);
              }
            }*/
            for(ItemStack item:playerInventory.getContents()){
              if (item != null && clickBlockSSData.importContentItem(item)){
                playerInventory.remove(item);
              }
            }
          }
        } else if(ConfigLoader.getManualImport()){
          //出庫
          logger.debug("Export StorageSign Item.");

          logger.trace("player.isSneaking(): " + player.isSneaking());
          if(! clickBlockSSData.isContentEmpty()) {
            ItemStack item = clickBlockSSData.outputContentItem(player.isSneaking());

            logger.debug("drop Item.");
            Location loc = player.getLocation();
            loc.setY(loc.getY() + 0.5);
            player.getWorld().dropItem(loc, item);
          }
        }
      }

      logger.debug("SignTextUpdate.");
      logger.debug("★ItemRegist: End");
      clickBlockSSData.setStorageData(clickBlock);
      player.updateInventory();

    }
  }

  /*
  //染料の場合、放出せずに看板に色がつく
            if(!player.getGameMode().equals(GameMode.ADVENTURE)){
              if (isDye || isSac){
                logger.debug("★Set SignColor.");
                event.setUseItemInHand(Event.Result.ALLOW);
                //最初にDENYにしてたので戻す、同色染料が使えない。
                event.setUseInteractedBlock(Event.Result.ALLOW);

                SignSide side = ((Sign)clickBlock.getState()).getSide(Side.FRONT);
                if (isDye) {
                  logger.debug("mainHandItem is Dye.");
                  side.setColor(getDyeColor(itemMainHand)); //同色用
                  return;
                }
                if (isSac(itemMainHand)) {
                  logger.debug("mainHandItem is Sac.");
                  side.setGlowingText(isGlowSac(itemMainHand)); //イカスミ用
                  return;
                }
              }
            } else
   */

  private final Map<Material,DyeColor> DayColor = Map.ofEntries(
          Map.entry(Material.WHITE_DYE, org.bukkit.DyeColor.WHITE),
          Map.entry(Material.ORANGE_DYE, org.bukkit.DyeColor.ORANGE),
          Map.entry(Material.MAGENTA_DYE, org.bukkit.DyeColor.MAGENTA),
          Map.entry(Material.LIGHT_BLUE_DYE, org.bukkit.DyeColor.LIGHT_BLUE),
          Map.entry(Material.YELLOW_DYE, org.bukkit.DyeColor.YELLOW),
          Map.entry(Material.LIME_DYE, org.bukkit.DyeColor.LIME),
          Map.entry(Material.PINK_DYE, org.bukkit.DyeColor.PINK),
          Map.entry(Material.GRAY_DYE, org.bukkit.DyeColor.GRAY),
          Map.entry(Material.LIGHT_GRAY_DYE, org.bukkit.DyeColor.LIGHT_GRAY),
          Map.entry(Material.CYAN_DYE, org.bukkit.DyeColor.CYAN),
          Map.entry(Material.PURPLE_DYE, org.bukkit.DyeColor.PURPLE),
          Map.entry(Material.BLUE_DYE, org.bukkit.DyeColor.BLUE),
          Map.entry(Material.BROWN_DYE, org.bukkit.DyeColor.BROWN),
          Map.entry(Material.GREEN_DYE, org.bukkit.DyeColor.GREEN),
          Map.entry(Material.RED_DYE, org.bukkit.DyeColor.RED),
          Map.entry(Material.BLACK_DYE, org.bukkit.DyeColor.BLACK)
  );

  private boolean isDye(ItemStack item) {
    return DayColor.containsKey(item.getType());
  }

  private DyeColor getDyeColor(ItemStack item) {
    return DayColor.get(item.getType());
  }

  private final Map<Material,Boolean> InkSac = Map.ofEntries(
          Map.entry(Material.INK_SAC, false),
          Map.entry(Material.GLOW_INK_SAC, true)
  );

  private boolean isSac(ItemStack item) {
    return InkSac.containsKey(item.getType());
  }

  private boolean isGlowSac(ItemStack item) {
    return InkSac.get(item.getType());
  }

}
