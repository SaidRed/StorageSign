package wacky.storagesign;

import com.github.teruteru128.logger.Logger;

import java.util.*;
import java.util.regex.Pattern;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import org.bukkit.util.NumberConversions;
import wacky.storagesign.Exception.PotionException;

import static org.bukkit.potion.PotionType.*;

public class PotionInfo {
  @Getter
  protected Material material;
  @Getter
  protected PotionType potionType;
  @Getter
  protected short damage = 0;
  protected Logger logger;

  private static final String ERROR_MESSAGE_NOT_POTION = "ポーションデータが存在しません";

  /**
   * ポーションタイプの表記リスト<br>
   * 登録済み : 通常,スプラッシュ,残留<br>
   * <br>
   * Type : potionType<br>
   * pref : SS表記のポーション種類文字列
   */
  protected static final class Type {
    private static final TY DEFALT = new TY(Material.POTION,"POTION");
    private static final Set<TY> TYPE = Set.of(
            DEFALT,
            new TY(Material.SPLASH_POTION, "SPOTION"),
            new TY(Material.LINGERING_POTION, "LPOTION")
    );

    @Getter
    private static final class TY{
      private final Material material;
      private final String pref;
      public TY(Material material, String pref){
        this.material = material;
        this.pref = pref;
      }
    }

    /**
     * StorageSign表記のポーション名 から ポーションのMaterial を取得する
     * @param pref SS表記のポーション種類文字列
     * @return ポーションの Material
     */
    public static Material getType (String pref){
      return TYPE.stream()
              .filter(T->T.getPref().equals(pref))
              .map(Type.TY::getMaterial)
              .findFirst()
              .orElse(DEFALT.material);
    }

    /**
     * ポーションMaterial から StorageSign表記のポーション名 を取得する
     * @param material ポーションの Material
     * @return SS表記の"POTION"部分の表記
     */
    public static String getPref (Material material){
      return TYPE.stream()
              .filter(T->T.getMaterial().equals(material))
              .map(Type.TY::getPref)
              .findFirst()
              .orElse(DEFALT.pref);
    }
  }

  /**
   * ポーションの プレフィックス 表記リスト<br>
   * 登録済み : 通常,延長,強化<br>
   * <br>
   * code : SS表記の数値<br>
   * pref : プレフィックス文字列
   */
  protected static final class Enhance {
    private static final EN DEFAULT = new EN("0" , "");
    private static final Set<EN> ENHANCE =Set.of(
            DEFAULT,
            new EN("1" , "LONG_"),
            new EN("2" , "STRONG_")
    );

    @Getter
    private static class EN {
      private final String code;
      private final String pref;
      public EN(String code,String pref){
        this.code = code;
        this.pref = pref;
      }
    }

    /**
     * プレフィックス から SS表記コード を取得する
     * @param pref potionType 表記のプレフィックス
     * @return SS表記のDamage値
     */
    public static String getCode (String pref){
      return ENHANCE.stream()
              .filter(E->pref.startsWith(E.getPref()))
              .map(EN::getCode)
              .findFirst()
              .orElse(DEFAULT.code);
    }

    /**
     * SS表記コード から プレフィックス を取得する
     * @param code SS表記のDamage値
     * @return potionType 表記のプレフィックス
     */
    public static String getPref (String code){
      return ENHANCE.stream()
              .filter(E->E.getCode().equals(code))
              .map(EN::getPref)
              .findFirst()
              .orElse(DEFAULT.pref);
    }

    /**
     * SS表記のDamage値 をリストで取得
     * @return Codeリスト
     */
    public static List<String> getCodeList(){
      return ENHANCE.stream()
              .map(EN::getCode)
              .toList();
    }

    /**
     * potionType 表記のプレフィックス をリストで取得
     * @return 空白を抜いた potionType 表記のプレフィックス リスト
     */
    public static List<String> getPrefList(){
      return ENHANCE.stream()
              .filter(E-> !E.getPref().isEmpty())
              .map(EN::getPref)
              .toList();
    }

