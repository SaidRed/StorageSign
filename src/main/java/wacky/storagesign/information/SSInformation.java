package wacky.storagesign.information;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface SSInformation {

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[タイプショートネーム]:[コード値]
   *
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  String getSSStorageItemData();

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]
   *
   * @return 貯蔵アイテム情報 (Lora)
   */
  String getSSLoraItemData();

  /**
   * StorageSign として排出する貯蔵アイテム ItemStack
   *
   * @return Storage ItemStack
   */
  ItemStack getStorageItemStack();

  /**
   * アイテムスタックを使ってのアイテム比較
   *
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  boolean isSimilar(ItemStack itemStack);

  /**
   * Lora 文字列を使ってのアイテム比較
   *
   * @param lora 比較する Lora アイテム情報 [アイテムフルネーム]:[タイプフルネーム]:[コード値]
   * @return true 同一と認める/false 同一と認めない
   */
  boolean isSimilar(String lora);

  Material getMaterial();
}
