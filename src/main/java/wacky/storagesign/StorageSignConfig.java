package wacky.storagesign;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionType;
import wacky.storagesign.information.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.entry;
import static org.bukkit.Material.*;

public class StorageSignConfig {

  public static class defaultData{
    /**
     * StorageSign 固定名称
     */
    public static final String STORAGE_SIGN_NAME = "StorageSign";
    /**
     * アイテム無しの表記
     */
    public static final String empty = "Empty";
    /**
     * 襲撃バーナーのSS表記
     */
    public static final String ominousBanner = "OMINOUS_BANNER";

    private static final Map<String, Material> ORIGINAL_ITEM_NAME = Map.ofEntries(
            entry(enchantedBook.SS_ITEM_NAME,ENCHANTED_BOOK),
            entry("SPOTION",SPLASH_POTION),
            entry("LPOTION",LINGERING_POTION),
            entry("TARROW",TIPPED_ARROW),
            // 襲撃バーナー
            entry(ominousBanner,WHITE_BANNER),
            // SS オリジナル名 Empty表記
            entry(empty,AIR)
    );

    /**
     * オリジナルアイテム名を使用しているアイテムのコンバーター
     * @param itemName SS オリジナル名
     * @return 対応する Material データ
     */
    public static Material getOriginalItemName(String itemName){
      return ORIGINAL_ITEM_NAME.getOrDefault(itemName, matchMaterial(itemName));
    }

    /**
     * バージョンアップにて消滅した名称一覧
     */
    private static final Map<String, Material> OLD_ITEM_NAME = Map.ofEntries(
            // 1.13→1.14 名称変更
            entry("SIGN", OAK_SIGN),
            entry("ROSE_RED", RED_DYE),
            entry("DANDELION_YELLOW", YELLOW_DYE),
            entry("CACTUS_GREEN", GREEN_DYE),
            entry("OMINOUS_BOTTLE", OMINOUS_BOTTLE)
    );

    /**
     * バージョン変更にて消滅した名称一覧
     * バージョンアップ時の命名変更によって問題が発生したアイテムの取得
     * @param itemName 取得したいアイテム名
     * @return 存在したら変換 / 見つからなかったら現在の Materialで検索をしてみる
     */
    public static Material getMaterial(String itemName){
      return OLD_ITEM_NAME.getOrDefault(itemName, getOriginalItemName(itemName));
    }

    /**
     *
     */
    public enum nameLength{
      Long(true),
      Short(false)
      ;
      public final boolean length;
      nameLength(boolean length){
        this.length = length;
      };
    }

  }

  public static class informationData{
    /**
     * 作成した SSInformation の対応リスト
     * ここへ登録すれば Material に対応する Information を使って StorageSign の情報作る
     */
    private static final Map<Material,Class<? extends SSInformation>> informationList = Map.ofEntries(
            entry(ENCHANTED_BOOK, EnchantedBook.class),
            entry(SPLASH_POTION, PotionInfo.class),
            entry(LINGERING_POTION, PotionInfo.class),
            entry(POTION, PotionInfo.class),
            entry(TIPPED_ARROW, PotionInfo.class),
            entry(OMINOUS_BOTTLE, OminousBottle.class),
            entry(FIREWORK_ROCKET, FireworkRocket.class),
            entry(WHITE_BANNER, OminousBanner.class)
    );
    public static boolean containsKey(Material material){
      return informationList.containsKey(material);
    }
    public static Class<? extends SSInformation> get(Material material){
      return informationList.get(material);
    }

  }

  public static class versionConvert{
    /**
     * バージョンアップ時の命名変更によって問題が発生したアイテムの登録
     * <pre>{@code
     *     Map<Material, NavigableMap<Integer, Material>> VERSION_CONVERT = Map.ofEntries(
     *            entry(NewMaterial,
     *                   new TreeMap<Integer,Material>(){{
     *                           put(   0, OldMaterial1);
     *                           put(   1, OldMaterial2);
     *                           ...
     *                           put(Cord, NewMaterial);
     *                   }}
     *            )
     *     );
     * }</pre>
     */
    private static final Map<Material, NavigableMap<Integer, Material>> VERSION_CONVERT = Map.ofEntries(
            // 1.13の滑らかハーフと1.14の石ハーフ区別
            entry(STONE_SLAB,
                    new TreeMap<Integer,Material>(){{
                      put(0, SMOOTH_STONE_SLAB);
                      put(1, STONE_SLAB);
                    }}
            )
    );

    /**
     * VERSION_CONVERT に登録された Material 内の最大値マップ
     */
    private static final Map<Material,Integer> maxCord;

    static{
      maxCord = VERSION_CONVERT.entrySet().stream()
              .collect(Collectors.toMap(
                      Map.Entry::getKey,
                      T->T.getValue().lastKey()
              ));
    }

    /**
     * 一覧に存在するかの確認
     * true : ある / false : ありません
     */
    public static boolean containsKey(Material material){
      return VERSION_CONVERT.containsKey(material);
    }

