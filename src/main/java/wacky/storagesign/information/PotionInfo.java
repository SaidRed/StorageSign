package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import wacky.storagesign.StorageSignConfig;

/**
 * <strong>ポーションの表記ルール</strong><br>
 * [ポーションカテゴリ_ポーション名]:[ポーションタイプ]:[ポーションタイプ強化値] [ストレージ数]<br>
 * [Category _ potion] : [potionType] : [cord # 0:Normal /1:Long /2:Strong ]
 *
 * <br>
 *   <div>Block Line[1] ItemData:</div>
 *   <div>[MaterialPotionFullName] : [NormalPotionTypeShortName] : [cord # 0:Normal /1:Long /2:Strong]</div>
 * <br>
 *   <div>ItemStack Lora ItemData:</div>
 *   <div>[Category # ""/S/L + Potion ] : [NormalPotionTypeFullName] : [cord # 0:Normal /1:Long /2:Strong]</div>
 * <br>
 * <div>内包するPotionType に関しては NormalPotionType (強化・延長)の無い情報を持たせて、出庫する時だけ Cord値 を含めて作る</div>
 */
public class PotionInfo extends TypeInformation<PotionType, PotionMeta> implements SSInformation {

  public PotionInfo(String itemData, Logger logger){
    this(itemData.split("[: ]"),logger);
  }

  protected PotionInfo(String[] itemData, Logger logger) {
    this(getPotionCategory(itemData[0]), getPotionType(itemData[1]), Integer.parseInt(itemData[2]), logger);
  }

  public PotionInfo(ItemStack itemStack, Logger logger){
    this(itemStack.getType(), getPotionType((PotionMeta) itemStack.getItemMeta()),
            getStrongCord((PotionMeta) itemStack.getItemMeta()),logger);
  }

  public PotionInfo(Material material, PotionType type, int code, Logger logger) {
    super(material, type ,code, logger);
  }

  /**
   * SS表記ポーションカテゴリ から Material を取得する
   * @param itemData SS表記ポーションカテゴリ
   * @return Material
   */
  public static Material getPotionCategory(String itemData){
    if(itemData.equals("POTION")) return Material.POTION;
    return StorageSignConfig.defaultData.getOriginalItemName(itemData);
  }

  /**
   * PotionType の文字列から 強化無しの PotionType を取得する
   * @param name PotionType の文字列
   * @return PotionType
   */
  public static PotionType getPotionType(String name){
    //後ろ切れても可.
    return StorageSignConfig.potionType.getPotionType(name);
  }

  /**
   * メタデータ から ノーマルポーションタイプを取得する
   * @param meta ポーションタイプを取得したいメタデータ
   * @return ノーマルポーションタイプデータ
   */
  private static PotionType getPotionType(PotionMeta meta) {
    return StorageSignConfig.potionType.getNormalPotionType(meta.getBasePotionType());
  }

  /**
   * 今登録されている ItemData から PotionType を取得する
   * @return FullPotionType
   */
  private PotionType getPotionType() {
    return StorageSignConfig.potionType.getPotionTypeFull(type,cord);
  }

  /**
   * メタデータ から コード値を取得する
   * @param meta コード値を取得したいメタデータ
   * @return cord値
   */
  private static int getStrongCord(PotionMeta meta){
    return StorageSignConfig.potionType.getCord(meta.getBasePotionType());
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
    return StorageSignConfig.potionType.getNormalPotionType(type).toString();
  }

  /**
   * ブロックStorageSign に表記される タイプショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   * @return SS 表記の文字列
   */
  @Override
  protected String getTypeShortName() {
    return StorageSignConfig.potionType.getSSPotionTypeName(type);
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
    return StorageSignConfig.potionType.getCord(meta.getBasePotionType());
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
   * TODO こめじるし なぜかプレーヤーが持ってる ItemStack のポーションエフェクトに空情報入ってるからそのまま ItemStack.isSimilarできん
   *
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
