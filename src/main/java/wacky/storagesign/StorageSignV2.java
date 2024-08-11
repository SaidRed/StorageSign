package wacky.storagesign;

import com.github.teruteru128.logger.Logger;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import wacky.storagesign.information.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * ストレージサインの情報整理用中間処理
 */
public class StorageSignV2 implements StorageSignV2Interface {

  private static final java.util.logging.Logger logging = org.bukkit.Bukkit.getLogger();
  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(StorageSign.class);

  public static ItemStack emptyStorageSign(Material mat,Logger logger){
    ItemStack stack = new ItemStack(mat);
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(StorageSignConfig.defaultData.STORAGE_SIGN_NAME);
    meta.setLore(List.of(StorageSignConfig.defaultData.empty));
    meta.setMaxStackSize(ConfigLoader.getMaxStackSize());

    stack.setItemMeta(meta);
    return stack;
  }

  /**
   * StorageSignのアイテム素材.
   */
  protected Material materialSign;

  /**
   * 倉庫に収納されているアイテム Material
   */
  protected Material materialContent;

  /**
   * information
   */
  protected SSInformation info;

  /**
   * 看板に収納されているアイテム数.
   */
  protected int amount = 0;

  /**
   * 看板の中身が登録されているか.
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
    this(itemStack.getType(), itemStack.getItemMeta().getLore().getFirst().split(" "), logger);
  }

  protected StorageSignV2(Material material,String[] lora,Logger logger){
    this(material, lora[0], lora.length > 1 ? Integer.parseInt(lora[1]) : 0, logger);
  }

  /**
   * ブロックStorageSign からデータ取得
   *
   * @param block ブロックStorageSign
   * @param logger ロガー
   */
  public StorageSignV2(Block block,Logger logger){
    this(block.getType(), getSignSideFront(block.getState()), logger);
  }

  protected StorageSignV2(Material material, SignSide signSide, Logger logger){
    this(material,signSide.getLine(1),Integer.parseInt(signSide.getLine(2)),logger);
  }

  /**
   * BlockState(Sign) から Side(FRONT)を取得する
   * @param state 対象のBlockState
   * @return Front の SignSide
   */
  protected static SignSide getSignSideFront(BlockState state){
    if(state instanceof Sign sign) return sign.getSide(Side.FRONT);
    return null;
  }

