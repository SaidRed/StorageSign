package wacky.storagesign.eventHandler;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.StorageSignV2;

import java.util.*;

public class StorageSignAutoExportEvent implements Listener {
  private final JavaPlugin plugin;
  private final Logger logger;
  
  public StorageSignAutoExportEvent(StorageSignCore plugin) {
    this.plugin = plugin;
    this.logger = plugin.logger;
  }
  
  @EventHandler
  public void onAutoExport(InventoryMoveItemEvent event) {
    logger.debug("Export: Start");
    
    Inventory destination = event.getDestination(); // 送り先
    Inventory eventSource = event.getSource();      // 送り主
    Inventory initiator = event.getInitiator();     // 転送を開始した主。奪った場合は送り先。押し込む時は送り主。
    
    ItemStack moveItem = event.getItem().clone();
    
    if (eventSource.containsAtLeast(moveItem, moveItem.getMaxStackSize())) return;
    
    List<Block> souBlock;
    if (eventSource instanceof DoubleChestInventory doubleChest) {
      souBlock = new ArrayList<>(Arrays.asList(
              ((Container) doubleChest.getLeftSide().getHolder()).getBlock(),
              ((Container) doubleChest.getRightSide().getHolder()).getBlock()
      ));
    } else if (eventSource.getHolder() instanceof Container souContainer){
      souBlock = List.of(souContainer.getBlock());
    } else {
      return;
    }
    
    List<Block> targetSS = new ArrayList<>();
    for (Block block : souBlock) {
      targetSS = StorageSignV2.isLinkStorageSign(block, moveItem, true, logger);
    }
    
    if (targetSS.isEmpty()) return;
    
    InventoryHolder desHolder = destination.getHolder();
    if (destination instanceof DoubleChestInventory doubleChest) {
      Container left =(Container) doubleChest.getLeftSide().getHolder();
      Inventory leftSnapshot = left.getSnapshotInventory();
      moveItem = isInventoryMoveItem(leftSnapshot,moveItem);
      if (moveItem == null) {
        Container right = (Container) doubleChest.getRightSide().getHolder();
        Inventory rightSnapshot = right.getSnapshotInventory();
        moveItem = isInventoryMoveItem(rightSnapshot,moveItem);
        if (moveItem == null) return;
      }
    } else if (desHolder instanceof Container desContainer) {
      Inventory desSnapshotInventory = desContainer.getSnapshotInventory();
      if (destination.equals(initiator) || souBlock.size() == 2) {
        // ホッパーが奪う時 ダブルチェスト
        moveItem = isInventoryMoveItem(desSnapshotInventory,moveItem);
        if (moveItem == null) return;
      } else {
        BlockFace face = ((Directional) souBlock.getFirst().getBlockData()).getFacing().getOppositeFace();
        if (destination instanceof BrewerInventory) {
          // 醸造台
          List<Boolean> check = new ArrayList<Boolean>();
          
          for (int i : BrewingStandImportTest.getSlotsForFace(face)) {
            if (i == 3) {
              ItemStack slotItem = desSnapshotInventory.getItem(i);
              if (slotItem != null) {
                if (slotItem.getAmount() == slotItem.getMaxStackSize()) {
                  check.add(false);
                } else {
                  check.add(slotItem.isSimilar(moveItem));
                }
                continue;
              }
              // 実際に突っ込まれたか確認してから Export する
              
              new BrewingStandImportTest.exportTask(desContainer, eventSource, i, targetSS.getFirst(), logger).runTask(plugin);
              return;
            } else {
              check.add(i == 4 ? moveItem.getType().equals(Material.BLAZE_POWDER)
                      : (moveItem.getType().equals(Material.POTION) || moveItem.getType().equals(Material.SPLASH_POTION)
                      || moveItem.getType().equals(Material.LINGERING_POTION) || moveItem.getType().equals(Material.GLASS_BOTTLE))
              );
            }
          }
          if (!check.contains(true)) return;
        } else if (destination instanceof FurnaceInventory) {
          // カマド
          List<Boolean> check = new ArrayList<Boolean>();
          
          for (int i : FurnaceImportTest.getSlotsForFace(face)) {
            if (i != 1) {
              check.add(true);
            } else {
              ItemStack itemStack = desSnapshotInventory.getItem(1);
              if (itemStack == null) {
                check.add(moveItem.getType().isFuel() || moveItem.getType().equals(Material.BUCKET));
              } else if (itemStack.getAmount() == itemStack.getMaxStackSize()) {
                check.add(false);
              } else {
                check.add(itemStack.isSimilar(moveItem));
              }
            }
            if (check.contains(true)) break;
          }
          if (!check.contains(true)) return;
        } else {
          // その他
          moveItem = isInventoryMoveItem(desSnapshotInventory,moveItem);
          if (moveItem == null) return;
        }
      }
    } else{
      // エンティティ(ホッパートロッコ/チェストトロッコ)
      Inventory desSnapshotInventory = Bukkit.getServer().createInventory(null, destination.getType());
      desSnapshotInventory.setContents(destination.getContents());
      moveItem = isInventoryMoveItem(desSnapshotInventory, moveItem);
      if (moveItem == null) return;
    }
    
    for (Block SSBlock :targetSS) {
      StorageSignV2 SS = new StorageSignV2(SSBlock,logger);
      int amount = moveItem.getAmount();
      if (SS.getAmount() < amount) {
        SS.setAmount(0);
        moveItem.setAmount(SS.getAmount());
      } else {
        SS.setAmount(SS.getAmount() - amount);
      }
      SS.setContentData(SSBlock);
      eventSource.addItem(moveItem);
      return;
    }
  }
  
