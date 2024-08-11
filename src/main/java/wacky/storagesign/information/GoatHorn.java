package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Material;
import org.bukkit.MusicInstrument;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MusicInstrumentMeta;
import wacky.storagesign.StorageSignConfig;

public class GoatHorn extends NormalInformation implements SSInformation{
  private static final Material mate = Material.GOAT_HORN;
  private final MusicInstrument music;

  public GoatHorn(ItemStack itemStack, Logger logger) {
    this(itemStack.getType(), ((MusicInstrumentMeta) itemStack.getItemMeta()).getInstrument(), logger);
  }

  public GoatHorn(String itemData, Logger logger) {
    this(itemData.split("[: ]"), logger);
  }

  public GoatHorn(String[] itemData, Logger logger) {
    this(mate, getMusicInstrument(itemData[1]), logger);
  }

  public GoatHorn(Material material, MusicInstrument music, Logger logger) {
    super(material, logger);
    this.music = music;
  }

  private static MusicInstrument getMusicInstrument(String itemData){
    return StorageSignConfig.goatHorn.getMusicInstrument(itemData);
  }

  /**
   * ブロックStorageSign に表記される文字列
   * [アイテムショートネーム]:[音情報]
   * @return 貯蔵アイテム情報 (SignBlock)
   */
  @Override
  public String getSSStorageItemData() {
    return content.toString() + ":" + StorageSignConfig.goatHorn.getMusicName(music);
  }

  /**
   * アイテムStorageSign の Lore に書き込む情報を作成
   * [アイテムフルネーム]:[音情報] [アイテム数 amount]
   * @return 貯蔵アイテム情報 (Lore)
   */
  @Override
  public String getSSLoreItemData() {
    return getSSStorageItemData();
  }

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    logger.debug("getContentItemStack: Start");
    ItemStack item = new ItemStack(content);
    MusicInstrumentMeta meta =(MusicInstrumentMeta) item.getItemMeta();
    meta.setInstrument(music);
    item.setItemMeta(meta);
    return item;
  }

}
