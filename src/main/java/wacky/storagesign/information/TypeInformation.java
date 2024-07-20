package wacky.storagesign.information;

import com.github.teruteru128.logger.Logger;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class TypeInformation<T extends Keyed,M extends ItemMeta> extends CordInformation<M>{
  protected T type;

  public TypeInformation(Material material, T type, int cord, Logger logger) {
    super(material, cord, logger);
    this.type = type;
  }

  /**
   * アイテムStorageSign の Lora に書き込む アイテムフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   *
   * @return Lora 文字列
   */
  protected abstract String getStorageItemName();

  /**
   * ブロックStorageSign に表記される アイテムショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   *
   * @return SS 表記の文字列
   */
  protected abstract String getStorageItemShortName();

  /**
   * アイテムStorageSign の Lora に書き込む タイプフルネーム
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   *
   * @return Lora 文字列
   */
  protected abstract String getTypeName();

  /**
   * ブロックStorageSign に表記される タイプショートネーム
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   *
   * @return SS 表記の文字列
   */
  protected abstract String getTypeShortName();

  /**
   * ブロックStorageSign に表記される文字列
   * <p>[アイテムショートネーム]:[タイプショートネーム]:[コード値]</p>
   *
   * @return SS 表記の文字列
   */
  @Override
  public String getSSStorageItemData() {
    return getStorageItemShortName() + ":" + getTypeShortName() + ":" + cord;
  }

  /**
   * アイテムStorageSign の Lora に書き込む情報を作成
   * <p>[アイテムフルネーム]:[タイプフルネーム]:[コード値] [アイテム数 amount]</p>
   *
   * @return Lora 文字列
   */
  @Override
  public String getSSLoraItemData() {
    return getStorageItemName() + ":" + getTypeName() + ":" + cord;
  }

}
