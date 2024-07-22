package wacky.storagesign;

import org.bukkit.Material;
import org.checkerframework.checker.fenum.qual.FenumTop;

import java.util.Map;

import static java.util.Map.entry;
import static org.bukkit.Material.*;

/**
 * バージョンアップでの名前変更などで変換必要な物寄せ集め
 */
public class VersionConverter {

  /**
   * SSだけの略称一覧.
   * バージョン変更にて消滅した名称やオリジナル名称を追加していく
   */
  protected static Map<String, Material> SSOrignalItemNameConvertMap = Map.ofEntries(
          // 1.21時点 旧名称
          entry("SIGN", OAK_SIGN),
          entry("ROSE_RED", RED_DYE),
          entry("DANDELION_YELLOW", YELLOW_DYE),
          entry("CACTUS_GREEN", GREEN_DYE),
          entry("OMINOUS_BOTTLE", OMINOUS_BOTTLE),
          // SS オリジナル名
          entry("ENCHBOOK", ENCHANTED_BOOK),
          entry("SPOTION", SPLASH_POTION),
          entry("LPOTION", LINGERING_POTION),
          // SS オリジナル名 Empty表記
          entry("Empty",AIR),
          entry("",AIR)
  );

  /**
   * バージョンアップ時の命名変更によって問題が発生したアイテムの登録
   * <pre>{@code
   *     import static java.util.Map.entry;
   *
   *     Map<Material, Map<Integer, Material>> SSItemNameVersionStraddleMap = Map.ofEntries(
   *            entry(NewMaterial,
   *                   Map.ofEntries(
   *                           entry(   0, OldMaterial),
   *                           entry(Cord, NewCordMaterial),
   *                           ...
   *                           entry(Cord, NewCordMaterial)
   *                   )
   *             )
   *     );
   * }</pre>
   */
  protected static Map<Material, Map<Integer, Material>> SSItemNameVersionStraddleMap = Map.ofEntries(
          // 1.13の滑らかハーフと1.14の石ハーフ区別
          entry(STONE_SLAB,
                  Map.ofEntries(
                          entry(0,SMOOTH_STONE_SLAB),
                          entry(1,STONE_SLAB)
                  )
          )
  );

/*  protected static Map<String, Material> SSItemNameConvertMap = Map.ofEntries(
    //1.21時点 旧名称
    entry("SIGN", OAK_SIGN),
    entry("ROSE_RED", RED_DYE),
    entry("DANDELION_YELLOW", YELLOW_DYE),
    entry("CACTUS_GREEN", GREEN_DYE),
    entry("OMINOUS_BOTTLE", OMINOUS_BOTTLE),
    //SS オリジナル名
    entry("ENCHBOOK", ENCHANTED_BOOK),
    entry("SPOTION", SPLASH_POTION),
    entry("LPOTION", LINGERING_POTION)
  );*/
  /*
  public static PotionType potionConverter(PotionType potionType){
      PotionType.
      private static final Set<PotionType> HEAL_POTIONS =
              Collections.unmodifiableSet(EnumSet.of(HEALING, STRONG_HEALING));
      private static final Set<PotionType> BREATH_POTIONS =
              Collections.unmodifiableSet(EnumSet.of(WATER_BREATHING, LONG_WATER_BREATHING));
      private static final Set<PotionType> DAMAGE_POTIONS =
              Collections.unmodifiableSet(EnumSet.of(HARMING, STRONG_HARMING));
      private static final Set<PotionType> JUMP_POTIONS =
              Collections.unmodifiableSet(EnumSet.of(LEAPING, LONG_LEAPING, STRONG_LEAPING));
      private static final Set<PotionType> SPEED_POTIONS =
              Collections.unmodifiableSet(EnumSet.of(SWIFTNESS, LONG_SWIFTNESS, STRONG_SWIFTNESS));
      private static final Set<PotionType> REGENERATION_POTIONS =
              Collections.unmodifiableSet(EnumSet.of(REGENERATION, LONG_REGENERATION, STRONG_REGENERATION));

  }//*/

  public static Material SSItemNameConverter(String itemName){
    return SSOrignalItemNameConvertMap.getOrDefault(itemName, matchMaterial(itemName));
  }

  /**
   * バージョンアップ時の命名変更によって問題が発生したアイテムの取得
   * @param material 確認 Material
   * @param cord 取得したい Cord
   * @return 存在したら変換 / 見つからなかったら元の Material を返還
   */
  private static Material SSItemNameVersionStraddle(Material material, int cord){
    return SSItemNameVersionStraddleMap.containsKey(material) ?
            SSItemNameVersionStraddleMap.get(material).getOrDefault(cord,material):
            material;
  }

  /**
   * バージョンアップ時の命名変更によって問題が発生したアイテムの取得
   * アイテム名 文字列から検索する
   * @param itemName アイテム名
   * @param cord 取得したい Cord
   * @return 存在したら変換 / 見つからなかったら元の Material を返還
   */
  public static Material SSItemNameConverter(String itemName,int cord){
    return SSItemNameVersionStraddle(SSItemNameConverter(itemName.split(":")[0]),cord);
  }

}
