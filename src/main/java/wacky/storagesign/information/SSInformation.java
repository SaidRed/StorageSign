package wacky.storagesign.information;

import org.bukkit.inventory.ItemStack;

public interface SSInformation {

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[タイプショートネーム]:[コード値]
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  String getSSStorageItemData();

  /**
   * アイテムStorageSign の Lore に書き込む情報を作成
   * [アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]
   * @return 貯蔵アイテム情報 (Lore)
   */
  String getSSLoreItemData();

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
   * @return Storage ItemStack
   */
  ItemStack getStorageItemStack();

  /**
   * アイテムスタックを使ってのアイテム比較
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  boolean isSimilar(ItemStack itemStack);

  /**
   * Lore 文字列を使ってのアイテム比較
   * @param lore 比較する Lore アイテム情報 [アイテムフルネーム]:[タイプフルネーム]:[コード値]
   * @return true 同一と認める/false 同一と認めない
   */
  boolean isSimilar(String lore);

  /**
   * StorageSign の表記 ItemData から 新しいアイテムの上書き
   * @param itemData
   */
//  void setContent(String itemData);

}
