package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FireworkRocket extends CordInformation<FireworkMeta> implements SSInformation{

  public FireworkRocket(ItemStack itemStack, Logger logger) {
    this((FireworkMeta) itemStack.getItemMeta(), logger);
  }

  public FireworkRocket(FireworkMeta itemType, Logger logger) {
    this(itemType.getPower(),logger);
  }

  public FireworkRocket(String itemData, Logger logger) {
    this(Integer.parseInt(itemData.split(":")[1]),logger);
  }

  public FireworkRocket(int cord, Logger logger){
    super(Material.FIREWORK_ROCKET,cord,logger);
  }

  /**
   * StorageSign で使われる コード値取得
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(FireworkMeta meta) {
    return meta.getPower();
  }

  /**
   * ItemMeta に コード値 を設定
   * @param meta セットしたい ItemMeta
   * @param cord セットしたい Cord値
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(FireworkMeta meta, int cord) {
    meta.setPower(cord);
    return meta;
  }

}
