package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import wacky.storagesign.StorageSignConfig;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ポーションの表記ルール
 * [ポーションカテゴリ_ポーション名]:[ポーションタイプ]:[ポーションタイプ強化値] [ストレージ数]
 * Category _ potion : potionType : cord[0:Normal/1:Long/2:Strong]
 */
public class Potion extends TypeInformation<PotionType, PotionMeta> implements SSInformation {

  /**
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   */
  private static final Map<String,Integer> exPrefix = Map.ofEntries(
          Map.entry("FIRE_",6)
  );

  /**
   * 5文字でタイプ名が同じになるので取得する文字数を増やすプレフィックス一覧
   * @param potionType 確認する potionType名
   * @return 切り出す文字数
   */
  private static int containsExPrefix(String potionType){
    if(potionType.length()>5 && exPrefix.containsKey(potionType.substring(0,5)))
      return exPrefix.get(potionType.substring(0,5));
    return 5;
  }

  /**
   * 名前の先頭5文字に問題があるので削除するリスト
   */
  private static final List<String> delPrefix = List.of(
          "WATER_"
  );

  private static final Set<cordList> potionCord = Set.of(
          new cordList("LONG_",1),
          new cordList("STRONG_",2)
  );

  private static final record cordList(String prefix, Integer cord){}

  /**
   * ポーションタイプ強化 から コード値を取得する
   * @param itemData 変換する強化プレフィックス
   * @return cord値
   */
  private static int getCord(String itemData){
    for(cordList c : potionCord) if (itemData.startsWith(c.prefix)) return c.cord;
    return 0;
  }

  /**
   * コード値 から ポーション強化プレフィックスを取得する
   * @param cord cord値
   * @return 強化プレフィックス
   */
  private static String getStrongPrefix(int cord){
    for(cordList c : potionCord) if (c.cord == cord) return c.prefix;
    return "";
  }

  /**
   * ポーション強化 Prefix を削除する用 Pattern
   * @return delPrefix 削除用 Pattern
   */
  private static String potionPrefixPattern(){
    return "^(" + potionCord.stream().map(cordList::prefix).collect(Collectors.joining("|")) + ")";
  }

  public Potion(String itemData, Logger logger){
    this(itemData.split("[: ]"),logger);
  }

  protected Potion(String[] itemData, Logger logger) {
    this(getPotionCategory(itemData[0]), getPotionType(itemData[1]), Integer.parseInt(itemData[2]), logger);
  }

  public Potion(ItemStack itemStack, Logger logger){
    this(itemStack.getType(), getPotionType((PotionMeta) itemStack.getItemMeta()),
            getLevel((PotionMeta) itemStack.getItemMeta()),logger);
  }

  public Potion(Material material, PotionType type, int code, Logger logger) {
    super(material, type ,code, logger);
  }

  /**
   * SS表記ポーションカテゴリ から Material を取得する
   * @param itemData SS表記ポーションカテゴリ
   * @return Material
   */
  public static Material getPotionCategory(String itemData){
    if(itemData.length() == 6) return Material.POTION;
    return StorageSignConfig.defaultData.getOriginalItemName(itemData);
  }

  /**
   * PotionType の文字列から PotionType を取得する
   * @param name PotionType の文字列
   * @return PotionType
   */
  public static PotionType getPotionType(String name){
    return StorageSignConfig.potionType.containsNewName(name) ?
    StorageSignConfig.potionType.getPotionType(name) :
    Arrays.stream(PotionType.values())
            .filter(V->V.toString().matches("^(" + String.join("|",delPrefix) + ")?" + name + ".*"))
            .findFirst()
            .orElse(PotionType.WATER);
  }

  /**
   * メタデータ から ポーションタイプを取得する
   * @param meta ポーションタイプを取得したいメタデータ
   * @return ポーションタイプデータ
   */
  private static PotionType getPotionType(PotionMeta meta) {
    return meta.getBasePotionType();
  }

  private PotionType getPotionType() {
    return getPotionType(getStrongPrefix(cord) + type.toString());
  }
  /**
   * メタデータ から コード値を取得する
   * @param meta コード値を取得したいメタデータ
   * @return cord値
   */
  private static int getLevel(PotionMeta meta){
    return getCord(meta.getBasePotionType().toString());
  }


  /**
   * アイテムStorageSign の Lora に書き込む アイテムフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   * @return Lora 文字列
   */
  @Override
  protected String getStorageItemName() {
    return content.toString();
  }

  /**
   * ブロックStorageSign に表記される アイテムショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   * @return SS 表記の文字列
   */
  @Override
  protected String getStorageItemShortName() {
    return content.toString().replaceAll("(?<=^.).*?_","");
  }

  /**
   * アイテムStorageSign の Lora に書き込む タイプフルネーム
   * ポーションはショートネーム
   * <p>[アイテムフルネーム]:[タイプショートネーム]:[コード値] [アイテム数 amount]</p>
   * @return Lora 文字列
   */
  @Override
  protected String getTypeName() {
    return getTypeShortName();
  }

  /**
   * ブロックStorageSign に表記される タイプショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   * @return SS 表記の文字列
   */
  @Override
  protected String getTypeShortName() {
    if(StorageSignConfig.potionType.containsOldName(type)) {
      return StorageSignConfig.potionType.getPotionTypeName(type);
    }
    String name = type.toString().replaceAll(potionPrefixPattern(),"");
    name = name.replaceAll("^(" + String.join("|",delPrefix) + ")","");
    int len = containsExPrefix(name);
    name = name.length() < len ? name : name.substring( 0, len);
    return name;
  }

  /**
   * StorageSign で使われる コード値取得
   * [コード値] 部分に登録する値を戻す値にする
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(PotionMeta meta) {
    return getCord(meta.getBasePotionType().toString());
  }

  /**
   * ItemMeta に コード値 を参照してポーション強化状態を参照
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(){
    PotionMeta meta = getContentItemMeta();
    meta.setBasePotionType(getPotionType());
    meta.clearCustomEffects();
    return meta;
  }

  /**
   * アイテムスタックを使ってのアイテム比較
   * @param itemStack 比較するアイテムスタック
   * @return true 同一と認める/false 同一と認めない
   */
  @Override
  public boolean isSimilar(ItemStack itemStack) {
    if(! content.equals(itemStack.getType())) return false;
    PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
    if(! meta.getBasePotionType().equals(getPotionType())) return false;
    return true;
  }

}
