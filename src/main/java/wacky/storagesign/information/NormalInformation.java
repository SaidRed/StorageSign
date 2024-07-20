package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class NormalInformation implements SSInformation {

  protected final Material material;
  protected final Logger logger;

  public NormalInformation(ItemStack itemStack, Logger logger) {
    this(itemStack.getType(), logger);
  }

  public NormalInformation(String material, Logger logger){
    this(Material.getMaterial(material), logger);
  }

  public NormalInformation(Material material, Logger logger){
    this.material = material;
    this.logger = logger;
  }

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]
   *
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return getSSLoraItemData();
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム] [アイテム数 amount]
   *
   * @return 貯蔵アイテム情報 (Lora)
   */
  @Override
  public String getSSLoraItemData() {
    return material.toString();
  }

  /**
   * StorageSign として排出する貯蔵アイテム ItemStack
   *
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    logger.debug("getStorageItemStack: Start");
    return new ItemStack(material);
  }

  /**
   * アイテムスタックを使ってのアイテム比較
   *
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(ItemStack itemStack) {
    return material == itemStack.getType();
  }

  /**
   * Lora 文字列を使ってのアイテム比較
   *
   * @param lora 比較する Lora アイテム情報 [アイテムフルネーム]:[タイプフルネーム]:[コード値]
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(String lora) {
    return lora.startsWith(getSSLoraItemData());
  }

  @Override
  public Material getMaterial() {
    return this.material;
  }

}