  /**
   * StorageSign からデータ取得
   *
   * @param materialSign StorageSign の種類 Material
   * @param itemData content されているアイテムデータ
   * @param amount Storage されているアイテム数
   * @param logger ロガー
   */
  protected StorageSignV2(Material materialSign, String itemData, int amount, Logger logger){
    this.materialSign = materialSign;
    this.logger = logger;

    // 文字無しの場合は Empty と 同じ処理にするために代入
    if(itemData.isEmpty()) itemData = StorageSignConfig.defaultData.empty;

    this.amount = amount;

    // ***StorageSign だった場合
    if(itemData.endsWith(StorageSignConfig.defaultData.STORAGE_SIGN_NAME)){
      info = new StorageSignInfo(itemData,logger);
      return;
    }

    // SS特殊itemData などを含めた Material変換器 通す
    this.materialContent = StorageSignConfig.defaultData.getMaterial(itemData.split(":")[0]);

    // Content 登録されていない場合 AIR で登録
    if(Objects.isNull(materialContent) || Material.AIR.equals(materialContent)){
      this.info = new NormalInformation(materialContent,logger);
      empty = true;
      return;
    }

    /*
    // Material によって登録informationを切り替える
    this.info = switch (materialContent) {
      case OMINOUS_BOTTLE -> new OminousBottle(itemData, logger);
      case ENCHANTED_BOOK -> new EnchantedBook(itemData, logger);
      case FIREWORK_ROCKET -> new FireworkRocket(itemData, logger);
      case POTION, SPLASH_POTION, LINGERING_POTION -> new Potion(itemData, logger);
      default -> {
        if(StorageSignConfig.isSSItemNameVersionStraddle(materialContent)){
          yield new VersionStraddle(itemData, logger);
        }
        if(itemData.split(":").length == 2){
          yield new ToolInformation(itemData, logger);
        }
        yield new NormalInformation(itemData, logger);
      }
    };*/
    if(StorageSignConfig.informationData.containsKey(materialContent)){
      Class<? extends SSInformation> info = StorageSignConfig.informationData.get(materialContent);
      try{
        this.info = info.getConstructor(String.class,Logger.class).newInstance(itemData, logger);
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      return;
    }
    if(StorageSignConfig.versionConvert.containsKey(materialContent)){
      this.info = new VersionStraddle(itemData, logger);
      return;
    }
    if(itemData.split(":").length == 2) {
      this.info = new ToolInformation(itemData, logger);
      return;
    }
    this.info = new NormalInformation(itemData, logger);

  }


  /**
   * Material から Sign であるか確認する
   *
   * @param material 確認するMaterial
   * @return true : Sign である / false : ではない
   */
  protected static boolean isSign(Material material){
    return isFloorSign(material) || isWallSign(material);
    /*
    if(material.data.equals(org.bukkit.block.data.type.Sign.class)) return true;
    if(material.data.equals(org.bukkit.block.data.type.WallSign.class)) return true;
    return false;*/
  }

  public static boolean isFloorSign(Material material) {
    return material.data.equals(org.bukkit.block.data.type.Sign.class);
  }

  public static boolean isWallSign(Material material){
    return material.data.equals(org.bukkit.block.data.type.WallSign.class);
  }

  /**
   * ItemStack に アイテムStorageSign情報を書き込む
   *
   * @param itemStack 情報を書き込むItemStack
   * @return StorageSign になった ItemStack
   */
  public ItemStack getStorageSign(ItemStack itemStack){
    ItemMeta meta = itemStack.getItemMeta();
    Objects.requireNonNull(meta).setDisplayName(StorageSignConfig.defaultData.STORAGE_SIGN_NAME);
    if(isContentEmpty())clear();
    meta.setLore(List.of(info.getSSLoreItemData() + (
            info.getSSLoreItemData().equals(StorageSignConfig.defaultData.empty) ? "" : " " + amount)
    ));
    meta.setMaxStackSize(ConfigLoader.getMaxStackSize());
    itemStack.setItemMeta(meta);
    return itemStack;
  }

  /**
   * 在庫しているアイテム の 出庫
   * 最大スタック数 と 在庫数 を比較して出庫する
   * amount の出庫数カウントダウン
   *
   * @param sneaking スニークしているか true : スニーク中 / false : スニークしていない
   * @return 在庫しているアイテムの ItemStack
   */
  public ItemStack outputContentItem(boolean sneaking){
    ItemStack storageItem = info.getStorageItemStack();
    int max = storageItem.getMaxStackSize();
    int outAmount = sneaking ? 1 : Math.min(amount, max);
    storageItem.setAmount(outAmount);
    amount -= outAmount;
    return storageItem;
  }

  /**
   * 現在の情報を ItemStack に書き込む
   *
   * @param storageSign 書き込みたい StorageSign
   * @return 成否 true : 成功 / false : 失敗
   */
  public boolean setStorageSignData(ItemStack storageSign){
//    if(! isStorageSign(storageSign))return false;
    ItemMeta meta = storageSign.getItemMeta();
    meta.setLore(List.of(
            Material.AIR.equals(materialContent) ?
                    StorageSignConfig.defaultData.empty :
                    info.getSSLoreItemData() + " " + amount
    ));
    meta.setMaxStackSize(ConfigLoader.getMaxStackSize());
    storageSign.setItemMeta(meta);
    return true;
  }

  /**
   * 現在の情報をブロックに書き込む
   *
   * @param signBlock Signブロック に持っている情報を書き込む
   * @return 書き込みの成否 true : 成功 / false : 失敗
   */
  public boolean setStorageData(Block signBlock){
    if(signBlock.getState() instanceof Sign signState){
      SignSide side = signState.getSide(Side.FRONT);
      side.setLine(0, StorageSignConfig.defaultData.STORAGE_SIGN_NAME);
      side.setLine(1, info.getSSStorageItemData());
      side.setLine(2, String.valueOf(amount));
      side.setLine(3,(this.amount / 3456) + "LC " + (this.amount % 3456 / 64) + "s " + (this.amount % 64));
      signState.update();
      return true;
    }
    return false;
  }

  /**
   * Content 登録
   * @param itemStack 登録するアイテム
   */
  public void entryContent(ItemStack itemStack){
    if(isSign(itemStack.getType()) && isStorageSign(itemStack)) {
        this.info = new StorageSignInfo(itemStack, logger);
        return;
    }

    /*
    this.info = switch (itemStack.getType()){
      case OMINOUS_BOTTLE -> new OminousBottle(itemStack,logger);
      case ENCHANTED_BOOK -> new EnchantedBook(itemStack,logger);
      case FIREWORK_ROCKET -> new FireworkRocket(itemStack,logger);
      case POTION,SPLASH_POTION,LINGERING_POTION -> new Potion(itemStack,logger);
      default -> {
        if(StorageSignConfig.isSSItemNameVersionStraddle(itemStack.getType())){
          yield new VersionStraddle(itemStack, logger);
        }
        if(! itemStack.getType().data.isAssignableFrom(org.bukkit.block.data.BlockData.class)){
          if(itemStack.getItemMeta() instanceof Damageable meta){
            yield new ToolInformation(itemStack, logger);
          }
        }
        yield new NormalInformation(itemStack, logger);
      }
    };*/
    if(StorageSignConfig.informationData.containsKey(itemStack.getType())){
      Class<? extends SSInformation> info = StorageSignConfig.informationData.get(itemStack.getType());
      try{
        this.info = info.getConstructor(ItemStack.class, Logger.class).newInstance(itemStack, logger);
      } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      return;
    }
    if(StorageSignConfig.versionConvert.containsKey(itemStack.getType())){
      this.info = new VersionStraddle(itemStack, logger);
      return;
    }
    if(! itemStack.getType().isBlock()){
      if(itemStack.getItemMeta() instanceof Damageable) {
        this.info = new ToolInformation(itemStack, logger);
        return;
      }
    }
    this.info = new NormalInformation(itemStack, logger);

  }

  /**
   * itemStack の入庫を挑戦する
   * 入庫したら amount のカウントアップ
   * インベントリのアイテム消去は別処理
   *
   * @param itemStack 入庫するアイテム
   * @return 入庫の有無 true : 入庫完了 / false : 入庫失敗
   */
  public boolean importContentItem(ItemStack itemStack){
    if(! info.isSimilar(itemStack)) return false;
    this.amount += itemStack.getAmount();
    return true;
  }

  /**
   * 渡された ItemStack が StorageSign か判断.
   *
   * @param itemStack 確認するItemStack
   */
  public static boolean isStorageSign(ItemStack itemStack) {
    if (Objects.isNull(itemStack)) return false;
    if (Objects.isNull(itemStack.getItemMeta())) return false;
    if (!itemStack.getItemMeta().hasDisplayName()) return false;
    if (!itemStack.getItemMeta().getDisplayName()
            .equals(StorageSignConfig.defaultData.STORAGE_SIGN_NAME)) return false;
    return itemStack.getItemMeta().hasLore()
            && isStorageSign(itemStack.getType(), itemStack.getItemMeta().getDisplayName()) ;
  }

  /**
   * 渡された Block が StorageSign か判断.
   *
   * @param block チェックするブロック
   * @return true : is StorageSign / false : not StorageSign
   */
  public static boolean isStorageSign(Block block) {
    if(block.getState() instanceof Sign sign)
      return isStorageSign(block.getType(),sign.getSide(Side.FRONT).getLine(0));
    return false;
  }

  /**
   *　渡された情報から StorageSign か判断.
   *
   * @param material Sign かどうか確認する
   * @param itemName "StorageSign" の文字列が入っているか
   * @return true : is StorageSign / false : not StorageSign
   */
  protected static boolean isStorageSign(Material material, String itemName) {
    return isSign(material) && itemName.startsWith(StorageSignConfig.defaultData.STORAGE_SIGN_NAME);
  }

  /**
   * 入庫しているアイテムと同一かの判定
   * エンチャ本は本自身の合成回数を問わない.
   *
   * @param itemStack 入庫アイテムと比較する ItemStack
   * @return  true：同一と認める / false：同一と認めない
   */
  public boolean isContentItemEquals(ItemStack itemStack) {
    logger.debug(" isSimilar:start");
    if (itemStack == null) {
      logger.debug(" Item isn't Similar");
      return false;
    }

    return this.info.isSimilar(itemStack);
  }

  /**
   * 入庫しているアイテムと同一かの判定
   * エンチャ本は本自身の合成回数を問わない.
   *
   * @param itemData 入庫アイテムと比較する ItemData
   * @return  true：同一と認める / false：同一と認めない
   */
  public boolean isContentItemEquals(String itemData) {
    logger.debug(" isSimilar:start");
    return info.isSimilar(itemData);
  }

  /**
   * アイテムStorageSign が同じアイテムを収納したStorageSignであるかを判定
   * 収納数 amount は比較しない
   *
   * @param itemStack 比較したい アイテム (StorageSign)
   * @return true : 同一と認める / false : 同一と認めない
   */
  public boolean isSimilar(ItemStack itemStack) {
    return info.isSimilar(itemStack);
  }

  /**
   * StorageSign の倉庫が空かを確認します.
   *
   * @return true：空っぽ / false：入ってます
   */
  public boolean isEmpty() {
    return this.empty;
  }

  /**
   * StorageSign の倉庫在庫が0かを確認します.
   *
   * @return true：空っぽ / false：入ってます
   */
  public boolean isContentEmpty() {
    return amount == 0;
  }

  // TODO 以下SS中身のやり取り用

  /**
   * ブロックStorageSign から アイテムStorageSign へアイテムを入れる
   * @param itemStack 出庫先StorageSign
   * @param sneaking true スニーク / false ノーマル
   */
  public boolean SSExchangeExport(ItemStack itemStack, boolean sneaking){
    int limit = sneaking ? ConfigLoader.getSneakDivideLimit() : ConfigLoader.getDivideLimit();

    int outputAmount = 0;
    if (limit > 0 && amount > limit * (itemStack.getAmount() + 1)) {
      logger.debug("Item Export EmptySign divide-limit.");
      outputAmount = limit;
    } else {
      logger.debug("Item Export EmptySign Equality divide.");
      outputAmount = amount / (itemStack.getAmount() + 1);
    }

    if(outputAmount == 0)return false;
    ItemMeta meta = itemStack.getItemMeta();
    meta.setLore(List.of(info.getSSLoreItemData() + " " + outputAmount));
    itemStack.setItemMeta(meta);
    amount -= outputAmount * itemStack.getAmount();
    return true;
  }

  /**
   * アイテムStorageSign から ブロックStorageSign へアイテムを入れる
   * @param itemSS 入庫したい
   */
  public boolean SSExchangeExport(StorageSignV2 itemSS, ItemStack itemStack){
    if(! itemSS.isContentItemEquals(info.getSSLoreItemData())) return false;
    int stack = itemStack.getAmount();
    int importAmount = stack * itemSS.amount;

    amount += importAmount;
    itemSS.clear();
    itemSS.setStorageSignData(itemStack);
    return true;
  }

  /**
   * SS情報の削除
   */
  public void clear(){
    amount = 0;
    materialContent = Material.AIR;
    empty = true;
    info = new NormalInformation(materialContent, logger);
  }

  /**
   * 設置時に見づらい看板の色を変換するように取得できるようにしておく
   * @return StorageSign の木材情報返す
   */
  public Material getMaterialSign(){return materialSign;}

  // TODO ホッパー処理用（元処理から変更しないための処置なので削除したい）
  public int getAmount(){return amount;}

  public void addAmount(int addAmount){amount +=addAmount;}
}
