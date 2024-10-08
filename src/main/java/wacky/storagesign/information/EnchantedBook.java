package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import wacky.storagesign.StorageSignConfig.enchantedBook;

/**
 * エンチャントブックの表記ルール
 * [エンチャントブック ショートネーム]:[エンチャント効果]:[エンチャントレベル] [ストレージ数]
 * ENCHBOOK : EnchantType : EnchantLevel
 */
public class EnchantedBook extends TypeInformation<Enchantment, EnchantmentStorageMeta> implements SSInformation{

  private static final Material material = Material.ENCHANTED_BOOK;

  public EnchantedBook(String itemData, Logger logger){
    this(itemData.split("[: ]"), logger);
  }

  private EnchantedBook(String[] itemData, Logger logger){
    this(getEnchantment(itemData[1]), Integer.parseInt(itemData[2]), logger);
  }

  public EnchantedBook(ItemStack itemStack, Logger logger){
    this(getEnchantment(itemStack.getItemMeta()), getLevel(itemStack.getItemMeta()), logger);
  }

  public EnchantedBook(Enchantment type, int cord, Logger logger) {
    super(Material.ENCHANTED_BOOK, type, cord, logger);
  }


  /**
   * SS 表記の名前から エンチャントタイプを取得
   * @param enchantName エンチャント名 (ショートネーム可)
   * @return エンチャント名
   */
  private static Enchantment getEnchantment(String enchantName) {
    //後ろ切れても可.
    return enchantedBook.getEnchantment(enchantName);
  }

  /**
   * メタデータからエンチャントタイプを取得
   * @param meta エンチャントタイプを取得したいメタデータ
   * @return エンチャントタイプデータ
   */
  private static Enchantment getEnchantment(ItemMeta meta) {
    EnchantmentStorageMeta enchant = (EnchantmentStorageMeta) meta;
    return enchant.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
  }

  /**
   * メタデータからエンチャントレベルを取得
   * @param meta エンチャントレベルを取得したいメタデータ
   * @return エンチャントレベル
   */
  private static int getLevel(ItemMeta meta) {
    EnchantmentStorageMeta enchantMeta = (EnchantmentStorageMeta) meta;
    Enchantment enchantment = getEnchantment(meta);
    return enchantMeta.getStoredEnchantLevel(enchantment);
  }


  /**
   * アイテムStorageSign の Lore に書き込む アイテムフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   * @return Lore 文字列
   */
  @Override
  protected String getStorageItemName() {
    return material.toString();
  }

  /**
   * ブロックStorageSign に表記される アイテムショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   * @return SS 表記の文字列
   */
  @Override
  protected String getStorageItemShortName() {
    return enchantedBook.SS_ITEM_NAME;
  }

  /**
   * アイテムStorageSign の Lore に書き込む タイプフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   * @return Lore 文字列
   */
  @Override
  protected String getTypeName() {
    return type.getKey().getKey();
  }

  /**
   * ブロックStorageSign に表記される タイプショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   * @return SS 表記の文字列
   */
  @Override
  protected String getTypeShortName() {
    return enchantedBook.getEnchantShortName(type);
  }

  /**
   * StorageSign で使われる コード値取得
   * [コード値] 部分に登録する値を戻す値にする
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(EnchantmentStorageMeta meta) {
    return getLevel(meta);
  }

  /**
   * ItemMeta に コード値 を設定
   * [コード値] 部分を参照して ItemMeta に情報を追加する
   * <p>[アイテムショートネーム]:[コード値]</p>
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord() {
    EnchantmentStorageMeta meta = getContentItemMeta();
    meta.addStoredEnchant(type, cord,true);
    return meta;
  }

}