    /**
     * potionType 表記のプレフィックス を正規表現で or ヒットさせる Pattern
     * @return プレフィックスを正規表現でヒットさせる regex
     */
    public static String regPrefList(){
      return String.join("|",getPrefList());
    }
  }

  /**
   * バージョンで変更があったアイテム名のマージ<br>
   * <br>
   * newName : 新名称<br>
   * oldName : 旧名称
   */
  private static class Old_Name {
    /**
     * StorageSign 記載時に重複してしまうプレフィックス一覧
     * 削除等で使う用で List 用意
     */
    private static final Set<String> delPref = Set.of(
            "INSTANT_"
    );

    /**
     * StorageSign 記載時に重複してしまうプレフィックス一覧
     * 削除用 regex 文字列
     */
    private static final String delPrefPattern = String.join("|",delPref);

    /**
     * バージョンアップにより名称に変更があった PotionType 一覧
     */
    private static final Set<Old_Name.OL> NAME_LIST = Set.of(
            new OL("HEALING" , "INSTANT_HEAL"),
            new OL("HARMING" , "INSTANT_DAMAGE"),
            new OL("LEAPING" , "JUMP"),
            new OL("SWIFTNESS" , "SPEED"),
            new OL("REGENERATION" , "REGEN"),
            new OL("WATER_BREATHING" , "BREAT")
    );

    /**
     * NAME_LIST 作成用クラス
     */
    @Getter
    private static class OL {
      private final String newName;
      private final String oldName;
      public OL(String newName,String oldName){
        this.newName = newName;
        this.oldName = oldName;
      }
    }

    /**
     * 新名称 から 旧名称 を取得する
     * @param newName 新 PotionType 名称
     * @return 旧 PotionType 名称
     */
    public static String getOldName (String newName){
      return NAME_LIST.stream()
              .filter(E->E.getNewName().equals(newName))
              .map(OL::getOldName)
              .findFirst()
              .orElse(newName);
    }

    /**
     * 新名称 から 旧名称 のショートネームを取得する
     * @param newName 新 PotionType 名称
     * @return 旧 PotionType 名称
     */
    public static String getOldShortName (String newName){
      return getOldName(newName).replaceAll(delPrefPattern,"");
    }

    /**
     * 旧名称 から 新名称 を取得する
     * @param oldName 旧名称
     * @return 新 PotionType 名称
     */
    public static String getNewName (String oldName){
      return NAME_LIST.stream()
              .filter(E->E.getOldName().equals(oldName))
              .map(OL::getNewName)
              .findFirst()
              .orElse(oldName);
    }

    /**
     * 旧ショート名称 から 新名称 を取得する
     * @param oldShortName 旧ショート名称
     * @return 新 PotionType 名称
     */
    public static String getNewNameByShort (String oldShortName){
      return NAME_LIST.stream()
              .map(E->new OL(E.getNewName(),E.getOldName().replaceFirst(delPrefPattern,"")))
              .filter(E->E.getOldName().startsWith(oldShortName))
              .map(OL::getNewName)
              .findFirst()
              .orElse(oldShortName);
    }
  }

  /**
   * PotionType の文字列から PotionType を取得する
   * @param name PotionType の文字列
   * @return PotionType
   */
  public static final PotionType getPotionType (String name){
    return Arrays.stream(values())
            .filter(T->T.toString().startsWith(name))
            .findFirst()
            .orElse(null);
  }

