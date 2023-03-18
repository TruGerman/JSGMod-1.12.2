package tauri.dev.jsg.worldgen.structures;

import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import tauri.dev.jsg.stargate.network.SymbolTypeEnum;
import tauri.dev.jsg.worldgen.structures.processor.NetherProcessor;
import tauri.dev.jsg.worldgen.structures.processor.OverworldProcessor;
import tauri.dev.jsg.worldgen.structures.stargate.nether.JSGNetherStructure;
import tauri.dev.jsg.worldgen.util.EnumGenerationHeight;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public enum EnumStructures {

    // ---------------------------------------------------------------------------
    // STARGATE STRUCTURES

    // Milkyway
    PLAINS_MW("sg_plains_milkyway", 0, true, false, SymbolTypeEnum.MILKYWAY, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    DESERT_MW("sg_desert_milkyway", 0, true, false, SymbolTypeEnum.MILKYWAY, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    MOSSY_MW("sg_mossy_milkyway", 0, true, false, SymbolTypeEnum.MILKYWAY, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    FROST_MW("sg_frosty_milkyway", 0, true, false, SymbolTypeEnum.MILKYWAY, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    // Pegasus
    PLAINS_PG("sg_plains_pegasus", 0, true, false, SymbolTypeEnum.PEGASUS, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.8, 0.8, EnumGenerationHeight.MIDDLE),
    DESERT_PG("sg_desert_pegasus", 0, true, false, SymbolTypeEnum.PEGASUS, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    MOSSY_PG("sg_mossy_pegasus", 0, true, false, SymbolTypeEnum.PEGASUS, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    FROST_PG("sg_frosty_pegasus", 0, true, false, SymbolTypeEnum.PEGASUS, 13, 13, 0, true, new OverworldProcessor(), 35, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.MIDDLE),
    // Universe
    END_UNI("sg_end_universe", 0, true, false, SymbolTypeEnum.UNIVERSE, 10, 10, 1, true, new OverworldProcessor(), 15, Rotation.CLOCKWISE_90, 0.7, 0.8, EnumGenerationHeight.LOW),

    // Nether
    NETHER_MW("sg_nether_milkyway", 0, true, false, SymbolTypeEnum.MILKYWAY, 16, 16, -1, false, new NetherProcessor(), 12, Rotation.NONE, 0.3, 0.8, EnumGenerationHeight.HEIGHT),

    // ---------------------------------------------------------------------------
    // GENERAL STRUCTURES

    NAQUADAH_MINE("naquadah_mine", 10, false, false, null, 15, 15, 0, false, null, 35, Rotation.NONE, 0.5, 0.5, EnumGenerationHeight.LOW),

    TOKRA_TUNNEL("tr_tokra", 21, false, true, null, 7, 7, 0, false, null, 35, Rotation.NONE, 0.88, 0.8, EnumGenerationHeight.LOW),

    ANCIENT_TOTEM("ancient_totem", 0, false, false, null, 3, 3, 0, false, new OverworldProcessor(), 5, Rotation.NONE, 0.9, 0.3, EnumGenerationHeight.LOW);

    public final String name;
    private final JSGStructure structure;
    private final JSGNetherStructure netherStructure;

    EnumStructures(String structureName, int yNegativeOffset, boolean isStargateStructure, boolean isRingsStructure, SymbolTypeEnum symbolType, int structureSizeX, int structureSizeZ, int dimensionToSpawn, boolean findOptimalRotation, ITemplateProcessor templateProcessor, int airCountUp, Rotation rotationToNorth, double terrainFlatPercents, double topBlockMatchPercent, @Nonnull EnumGenerationHeight genHeight) {
        this.name = structureName;
        this.netherStructure = new JSGNetherStructure(structureName, yNegativeOffset, isStargateStructure, isRingsStructure, symbolType, structureSizeX, structureSizeZ, airCountUp, dimensionToSpawn, findOptimalRotation, templateProcessor, rotationToNorth, terrainFlatPercents, topBlockMatchPercent, genHeight);
        this.structure = new JSGStructure(structureName, yNegativeOffset, isStargateStructure, isRingsStructure, symbolType, structureSizeX, structureSizeZ, airCountUp, dimensionToSpawn, findOptimalRotation, templateProcessor, rotationToNorth, terrainFlatPercents, topBlockMatchPercent, genHeight);

    }

    public JSGStructure getActualStructure(int dimId){
        if(dimId == -1) return netherStructure;
        return structure;
    }

    @Nullable
    public static EnumStructures getStructureByName(String name){
        for(EnumStructures s : EnumStructures.values()){
            if(s.name.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    public static Collection<String> getAllStructureNames(){
        Collection<String> col = new ArrayList<>();
        for(EnumStructures s : EnumStructures.values())
            col.add(s.name);
        return col;
    }
}
