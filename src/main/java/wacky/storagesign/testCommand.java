package wacky.storagesign;

import static org.bukkit.Bukkit.getLogger;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import wacky.storagesign.information.Potion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class testCommand implements CommandExecutor {
  Player player;

  /**
   * Executes the given command, returning its success.
   * <br>
   * If false is returned, then the "usage" plugin.yml entry for this command
   * (if defined) will be sent to the player.
   *
   * @param sender  Source of the command
   * @param command Command which was executed
   * @param label   Alias of the command which was used
   * @param args    Passed command arguments
   * @return true if a valid command, otherwise false
   */


  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      if(sender instanceof Player) this.player = (Player) sender;

    return switch (label) {
      case "test" -> this.test();
      case "test2" -> this.test2();
      case "CustomItemDataSetTest" -> this.CustomItemDataSetTest();
      case "CustomItemDataGetTest" -> this.CustomItemDataGetTest();
      default -> false;
    };
  }
  protected boolean test(){
    String name = "DarkOakStorageSign";
    String[] machi = name.split("(?<=[a-z])(?=[A-Z])");
    String NAME = Arrays.stream(machi)
            .filter(T->! T.equals("Storage"))
            .map(String::toUpperCase)
            .collect(Collectors.joining("_"));
    Material m1 = Material.getMaterial(NAME);
    String s = Arrays.stream(
            "DarkOakStorageSign".replace(StorageSignConfig.STORAGE_SIGN_NAME,"Sign")
                    .split("(?<=[a-z])(?=[A-Z])"))
            .map(String::toUpperCase)
            .collect(Collectors.joining("_"));

    String NP = Material.POTION.toString().replaceAll("(?<=^.).*?_","");
    String SP = Material.SPLASH_POTION.toString().replaceAll("(?<=^.).*?_","");
    String LP = Material.LINGERING_POTION.toString().replaceAll("(?<=^.).*?_","");
    String potionPrefix = Potion.delPrefixPattern();
    String TP = PotionType.WATER_BREATHING.getKey().getKey().replaceAll(Potion.delPrefixPattern(),"");

    //    String name = Material.DARK_OAK_SIGN.toString();
/*    String names = Arrays.asList(name.split("_"))
            .stream()
            .filter(T->! (T.equals("SIGN") || T.equals("HANGING")))
            .map(String::toLowerCase)
            .map(StringUtils::capitalize)
            .collect(Collectors.joining());*/

/*    getLogger().info("test: CHERRY_SIGN :" + StorageSignV2.isSign(Material.CHERRY_SIGN));
    getLogger().info("test: ENCHANTED_BOOK :" + StorageSignV2.isSign(Material.ENCHANTED_BOOK));
    getLogger().info("test: CHERRY_WALL_SIGN :" + StorageSignV2.isSign(Material.CHERRY_WALL_SIGN));
    getLogger().info("test: CHERRY_HANGING_SIGN :" + StorageSignV2.isSign(Material.CHERRY_HANGING_SIGN));
    getLogger().info("test: CHERRY_WALL_HANGING_SIGN :" + StorageSignV2.isSign(Material.CHERRY_WALL_HANGING_SIGN));

    Block block = player.getTargetBlock((Set) null, 3);
    getLogger().info("test: TargetBlock.Type :" + block.getType());
    getLogger().info("test: TargetBlock :" + StorageSignV2.isSign(block.getType()));//*/
    return true;
  }

  protected boolean test2() {
    Potion.getPotionType("");

/*    getLogger().info("test2: CHERRY_SIGN isAss Sign : "
            + Material.CHERRY_SIGN.data.isAssignableFrom(org.bukkit.block.data.type.Sign.class));
    getLogger().info("test2: CHERRY_WALL_HANGING_SIGN isAss Sign : "
            + Material.CHERRY_HANGING_SIGN.data.isAssignableFrom(org.bukkit.block.data.type.Sign.class));
    getLogger().info("test2: CHERRY_SIGN isAss HangingSign : "
            + Material.CHERRY_SIGN.data.isAssignableFrom(org.bukkit.block.data.type.HangingSign.class));
    getLogger().info("test2: CHERRY_WALL_HANGING_SIGN isAss HangingSign : "
            + Material.CHERRY_HANGING_SIGN.data.isAssignableFrom(org.bukkit.block.data.type.HangingSign.class));

    getLogger().info("test2: CHERRY_SIGN isAss Sign : "
            + StorageSignV2.isSign(Material.CHERRY_SIGN));
    getLogger().info("test2: CHERRY_WALL_SIGN isAss Sign : "
            + StorageSignV2.isSign(Material.CHERRY_WALL_SIGN));
    getLogger().info("test2: CHERRY_HANGING_SIGN isAss HangingSign : "
            + StorageSignV2.isSign(Material.CHERRY_HANGING_SIGN));
    getLogger().info("test2: CHERRY_WALL_HANGING_SIGN isAss HangingSign : "
            + StorageSignV2.isSign(Material.CHERRY_WALL_HANGING_SIGN));*/

/*    Constructor<?>[] b = ttt.test.ench.getConstructors();
    try {
      Constructor<?> c = ttt.test.ench.getConstructor(String.class, Logger.class);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

    Constructor<?>[] d = EnchantedBook.class.getConstructors();*/
    return true;
  }

  protected boolean CustomItemDataSetTest(){
    ItemStack itemStack = player.getInventory().getItemInMainHand();
    ItemMeta meta = itemStack.getItemMeta();

    meta.setCustomModelData(55);
    itemStack.setItemMeta(meta);
    return true;
  }
  protected boolean CustomItemDataGetTest(){
    ItemStack itemStack = player.getInventory().getItemInMainHand();
    ItemMeta meta = itemStack.getItemMeta();

    int CMD = meta.getCustomModelData();

    getLogger().info(String.valueOf(CMD));
    return true;
  }
}
