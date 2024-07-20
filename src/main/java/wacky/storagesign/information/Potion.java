package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.util.NumberConversions;
import wacky.storagesign.ConfigLoader;

import java.util.*;
import java.util.stream.Collectors;

public class Potion extends InformationAbstract<PotionMeta> implements SSInformation {

  private static final String ERROR_MESSAGE_NOT_POTION = "ポーションデータが存在しません";
  private PotionType itemType;

  /**
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   */
  private static final List<String> exPrefix = List.of(
          "fire_"
  );

  /**
   * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
   *
   * @param potionType 確認する potionType名
   * @return true 長くする/ false 5文字
   */
  private static boolean containsExPrefix(String potionType){
    for(String prefix:exPrefix) if(potionType.startsWith(prefix)) return true;
    return false;
  }

  /**
   * 名前の先頭5文字に問題があって別処理するリスト
   */
  private static final Map<String, PotionType> exPotionType = Map.of(
          "WATER",PotionType.WATER,
          "BREAT",PotionType.WATER_BREATHING
  );

  /**
   * バージョンで変更があったアイテム名のマージ<br>
   * <br>
   * newName : 新名称<br>
   * oldName : 旧名称
   */
  private enum OLD_NAME {
    INSTANT_HEAL(PotionType.HEALING),
    INSTANT_DAMAGE(PotionType.HARMING),
    JUMP(PotionType.LEAPING),
    SPEED(PotionType.SWIFTNESS),
    REGEN(PotionType.REGENERATION),
    BREAT(PotionType.WATER_BREATHING)
    ;

    private final PotionType potionType;

    OLD_NAME(PotionType potionType){
      this.potionType = potionType;
    }

    private static final Map<PotionType, String> oldMap;
    private static final Map<String, PotionType> newMap;
    private static final String delPrefixPattern;

    static {
      /*
        StorageSign 記載時に重複してしまうプレフィックス一覧
        削除用 regex 文字列
       */
      Set<String> delPref = Set.of(
              "INSTANT_",
              "WATER_"
      );
      delPrefixPattern = String.join("|",delPref);
      Map<PotionType, String> old = Arrays.stream(OLD_NAME.values()).collect(Collectors.toMap(e -> e.potionType,Enum::name));
      oldMap = Collections.unmodifiableMap(old);
      Map<String, PotionType> newP = Arrays.stream(OLD_NAME.values()).collect(Collectors.toMap(Enum::name,e -> e.potionType));
      newMap = Collections.unmodifiableMap(newP);
    }

    /**
     * 旧名確認
     *
     * @param oldName 旧ポーションタイプ名
     * @return true 旧名あり / false なし
     */
    public static boolean containsOldName(PotionType oldName){
      String name = oldName.toString().replaceAll(PREFIX.regPrefixList(),"");
      return newMap.containsKey(name);
    }

    /**
     * 新名称 から 旧名称 を取得する
     * @param newName 新 PotionType 名称
     * @return 旧 PotionType 名称
     */
    public static String getOldName (String newName){
      return getOldName(getPotionType(newName));
    }

    /**
     * PotionType から 旧名称 を取得する
     * @param newPotionType 新 PotionType 名称
     * @return 旧 PotionType 名称
     */
    public static String getOldName (PotionType newPotionType){
      return oldMap.get(newPotionType);
    }

    /**
     * 新名称 から 旧名称 のショートネームを取得する
     * @param newName 新 PotionType 名称
     * @return 旧 PotionType 名称
     */
    public static String getOldShortName (String newName){
      return getOldName(newName).replaceAll(delPrefixPattern,"");
    }

    /**
     * 旧名称 から 新名称 を取得する
     * @param oldName 旧名称
     * @return 新 PotionType 名称
     */
    public static String getNewName (String oldName){
      return newMap.get(oldName).toString();
    }

    /**
     * 旧ショート名称 から 新名称 を取得する
     * @param oldShortName 旧ショート名称
     * @return 新 PotionType 名称
     */
    public static String getNewNameByShort (String oldShortName){
      return newMap.entrySet().stream()
              .filter(E -> E.getKey().startsWith(oldShortName))
              .map(E-> E.getValue().toString())
              .findFirst()
              .orElse(oldShortName);
    }

    /**
     * 旧ショート名称 から 新名称 を取得する
     * @param newShortName 旧ショート名称
     * @return 新 PotionType 名称
     */
    public static String getOldNameByShort (String newShortName){
      return oldMap.entrySet().stream()
              .filter(E -> E.getKey().toString().startsWith(newShortName))
              .map(E -> E.getValue())
              .findFirst()
              .orElse(newShortName);
    }

  }

