package wacky.storagesign.eventHandler;

import com.github.teruteru128.logger.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import wacky.storagesign.StorageSignConfig;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.StorageSignV2;

import java.util.*;

public class StorageSignAutoExportEventTask implements Listener {
  private final JavaPlugin plugin;
  private final Logger logger;
  
  public StorageSignAutoExportEventTask(StorageSignCore plugin) {
    this.plugin = plugin;
    this.logger = plugin.logger;
  }
  
  @EventHandler
  public void onAutoExport(InventoryMoveItemEvent event) {
    logger.debug("Export: Start");
    
    Inventory destination = event.getDestination(); // 送り先
    Inventory source = event.getSource();           // 送り主
    Inventory initiator = event.getInitiator();     // 転送を開始した主。奪った場合は送り先。押し込む時は送り主。

    ItemStack moveItem = event.getItem().clone();
    
    if (source.containsAtLeast(moveItem, moveItem.getMaxStackSize())) return;
    // 送り主が BlockInventoryHolder で無ければ SS から奪えないのでスキップ
    if (!(source.getHolder() instanceof Container souHolder))return;

    Block block = souHolder.getBlock();
    
    for (BlockFace face : StorageSignConfig.defaultData.faceList) {
      Block checkBlock = block.getRelative(face);
      if (!StorageSignV2.isLinkStorageSign(checkBlock, block)) continue;
      
      StorageSignV2 SS = new StorageSignV2(checkBlock, logger);
      
      if (!SS.isContentItemEquals(moveItem)) continue;
      if (SS.getAmount() == 0) continue;
      
      boolean isUpDownHopper = (destination.equals(initiator)
              && (destination.getHolder() instanceof org.bukkit.block.data.type.Hopper)
              && (source.getHolder() instanceof org.bukkit.block.data.type.Hopper));
      
      // 情報だけ渡し1チックずらしてから判定・アイテム送付をする
      new exportTask(souHolder, moveItem, SS, checkBlock, isUpDownHopper).runTask(plugin);
    }
  }
  
  public static class exportTask extends BukkitRunnable {
    // 実際に入れ込むインベントリ情報
    private final Container source;
    // 移動するアイテム情報
    private final ItemStack moveItem;
    // 取り出すSS情報
    private final StorageSignV2 targetStorageSign;
    // SS の情報を書き込むためのブロック情報
    private final Block signBlock;
    
    exportTask(Container source, ItemStack moveItem, StorageSignV2 SS, Block signBlock, boolean isUpDownHopper) {
      this.source = source;
      this.moveItem = moveItem;
      this.targetStorageSign = SS;
      this.signBlock = signBlock;
    }
    
    @Override
    public void run() {
      
      Inventory inventory = source.getInventory();
      // 今のインベントリ内が MaxStackSize より少ないか確認
      HashMap<Integer,ItemStack> targetItemStack = (HashMap<Integer, ItemStack>) inventory.all(moveItem.getType());
      if(!targetItemStack.isEmpty()){
        if(targetItemStack.values().stream().mapToInt(ItemStack::getAmount).sum() + moveItem.getAmount()
                > moveItem.getMaxStackSize()) return;
      }
      
      int amount = targetStorageSign.getAmount() - moveItem.getAmount();
      if(amount < 0) {
        targetStorageSign.setAmount(0);
        moveItem.setAmount(targetStorageSign.getAmount());
      } else {
        targetStorageSign.setAmount(amount);
      }
      targetStorageSign.setContentData(signBlock);
      inventory.addItem(moveItem);
    }
  }
}