  public ItemStack isInventoryMoveItem(Inventory desSnapshotInventory, ItemStack moveItem){
    HashMap<Integer, ItemStack> notMoveItems = desSnapshotInventory.addItem(moveItem);
    
    if (!notMoveItems.isEmpty()) {
      ItemStack notMoveItem = notMoveItems.values().toArray(ItemStack[]::new)[0];
      if (notMoveItem.equals(moveItem)) return null;
      moveItem.setAmount(moveItem.getAmount() - notMoveItem.getAmount());
    }
    return moveItem;
  }
  
  public static class FurnaceImportTest{
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    
    public static int[] getSlotsForFace(BlockFace face) {
      return face == BlockFace.UP ? SLOTS_FOR_UP : (face == BlockFace.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES);
    }
  }
  
  
  public static class BrewingStandImportTest{
    private static final int[] SLOTS_FOR_UP = new int[]{3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
    
    public static int[] getSlotsForFace(BlockFace face) {
      return face == BlockFace.UP ? SLOTS_FOR_UP : (face == BlockFace.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES);
    }
    
    public static class exportTask extends BukkitRunnable {
      //
      private final Container destination;
      // 実際に入れ込むインベントリ情報
      private final Inventory source;
      // 確認するスロット
      private final int slot;
      // SSブロック
      private final Block SSBlock;
      // ロガー
      private final Logger logger;
      
      exportTask(Container desContainer, Inventory source, int i, Block SSBlock, Logger logger) {
        this.destination = desContainer;
        this.source = source;
        this.slot = i;
        this.SSBlock = SSBlock;
        this.logger = logger;
      }
      
      private ItemStack[] nonNullItemStack(ItemStack[] itemStacks){
        return Arrays.stream(itemStacks).filter(Objects::nonNull).toArray(ItemStack[]::new);
      }
      
      @Override
      public void run() {
        ItemStack moveItem = destination.getInventory().getItem(slot);
        
        StorageSignV2 SS = new StorageSignV2(SSBlock,logger);
        if (moveItem == null) return;
        int amount = moveItem.getAmount();
        if (SS.getAmount() < amount) {
          SS.setAmount(0);
          moveItem.setAmount(SS.getAmount());
        } else {
          SS.setAmount(SS.getAmount() - amount);
        }
        SS.setContentData(SSBlock);
        source.addItem(moveItem);
      }
    }
  }
}
