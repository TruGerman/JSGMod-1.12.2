package tauri.dev.jsg.tileentity.machine;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import tauri.dev.jsg.block.machine.OreWashingBlock;
import tauri.dev.jsg.gui.container.machine.orewashing.OreWashingContainerGuiUpdate;
import tauri.dev.jsg.machine.AbstractMachineRecipe;
import tauri.dev.jsg.machine.orewashing.OreWashingRecipe;
import tauri.dev.jsg.machine.orewashing.OreWashingRecipes;
import tauri.dev.jsg.renderer.machine.AbstractMachineRendererState;
import tauri.dev.jsg.renderer.machine.OreWashingRendererState;
import tauri.dev.jsg.sound.JSGSoundHelper;
import tauri.dev.jsg.sound.SoundEventEnum;
import tauri.dev.jsg.sound.SoundPositionedEnum;
import tauri.dev.jsg.power.stargate.StargateAbstractEnergyStorage;
import tauri.dev.jsg.state.State;
import tauri.dev.jsg.state.StateTypeEnum;
import tauri.dev.jsg.util.JSGItemStackHandler;

import javax.annotation.Nonnull;

public class OreWashingTile extends AbstractMachineTile {

    public OreWashingRendererState rendererState = new OreWashingRendererState();
    public static final int CONTAINER_SIZE = 2;
    protected final JSGItemStackHandler itemStackHandler = new JSGItemStackHandler(CONTAINER_SIZE) {

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return slot != 1; // output slot
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            markDirty();
            sendState(StateTypeEnum.RENDERER_UPDATE, getState(StateTypeEnum.RENDERER_UPDATE));
        }
    };
    protected final StargateAbstractEnergyStorage energyStorage = new StargateAbstractEnergyStorage(OreWashingBlock.MAX_ENERGY, OreWashingBlock.MAX_ENERGY_TRANSFER) {
        @Override
        protected void onEnergyChanged() {
            markDirty();
        }
    };
    protected final FluidTank fluidHandler = new FluidTank(OreWashingBlock.FLUID_CAPACITY) {
        @Override
        public boolean canFillFluidType(FluidStack fluid) {
            return fluid != null;
        }

        @Override
        protected void onContentsChanged() {
            markDirty();
            sendState(StateTypeEnum.RENDERER_UPDATE, getState(StateTypeEnum.RENDERER_UPDATE));
        }
    };

    @Override
    public StargateAbstractEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public JSGItemStackHandler getJSGItemHandler() {
        return itemStackHandler;
    }

    @Override
    protected void playLoopSound(boolean stop) {
        JSGSoundHelper.playPositionedSound(world, pos, SoundPositionedEnum.BEAMER_LOOP, !stop);
    }

    @Override
    protected void playSound(boolean start) {
        if (!start)
            JSGSoundHelper.playSoundEvent(world, pos, SoundEventEnum.BEAMER_STOP);
        else
            JSGSoundHelper.playSoundEvent(world, pos, SoundEventEnum.BEAMER_START);
    }

    @Override
    public AbstractMachineRecipe getRecipeIfPossible() {
        for (OreWashingRecipe recipe : OreWashingRecipes.RECIPES) {
            if (itemStackHandler.insertItem(1, recipe.getResult(), true).equals(recipe.getResult())) continue;
            if (fluidHandler.getFluid() == null) continue;
            if (recipe.isOk(energyStorage.getEnergyStored(), new FluidStack(fluidHandler.getFluid(), fluidHandler.getFluidAmount()), itemStackHandler.getStackInSlot(0)))
                return recipe;
        }
        return null;
    }

    @Override
    protected void workIsDone() {
        if (!isWorking) return;
        OreWashingRecipe recipe = (OreWashingRecipe) currentRecipe;
        itemStackHandler.insertItem(1, recipe.getResult(), false);
        itemStackHandler.extractItem(0, recipe.getItemNeeded().getCount(), false);
        fluidHandler.drainInternal(recipe.getSubFluidStack().amount, true);
        super.workIsDone();
    }

    @Override
    public State getState(StateTypeEnum stateType) {
        switch (stateType) {
            case GUI_UPDATE:
                return new OreWashingContainerGuiUpdate(energyStorage.getEnergyStored(), (fluidHandler.getFluid() != null ? new FluidStack(fluidHandler.getFluid(), fluidHandler.getFluidAmount()) : null), energyTransferedLastTick, machineStart, machineEnd);
            case RENDERER_UPDATE:
                ItemStack stack = currentRecipe != null ? ((OreWashingRecipe) currentRecipe).getResult() : itemStackHandler.getStackInSlot(1);
                return new OreWashingRendererState(workStateChanged, machineProgress, isWorking, stack);
        }
        return null;
    }

    @Override
    public State createState(StateTypeEnum stateType) {
        switch (stateType) {
            case GUI_UPDATE:
                return new OreWashingContainerGuiUpdate();
            case RENDERER_UPDATE:
                return new OreWashingRendererState();
        }
        return null;
    }

    @Override
    public void setState(StateTypeEnum stateType, State state) {
        switch (stateType) {
            case GUI_UPDATE:
                OreWashingContainerGuiUpdate guiUpdate = (OreWashingContainerGuiUpdate) state;
                energyStorage.setEnergyStored(guiUpdate.energyStored);
                energyTransferedLastTick = guiUpdate.energyTransferedLastTick;
                machineStart = guiUpdate.machineStart;
                machineEnd = guiUpdate.machineEnd;
                fluidHandler.setFluid(guiUpdate.fluidStack);
                markDirty();
                break;
            case RENDERER_UPDATE:
                rendererState = (OreWashingRendererState) state;
                this.machineProgress = rendererState.machineProgress;
                this.isWorking = rendererState.isWorking;
                markDirty();
                break;
        }
    }

    public AbstractMachineRendererState getRendererState() {
        return rendererState;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        return super.getCapability(capability, facing);
    }


    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound fluidHandlerCompound = new NBTTagCompound();
        fluidHandler.writeToNBT(fluidHandlerCompound);
        compound.setTag("fluidHandler", fluidHandlerCompound);

        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        fluidHandler.readFromNBT(compound.getCompoundTag("fluidHandler"));

        super.readFromNBT(compound);
    }
}