  /**
   * ポーション種類の表記リスト<br>
   * 登録済み : 通常,スプラッシュ,残留<br>
   * <br>
   * Type : potionType<br>
   * pref : SS表記のポーション種類文字列
   */
  protected enum CATEGORY {
    POTION(Material.POTION),
    SPOTION(Material.SPLASH_POTION),
    LPOTION(Material.LINGERING_POTION)
    ;

    private final Material material;

    CATEGORY(Material material) {
      this.material = material;
    }

    private static final Map<String, Material> materialMap;
    private static final Map<Material, String> categoryMap;

    static {
      Map<String, Material> category = Arrays.stream(CATEGORY.values()).collect(Collectors.toMap(Enum::name, e -> e.material));
      materialMap = Collections.unmodifiableMap(category);
      Map<Material, String> material = Arrays.stream(CATEGORY.values()).collect(Collectors.toMap(e -> e.material, Enum::name));
      categoryMap = Collections.unmodifiableMap(material);
    }

    /**
     * StorageSign ブロック表記のアイテム名からマテリアル情報を取得
     *
     * @param potionCategory SSブロック表記アイテム名
     * @return ポーションマテリアル
     */
    public static Material getMaterial(String potionCategory) {
      return materialMap.get(potionCategory);
    }

    /**
     * マテリアル情報から StorageSign ブロック表記のアイテム名を取得
     *
     * @param material ポーションマテリアル
     * @return SSブロック表記アイテム名
     */
    public static String getCategoryName(Material material) {
      return categoryMap.get(material);
    }

  }

  /**
   * ポーション強化のプレフィックス 表記リスト<br>
   * 登録済み : 通常,延長,強化<br>
   * <br>
   * code : SS表記のCode値<br>
   * pref : プレフィックス文字列
   */
  protected enum PREFIX {
    LONG(1),
    STRONG(2)
    ;
    private final int code;

    PREFIX(int code) {
      this.code = code;
    }

    private static final Map<String, Integer> codeMap;
    private static final Map<Integer, String> prefixMap;

    static {
      Map<String, Integer> code = Arrays.stream(PREFIX.values()).collect(Collectors.toMap(Enum::name,E -> E.code));
      codeMap = Collections.unmodifiableMap(code);
      Map<Integer, String> prefix = Arrays.stream(PREFIX.values()).collect(Collectors.toMap(E -> E.code,Enum::name));
      prefixMap = Collections.unmodifiableMap(prefix);
    }

    /**
     * プレフィックス から SS表記コード を取得する
     *
     * @param potionType potionType 表記のプレフィックス
     * @return SS表記のCode値
     */
    public static int getCode (PotionType potionType){
      return getCode(potionType.toString());
    }

    /**
     * プレフィックス から SS表記コード を取得する
     *
     * @param potionType potionType 表記のプレフィックス
     * @return SS表記のCode値
     */
    public static int getCode (String potionType){
      String prefix = potionType.split("_")[0];
      return codeMap.getOrDefault(prefix, 0);
    }

    /**
     * SS表記コード から プレフィックス を取得する
     * @param code SS表記のCode値
     * @return potionType 表記のプレフィックス
     */
    public static String getPrefix (int code){
      return prefixMap.containsKey(code) ? prefixMap.get(code) + "_" : "" ;
    }

    /**
     * potionType 表記のプレフィックス を正規表現で or ヒットさせる Pattern
     *
     * @return プレフィックスを正規表現でヒットさせる regex
     */
    public static String regPrefixList() {
      return "^" + String.join("_|", codeMap.keySet()) + "_";
    }
  }

  /**
   * PotionType 文字列ヒット用ライブラリ
   */
  private static final Map<String, PotionType> typeMap;
  static {
    Map<String, PotionType> type = Arrays.stream(PotionType.values()).collect(Collectors.toMap(Enum::name,T -> T));
    typeMap = Collections.unmodifiableMap(type);
  }

  /**
   * PotionType の文字列から PotionType を取得する
   * @param name PotionType の文字列
   * @return PotionType
   */
  public static PotionType getPotionType(String name){
    if(exPotionType.containsKey(name.toUpperCase())) return exPotionType.get(name.toUpperCase());
    return typeMap.values().stream()
            .filter(T -> T.toString().startsWith(name.toUpperCase()))
            .findFirst()
            .orElse(PotionType.WATER);
  }


