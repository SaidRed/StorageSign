package wacky.storagesign.eventHandler;

import com.github.teruteru128.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import wacky.storagesign.ConfigLoader;
import wacky.storagesign.StorageSignConfig;
import wacky.storagesign.StorageSignCore;
import wacky.storagesign.StorageSignV2;

import java.util.HashMap;
import java.util.Map;

public class StorageSignBlockPlaceBreak implements Listener {

  private final Logger logger;

  public StorageSignBlockPlaceBreak(StorageSignCore plugin) {
    this.logger = plugin.logger;
    PluginManager manager = plugin.getServer().getPluginManager();
    manager.registerEvents(new blockPlaceBreak(),plugin);
    if (ConfigLoader.getFallingBlockItemSs()) {
      manager.registerEvents(new entityChangeBlock(), plugin);
    }
  }

  private class blockPlaceBreak extends dropStorageSign implements Listener {
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

      //    boolean handItemIsSS = StorageSign.isStorageSign(event.getItemInHand(), logger);
      logger.trace("event.isCancelled(): " + event.isCancelled());
      //    logger.trace("!handItemIsSS: " + !handItemIsSS);
      if (event.isCancelled() || !StorageSignV2.isStorageSign(event.getItemInHand())) {
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
      StorageSignV2 storageSign = new StorageSignV2(event.getItemInHand(), logger);

      Block block = event.getBlock();
  /*    for (int i = 0; i < 4; i++) {
        logger.trace(" set Line i:" + i + ". Text: " + storageSign.getSigntext(i));
        sign.getSide(Side.FRONT).setLine(i, storageSign.getSigntext(i));
      }*/
      
      logger.debug("UpdateSign.");
      //ダークオークの場合文字色を白くする
      storageSign.playerPlace(block);

      //    logger.trace("storageSign.getSmat() == Material.DARK_OAK_SIGN: " + (storageSign.getSmat() == Material.DARK_OAK_SIGN));
      //logger.trace("storageSign.materialContent: " + storageSign.materialContent.toString());
      


      //時差発動が必要らしい
      player.closeInventory();
      logger.debug("★onBlockPlace: End");
    }
  }

  private class entityChangeBlock extends dropStorageSign implements Listener {
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
      if (event.getEntity() instanceof FallingBlock fallingBlock) {
        dropRelativeSign(event.getBlock());
      }
    }
  }

  private class dropStorageSign {
    protected void dropRelativeSign(Block block) {
      logger.debug(" dropRelativeSign:Start");

      Map<Location, Block> breakSignMap = new HashMap<>();

      logger.trace(" isStorageSign: " + StorageSignV2.isStorageSign(block));
      if (StorageSignV2.isStorageSign(block)) {
        logger.debug(" breakItem is StorageSign.");
        breakSignMap.put(block.getLocation(), block);
      }
      
      for (BlockFace face : new BlockFace[]{BlockFace.UP,BlockFace.NORTH,BlockFace.EAST,BlockFace.WEST,BlockFace.SOUTH}){
        Block relBlock = block.getRelative(face);
        logger.trace("  relBlock: " + relBlock);
        if (StorageSignV2.isLinkStorageSign(relBlock,block))
          breakSignMap.put(relBlock.getLocation(),relBlock);
        
        //if (face.equals(BlockFace.UP) && StorageSignConfig.defaultData.isFloorSign(relBlock.getType())) {
        //  breakSignMap.put(relBlock.getLocation(), relBlock);
        //} else if (relBlock.getBlockData() instanceof WallSign wallSign)
        //  if (wallSign.getFaces().equals(face.getOppositeFace()))
        //    breakSignMap.put(relBlock.getLocation(), relBlock);
      }

      /*
      Block relBlock = block.getRelative(BlockFace.UP);
      logger.trace("  relBlock: " + relBlock);
      if (relBlock.getBlockData() instanceof Sign) {
          breakSignMap.put(relBlock.getLocation(), relBlock);
      }

      //東西南北で判定
      BlockFace[] faces = {BlockFace.SOUTH,
              BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST};
      for (BlockFace face : faces) {
        relBlock = block.getRelative(face);
        logger.trace("  relBlock: " + relBlock);
        logger.trace("  relIsStorageSign: " + StorageSignV2.isStorageSign(block));

        if (relBlock.getBlockData() instanceof WallSign sign) {
          if (sign.getFacing() == face)
            breakSignMap.put(relBlock.getLocation(), relBlock);
        }
      }*/

      logger.trace(" breakSignMap.isEmpty(): " + breakSignMap.isEmpty());
      if (breakSignMap.isEmpty()) {
        logger.debug(" This Block isn't block StorageSign.");
        return;
      }

      logger.debug(" Break StorageSign Set Item in World.");
      for (Location loc : breakSignMap.keySet()) {
        Block SS = breakSignMap.get(loc);
        ItemStack drops = SS.getDrops().toArray(new ItemStack[0])[0];
        StorageSignV2 breakSS = new StorageSignV2(SS, logger);

        logger.trace("  loc: " + loc);

        loc.getWorld().dropItem(loc.add(0.5, 0.5, 0.5), breakSS.getStorageSign(drops));
        loc.getBlock().setType(Material.AIR);
        logger.debug("  Storage Sign drop in World.");
      }
    }
  }

}
