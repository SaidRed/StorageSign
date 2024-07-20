package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;

public class OminousBottle extends CordInformation<OminousBottleMeta> implements SSInformation{

  public OminousBottle(ItemStack itemStack, Logger logger) {
    this((OminousBottleMeta) itemStack.getItemMeta(), logger);
  }

  public OminousBottle(OminousBottleMeta itemType, Logger logger) {
    this(itemType.getAmplifier(), logger);
  }

  public OminousBottle(String itemData, Logger logger) {
    this(Integer.parseInt(itemData.split(":")[1]), logger);
  }

  public OminousBottle(int cord, Logger logger){
    super(Material.OMINOUS_BOTTLE, cord, logger);
  }

  /**
   * StorageSign で使われる コード値取得
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(OminousBottleMeta meta){
    return meta.getAmplifier();
  }

  /**
   * ItemMeta に コード値 を設定
   * @param meta セットしたい ItemMeta
   * @param cord セットしたい Cord値
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(OminousBottleMeta meta, int cord) {
    meta.setAmplifier(cord);
    return meta;
  }

}
