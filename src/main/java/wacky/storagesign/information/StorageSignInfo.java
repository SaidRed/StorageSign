package wacky.storagesign.information;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import wacky.storagesign.ConfigLoader;
import wacky.storagesign.StorageSignConfig;
import com.github.teruteru128.logger.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * StorageSign に入庫されている StorageSign 情報
 * なので Empty StorageSign の情報を保管する
 */
public class StorageSignInfo extends NormalInformation implements SSInformation{

  /**
   * Empty StorageSign だけが対象とする
   * @param itemStack StorageSign
   * @param logger ロガー
   */
  public StorageSignInfo(ItemStack itemStack, Logger logger) {
    this(itemStack.getType(), logger);
  }

  /**
   * Block から排出する時 だけが対象とする
   * @param itemData ***StorageSign
   * @param logger ロガー
   */
  public StorageSignInfo(String itemData, Logger logger) {
    this(checkMaterial(itemData), logger);
  }

  public StorageSignInfo(Material material, Logger logger) {
    super(material, logger);
  }

  /**
   * StorageSign に登録されているアイテム情報から Material を取得する
   * @param itemData チェックしたいアイテム情報
   * @return Material 情報
   */
  private static Material checkMaterial(String itemData){
    String materialName = Arrays.stream(
                    itemData.replace(StorageSignConfig.defaultData.STORAGE_SIGN_NAME,"Sign")
                            .split("(?<=[a-z])(?=[A-Z])"))
            .map(String::toUpperCase)
            .collect(Collectors.joining("_"));
    return Material.getMaterial(materialName);
  }

  /**
   * MaterialName から StorageSign に収納されている場合の StorageSign 表記名
   * @return 素材木材名 + StorageSign
   */
  private String StorageSignItemName(Material material) {
    return Arrays.asList(material.toString().split("_"))
            .stream()
            .filter(S ->! S.equals("SIGN"))
            .map(String::toLowerCase)
            .map(StringUtils::capitalize)
            .collect(Collectors.joining()) + StorageSignConfig.defaultData.STORAGE_SIGN_NAME;
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム] [アイテム数 amount]
   * @return 貯蔵アイテム情報 (Lora)
   */
  @Override
  public String getSSStorageItemData() {
    return StorageSignItemName(content);
  }

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
   * Empty の StorageSign を出庫する
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    logger.debug("getStorageItemStack: Start");
    ItemStack item = new ItemStack(content);

    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(StorageSignConfig.defaultData.STORAGE_SIGN_NAME);
    meta.setLore(List.of(StorageSignConfig.defaultData.empty));
    // コンフィグ設定 MaxStackSize
    if(ConfigLoader.getMaxStackSize() != 0)meta.setMaxStackSize(ConfigLoader.getMaxStackSize());
    item.setItemMeta(meta);
    return item;
  }

  /**
   * アイテムスタックを使ってのアイテム比較
   * Lore 名で比較して同一か判定
   * 保管できる StorageSign が Empty のみなので Lora の比較は Empty のみ対象
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(ItemStack itemStack) {
    if(! content.equals(itemStack.getType()))return false;
    ItemMeta meta = itemStack.getItemMeta();
    if(! meta.getDisplayName().equals(StorageSignConfig.defaultData.STORAGE_SIGN_NAME))return false;
    String itemData = meta.getLore().getFirst();
    return itemData.equals(StorageSignConfig.defaultData.empty);
  }

}
