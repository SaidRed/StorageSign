package wacky.storagesign;

import org.bukkit.Material;
import java.util.Map;

import static java.util.Map.entry;
import static org.bukkit.Material.*;

public class StorageSignConfig {

  /**
   * SSオリジナル名称
   */
  private static final Map<String, Material> SSOriginalItemNameConvertMap = Map.ofEntries(
          entry("ENCHBOOK",ENCHANTED_BOOK),
          entry("SPOTION",SPLASH_POTION),
          entry("LPOTION",LINGERING_POTION)
  );

  /**
   * バージョンアップにて消滅した名称一覧
   */
  private static final Map<String, Material> OldItemNameConvertMap = Map.ofEntries(
          // 1.21時点 旧名称
          entry("SIGN", OAK_SIGN),
          entry("ROSE_RED", RED_DYE),
          entry("DANDELION_YELLOW", YELLOW_DYE),
          entry("CACTUS_GREEN", GREEN_DYE),
          entry("OMINOUS_BOTTLE", OMINOUS_BOTTLE)
  );

  /**
   * バージョンアップ時の命名変更によって問題が発生したアイテムの登録
   * <pre>{@code
   *     Map<Material, Map<Integer, Material>> SSItemNameVersionStraddleMap = Map.ofEntries(
   *            entry(NewMaterial,
   *                   Map.ofEntries(
   *                           entry(   0,  OldMaterial),
   *                           entry(   1, NextMaterial),
   *                           ...
   *                           entry(Cord,  NewMaterial)
   *                   )
   *             )
   *     );
   * }</pre>
   */
  protected static final Map<Material, Map<Integer, Material>> SSItemNameVersionStraddleMap = Map.ofEntries(
          // 1.13の滑らかハーフと1.14の石ハーフ区別
          entry(STONE_SLAB,
                  Map.ofEntries(
                          entry(0,SMOOTH_STONE_SLAB),
                          entry(1,STONE_SLAB)
                  )
          )
  );

  /**
   * オリジナルアイテム名を使用しているアイテムのコンバーター
   * @param itemName SS オリジナル名
   * @return 対応する Material データ
   */
  public static Material SSOriginalItemNameConverter(String itemName){
    return SSOriginalItemNameConvertMap.get(itemName);
  }

  /**
   * バージョン変更にて消滅した名称一覧
   * バージョンアップ時の命名変更によって問題が発生したアイテムの取得
   * @param itemName 取得したいアイテム名
   * @return 存在したら変換 / 見つからなかったら現在の Materialで検索をしてみる
   */
  public static Material SSOldItemNameConvert(String itemName){
    return OldItemNameConvertMap.getOrDefault(itemName, matchMaterial(itemName));
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
  public static Material SSItemNameVersionStraddle(String itemName,int cord){
    return SSItemNameVersionStraddle(SSOldItemNameConvert(itemName),cord);
  }

}
