package tauri.dev.jsg.integration.jei.recipe;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import tauri.dev.jsg.item.JSGItems;
import tauri.dev.jsg.item.linkable.dialer.UniverseDialerMode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Arrays;

public class JEIUniverseDialerCloneRecipe implements IRecipeWrapper {

  static ItemStack getDialerWithAddresses(int quantity, String... addrNames) {
    ItemStack stack = new ItemStack(JSGItems.UNIVERSE_DIALER, quantity);
    NBTTagCompound compound = new NBTTagCompound();
    NBTTagList list = new NBTTagList();
    compound.setTag(UniverseDialerMode.MEMORY.tagListName, list);
    stack.setTagCompound(compound);

    for (String name : addrNames) {
      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setString("name", name);
      list.appendTag(nbt);
    }

    return stack;
  }

  static ItemStack getNotebookWithPages(int quantity, ItemStack... pages) {
    ItemStack notebook = new ItemStack(JSGItems.NOTEBOOK_ITEM, quantity);
    NBTTagCompound compound = new NBTTagCompound();
    NBTTagList list = new NBTTagList();

    compound.setTag("addressList", list);
    notebook.setTagCompound(compound);

    for (ItemStack stack : pages) {
      list.appendTag(stack.getTagCompound());
    }

    return notebook;
  }

  static final ItemStack DIALER1 = getDialerWithAddresses(1, "Plains");
  static final ItemStack DIALER2 = getDialerWithAddresses(1, "Tundra");
  static final ItemStack DIALER_OUT = getDialerWithAddresses(2, "Plains", "Tundra");

  @Override
  public void getIngredients(IIngredients ingredients) {
    ingredients.setInputs(VanillaTypes.ITEM, Arrays.asList(DIALER1, DIALER2));
    ingredients.setOutput(VanillaTypes.ITEM, DIALER_OUT);
  }
}
