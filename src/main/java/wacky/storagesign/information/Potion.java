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

public class Potion extends TypeInformation<PotionType, PotionMeta> implements SSInformation {

  /**
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   */
  private static final List<String> exPrefix = List.of(
          "FIRE_"
  );

  /**
   * 名前の先頭5文字に問題があって別処理するリスト
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
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   *
   * @param potionType 確認する potionType名
   * @return true 長くする/ false 5文字
   */
  private static boolean containsExPrefix(String potionType){
    for(String prefix:exPrefix)
      if(potionType.startsWith(prefix))
        return true;
    return false;
  }

  /**
   * ポーション強化 から コード値を取得する
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

  /**
   * 名前の先頭 Prefix を削除するよう Pattern
   * @return delPrefix 削除用 Pattern
   */
  public static String delPrefixPattern(){
    return "^(" + String.join("|",delPrefix) + ")";
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
    return StorageSignConfig.SSOriginalItemNameConverter(itemData);
  }

  /**
   * PotionType の文字列から PotionType を取得する
   * @param name PotionType の文字列
   * @return PotionType
   */
  public static PotionType getPotionType(String name){
    return Arrays.stream(PotionType.values())
            .filter(V -> V.name().replaceAll(delPrefixPattern(),"").startsWith(name))
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
    String name = type.toString().replaceAll(potionPrefixPattern(),"");
    name = name.replaceAll(delPrefixPattern(),"");
    int len = containsExPrefix(name) ? 6 : 5;
    return name.length() < len ? name : name.substring( 0, len);
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
   * @param meta セットしたい ItemMeta
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(PotionMeta meta) {
    meta.setBasePotionType(getPotionType(getStrongPrefix(cord) + type.toString()));
    return meta;
  }

}
