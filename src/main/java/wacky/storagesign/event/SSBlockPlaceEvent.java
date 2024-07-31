package wacky.storagesign.event;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SSBlockPlaceEvent extends BlockPlaceEvent {

  public SSBlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild, EquipmentSlot hand) {
    super(placedBlock, replacedBlockState, placedAgainst, itemInHand, thePlayer, canBuild, hand);
  }

  public void setBlockReplacedState(BlockState blockReplacedStateState){
    this.replacedBlockState = blockReplacedStateState;
  }
}