  /**
   * ブロックStorageSign から アイテムStorageSign を生成する.
   * >アイテムStorageSign への情報提供用メゾット
   * @param material Material データ
   * @param type ポーション種別の設定
   * @param effName ポーションエフェクト名
   * @param enhance SS表記のポーション強化情報 => SSとしてはダメージ値
   * @param logger ロガー
   */
  public PotionInfo(Material material, String type, String effName, String enhance, Logger logger) {

    this.logger = logger;
    logger.debug("PotionInfo:Start");
    logger.trace("material=" + material + ", type=" + type + ", effName=" + effName + ", enhance=" + enhance + ", logger=" + logger);
    logger.trace("type: " + type);

    this.material = Type.getType(type);
    this.potionType = getType(effName, enhance);
    this.damage = NumberConversions.toShort(enhance);

    // 一応前のロジックも入れておく
    logger.trace("damage: " + damage);
    logger.trace("potionType.isExtendable(): " + potionType.isExtendable());
    logger.trace("potionType.isUpgradeable(): " + potionType.isUpgradeable());
    if (damage % 8192 > 64 && potionType.isExtendable()) {
      this.damage = 1;// 延長
    } else if (damage % 64 > 32 && potionType.isUpgradeable()) {
      this.damage = 2;// 強化
    }

  }

  /**
   * ブロックStorageSign に記載されるポーションタイプの短い名前
   * @param pot PotionType
   * @return ポーションタイプショートネーム
   */
  public static String getShortName(PotionType pot) {
    String name = Old_Name.getOldShortName(pot.toString());
    name = name.replaceAll(Enhance.regPrefList(),"");
    return name.length() <= 5 ? name : name.substring(0, 5);
  }

  /**
   * ブロックStorageSign 記載のポーションアイテム情報
   * @param mat Material
   * @param pot PotionType
   * @param damage SS表記のポーション強化情報 => SSとしてはダメージ値
   * @return SS Block アイテム表記名
   */
  public static String getSignData(Material mat, PotionType pot, short damage) {
    return Type.getPref(mat) + ":" + PotionInfo.getShortName(pot) + ":" + damage;
  }

  /**
   * アイテムStorageSign Lora への表記情報
   * @param mat Material
   * @param pot PotionType
   * @param damage SS表記のポーション強化情報 => SSとしてはダメージ値
   * @param amount 収納アイテム数
   * @return SS ItemStack Lora への表記
   */
  public static String getTagData(Material mat, PotionType pot, short damage, int amount) {
    return mat.toString() + ":" + getNormalType(pot) + ":" + damage + " " + amount;
  }

  /**
   * PotionType から StorageSign 表記のポーションの強化情報へ変換
   * @param pot 取得したい PotionType
   * @return SS表記のポーション強化情報 => SSとしてはダメージ値
   */
  public static String getPotionTypeCode(PotionType pot) {
    return Enhance.getCode(pot.toString());
  }

  /**
   * SS 記載の内容から PotionType を作成
   * @param effName PotionEffect
   * @param enhance SS表記のポーション強化情報 => SSとしてはダメージ値
   * @return ポーションタイプ
   */
  private PotionType getType(String effName, String enhance) {
    logger.debug("getType: Start");
    logger.debug("SearchPotion");

    String eff = Old_Name.getNewNameByShort(effName);
    String name = Enhance.getPref(enhance) + eff;

    logger.trace("name: " + name);

    PotionType p = getPotionType(name);
    if(p != null) return p;
    logger.error("Enhance is not Exist!");
    throw new PotionException(ERROR_MESSAGE_NOT_POTION);
  }

  /**
   * アイテムStorageSine の lora へ書き込む際に 通常ポーションを表記するので延長・強化から通常の表記へ変換する
   * @param pot PotionType
   * @return 通常ポーションのポーションタイプ
   */
  private static String getNormalType(PotionType pot) {
    String name = Pattern.compile(Enhance.regPrefList()).matcher(pot.toString()).replaceFirst("");
    PotionType type=getPotionType(name);
    if(type != null)return type.toString();
    throw new PotionException(ERROR_MESSAGE_NOT_POTION);
  }

  /**
   *
   * @param nbtName
   * @return
   */
  public static String convertNBTNameToShortName(String nbtName) {
    return Old_Name.getOldShortName(nbtName);
  }
}