    /**
     * バージョンアップ時の命名変更によって問題が発生したアイテムの取得
     * @param material 確認 Material
     * @param cord 取得したい Cord
     * @return 存在したら変換 / 見つからなかったら元の Material を返還
     */
    public static Material getMaterial(Material material, int cord){
      return VERSION_CONVERT.containsKey(material) ?
            VERSION_CONVERT.get(material).get(cord) :
            material;
    }

    /**
     * 最新アイテムは最大値のコード値になる
     * @param material 取得したい Material
     * @return 登録されているコード値の最大値
     */
    public static int getMaxCord(Material material){
      return maxCord.get(material);
/*      Map<Integer,Material> t = VERSION_CONVERT.get(material);
      return t.entrySet().stream()
            .max(Comparator.comparing(Map.Entry::getKey))
            .map(Map.Entry::getKey)
            .orElse(0);*/
    }

  }

  public static class potionType{
    /**
     * SSオリジナル PotionType 表記一覧
     * oldName : 旧名称 / newName : 新 PotionType
     */
/*    private static final Set<potionTypeData> ORIGINAL_TYPE_NAME = Set.of(
          //new potionTypeData("REGEN", PotionType.REGENERATION),
          //new potionTypeData("BREAT", PotionType.WATER_BREATHING),
          new potionTypeData("HEAL", PotionType.HEALING),
          new potionTypeData("DAMAG", PotionType.HARMING),
          new potionTypeData("JUMP", PotionType.LEAPING),
          new potionTypeData("SPEED", PotionType.SWIFTNESS)
  );*/
    private static final Map<PotionType, String> ORIGINAL_TYPE_NAME = Map.ofEntries(
            Map.entry(PotionType.HEALING, "HEAL"),
            Map.entry(PotionType.HARMING, "DAMAG"),
            Map.entry(PotionType.LEAPING, "JUMP"),
            Map.entry(PotionType.SWIFTNESS, "SPEED")
    );

    /**
     * ポーションの強化情報とCord値の変換セット
     */
    private static final Set<cordList> potionCordList = Set.of(
            new cordList("LONG_",1),
            new cordList("STRONG_",2)
    );
    private static final record cordList(String prefix, Integer cord){}

    /**
     * 名前の先頭5文字に問題があるので削除するプレフィックスリスト
     */
    private static final List<String> deletePrefix = List.of(
            "WATER_"
    );

    /**
     * 5文字でタイプ名が同じになるので文字数を増やすプレフィックス一覧
     */
    private static final Map<String, Integer> exPrefix = Map.ofEntries(
            Map.entry("FIRE_", 6)
    );

    public static class Type{
      /**
       * ポーションタイプ文字列から NormalPotionType変換用
       * FullName と ShortName の両方を登録する。
       */
      public static Map<String, PotionType> PotionTypeItemData;

      /**
       * PotionInfo に格納している情報 NormalPotionType & Cord値 から FullPotionType を生成する Map
       */
      public static Map<PotionType, NavigableMap<Integer,PotionType>> PotionTypeMap;

      /**
       * PotionType から Cord値 と NormalPotionType を取得する Map
       */
      public static Map<PotionType, cordNormal> SSDataMap;
      public record cordNormal(int cord, PotionType type, String SSItemData){}
    }

    public static class Cord{
      /**
       * ポーション効果強化から Cord値 を取得する Map
       */
      public static Map<String,Integer> CordMap;
      /**
       * Cord値 から ポーション効果強化プレフィックスを取得する Map
       */
      public static Map<Integer,String> StrongPrefixMap;
    }


    static {
      Cord.CordMap = potionCordList.stream().collect(Collectors.toMap(cordList::prefix,cordList::cord));
      Cord.StrongPrefixMap = potionCordList.stream().collect(Collectors.toMap(cordList::cord,cordList::prefix));

      Type.PotionTypeMap = new HashMap<>();
      Type.SSDataMap = new HashMap<>();

      Type.PotionTypeItemData = new HashMap<>();

      Map<String,PotionType> PotionTypeList = Arrays.stream(PotionType.values()).collect(Collectors.toMap(Enum::toString, T->T));

      String del = "(^" + String.join("|^", deletePrefix) + ")";
      String st = "(^" + String.join("|^", potionCordList.stream().map(cordList::prefix).toList()) + ")";

      for(PotionType T : PotionType.values()) {
        String strong = T.toString().matches(st + ".*") ? T.toString().replaceAll("(?<=_).*$","") : "";
        int cord = Cord.CordMap.getOrDefault(strong,0);
        PotionType normalPotionType = PotionTypeList.get(T.toString().replaceAll("^"+strong,""));

        String name = ORIGINAL_TYPE_NAME.containsKey(T)
                ? ORIGINAL_TYPE_NAME.get(T)
                : T.toString().replaceAll(del + "|" + st, "");

        if (name.length() > 5){
          String prefix = name.toString().replaceAll("(?<=_).*$","");
          int i = exPrefix.getOrDefault(prefix, 5);
          name = name.substring(0,i);
        }

        NavigableMap<Integer, PotionType> typeSet;

        if(! Type.PotionTypeItemData.containsKey(T.toString()) ) {
          typeSet = new TreeMap<>();
          typeSet.put(cord,normalPotionType);
          Type.PotionTypeItemData.put(T.toString(),normalPotionType);
        }
        if(! Type.PotionTypeItemData.containsKey(name)) {
          typeSet = new TreeMap<>();
          typeSet.put(cord,normalPotionType);
          Type.PotionTypeItemData.put(name,normalPotionType);
        }

        if(Type.PotionTypeMap.containsKey(normalPotionType)){
          typeSet = Type.PotionTypeMap.get(normalPotionType);
          typeSet.put(cord, T);
        }else{
          typeSet = new TreeMap<>();
          typeSet.put(cord, T);
          Type.PotionTypeMap.put(normalPotionType, typeSet);
        }

        Type.SSDataMap.put(T, new Type.cordNormal(cord,normalPotionType,name));
      }
    }

