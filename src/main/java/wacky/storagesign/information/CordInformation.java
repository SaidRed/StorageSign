package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public abstract class CordInformation<M extends ItemMeta> extends NormalInformation implements SSInformation{
  protected final int cord;
  /**
   * ゼロを表するかどうかの設定
   * true : 表示する / false : 表示しない
   */
  protected boolean truncateZero = true;

  public CordInformation(Material material, int cord, Logger logger) {
    super(material, logger);
    this.cord = cord;
  }

  /**
   * StorageSign で使われる コード値取得
   * [コード値] 部分に登録する値を戻す値にする
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  protected abstract int getCord(M meta);

  /**
   * ItemMeta に コード値 を設定
   * [コード値] 部分を参照して ItemMeta に情報を追加する
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @return Cord値 をセットし終わった itemMeta
   */
  protected abstract ItemMeta setCord();

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[コード値]
   *
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return  getSSLoreItemData();
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム]:[コード値] [アイテム数 amount]
   *
   * @return 貯蔵アイテム情報 (Lora)
   */
  @Override
  public String getSSLoreItemData() {
    return content.toString() + (! truncateZero && cord == 0 ? "" : ":" + cord);
  }

  /**
   * 収納しているアイテムのデフォルトメタデータ
   * @return 収納アイテムのメタデータ
   */
  public M getContentItemMeta() {
    return (M) Bukkit.getServer().getItemFactory().getItemMeta(content);
  }

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    logger.debug("getStorageItemStack: Start");
    ItemStack item = new ItemStack(content);

    logger.debug("cord: " + cord);
    if(setCord() != null) item.setItemMeta(setCord());

    return item;
  }

}
