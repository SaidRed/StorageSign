package wacky.storagesign;

import org.bukkit.Material;
import org.bukkit.potion.PotionType;
import wacky.storagesign.information.*;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

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

    private static final Map<String, Material> ORIGINAL_ITEM_NAME = Map.ofEntries(
            entry("ENCHBOOK",ENCHANTED_BOOK),
            entry("SPOTION",SPLASH_POTION),
            entry("LPOTION",LINGERING_POTION),
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
    public static Material getNewItemName(String itemName){
      return OLD_ITEM_NAME.getOrDefault(itemName, getOriginalItemName(itemName));
    }


  }

  public static class informationData{
    /**
     * 作成した SSInformation の対応リスト
     * ここへ登録すれば Material に対応する Information を使って StorageSign の情報作る
     */
    private static final Map<Material,Class<? extends SSInformation>> informationList = Map.ofEntries(
            entry(ENCHANTED_BOOK, EnchantedBook.class),
            entry(SPLASH_POTION, Potion.class),
            entry(LINGERING_POTION, Potion.class),
            entry(POTION, Potion.class),
            entry(OMINOUS_BOTTLE, OminousBottle.class),
            entry(FIREWORK_ROCKET, FireworkRocket.class)
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
     *     Map<Material, Map<Integer, Material>> SSItemNameVersionStraddleMap = Map.ofEntries(
     *            entry(NewMaterial,
     *                   Map.ofEntries(
     *                           entry(   0, OldMaterial1),
     *                           entry(   1, OldMaterial2),
     *                           ...
     *                           entry(Cord, NewMaterial)
     *                   )
     *             )
     *     );
     * }</pre>
     */
    private static final Map<Material, Map<Integer, Material>> VERSION_CONVERT = Map.ofEntries(
            // 1.13の滑らかハーフと1.14の石ハーフ区別
            entry(STONE_SLAB,
                    Map.ofEntries(
                            entry(0,SMOOTH_STONE_SLAB),
                            entry(1,STONE_SLAB)
                    )
            )
    );

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
      Map<Integer,Material> t = VERSION_CONVERT.get(material);
      return t.entrySet().stream()
              .max(Comparator.comparing(Map.Entry::getKey))
              .map(Map.Entry::getKey)
              .orElse(0);
    }

  }

  public static class potionType{
    /**
     * SSオリジナル PotionType 表記一覧
     * oldName : 旧名称 / newName : 新 PotionType
     *
     */
    private static final Set<potionTypeData> ORIGINAL_TYPE_NAME = Set.of(
//          new potionTypeData("REGEN", PotionType.REGENERATION),
//            new potionTypeData("BREAT", PotionType.WATER_BREATHING),
            new potionTypeData("HEAL", PotionType.HEALING),
            new potionTypeData("DAMAG", PotionType.HARMING),
            new potionTypeData("JUMP", PotionType.LEAPING),
            new potionTypeData("SPEED", PotionType.SWIFTNESS)
    );
    private record potionTypeData (String oldName,PotionType newName){}


    public static boolean containsNewName(String potionTypeName){
      return ORIGINAL_TYPE_NAME.stream()
              .anyMatch(T->T.oldName.equals(potionTypeName));
    }
    /**
     * オリジナルのポーションタイプから PortionType に変換する
     * @param potionTypeName 取得したいオリジナル PotionType文字列
     * @return 登録されている PotionType
     */
    public static PotionType getPotionType(String potionTypeName){
      return ORIGINAL_TYPE_NAME.stream()
              .filter(T->T.oldName.equals(potionTypeName))
              .findFirst()
              .map(potionTypeData::newName)
              .orElse(PotionType.WATER);
    }

    public static boolean containsOldName(PotionType potionType){
      return ORIGINAL_TYPE_NAME.stream()
              .anyMatch(T->T.newName.equals(potionType));
    }
    /**
     * オリジナルのポーション名に変換する
     * @param potionType 変換をしたいポーションタイプ
     * @return 登録されているオリジナル名があれば変換 / 無い場合はそのままの名称
     */
    public static String getPotionTypeName(PotionType potionType){
      return ORIGINAL_TYPE_NAME.stream()
              .filter(T->T.newName().equals(potionType))
              .findFirst()
              .map(potionTypeData::oldName)
              .orElse(potionType.toString());
    }

  }

}
