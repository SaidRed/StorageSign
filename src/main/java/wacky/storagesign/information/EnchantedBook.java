package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBase;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EnchantedBook extends TypeInformation<Enchantment, EnchantmentStorageMeta> implements SSInformation{

  /**
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   */
  private static final List<String> exPrefix = List.of(
          "fire_"
  );

  private static final Material material = Material.ENCHANTED_BOOK;
  private static final String SS_ITEM_NAME = "ENCHBOOK";

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

  public EnchantedBook(String SSItemData, Logger logger){
    this(SSItemData.split("[: ]"),logger);
  }

  private EnchantedBook(String[] SSItemData, Logger logger){
    this(SSItemData[1],SSItemData[2],logger);
  }

  public EnchantedBook(String enchantName, String cord, Logger logger){
    this(material,getEnchantment(enchantName), Integer.parseInt(cord),logger);
  }

  public EnchantedBook(String enchantName, int cord, Logger logger){
    this(material,getEnchantment(enchantName),cord,logger);
  }

  public EnchantedBook(ItemStack itemStack, Logger logger){
    this((EnchantmentStorageMeta) itemStack.getItemMeta(),logger);
  }

  public EnchantedBook(EnchantmentStorageMeta meta, Logger logger){
    this(material,getEnchantment(meta),getEnchantLevel(meta),logger);
  }

  public EnchantedBook(Material material, Enchantment type, int cord, Logger logger) {
    super(material, type, cord, logger);
  }






  /**
   * アイテムStorageSign の Lora に書き込む アイテムフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   *
   * @return Lora 文字列
   */
  @Override
  protected String getStorageItemName() {
    return material.toString();
  }

  /**
   * ブロックStorageSign に表記される アイテムショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   *
   * @return SS 表記の文字列
   */
  @Override
  protected String getStorageItemShortName() {
    return SS_ITEM_NAME;
  }

  /**
   * アイテムStorageSign の Lora に書き込む タイプフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   *
   * @return Lora 文字列
   */
  @Override
  protected String getTypeName() {
    return type.getKey().getKey();
  }

  /**
   * ブロックStorageSign に表記される タイプショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   *
   * @return SS 表記の文字列
   */
  @Override
  protected String getTypeShortName() {
    String type = getTypeName();
    int len = exPrefix.stream().anyMatch(type::startsWith) ? 6 : 5;
    return type.length() > len ? type.substring(0,len) : type;
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
   * ItemMeta に コード値 を設定
   *
   * @param meta セットしたい ItemMeta
   * @param cord セットしたい Cord値
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(EnchantmentStorageMeta meta, int cord) {
    meta.addStoredEnchant(type, cord,true);
    return meta;
  }

}
