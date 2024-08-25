package wacky.storagesign.eventHandler;

import com.github.teruteru128.logger.Logger;
import org.bukkit.block.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.*;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.StorageSignV2;

import java.util.*;

public class StorageSignAutoImportEvent implements Listener {
  
  private final Logger logger;
  
  public StorageSignAutoImportEvent(StorageSignCore plugin) {
    this.logger = plugin.logger;
  }
  
  @EventHandler
  public void onAutoImport(InventoryMoveItemEvent event) {
    logger.debug("★onItemMove: Start");
    
    Inventory destination = event.getDestination();
    ItemStack moveItem = event.getItem();
    
    if (!destination.containsAtLeast(moveItem, moveItem.getMaxStackSize())) return;

    List<Block> invBlock;
    if (destination instanceof DoubleChestInventory doubleChest){
      invBlock = new ArrayList<>(Arrays.asList(
              ((Container) doubleChest.getLeftSide().getHolder()).getBlock(),
              ((Container) doubleChest.getRightSide().getHolder()).getBlock()
      ));
    } else if (destination.getHolder() instanceof Container desContainer) {
      invBlock = new ArrayList<>(List.of(desContainer.getBlock()));
    } else {
      return;
    }
    
    List<Block> targetSS = new ArrayList<>();
    for (Block block : invBlock) {
      targetSS.addAll(StorageSignV2.isLinkStorageSign(block,moveItem,false,logger));
    }
    if (targetSS.isEmpty()) return;
    
    for (Block block : targetSS){
      StorageSignV2 SS = new StorageSignV2(block,logger);
      if (SS.importContentItem(moveItem)) {
        destination.removeItem(moveItem);
        SS.setContentData(block);
        return;
      }
    }
  }
  
  @EventHandler
  public void onInventoryPickup(InventoryPickupItemEvent event) {//ホッパーに投げ込まれたとき
    logger.debug("★onInventoryPickup:Start");
    
    if (event.getInventory().getHolder() instanceof Hopper holder) {
      logger.debug("holder has BlockState.");
      
      Block hopper = holder.getBlock();
      Inventory eventInventory = event.getInventory();
      ItemStack moveItem = event.getItem().getItemStack();
      
      if (!eventInventory.containsAtLeast(moveItem, moveItem.getMaxStackSize())) return;
      
      List<Block> target = StorageSignV2.isLinkStorageSign(hopper, moveItem, false, logger);
      if (target.isEmpty()) return;
      
      for (Block block : target){
        StorageSignV2 SS = new StorageSignV2(block,logger);
        if (SS.importContentItem(moveItem)){
          eventInventory.removeItem(moveItem);
          SS.setContentData(block);
          return;
        }
      }
    }
  }
}
