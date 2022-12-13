package tauri.dev.jsg.command.stargate;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import tauri.dev.jsg.item.JSGItems;
import tauri.dev.jsg.item.notebook.PageNotebookItem;
import tauri.dev.jsg.stargate.network.StargateAddressDynamic;
import tauri.dev.jsg.stargate.network.SymbolInterface;
import tauri.dev.jsg.stargate.network.SymbolTypeEnum;

public class CommandPageGive extends CommandBase {

    @Override
    public String getName() {
        return "sggivepage";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sggivepage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        boolean hasUpgrade = true;

        if (args.length < 8) {
            throw new WrongUsageException("commands.sggivepage.usage");
        }

        if (args.length < 10)
            hasUpgrade = false;

        SymbolTypeEnum symbolType = null;
        String biome = "plains";

        for (int i = 1; i < 3; i++) {
            if (args[i].startsWith("map=")) {
                try {
                    symbolType = SymbolTypeEnum.valueOf(args[i].substring(4).toUpperCase());
                } catch (IllegalArgumentException e) {
                }
            } else if (args[i].startsWith("biome=")) {
                biome = args[i].substring(4);
            }
        }

        if (symbolType == null) {
            throw new WrongUsageException("commands.sggivepage.no_map");
        }

        EntityPlayer player = getPlayer(server, sender, args[0]);
        notifyCommandListener(sender, this, "commands.sggivepage.give_page", player.getName());

        StargateAddressDynamic stargateAddress = new StargateAddressDynamic(symbolType);
        int index = 1;

        for (int i = args.length - (hasUpgrade ? 8 : 6); i < args.length; i++) {
            SymbolInterface symbol = symbolType.fromEnglishName(args[i].replace("-", " "));

            if (symbol == null) {
                throw new WrongUsageException("commands.sgsetaddress.wrongsymbol", index);
            }

            stargateAddress.addSymbol(symbol);
            index++;
        }

        NBTTagCompound compound = PageNotebookItem.getCompoundFromAddress(
                stargateAddress,
                hasUpgrade,
                biome,
				-1);

        ItemStack stack = new ItemStack(JSGItems.PAGE_NOTEBOOK_ITEM, 1, 1);
        stack.setTagCompound(compound);
        player.addItemStackToInventory(stack);
    }

}
