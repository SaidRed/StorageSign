package wacky.storagesign;

import com.github.teruteru128.logger.Logger;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.inventory.ItemStack;
import wacky.storagesign.information.*;

import java.util.*;

public class StorageSignV2 implements StorageSignV2Interface {

  private static final String STORAGE_SIGN_NAME = "StorageSign";

  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(StorageSign.class);

  /**
   * 看板のアイテム素材.
   */
  protected Material materialSign;

  /**
   * 倉庫に収納されているアイテム Material
   */
  protected Material materialStorage;

  /**
   * information
   */
  protected SSInformation info;

  /**
   * Storage アイテム cord
   */
  protected int cord = 0;

  /**
   * 看板に収納されているアイテム数.
   */
  protected int amount = 0;

  /**
   * SSのアイテムスタック数.
   */
  protected int stack = 1;

  /**
   * 看板の中身が空か.
   */
  protected boolean empty = false;

  /**
   * ログ出力用ロガー.
   */
  private final Logger logger;

  /**
   * アイテムStorageSign から データ取得
   *
   * @param itemStack アイテムStorageSign
   * @param logger ロガー
   */
  public StorageSignV2(ItemStack itemStack, Logger logger){
    this(itemStack.getType(),itemStack.getItemMeta().getLore().getFirst().split(" "),logger);
  }

  protected StorageSignV2(Material material,String[] lora,Logger logger){
    this(material,lora[0],Integer.parseInt(lora[1]),logger);
  }


  /**
   * ブロックStorageSign からデータ取得
   *
   * @param block ブロックStorageSign
   * @param logger ロガー
   */
  public StorageSignV2(Block block,Logger logger){
    this(block.getType(),(Sign) block.getState(),logger);
  }

  protected StorageSignV2(Material material,Sign sign,Logger logger){
    this(material,sign.getSide(Side.FRONT),logger);
  }

  protected StorageSignV2(Material material, SignSide signSide, Logger logger){
    this(material,signSide.getLine(1),Integer.parseInt(signSide.getLine(2)),logger);
  }

  /**
   * StorageSign からデータ取得
   *
   * @param materialSign StorageSign の種類 Material
   * @param itemData Storage されているアイテムデータ
   * @param amount Storage されているアイテム数
   * @param logger ロガー
   */
  protected StorageSignV2(Material materialSign, String itemData, int amount, Logger logger){
    this.materialSign = materialSign;
    this.logger = logger;

    if(itemData.equals("Empty")){
      empty = true;
      return;
    }

    this.materialStorage = VersionConverter.SSItemNameConverter(itemData,cord);
    this.amount = amount;

    this.info = switch (materialStorage){
      case OMINOUS_BOTTLE -> new OminousBottle(itemData,logger);
      case ENCHANTED_BOOK -> new EnchantedBook(itemData,logger);
      case FIREWORK_ROCKET -> new FireworkRocket(itemData,logger);
      case POTION,SPLASH_POTION,LINGERING_POTION -> new Potion(itemData,logger);
      default -> null;
    };

  }

  /**
   * Material から Sign であるか確認する
   * ※ このままだと Hanging Sign
   *
   * @param material 確認するMaterial
   * @return true : Sign である / false : ではない
   */
  protected static boolean isSign(Material material){
    return material.data.isAssignableFrom(org.bukkit.block.data.type.Sign.class);
  }

  /**
   * アイテムStorageSign の ItemStack
   *
   * @return StorageSign ItemStack
   */
  public ItemStack getStorageSign(){
//    return info.getSSItemStack(materialSign);
    return null;
  }

  /**
   * 在庫しているアイテム の 出庫
   * 最大スタック数 と 在庫数 を比較して出庫する
   * amount の出庫数カウントダウン
   *
   * @param sneaking スニークしているか true : スニーク中 / false : スニークしていない
   * @return 在庫しているアイテムの ItemStack
   */
  public ItemStack getStorageItem(boolean sneaking){
    ItemStack storageItem = info.getStorageItemStack();
    int max = storageItem.getMaxStackSize();
    int amount = sneaking ? 1 : Math.min(this.amount, max);
    storageItem.setAmount(amount);
    this.amount -= amount;
    return storageItem;
  }

  /**
   * itemStack の入庫を挑戦する
   * 入庫したら amount のカウントアップ
   * インベントリのアイテム消去は別処理
   *
   * @param itemStack 入庫するアイテム
   * @return 入庫の有無 true : 入庫完了 / false : 入庫失敗
   */
  public boolean importItemStack(ItemStack itemStack){
    if(info.isSimilar(itemStack)) return false;
    this.amount += itemStack.getAmount();
    return true;
  }

  /**
   * 渡された ItemStack が StorageSign か判断.
   *
   * @param itemStack 確認するItemStack
   * @param logger ロガー
   */
  public static boolean isStorageSign(ItemStack itemStack, Logger logger) {
    logger.debug(" isStorageSign(ItemStack):Start");

    if (Objects.isNull(itemStack.getItemMeta()))return false;

    if (!isSign(itemStack.getType()))return false;

    logger.trace(" !item.getItemMeta().hasDisplayName(): " + !itemStack.getItemMeta().hasDisplayName());
    if (!itemStack.getItemMeta().hasDisplayName()) {
      logger.debug(" itemMeta hasn't displayName.");
      return false;
    }

    logger.trace(" !item.getItemMeta().getDisplayName().matches(\"StorageSign\"): "
            + !itemStack.getItemMeta().getDisplayName().matches(STORAGE_SIGN_NAME));
    if (!itemStack.getItemMeta().getDisplayName().matches(STORAGE_SIGN_NAME)) {
      logger.debug(" itemMetaName hasn't StorageSign.");
      return false;
    }

    logger.trace(" item.getItemMeta().hasLore(): " + itemStack.getItemMeta().hasLore());
    return itemStack.getItemMeta().hasLore();

  }

  /**
   * 渡された Block が StorageSign か判断.
   *
   * @param block チェックするブロック
   * @param logger ロガー
   * @return true : is StorageSign / false : not StorageSign
   */
  public static boolean isStorageSign(Block block, Logger logger) {
    logger.debug(" isStorageSign(Block):Start");

    if(block.getState() instanceof Sign sign){
      logger.debug(" This Block is Sign.");

      logger.trace(" sign.getSide(Side.FRONT).getLine(0).matches(\"StorageSign\"): "
              + sign.getSide(Side.FRONT).getLine(0).matches(STORAGE_SIGN_NAME));
      if (sign.getSide(Side.FRONT).getLine(0).matches(STORAGE_SIGN_NAME)) {
        logger.debug(" This Sign is StorageSign.");
        return true;
      }
    }

    logger.debug(" This Block isn't StorageSign.");
    return false;
  }

  /**
   * 入庫しているアイテムと同一かの判定
   * エンチャ本は本自身の合成回数を問わない.
   *
   * @param itemStack 入庫アイテムと比較する ItemStack
   * @return  true：同一と認める / false：同一と認めない
   */
  public boolean isStorageItemEquals(ItemStack itemStack) {
    logger.debug(" isSimilar:start");
    if (itemStack == null) {
      logger.debug(" Item isn't Similar");
      return false;
    }

    return this.info.isSimilar(itemStack);
  }

  /**
   * StrageSign の倉庫が空かを確認します.
   *
   * @return true：空っぽ / false：入ってます
   */
  public boolean isEmpty() {
    return this.empty;
  }

}
