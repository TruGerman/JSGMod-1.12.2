package tauri.dev.jsg.proxy;

import tauri.dev.jsg.renderer.stargate.StargateAbstractRendererState;
import tauri.dev.jsg.sound.SoundPositionedEnum;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface IProxy {
	public void preInit(FMLPreInitializationEvent event);
	public void init(FMLInitializationEvent event);
	public void postInit(FMLPostInitializationEvent event);
	
	public String localize(String unlocalized, Object... args);
	
	public EntityPlayer getPlayerInMessageHandler(MessageContext ctx);
	public void setTileEntityItemStackRenderer(Item item);
	public EntityPlayer getPlayerClientSide();
	public void addScheduledTaskClientSide(Runnable runnable);
	
	public void orlinRendererSpawnParticles(World world, StargateAbstractRendererState rendererState);
	public void playPositionedSoundClientSide(BlockPos pos, SoundPositionedEnum soundEnum, boolean play);
	
	public void openGui(GuiScreen gui);
}
