package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.OminousBottleMeta;

public class OminousBottle extends CordInformation<OminousBottleMeta> implements SSInformation{

  public OminousBottle(ItemStack itemStack, Logger logger) {
    this(getLevel((OminousBottleMeta) itemStack.getItemMeta()), logger);
  }

  public OminousBottle(String itemData, Logger logger) {
    this(Integer.parseInt(itemData.split("[: ]")[1]), logger);
  }

  public OminousBottle(int cord, Logger logger){
    super(Material.OMINOUS_BOTTLE, cord, logger);
  }

  /**
   * OminousBottleMeta から AmplifierLevel を取得
   */
  private static int getLevel(OminousBottleMeta meta){
    return meta.hasAmplifier() ? meta.getAmplifier() : 0;
  }

  /**
   * StorageSign で使われる コード値取得
   * [コード値] 部分に登録する値を戻す値にする
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(OminousBottleMeta meta){
    return getLevel(meta);
  }

  /**
   * ItemMeta に コード値 を設定
   * [コード値] 部分を参照して ItemMeta に情報を追加する
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(){
    if(cord == 0) return null;
    OminousBottleMeta meta = getContentItemMeta();
    meta.setAmplifier(cord);
    return meta;
  }

}
