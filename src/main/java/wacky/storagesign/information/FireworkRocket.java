package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FireworkRocket extends CordInformation<FireworkMeta> implements SSInformation{

  public FireworkRocket(ItemStack itemStack, Logger logger) {
    this(getLevel((FireworkMeta) itemStack.getItemMeta()), logger);
  }

  public FireworkRocket(String itemData, Logger logger) {
    this(Integer.parseInt(itemData.split("[: ]")[1]),logger);
  }

  public FireworkRocket(int cord, Logger logger){
    super(Material.FIREWORK_ROCKET,cord,logger);
  }

  /**
   * FireworkMeta から AmplifierLevel を取得
   */
  private static int getLevel(FireworkMeta meta){
    return meta.getPower();
  }

  /**
   * StorageSign で使われる コード値取得
   * [コード値] 部分に登録する値を戻す値にする
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(FireworkMeta meta) {
    return getLevel(meta);
  }

  /**
   * ItemMeta に コード値 を設定
   * [コード値] 部分を参照して ItemMeta に情報を追加する
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @param meta セットしたい ItemMeta
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(FireworkMeta meta) {
    meta.setPower(cord);
    return meta;
  }

}