  public Potion(ItemStack itemStack, Logger logger){
    this(itemStack.getType(), (PotionMeta) itemStack.getItemMeta(),logger);
  }

  public Potion(Material material, PotionMeta potionMeta, Logger logger){
    this(material, potionMeta.getBasePotionType(), logger);
  }

  public Potion(Material material, PotionType potionType, Logger logger){
    this(material, potionType, PREFIX.getCode(potionType), logger);
  }

  public Potion(String SSItemData, Logger logger){
    this(SSItemData.split("[: ]"), logger);
  }

  protected  Potion(String[] SSItemData, Logger logger){
    this(CATEGORY.getMaterial(SSItemData[0]), getPotionType(SSItemData[1]), Integer.parseInt(SSItemData[2]), logger);
  }

  public Potion(Material material, PotionType itemType, int code, Logger logger) {
    super(material,  code, logger);
    this.itemType=getNomalPotionType(itemType);
  }


  /**
   * メタデータからポーションタイプを取得する
   *
   * @param meta ポーションタイプを取得したいメタデータ
   * @return ポーションタイプデータ
   */
  private static PotionType getPotionType(PotionMeta meta) {
    return meta.getBasePotionType();
  }

  /**
   * 強化されていないポーションタイプを取得する
   *
   * @param potionType ノーマルのポーションタイプを取得したいPotionType
   * @return ノーマルポーションタイプデータ
   */
  private static PotionType getNomalPotionType(PotionType potionType) {
    String name = potionType.toString().replaceAll(PREFIX.regPrefixList(),"");
    return getPotionType(name);
  }

  /**
   * メタデータからポーションタイプ名を取得する
   *
   * @param meta ポーションタイプ名を取得したいメタデータ
   * @return ポーションタイプ名
   */
  private static String getPotionTypeName(PotionMeta meta){
    return getPotionType(meta).getKey().getKey().replaceAll(PREFIX.regPrefixList().toLowerCase(),"");
  }

  /**
   * ポーションタイプ の文字列
   *
   * @return ポーションタイプ名
   */
  private String getPotionTypeName() {
    logger.debug("getPotionTypeName: Start");
    if(exPotionType.containsValue(itemType)) return itemType.getKey().getKey();
    return OLD_NAME.containsOldName(itemType) ? OLD_NAME.getOldName(itemType) : itemType.getKey().getKey();
  }

  /**
   * ポーションタイプ の文字列 ショートネーム
   *
   * @return ポーションタイプ ショートネーム
   */
  public String getPotionTypeShortName() {
    logger.debug("getPotionTypeShortName: Start");
    String type = getPotionTypeName().replaceAll(PREFIX.regPrefixList().toLowerCase(),"");
    type = type.replaceAll(OLD_NAME.delPrefixPattern.toLowerCase(),"");
    logger.debug("PotionTypeName: " + type);
    int len = containsExPrefix(type) ? 6 : 5 ;
    return type.length() > len ? type.substring(0,len) : type;
  }

  /**
   * StorageSign で使われる コード値取得
   *
   * @param meta Cord値 を取得したい ItemMeta
   * @return Cord値
   */
  @Override
  protected int getCord(PotionMeta meta) {
    PotionType potionType = getPotionType(meta);
    return PREFIX.getCode(potionType);
  }

  /**
   * ItemMeta に コード値 を設定
   *
   * @param meta セットしたい ItemMeta
   * @param cord セットしたい Cord値
   * @return Cord値 をセットし終わった itemMeta
   */
  @Override
  protected ItemMeta setCord(PotionMeta meta, int cord) {
    return null;
  }

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[タイプショートネーム]:[コード値]
   *
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return CATEGORY.getCategoryName(this.material) + ":" + getPotionTypeShortName() + ":" + this.cord;
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * [アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]
   *
   * @return 貯蔵アイテム情報 (Lora)
   */
  @Override
  public String getSSLoraItemData() {
    return material.toString() + ":"
            + itemType.toString().replaceAll(PREFIX.regPrefixList().toLowerCase(),"") + ":" + cord;
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

    logger.debug("PotionType: " + getPotionTypeName());
    logger.debug("Level: " + cord);
    PotionMeta meta = (PotionMeta) item.getItemMeta();
    PotionType potionType = getPotionType(PREFIX.getPrefix(cord) + itemType.toString().toUpperCase() );
    meta.setBasePotionType(potionType);
    item.setItemMeta(meta);

    return item;
  }

}
