package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import wacky.storagesign.StorageSignConfig.defaultData;

public class OminousBanner extends NormalInformation implements SSInformation{
  private static final String ominousBannerComponent = "minecraft:white_banner[minecraft:item_name='{\"color\":\"gold\",\"translate\":\"block.minecraft.ominous_banner\"}',minecraft:hide_additional_tooltip={},minecraft:banner_patterns=[{color: \"cyan\", pattern: \"minecraft:rhombus\"}, {color: \"light_gray\", pattern: \"minecraft:stripe_bottom\"}, {color: \"gray\", pattern: \"minecraft:stripe_center\"}, {color: \"light_gray\", pattern: \"minecraft:border\"}, {color: \"black\", pattern: \"minecraft:stripe_middle\"}, {color: \"light_gray\", pattern: \"minecraft:half_horizontal\"}, {color: \"light_gray\", pattern: \"minecraft:circle\"}, {color: \"black\", pattern: \"minecraft:border\"}]]";
  private static final String itemName = "§6Ominous Banner";
  /**
   * 不吉な旗フラグ
   * true : 不吉な旗 / false : 白旗
   */
  private boolean ominousBanner = false;

  public OminousBanner(ItemStack itemStack, Logger logger) {
    this(isOminousBanner((BannerMeta) itemStack.getItemMeta()), logger);
  }

  public OminousBanner(String itemData, Logger logger) {
    this(isOminousBanner(itemData),logger);
  }

  public OminousBanner(boolean ominousBanner, Logger logger) {
    super(Material.WHITE_BANNER, logger);
    this.ominousBanner = ominousBanner;
  }

  private static boolean isOminousBanner(BannerMeta meta){
    return meta.getItemName().equals(itemName);
  }

  private static boolean isOminousBanner(String itemData){
    return itemData.equals(defaultData.ominousBanner);
  }

  /**
   * アイテムStorageSign の Lore に書き込む情報を作成
   * [アイテムフルネーム] [アイテム数 amount]
   * @return 貯蔵アイテム情報 (Lore)
   */
  @Override
  public String getSSStorageItemData() {
    return ominousBanner ? defaultData.ominousBanner : content.toString();
  }

  /**
   * StorageSign として出庫する貯蔵アイテム ItemStack
   * @return Storage ItemStack
   */
  @Override
  public ItemStack getStorageItemStack() {
    return ominousBanner ?
            Bukkit.getItemFactory().createItemStack(ominousBannerComponent) :
            new ItemStack(content);
  }

}