    /**
     * オリジナルのポーションタイプ名から NormalPotionType に変換する
     *
     * @param potionTypeName SSPotionTypeショート名
     * @return 登録されている PotionType
     */
    public static PotionType getPotionType(String potionTypeName) {
      return Type.PotionTypeItemData.get(potionTypeName);
      //return ShortNameToTypeMap.getOrDefault(potionTypeName,FullNameToTypeMap.get(potionTypeName));
    }
    /**
     * NormalPotionTypeとCord値から PotionType に変換する
     *
     * @param potionType NormalPotionType
     * @param cord ポーションの強化値(Cord値)
     * @return 効果強化を含んだ PotionType
     */
    public static PotionType getPotionTypeFull(PotionType potionType, int cord) {
      return Type.PotionTypeMap.get(potionType).get(cord);
    }

    /**
     * PotionType から NormalPotionType を取得する
     * @param potionType ポーション効果強化 されている PotionType
     * @return NormalPotionType
     */
    public static PotionType getNormalPotionType(PotionType potionType){
      return Type.SSDataMap.get(potionType).type;
    }

    /**
     * PotionType から Cord値 を取得する
     * @param potionType ポーション効果強化 されている PotionType
     * @return ポーション効果強化 コード値
     */
    public static int getCord(PotionType potionType){
      return Type.SSDataMap.get(potionType).cord;
    }

    /**
     * オリジナルのポーションタイプ名に変換する
     * @param potionType 変換をしたいポーションタイプ
     * @return SS表記のポーションタイプ名
     */
    public static String getSSPotionTypeName(PotionType potionType) {
      return Type.SSDataMap.get(potionType).SSItemData;
    }

    /**
     * コード値 から ポーション強化プレフィックスを取得する
     * @param cord cord値
     * @return 強化プレフィックス
     */
    public static String getPrefix(int cord){
      return Cord.StrongPrefixMap.getOrDefault(cord,"");
    }

  }

  public static class enchantedBook{
    /**
     * ブロックStorageSign に記載する文字列
     */
    public static final String SS_ITEM_NAME = "ENCHBOOK";

    /**
     * 5文字でタイプ名が同じになるので6文字にするプレフィックス一覧
     */
    private static final List<String> exPrefix = List.of(
            "fire_"
    );

    private static final Map<Enchantment,String> EnchantShortNameMap;
    private static final Map<String,Enchantment> ShortNameToEnchantmentMap;
    private static final Map<Enchantment,String> EnchantFullNameMap;
    private static final Map<String,Enchantment> FullNameToEnchantmentMap;

    static{
      EnchantShortNameMap = new HashMap<>();
      ShortNameToEnchantmentMap = new HashMap<>();
      EnchantFullNameMap = new HashMap<>();
      FullNameToEnchantmentMap = new HashMap<>();
      List<Enchantment> enchants = org.bukkit.Bukkit.getRegistry(Enchantment.class).stream().toList();
      for(Enchantment E : enchants){
        String name = E.getKey().getKey();
        EnchantFullNameMap.put(E,name);
        FullNameToEnchantmentMap.put(name,E);
        if (name.length() > 5) name = name.substring(0,name.matches("(^" + String.join("|^", exPrefix) + ").*") ? 6: 5);
        EnchantShortNameMap.put(E,name);
        ShortNameToEnchantmentMap.put(name,E);
      }
    }

    public static Enchantment getEnchantment(String enchantName){
      return ShortNameToEnchantmentMap.getOrDefault(enchantName,FullNameToEnchantmentMap.get(enchantName));
    }

    public static String getEnchantShortName(Enchantment enchantment){
      return EnchantShortNameMap.get(enchantment);
    }

    public static String getEnchantFullName(Enchantment enchantment){
      return EnchantFullNameMap.get(enchantment);
    }

  }

}
