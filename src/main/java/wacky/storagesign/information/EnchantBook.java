package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;
import wacky.storagesign.ConfigLoader;

import java.util.*;

public class EnchantBook extends InformationAbstract<EnchantmentStorageMeta> implements SSInformation{

  private static final String SS_ITEM_NAME = "ENCHBOOK";
  private static final Material material = Material.ENCHANTED_BOOK;
  protected Enchantment itemType;
  protected String STORAGE_SIGN_NAME = "StorageSogn";

  /**
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   */
  private static final List<String> exPrefix = List.of(
          "fire_"
  );

  /**
   * メタデータからエンチャントタイプを取得する
   *
   * @param meta エンチャントタイプを取得したいメタデータ
   * @return エンチャントタイプデータ
   */
  private static Enchantment getEnchantment(EnchantmentStorageMeta meta) {
    return meta.getStoredEnchants().keySet().toArray(new Enchantment[0])[0];
  }

  /**
   * SS 表記の名前から エンチャント名を取得
   *
   * @param substring エンチャント名 (ショートネーム可)
   * @return エンチャント名
   */
  private static Enchantment getEnchantment(String substring) {
    //後ろ切れても可.
    return org.bukkit.Bukkit.getRegistry(Enchantment.class).stream()
            .filter(E->E.getKey().getKey().startsWith(substring))
            .findFirst()
            .orElse(null);
  }

  /**
   * メタデータからエンチャントタイプ名を取得する
   *
   * @param meta エンチャントタイプ名を取得したいメタデータ
   * @return エンチャントタイプ名
   */
  private static String getEnchantmentName(EnchantmentStorageMeta meta){
    return getEnchantment(meta).getKey().getKey();
  }

  /**
   * エンチャントタイプ の文字列
   *
   * @return エンチャントタイプ
   */
  private String getEnchantmentName() {
    logger.debug("getEnchantmentName: ");
    return itemType.getKey().getKey();
  }

  /**
   * エンチャントタイプ の文字列 ショートネーム
   *
   * @return エンチャントタイプ ショートネーム
   */
  public String getEnchantmentShortName() {
    logger.debug("getEnchantmentShortName: Start");
    String type = getEnchantmentName();
    int len = exPrefix.stream().anyMatch(type::startsWith) ? 6 : 5;
    return type.length() > len ? type.substring(0,len) : type;
  }

  public EnchantBook(String SSItemData, Logger logger){
    this(SSItemData.split("[: ]"),logger);
  }

  private EnchantBook(String[] SSItemData, Logger logger){
    this(SSItemData[1],SSItemData[2],logger);
  }

  public EnchantBook(String enchantName, String cord, Logger logger){
    this(material,getEnchantment(enchantName), Integer.parseInt(cord),logger);
  }

  public EnchantBook(String enchantName, int cord, Logger logger){
    this(material,getEnchantment(enchantName),cord,logger);
  }

  public EnchantBook(ItemStack itemStack, Logger logger){
    this((EnchantmentStorageMeta) itemStack.getItemMeta(),logger);
  }

  public EnchantBook(EnchantmentStorageMeta meta, Logger logger){
    this(material,getEnchantment(meta),getEnchantLevel(meta),logger);
  }

  public EnchantBook(Material material, Enchantment itemType, int cord, Logger logger) {
    super(material, cord, logger);
    this.itemType = itemType;
  }

  /**
   * StorageSign で使われる コード値取得
   *
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(EnchantmentStorageMeta meta) {
    return getEnchantLevel(meta);
  }

  /**
   * メタデータからエンチャントレベルを取得する
   *
   * @param meta エンチャントレベルを取得したいメタデータ
   * @return エンチャントレベル
   */
  private static int getEnchantLevel(EnchantmentStorageMeta meta) {
    Enchantment enchantment = getEnchantment(meta);
    return meta.getStoredEnchantLevel(enchantment);
  }

  /**
   * ItemMeta に コード値 を設定
   *
   * @param meta セットしたい ItemMeta
   * @param cord セットしたい Cord値
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected EnchantmentStorageMeta setCord(EnchantmentStorageMeta meta, int cord) {
    meta.addStoredEnchant(itemType, cord, true);
    return meta;
  }

  /**
   * ブロックStorageSign に表記される文字列
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   *
   * @return SS 表記の文字列
   */
  @Override
  public String getSSStorageItemData() {
    logger.debug("getSSStorageItemData: Start");
    return SS_ITEM_NAME + ":" + getEnchantmentShortName() + ":" + cord;
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   *
   * @return Lora 文字列
   */
  @Override
  public String getSSLoraItemData() {
    logger.debug("getSSLoraItemData: Start");
    return material.toString() + ":" + itemType.getKey().getKey() + ":" + cord;
  }

  /**
   * StorageSign としてのドロップ ItemStack
   *
   * @param material SS の Sign 種類指定
   * @return SS ItemStack
   */
  public ItemStack getSSItemStack(Material material) {
    logger.debug("getSSItemStack: Start");
    logger.debug("type: EnchantBook");
    ItemStack item = new ItemStack(material);

    ItemMeta meta = item.getItemMeta();
    Objects.requireNonNull(meta).setDisplayName(STORAGE_SIGN_NAME);
    meta.setLore(new ArrayList<String>(Collections.singletonList(getSSLoraItemData())));
    meta.setMaxStackSize(ConfigLoader.getMaxStackSize());

    item.setItemMeta(meta);
    return item;
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

    logger.debug("Enchantment: " + getEnchantmentName());
    logger.debug("Level: " + cord);
    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
    meta.addStoredEnchant(itemType, cord, true);
    item.setItemMeta(meta);

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
    if(! material.equals(itemStack.getType()))return false;

    ItemMeta meta = itemStack.getItemMeta();
    if(Objects.isNull(meta)) return false;
    if(!(meta instanceof EnchantmentStorageMeta eMeta)) return false;
    return getEnchantmentName().equals(getEnchantmentName(eMeta)) && cord == getEnchantLevel(eMeta);
  }

}
