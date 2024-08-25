package wacky.storagesign.eventHandler;

import com.github.teruteru128.logger.Logger;

import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import wacky.storagesign.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public class StorageSignItemMoveEvent implements Listener {

  private final JavaPlugin plugin;
  private final Logger logger;

  private static final BlockFace[] faceList = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
  
  public StorageSignItemMoveEvent(StorageSignCore plugin){
    this.plugin = plugin;
    this.logger = plugin.logger;
  }
  
  @EventHandler
  //public void onItemMove(InventoryMoveItemEvent event) {
  public void onStorageSignAutoImport(InventoryMoveItemEvent event) {
    logger.debug("★onItemMove: Start");
    
    Inventory destination = event.getDestination(); // 送り先
    Inventory source = event.getSource();           // 送り主
    Inventory initiator = event.getInitiator();     // 転送を開始した主。奪った場合は送り先。押し込む時は送り主。
    
    ItemStack moveItem = event.getItem();
    
    if (!destination.containsAtLeast(moveItem, moveItem.getMaxStackSize())) return;
    if (destination.getHolder() instanceof Container holder){
      Block block = holder.getBlock();
      
      // 醸造台に SS 貼られないからスキップ
      if (holder instanceof BrewingStand) return;
      
      for (BlockFace face : faceList) {
        Block tar = block.getRelative(face);
        if (!StorageSignV2.isLinkStorageSign(tar, block)) continue;
        /*
        if (((BlockInventoryHolder) destination.getHolder()).getBlock().getState() instanceof Hopper) {
          if (destination.equals(initiator)) {
            // 送付元に貼られている SS から奪う場合
            // SS の引っこ抜きと押し込みが重複しないように片方だけキャンセルするようにしてみる
            Block upBlock = sourceBlock.getRelative(BlockFace.UP);
            if (upBlock.getState() instanceof Hopper) {
              if (((org.bukkit.block.data.type.Hopper) upBlock.getBlockData()).getFacing().equals(BlockFace.DOWN)) {
                event.setCancelled(true);
                return;
              }
            }
          }
        }*/
        
        StorageSignV2 SS = new StorageSignV2(tar, logger);
        if (!SS.isContentItemEquals(moveItem)) continue;
        
        new importTask(holder.getSnapshotInventory(),block,SS,tar).runTask(plugin);
        /*if (holder instanceof BrewingStand stand) {
          // 醸造台に SS 貼られないからスキップ
        }else{
          new importTask(holder.getSnapshotInventory(),block,SS,tar).runTask(plugin);
        }*/
      }
    }
  }
  
  public static class importTask extends BukkitRunnable {
    private final Inventory snapshotInventory;
    private final Block containerBlock;
    private final StorageSignV2 storageSign;
    private final Block signBlock;
    
    importTask(Inventory snapshotInventory, Block containerBlock, StorageSignV2 storageSign, Block signBlock) {
      this.snapshotInventory = snapshotInventory;
      this.containerBlock = containerBlock;
      this.storageSign = storageSign;
      this.signBlock = signBlock;
    }
    
    private ItemStack[] nonNullItemStacks(ItemStack[] itemStacks){
      return Arrays.stream(itemStacks).filter(Objects::nonNull).toArray(ItemStack[]::new);
    }
    
    @Override
    public void run() {
      // コピーから旧インベントリ分のアイテム消して実際に増えたアイテムをチェック
      Container tarInventory = (Container) containerBlock.getState();
      Inventory tarSnapshotInventory = tarInventory.getSnapshotInventory();
      tarSnapshotInventory.removeItem(nonNullItemStacks(snapshotInventory.getContents()));
      
      for (ItemStack item : nonNullItemStacks(tarSnapshotInventory.getContents())){
        // SS にねじ込む
        if(storageSign.importContentItem(item)){
          storageSign.setContentData(signBlock);
          tarInventory.getInventory().removeItem(item);
          break;
        }
      }
      
      //ItemStack[] si = nonNullItemStacks(snapshotInventory.getContents());
      //ItemStack[] ti = nonNullItemStacks(tarInventory.getInventory().getContents());
      //ItemStack[] tsi = nonNullItemStacks(tarSnapshotInventory.getContents());
      //tarSnapshotInventory.removeItem(nonNullItemStacks(snapshotInventory.getContents()));
//      tsi = nonNullItemStacks(tarSnapshotInventory.getContents());
      /*int i;
      for(ItemStack item : nonNullItemStacks(snapshotInventory.getContents()) ){
        tarSnapshotInventory.removeItem(item);
        tsi = nonNullItemStacks(tarSnapshotInventory.getContents());
        i = 0;
      }*/
      
/*      for (ItemStack item : nonNullItemStacks(tarSnapshotInventory.getContents())){
        if(storageSign.importContentItem(item)){
          storageSign.setContentData(signBlock);
          tarInventory.getInventory().removeItem(item);
          break;
        }
      }
      */
/*      ItemStack[] si = Arrays.stream(snapshotInventory.getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new);
      ItemStack[] ti = tarSnapshotInventory.getContents();
      for(ItemStack i : si){
        if(i!=null)tarSnapshotInventory.removeItem(i);
      }
      ItemStack[] importItem = tarSnapshotInventory.getContents();
      
      for (ItemStack item : importItem){
        // SS にねじ込む
        if (item != null && storageSign.importContentItem(item)){
          storageSign.setContentData(tarInventory.getBlock());
          tarInventory.getInventory().removeItem(item);
          break;
        }
      }*/
    }
  }

  
  
  
  
    /*  if(source.equals(initiator)){
        // 送付先に貼られている SS に押し込む時
        if (destination.containsAtLeast(moveItem, moveItem.getMaxStackSize())) {
          if (destination.getHolder() instanceof BlockInventoryHolder holder) {
            Block block = holder.getBlock();
            for (BlockFace face : faceList) {
              Block tar = block.getRelative(face);
              if (StorageSignV2.isLinkStorageSign(tar, block)) {
                StorageSignV2 SS = new StorageSignV2(tar, logger);
                if (SS.importContentItem(moveItem)) {
                  SS.setContentData(tar);
                  destination.removeItem(moveItem);
                  break;
                }
              }
            }
          }
        }
      }else if(destination.equals(initiator)){
        //自分に貼られている SS に押し付ける
        if (source.containsAtLeast(moveItem, moveItem.getMaxStackSize())) {
          if (source.getHolder() instanceof BlockInventoryHolder holder) {
            Block block = holder.getBlock();
            for (BlockFace face : faceList) {
              Block tar = block.getRelative(face);
              if (StorageSignV2.isLinkStorageSign(tar, block)){
                StorageSignV2 SS = new StorageSignV2(tar, logger);
                if (SS.importContentItem(moveItem)) {
                  SS.setContentData(tar);
                  destination.removeItem(moveItem);
                  break;
                }
              }
            }
          }
        }
      }
    }
    */
    //if(ConfigLoader.getAutoExport()){
      
      
      /*
      // 奪ってきた時
      logger.debug("Export: Start");
      if(source.getHolder() instanceof BlockInventoryHolder holder) {
        Block sourceBlock = holder.getBlock();
        
        for (BlockFace face : faceList) {
          Block tar = sourceBlock.getRelative(face);
          if (StorageSignV2.isLinkStorageSign(tar, sourceBlock)) {
            // SS の引っこ抜きと押し込みが重複しないように片方だけキャンセルするようにしてみる
            if (((BlockInventoryHolder) destination.getHolder()).getBlock().getState() instanceof Hopper) {
              Block upBlock = sourceBlock.getRelative(BlockFace.UP);
              if (upBlock.getState() instanceof Hopper) {
                if (((org.bukkit.block.data.type.Hopper) upBlock.getBlockData()).getFacing().equals(BlockFace.DOWN)) {
                  event.setCancelled(true);
                  return;
                }
              }
            }
            
            if (ConfigLoader.getAutoExport()) return;
            StorageSignV2 SS = new StorageSignV2(tar, logger);
            if (SS.getAmount() == 0) return;
            if (SS.isContentItemEquals(moveItem)) {
              Map<Integer, ItemStack> returnItem = destination.addItem(moveItem);
              ItemStack reItem;
              if (!returnItem.isEmpty()) {
                reItem = returnItem.get(0);
                if (moveItem.getAmount() == reItem.getAmount()) return;
                moveItem.setAmount(moveItem.getAmount() - returnItem.get(0).getAmount());
              }
              destination.removeItem(moveItem);
              
              returnItem = source.addItem(moveItem);
              if (!returnItem.isEmpty()) {
                reItem = returnItem.get(0);
                if (moveItem.getAmount() == reItem.getAmount()) return;
                moveItem.setAmount(moveItem.getAmount() - returnItem.get(0).getAmount());
              }
              SS.setAmount(SS.getAmount() - Math.min(SS.getAmount(), moveItem.getAmount()));
              SS.setContentData(tar);
            }
          }
        }
      }*/
    //}
    
    //if (ConfigLoader.getAutoImport() && source.equals(initiator) ) {
      //logger.debug("Export: Start");
      // 押し込む時
      /*
      if(destination.containsAtLeast(moveItem, moveItem.getMaxStackSize())) {
        if (destination.getHolder() instanceof BlockInventoryHolder holder) {
          Block block = holder.getBlock();
          for (BlockFace face : faceList) {
            Block tar = block.getRelative(face);
            if (StorageSignV2.isLinkStorageSign(tar, block)) {
              StorageSignV2 SS = new StorageSignV2(tar, logger);
              if (SS.importContentItem(moveItem)) {
                SS.setContentData(tar);
                destination.removeItem(moveItem);
                break;
              }
            }
          }
        }
      }
      */
    //}else if (destination.equals(initiator) ) {
      // 奪ってきた時
      /*if(source.getHolder() instanceof BlockInventoryHolder holder){
        Block sourceBlock = holder.getBlock();
        
        for (BlockFace face : faceList) {
          Block tar = sourceBlock.getRelative(face);
          if (StorageSignV2.isLinkStorageSign(tar, sourceBlock)) {
            // SS の引っこ抜きと押し込みが重複しないように片方だけキャンセルするようにしてみる
            if(( (BlockInventoryHolder)destination.getHolder() ).getBlock().getState() instanceof Hopper) {
              Block upBlock = sourceBlock.getRelative(BlockFace.UP);
              if (upBlock.getState() instanceof Hopper) {
                if (((org.bukkit.block.data.type.Hopper) upBlock.getBlockData()).getFacing().equals(BlockFace.DOWN)) {
                  event.setCancelled(true);
                  return;
                }
              }
            }
            
            exportStorageSignContent(destination,source,tar,moveItem);*/
            /*
            StorageSignV2 SS = new StorageSignV2(tar, logger);
            if (SS.getAmount() == 0) break;
            if (SS.isContentItemEquals(moveItem)) {
              Map<Integer, ItemStack> returnItem = destination.addItem(moveItem);
              ItemStack reItem;
              if (!returnItem.isEmpty()) {
                reItem = returnItem.get(0);
                if (moveItem.getAmount() == reItem.getAmount()) break;
                moveItem.setAmount(moveItem.getAmount() - returnItem.get(0).getAmount());
              }
              destination.removeItem(moveItem);
              
              returnItem = source.addItem(moveItem);
              if (!returnItem.isEmpty()) {
                reItem = returnItem.get(0);
                if (moveItem.getAmount() == reItem.getAmount()) break;
                moveItem.setAmount(moveItem.getAmount() - returnItem.get(0).getAmount());
              }
              SS.setAmount(SS.getAmount() - Math.min(SS.getAmount(), moveItem.getAmount()));
              SS.setContentData(tar);
              break;
            }
            
             */
          //}
        //}
      //}
    //}
    
    
/*
    logger.debug("ItemMoveEvent check");
    BlockState[] blockInventory = new BlockState[2];
    Boolean flag = false;
    //Sign sign = null;
    Block block = null;
    StorageSignV2 storageSign = null;
    ItemStack item = event.getItem();
    logger.trace("config.getBoolean(\"auto-import\"): " + ConfigLoader.getAutoImport());
    if (ConfigLoader.getAutoImport()) {
      logger.debug("auto-import Start");
      logger.trace("event.getDestination().getLocation(): " + event.getDestination().getLocation());
      logger.trace("event.getDestination().getHolder(): " + event.getDestination().getHolder());
      if (event.getDestination().getLocation() == null){
        logger.debug("This Event is Temp Inventory.");
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
        for(BlockFace face : faceList){
      //  for (int i = 0; i < 5; i++) {
      //    BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
      //    Block block = blockInventory[j].getBlock().getRelative(face[i]);
          block = blockInventory[j].getBlock().getRelative(face);
          //boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          //boolean relIsStorageSign = StorageSign.isStorageSign(block);
          //boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          boolean relIsSignPost = StorageSignConfig.defaultData.isFloorSign(block.getType());
          boolean relIsStorageSign = StorageSignV2.isStorageSign(block);
          boolean relIsWallSign = StorageSignConfig.defaultData.isWallSign(block.getType());
          logger.trace("blockInventory[j].getBlock(): "+ blockInventory[j].getBlock());
//          logger.trace("i: " + i);
          logger.trace("face[i]: " + face);
          logger.trace("block: " + block);
          logger.trace("relIsSignPost: " + relIsSignPost);
          logger.trace("relIsStorageSign: " + relIsStorageSign);
          logger.trace("relIsWallSign: " + relIsWallSign);
          if (relIsWallSign) {
            logger.trace("((WallSign) block.getBlockData()).getFacing(): "
                    + ((WallSign) block.getBlockData()).getFacing());
          }
          if (face.equals(BlockFace.UP) && relIsSignPost && relIsStorageSign) {
            storageSign = new StorageSignV2(block,logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              flag = true;
              break importLoop;
            }
          } else if (!face.equals(BlockFace.UP) && relIsWallSign
                  && ((WallSign) block.getBlockData()).getFacing() == face && relIsStorageSign) {
          //  sign = (Sign) block.getState();
            storageSign = new StorageSignV2(block, logger);
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
        importSign(block, storageSign, item, event.getDestination());
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
        for(BlockFace face: faceList){
//        for (int i = 0; i < 5; i++) {
//          BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
          block = blockInventory[j].getBlock().getRelative(face);
          //boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          //boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
          //boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          boolean relIsSignPost = StorageSignConfig.defaultData.isFloorSign(block.getType());
          boolean relIsStorageSign = StorageSignV2.isStorageSign(block);
          boolean relIsWallSign = StorageSignConfig.defaultData.isWallSign(block.getType());
          logger.trace("blockInventory[j].getBlock(): "+ blockInventory[j].getBlock());
//          logger.trace("i: " + i);
          logger.trace("face[i]: " + face);
          logger.trace("block: " + block);
          logger.trace("relIsSignPost: " + relIsSignPost);
          logger.trace("relIsStorageSign: " + relIsStorageSign);
          logger.trace("relIsWallSign: " + relIsWallSign);
          if (relIsWallSign) {
            logger.trace("((WallSign) block.getBlockData()).getFacing(): "
                    + ((WallSign) block.getBlockData()).getFacing());
          }
          if (face.equals(BlockFace.UP) && relIsSignPost && relIsStorageSign) {
            //sign = (Sign) block.getState();
            storageSign = new StorageSignV2(block, logger);
            logger.trace("storageSign.isSimilar(item): " + storageSign.isSimilar(item));
            if (storageSign.isSimilar(item)) {
              logger.debug("This item is StorageSign.break.");
              flag = true;
              break exportLoop;
            }
          } else if (!face.equals(BlockFace.UP) && relIsWallSign &&
                  ((WallSign) block.getBlockData()).getFacing() == face && relIsStorageSign) {
            //sign = (Sign) block.getState();
            storageSign = new StorageSignV2(block, logger);
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
        exportSign(block, storageSign, item, event.getSource(), event.getDestination());
      }
    }
    logger.debug("★onItemMove:End.");
    */
  //}
  
  /*private void importStorageSignContent(Inventory destination, Inventory source , Block StorageSignBlock, ItemStack moveItem){
  //private void importStorageSignContent(Inventory destination,ItemStack moveItem){
    if(destination.containsAtLeast(moveItem, moveItem.getMaxStackSize())) {
      if (destination.getHolder() instanceof BlockInventoryHolder holder) {
        Block block = holder.getBlock();
        for (BlockFace face : faceList) {
          Block tar = block.getRelative(face);
          if (StorageSignV2.isLinkStorageSign(tar, block)) {
            StorageSignV2 SS = new StorageSignV2(tar, logger);
            if (SS.importContentItem(moveItem)) {
              SS.setContentData(tar);
              destination.removeItem(moveItem);
              break;
            }
          }
        }
      }
    }
  }//*/
  
  /*private void exportStorageSignContent(Inventory destination, Inventory source , Block StorageSignBlock, ItemStack moveItem){
    if (ConfigLoader.getAutoExport()) return;
    StorageSignV2 SS = new StorageSignV2(StorageSignBlock, logger);
    if (SS.getAmount() == 0) return;
    if (SS.isContentItemEquals(moveItem)) {
      Map<Integer, ItemStack> returnItem = destination.addItem(moveItem);
      ItemStack reItem;
      if (!returnItem.isEmpty()) {
        reItem = returnItem.get(0);
        if (moveItem.getAmount() == reItem.getAmount()) return;
        moveItem.setAmount(moveItem.getAmount() - returnItem.get(0).getAmount());
      }
      destination.removeItem(moveItem);
      
      returnItem = source.addItem(moveItem);
      if (!returnItem.isEmpty()) {
        reItem = returnItem.get(0);
        if (moveItem.getAmount() == reItem.getAmount()) return;
        moveItem.setAmount(moveItem.getAmount() - returnItem.get(0).getAmount());
      }
      SS.setAmount(SS.getAmount() - Math.min(SS.getAmount(), moveItem.getAmount()));
      SS.setContentData(StorageSignBlock);
    }
  }//*/

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

      //Sign sign = null;
      Block block = null;
      StorageSignV2 storageSign = null;
      boolean flag = false;
      for(BlockFace face: faceList){
//      for (int i = 0; i < 5; i++) {
//        BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
        block = ((BlockState) holder).getBlock().getRelative(face);
        //boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
        //boolean relIsStorageSign = StorageSign.isStorageSign(block, logger);
        //boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
        boolean relIsSignPost = StorageSignConfig.defaultData.isFloorSign(block.getType());
        boolean relIsStorageSign = StorageSignV2.isStorageSign(block);
        boolean relIsWallSign = StorageSignConfig.defaultData.isWallSign(block.getType());
//        logger.trace(" i: " + i);
        logger.trace(" relIsSignPost: " + relIsSignPost);
        logger.trace(" relIsStorageSign: " + relIsStorageSign);
        logger.trace(" relIsWallSign: " + relIsWallSign);
        logger.trace(" face[i]: " + face);
        if(relIsWallSign) {
          logger.trace(" ((WallSign) block.getBlockData()).getFacing() == face[i]: " + (
                  ((WallSign) block.getBlockData()).getFacing() == face));
        }
        if (face.equals(BlockFace.UP) && relIsSignPost && relIsStorageSign) {
          logger.debug(" This block is StorageSign.");
          //sign = (Sign) block.getState();
          storageSign = new StorageSignV2(block, logger);
          logger.trace(" storageSign.isSimilar(event.getItem().getItemStack()): " + storageSign.isSimilar(event.getItem().getItemStack()));
          if (storageSign.isSimilar(event.getItem().getItemStack())) {
            logger.debug(" this Item is Import Item.");
            flag = true;
            break;
          }
        } else if (!face.equals(BlockFace.UP) && relIsWallSign &&
                ((WallSign) block.getBlockData()).getFacing() == face && relIsStorageSign) {
          //BlockFaceに変更？(めんどい)
          logger.debug(" This block is WallStorageSign.");
          //sign = (Sign) block.getState();
          storageSign = new StorageSignV2(block, logger);
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
        importSign(block, storageSign, event.getItem().getItemStack(), event.getInventory());
      }
    }
    logger.debug("★onInventoryPickup:End");
  }

  private void importSign(Block block, StorageSignV2 storageSign, ItemStack item, Inventory inv) {
    logger.debug("importSign:Start");
    //搬入　条件　1スタック以上アイテムが入っている
    logger.trace("inv.containsAtLeast(item, item.getMaxStackSize()): " + inv.containsAtLeast(item, item.getMaxStackSize()));

    if(storageSign.importContentItem(item)){
      inv.removeItem(item);
      storageSign.setContentData(block);
    }
/*    if (inv.containsAtLeast(item, item.getMaxStackSize())) {
      logger.debug("Item is more 1s.Import Item.");
      inv.removeItem(item);
      storageSign.addAmount(item.getAmount());
    }
    for (int i = 0; i < 4; i++) {
      logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
    }*/
    logger.debug("update Sign.");
//    sign.update();
  }

  //搬出先ブロックに枠指定があると事故る
  private void exportSign(Block block, StorageSignV2 storageSign, ItemStack item, Inventory inv, Inventory dest) {
    logger.debug("exportSign:Start.");
    logger.trace("item: " + item);
    logger.trace("!inv.containsAtLeast(item, item.getMaxStackSize(): " + !inv.containsAtLeast(item, item.getMaxStackSize()));
//    logger.trace("storageSign.getAmount(): " + storageSign.getAmount());
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
      storageSign.setAmount(storageSign.getAmount() - cItem.getAmount());
      //storageSign.addAmount(-cItem.getAmount());
    }
/*    for (int i = 0; i < 4; i++) {
      logger.trace("set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
    }
    sign.update();*/
    storageSign.setContentData(block);
    logger.debug("ExportSign:End");
  }

}
