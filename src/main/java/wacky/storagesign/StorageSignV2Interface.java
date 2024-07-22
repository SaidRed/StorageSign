package wacky.storagesign;

import org.bukkit.inventory.ItemStack;

public interface StorageSignV2Interface {

  /**
   * アイテムStorageSign の ItemStack
   *
   * @return StorageSign ItemStack
   */
  ItemStack getStorageSign();

  /**
   * 在庫しているアイテム の 出庫
   * 最大スタック数 と 在庫数 を比較して出庫する
   * 出庫したら amount のカウントダウン
   *
   * @param sneaking スニークしているか true : スニーク中 / false : スニークしていない
   * @return 在庫しているアイテムの ItemStack
   */
  ItemStack outputContentItem(boolean sneaking);

  /**
   * itemStack の入庫を挑戦する
   * 入庫したら amount のカウントアップ
   * インベントリのアイテム消去は別処理
   *
   * @param itemStack 入庫するアイテム
   * @return 入庫の有無 true : 入庫完了 / false : 入庫失敗
   */
  boolean importContentItem(ItemStack itemStack);

  /**
   * 入庫しているアイテムと同一かの判定
   * エンチャ本は本自身の合成回数を問わない.
   *
   * @param itemStack 入庫アイテムと比較する ItemStack
   * @return  true：同一と認める / false：同一と認めない
   */
  boolean isContentItemEquals(ItemStack itemStack);

  /**
   * StrageSign の倉庫が空かを確認します.
   *
   * @return true：空っぽ / false：入ってます
   */
  boolean isEmpty();

}
