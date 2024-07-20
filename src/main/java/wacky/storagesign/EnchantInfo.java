package wacky.storagesign;

import com.github.teruteru128.logger.Logger;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.util.NumberConversions;

import java.util.Arrays;

public class EnchantInfo {

  @Getter protected Material material;
  @Getter protected Enchantment enchantment;
  @Getter protected short damage = 0;
  private Logger _logger;

    /**
     * EnchantInfo
     * @param material エンチャント本の Material
     * @param str 文字列の配列 [エンチャント名,ダメージ値]
     * @param logger ロガー
     */
  public EnchantInfo(Material material, String[] str, Logger logger) {
    this.material = material;
    this._logger = logger;

    _logger.debug("EnchantInfo:Start");
    this.enchantment = getEnch(str[1]);
    _logger.trace("ench:" + enchantment);
    damage = NumberConversions.toShort(str[2]);
    _logger.trace("damage:" + damage);
  }

    /**
     * StorageSign 記載の短い名前を取得
     * @param enchantment エンチャント名
     * @return エンチャントショートネーム
     */
  public static String getShortType(Enchantment enchantment) {
    String key = enchantment.getKey().getKey();
    int len = key.startsWith("fire_") ? 6 : 5;
    return key.length() > len ? key.substring(0, len) : key;
  }

    /**
     * SS 表記の名前から エンチャント名を取得
     * @param substring エンチャント名 (ショートネーム可)
     * @return エンチャント名
     */
  private Enchantment getEnch(String substring) {
    _logger.debug("getEnch: Start");
    //後ろ切れても可.
    return org.bukkit.Bukkit.getRegistry(Enchantment.class).stream()
            .filter(E->E.getKey().getKey().startsWith(substring))
            .findFirst()
            .orElse(null);
  }

}
