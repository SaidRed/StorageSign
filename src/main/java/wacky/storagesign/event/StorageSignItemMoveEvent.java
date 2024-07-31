package wacky.storagesign.event;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import wacky.storagesign.ConfigLoader;
//import wacky.storagesign.StorageSign;
import wacky.storagesign.StorageSignV2;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.signdefinition.SignDefinition;

public class StorageSignItemMoveEvent implements Listener {

  private final Logger logger;

  private BlockFace[] faceList = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};

  public StorageSignItemMoveEvent(StorageSignCore plugin){
    this.logger = plugin.logger;
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
        for(BlockFace face : faceList){
      //  for (int i = 0; i < 5; i++) {
      //    BlockFace[] face = {BlockFace.UP, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
      //    Block block = blockInventory[j].getBlock().getRelative(face[i]);
          block = blockInventory[j].getBlock().getRelative(face);
          //boolean relIsSignPost = SignDefinition.sign_materials.contains(block.getType());
          //boolean relIsStorageSign = StorageSign.isStorageSign(block);
          //boolean relIsWallSign = SignDefinition.wall_sign_materials.contains(block.getType());
          boolean relIsSignPost = StorageSignV2.isFloorSign(block.getType());
          boolean relIsStorageSign = StorageSignV2.isStorageSign(block);
          boolean relIsWallSign = StorageSignV2.isWallSign(block.getType());
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
          boolean relIsSignPost = StorageSignV2.isFloorSign(block.getType());
          boolean relIsStorageSign = StorageSignV2.isStorageSign(block);
          boolean relIsWallSign = StorageSignV2.isWallSign(block.getType());
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
        boolean relIsSignPost = StorageSignV2.isFloorSign(block.getType());
        boolean relIsStorageSign = StorageSignV2.isStorageSign(block);
        boolean relIsWallSign = StorageSignV2.isWallSign(block.getType());
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
      storageSign.setStorageData(block);
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
      storageSign.addAmount(-cItem.getAmount());
    }
/*    for (int i = 0; i < 4; i++) {
      logger.trace("set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
      sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
    }
    sign.update();*/
    storageSign.setStorageData(block);
    logger.debug("ExportSign:End");
  }

}
