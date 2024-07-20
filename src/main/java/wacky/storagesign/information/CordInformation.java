package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public abstract class CordInformation<M extends ItemMeta> extends NormalInformation{
  protected final int cord;

  public CordInformation(Material material, int cord, Logger logger) {
    super(material, logger);
    this.cord = cord;
  }

  /**
   * StorageSign で使われる コード値取得
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  protected abstract int getCord(M meta);

  /**
   * ItemMeta に コード値 を設定
   * @param meta セットしたい ItemMeta
   * @param cord セットしたい Cord値
   * @return Cord値 をセットし終わった itemMeta
   */
  protected abstract ItemMeta setCord(M meta, int cord);

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[コード値]
   *
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return  getSSLoraItemData();
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム]:[コード値] [アイテム数 amount]
   *
   * @return 貯蔵アイテム情報 (Lora)
   */
  @Override
  public String getSSLoraItemData() {
    return material.toString() + ":" + cord;
  }

  /**
   * StorageSign として排出する貯蔵アイテム ItemStack
   *
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    logger.debug("getStorageItemStack: Start");
    ItemStack item = new ItemStack(material);

    logger.debug("Level: " + cord);
    M meta = (M) item.getItemMeta();
    item.setItemMeta(setCord(meta,cord));

    return item;
  }

  /**
   * アイテムスタックを使ってのアイテム比較
   *
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(ItemStack itemStack) {
    if(!(material.equals(itemStack.getType())))return false;

    ItemMeta meta = itemStack.getItemMeta();
    if(Objects.isNull(meta)) return false;
    M TMeta = (M) meta;
    return cord == getCord(TMeta);
  }

}
