package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * SSと情報をやり取りするシステム
 * ItemStack や String に関しては 継承クラスにて
 * cord値 を拾ったり
 * ItemMeta#getLore や BlockState#SignSide#Line[1] から情報拾う必要があるので用意しておく
 */
public class NormalInformation implements SSInformation {

  /**
   * 収納されているアイテム情報
   */
  protected Material content;
  protected final Logger logger;

  public NormalInformation(ItemStack itemStack, Logger logger) {
    this(itemStack.getType(), logger);
  }

  public NormalInformation(String material, Logger logger){
    this(Material.getMaterial(material), logger);
  }

  public NormalInformation(Material material, Logger logger){
    this.content = material;
    this.logger = logger;
  }

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return getSSLoreItemData();
  }

  /**
   * アイテムStorageSign の Lore に書き込む情報を作成
   * [アイテムフルネーム] [アイテム数 amount]
   * @return 貯蔵アイテム情報 (Lore)
   */
  @Override
  public String getSSLoreItemData() {
    return content.toString();
  }

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    logger.debug("getStorageItemStack: Start");
    return new ItemStack(content);
  }

  /**
   * アイテムスタックを使ってのアイテム比較
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(ItemStack itemStack) {
//    return itemStack.isSimilar(getStorageItemStack());
    ItemStack i = getStorageItemStack();
    return itemStack.isSimilar(i);
  }

  /**
   * Lore 文字列を使ってのアイテム比較
   * @param lore 比較する Lore
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(String lore) {
    return lore.startsWith(getSSLoreItemData());
  }

}
