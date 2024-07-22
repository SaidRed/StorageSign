package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public abstract class InformationAbstract<M extends ItemMeta> implements SSInformation{
  protected final Material material;
  protected final int cord;
  protected final Logger logger;

  public InformationAbstract(Material material,int cord, Logger logger) {
    this.material = material;
    this.cord = cord;
    this.logger = logger;
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
  protected abstract ItemMeta setCord(M meta,int cord);

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[タイプショートネーム]:[コード値]
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return  material.toString() + ":" + cord;
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]
   * @return 貯蔵アイテム情報 (Lora)
   */
  @Override
  public String getSSLoreItemData() {
    return material.toString() + ":" + cord;
  }

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
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

  /**
   * Lora 文字列を使ってのアイテム比較
   * @param lora 比較する Lora アイテム情報 [アイテムフルネーム]:[タイプフルネーム]:[コード値]
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(String lora) {
    return lora.startsWith(getSSLoreItemData());
  }

  public Material getMaterial(){
    return material;
  }

}
