package wacky.storagesign.eventHandler;

import com.github.teruteru128.logger.Logger;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import wacky.storagesign.ConfigLoader;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.StorageSignV2;

public class StorageSignPickupItemEvent implements Listener {
  private final Logger logger;
  public StorageSignPickupItemEvent(StorageSignCore plugin){
    this.logger = plugin.logger;
  }
  @EventHandler
  public void onPlayerPickupItem(EntityPickupItemEvent event) {
    logger.debug("★onPlayerPickupItem:Start");
    logger.trace("event.isCancelled(): " + event.isCancelled());
    if (event.isCancelled()) {
      logger.debug("★Event Cancelled.");
      return;
    }

    Item item = event.getItem();
    logger.trace("event.getEntityType(): " + event.getEntityType());
    logger.trace("config.getBoolean(\"autocollect\"): " + ConfigLoader.getAutoCollect());
    if (event.getEntityType() == EntityType.PLAYER && ConfigLoader.getAutoCollect()) {
      Player player = (Player) event.getEntity();
      PlayerInventory playerInv = player.getInventory();
//      ItemStack item = event.getItem().getItemStack();
//      StorageSign storagesign = null;

      logger.trace("!player.hasPermission(\"storagesign.autocollect\"): " + !player.hasPermission("storagesign.autocollect"));
      //ここでは、エラーを出さずに無視する
      if (!player.hasPermission("storagesign.autocollect")) {
        logger.debug("★This user hasn't Permission. storagesign.autocollect.");
        return;
      }

//      boolean mainHandhasSS = StorageSign.isStorageSign(playerInv.getItemInMainHand(), logger);
//      boolean offHandhasSS = StorageSign.isStorageSign(playerInv.getItemInOffHand(), logger);
//      logger.trace("mainHandhasSS: " + mainHandhasSS);
//      logger.trace("offHandisSS: " + offHandhasSS);
      logger.debug("check mainHand has SS.");
      ItemStack SS = null;
      if (StorageSignV2.isStorageSign(playerInv.getItemInMainHand())) {
        logger.debug("MainHand has StorageSign.");
        SS = playerInv.getItemInMainHand();
      } else if (StorageSignV2.isStorageSign(playerInv.getItemInOffHand())) {
        logger.debug("OffHand has StorageSign.");
        SS = playerInv.getItemInOffHand();
      }

      if (SS != null) {
        StorageSignV2 handSS = new StorageSignV2(SS, logger);
        if (handSS.importContentItem(item.getItemStack())) {
          playerInv.removeItem(item.getItemStack());
          handSS.setStorageSignData(SS);
          player.updateInventory();
        }
      }
    }
/*
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
    */
    //SSをプレイヤー以外拾えなくする
    if (event.getEntityType() != EntityType.PLAYER) {
      logger.debug("This Entity isn't Player.");
//      Item item = event.getItem();

      boolean isStorageSign = StorageSignV2.isStorageSign(item.getItemStack());
      logger.trace("isStorageSign(item.getItemStack()): " + isStorageSign);
      if (isStorageSign) {
        logger.debug("Delay Picked Entity StorageSign.");
        item.setPickupDelay(20);//毎tickキャンセルしてたら重そう
        event.setCancelled(true);
      }
    }
    logger.debug("★onPlayerPickupItem:End.");
  }
}
