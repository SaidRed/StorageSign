package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import wacky.storagesign.StorageSignConfig;

public class VersionStraddle extends CordInformation<ItemMeta> implements SSInformation{
  public VersionStraddle(ItemStack itemStack, Logger logger) {
    this(itemStack.getType(), StorageSignConfig.versionConvert.getMaxCord(itemStack.getType()), logger);
  }

  public VersionStraddle(String itemData, Logger logger) {
    this(itemData.split("[: ]"), logger);
  }

  private VersionStraddle(String[] itemData, Logger logger){
    this(Material.getMaterial(itemData[0]), itemData.length > 1 ? Integer.parseInt(itemData[1]) : 0, logger);
  }

  public VersionStraddle(Material material, int cord, Logger logger) {
    super(StorageSignConfig.versionConvert.getMaterial(material,cord), cord, logger);
    truncateZero = false;
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
  protected int getCord(ItemMeta meta) {
    return cord;
  }

  /**
   * ItemMeta に コード値 を設定
   * [コード値] 部分を参照して ItemMeta に情報を追加する
   * このクラスでは ItemMeta を作らない
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord() {
    return null;
  }

}
