package tauri.dev.jsg.stargate.network.internalgates;

import tauri.dev.jsg.stargate.network.StargateAddressDynamic;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

public class StargateInternalGates {
    public HashMap<Integer, StargateInternalAddress> map = new HashMap<>();

    public StargateInternalGates() {
        init();
    }

    public void init() {
        map.clear();
        for (StargateAddressesEnum e : StargateAddressesEnum.values()) {
            map.put(e.id, e.address);
        }
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        if (map.size() < StargateAddressesEnum.values().length) init();
        for (StargateAddressesEnum e : StargateAddressesEnum.values()) {
            int id = e.id;
            compound.setTag(id + "_replaceAddr", map.get(id).addressToReplace.serializeNBT());
        }
        return compound;
    }

    public void deserializeNBT(NBTTagCompound compound) {
        map.clear();
        init();
        for (StargateAddressesEnum e : StargateAddressesEnum.values()) {
            int id = e.id;
            StargateInternalAddress a = new StargateInternalAddress(
                    e.address.minAddressLength,
                    e.address.maxAddressLength,
                    e.address.addressToMatch,
                    new StargateAddressDynamic(compound.getCompoundTag(id + "_replaceAddr"))
            );
            map.put(id, a);
        }
    }
}
