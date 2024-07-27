package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class ToolInformation extends CordInformation<Damageable> implements SSInformation{

  public ToolInformation(ItemStack itemStack, Logger logger){
    this(itemStack.getType(), getDamage((Damageable)itemStack.getItemMeta()), logger);
  }

  public ToolInformation(String itemData, Logger logger){
    this(itemData.split("[: ]"), logger);
  }

  public ToolInformation(String[] itemData, Logger logger){
    this(Material.getMaterial(itemData[0]), itemData.length>1 ? Integer.parseInt(itemData[1]) : 0, logger);
  }

  public ToolInformation(Material material, int cord, Logger logger) {
    super(material, cord, logger);
    truncateZero = false;
  }

  protected static int getDamage(Damageable meta){
    return meta.getDamage();
  }

  /**
   * StorageSign で使われる コード値取得
   * [コード値] 部分に登録する値を戻す値にする
   * <p>[アイテムショートネーム]:[コード値]</p>
   *
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(Damageable meta) {
    return meta.getDamage();
  }

  /**
   * ItemMeta に コード値 を設定
   * [コード値] 部分を参照して ItemMeta に情報を追加する
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(){
    Damageable meta = getContentItemMeta();
    meta.setDamage(cord);
    return meta;
  }

}
