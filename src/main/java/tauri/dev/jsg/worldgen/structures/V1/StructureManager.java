package tauri.dev.jsg.worldgen.structures.V1;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import tauri.dev.jsg.JSG;
import tauri.dev.jsg.stargate.network.SymbolTypeEnum;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static net.minecraft.init.Blocks.*;

/**
 * Caches and processes registered structures, heavily inspired by GTCE(u)
 */
public final class StructureManager
{
    /**Stores all allowed structures for a given dimension*/
    public static Object2ObjectOpenHashMap<WorldProvider, ObjectArrayList<StructureConfigTemplate>> allowedStructures = new Object2ObjectOpenHashMap<>();
    /**Stores all precomputed structure weights per biome*/
    public static Object2ObjectOpenHashMap<Tuple<Biome, WorldProvider>, ObjectArrayList<Tuple<StructureConfigTemplate, Float>>> biomeWeightCache = new Object2ObjectOpenHashMap<>();
    /**Stores every registered template, enabled or not*/
    public static ObjectArrayList<StructureConfigTemplate> registeredTemplates;
    public static Object2ObjectOpenHashMap<StructureConfigTemplate, ObjectArrayList<Block>> allowedBlocks = new Object2ObjectOpenHashMap<>();


    public static void init()
    {
        regenerateDefaultConfigs();
        registeredTemplates = getAllStructures(new File(JSG.modConfigDir + "//jsg//" + "structures//"));
        IForgeRegistry<Block> registry = GameRegistry.findRegistry(Block.class);
        for (StructureConfigTemplate template : registeredTemplates)
        {
            if(template.restrictedBlocks != null)
            {
                if(allowedBlocks.get(template) == null) allowedBlocks.put(template, new ObjectArrayList<>());
                for (String line : template.restrictedBlocks)
                {
                    ResourceLocation loc = new ResourceLocation(line.split(":")[0], line.split(":")[1]);
                    if(registry.containsKey(loc)) allowedBlocks.get(template).add(registry.getValue(loc));
                    else JSG.error("Couldn't find block \"{}\" for template \"{}\", ignoring...", loc, template.structureName);
                }
            }
        }
    }

    /**
     * @param biome The biome to spawn the gate in
     * @param symbol The symbol group that should be used
     * @param world The world the gate should be spawned in
     * @return A random, preconfigured gate for this biome
     */
    //TODO improve this when improving mysterious pages. Maybe select a default gate in case it doesn't find anything?
    public static StructureConfigTemplate getRandomGateForBiome(Biome biome, SymbolTypeEnum symbol, World world)
    {
        ObjectArrayList<Tuple<StructureConfigTemplate, Float>> potentialStructures = getWeightedList(biome, world.provider);
        ObjectArrayList<StructureConfigTemplate> matchingStructures = new ObjectArrayList<>();
        for (Tuple<StructureConfigTemplate, Float> tuple : potentialStructures)
        {
            if(tuple.getFirst().structureName.contains(symbol.name().toLowerCase())) matchingStructures.add(tuple.getFirst());
        }
        return matchingStructures.size() > 0 ? matchingStructures.get(world.rand.nextInt(matchingStructures.size())) : null;
    }


    /**
     * @param biome The biome to generate a weighted list for
     * @param provider The WorldProvider for said biome
     * @return A list of tuples mapping all structures allowed in this biome in this world to their relative weights
     */
    public static ObjectArrayList<Tuple<StructureConfigTemplate, Float>> getWeightedList(Biome biome, WorldProvider provider)
    {
        Tuple<Biome, WorldProvider> key = new Tuple<>(biome, provider);
        if(biomeWeightCache.containsKey(key)) return biomeWeightCache.get(key);
        ObjectArrayList<Tuple<StructureConfigTemplate, Float>> weightedList = new ObjectArrayList<>();
        for(StructureConfigTemplate template : getAllowedStructures(provider))
        {
            float chance = getChanceForBiome(biome, template);
            if(chance > 0) weightedList.add(new Tuple<>(template, chance));
        }
        biomeWeightCache.put(new Tuple<>(biome, provider), weightedList);
        return weightedList;

    }

    /**
     * @param provider The provider to check for
     * @return All structures allowed to spawn in this dimension
     */
    public static ObjectArrayList<StructureConfigTemplate> getAllowedStructures(WorldProvider provider)
    {
        if(allowedStructures.containsKey(provider)) return allowedStructures.get(provider);
        ObjectArrayList<StructureConfigTemplate> allowed = new ObjectArrayList<>();
        for(StructureConfigTemplate template : registeredTemplates)
        {
            if(template.enabled && parseDimensionFilters(template.dimension_filter).test(provider)) allowed.add(template);
        }
        allowedStructures.put(provider, allowed);
        return allowed;
    }

    /**
     * @param biome The biome to generate weights for
     * @param template The template to check
     * @return The total weight for this template in this biome
     */
    public static float getChanceForBiome(Biome biome, StructureConfigTemplate template)
    {
        float weight = template.baseWeight;

        //If the biome is not allowed, we return 0

        //Whitelist
        if(template.isBiomeWhitelist)
        {
            boolean foundMatch = false;
            for(String line : template.restrictedBiomes)
            {
                if(foundMatch) break;
                String[] contents = line.split(":");
                switch(contents[0])
                {
                    case "name":
                        if(biome.getBiomeName().equalsIgnoreCase(contents[1]))
                            foundMatch = true;
                        break;
                    case "type":
                        if(BiomeDictionary.getTypes(biome).stream().anyMatch(type -> type.getName().equalsIgnoreCase(contents[1])))
                            foundMatch = true;
                        break;
                }
            }
            if(!foundMatch) return 0;
        }
        //Blacklist
        else
        {
            for(String line : template.restrictedBiomes)
            {
                String[] contents = line.split(":");
                switch(contents[0])
                {
                    case "name":
                        if(biome.getBiomeName().toLowerCase().equalsIgnoreCase(contents[1]))
                            return 0;
                        break;
                    case "type":
                        if(BiomeDictionary.getTypes(biome).stream().anyMatch(type -> type.getName().equalsIgnoreCase(contents[1])))
                            return 0;
                        break;
                }
            }
        }

        for (Map.Entry<String, Float> entry : template.biomeModifiers.entrySet())
        {
            String[] contents = entry.getKey().split(":");
            switch(contents[0])
            {
                case "name":
                    if(biome.getBiomeName().toLowerCase().equalsIgnoreCase(contents[1]))
                        weight += entry.getValue();
                    break;
                case "type":
                    if(BiomeDictionary.getTypes(biome).stream().anyMatch(type -> type.getName().equalsIgnoreCase(contents[1])))
                        weight += entry.getValue();
                    break;
            }
        }
        return weight;
    }

    //TODO implement as command
    /**
     * Reloads the entire structure configuration system
     */
    public static void reload()
    {
        allowedStructures.clear();
        allowedBlocks.clear();
        biomeWeightCache.clear();
        registeredTemplates.clear();
        init();
    }

    /**
     * @return A map of every registered structure in the given folder
     * @param folder The folder to scan
     */
    public static ObjectArrayList<StructureConfigTemplate> getAllStructures(File folder) {
        ObjectArrayList<StructureConfigTemplate> list = new ObjectArrayList<>();

        for (File file : folder.listFiles())
        {
            if(file.isDirectory()) list.addAll(getAllStructures(file));
            else list.add(StructureConfigTemplate.deserialize(file));
        }
        return list;
    }

    /**
     * @param filters The array containing the dimension filters
     * @return A predicate combining all parsed filters
     */
    //Heavily inspired by GTCE(u)
    public static Predicate<WorldProvider> parseDimensionFilters(List<String> filters)
    {
        ObjectArrayList<Predicate<WorldProvider>> intermediateList = new ObjectArrayList<>(filters.size());
        for (String line : filters)
        {
            Predicate<WorldProvider> predicate = provider -> true;
            String[] contents = line.split(":");
            switch (contents[0])
            {
                case("id"):
                    try {
                        //Dimension ID range
                        if (contents.length == 3) {
                            predicate = provider -> provider.getDimension() >= Integer.parseInt(contents[1]) && provider.getDimension() <= Integer.parseInt(contents[2]);
                            break;
                        }
                        else if (contents.length == 2) predicate = provider -> provider.getDimension() == Integer.parseInt(contents[1]);
                            break;
                    }
                    //Fall through to default
                    catch (NumberFormatException e) {}
                case("provider_class"):
                    predicate = provider -> provider.getClass().getSimpleName().equals(contents[1]);
                    break;
                case("is_surface"):
                    predicate = provider -> provider.isSurfaceWorld();
                    break;
                case("is_nether"):
                    predicate = provider -> provider.isNether();
                    break;
                case("name"):
                    predicate = provider -> provider.getDimensionType().getName().equals(contents[1]);
                    break;
                default:
                    JSG.logger.error(String.format("Exception parsing dimension filter \"%s\", ignoring!", line));
                    predicate = provider -> false;
                    break;
            }
            intermediateList.add(predicate);
        }
        return provider -> intermediateList.stream().anyMatch(predicate -> predicate.test(provider));
    }

    public static ObjectArrayList<StructureConfigTemplate> getDefaultOverworldConfigs()
    {
        ObjectArrayList<StructureConfigTemplate> list = new ObjectArrayList<>(13);
        list.add(new StructureConfigTemplate("sg_plains_milkyway", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ocean", "type:river")
                .addBlockFilters(GRASS, DIRT, STONE)
                .setBlockWhitelist());

        list.add(new StructureConfigTemplate("sg_desert_milkyway", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:sandy")
                .addBlockFilters(SAND, SANDSTONE)
                .setBlockWhitelist()
                .setBiomeWhitelist());

        list.add(new StructureConfigTemplate("sg_mossy_milkyway", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("name:taiga", "type:jungle", "type:swamp", "type:mushroom")
                .addBlockFilters(GRASS, DIRT, STONE)
                .setBlockWhitelist()
                .setBiomeWhitelist());

        list.add(new StructureConfigTemplate("sg_frosty_milkyway", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ice", "type:frozen", "type:cold")
                .addBlockFilters(SNOW, SNOW_LAYER, ICE, FROSTED_ICE, PACKED_ICE)
                .setBlockWhitelist()
                .setBiomeWhitelist());

        list.add(new StructureConfigTemplate("sg_plains_pegasus", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ocean", "type:river")
                .addBlockFilters(GRASS, DIRT, STONE)
                .setBlockWhitelist());

        list.add(new StructureConfigTemplate("sg_desert_pegasus", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:sandy")
                .addBlockFilters(SAND, SANDSTONE)
                .setBlockWhitelist()
                .setBiomeWhitelist());
        list.add(new StructureConfigTemplate("sg_mossy_pegasus", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("name:taiga")
                .addBlockFilters(GRASS, DIRT, STONE)
                .setBlockWhitelist()
                .setBiomeWhitelist());

        list.add(new StructureConfigTemplate("sg_frosty_pegasus", 0.0001F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ice", "type:frozen", "type:cold")
                .addBlockFilters(SNOW, SNOW_LAYER, ICE, FROSTED_ICE, PACKED_ICE)
                .setBlockWhitelist()
                .setBiomeWhitelist());

        list.add(new StructureConfigTemplate("naquadah_mine", 0.0005F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ocean", "type:river")
                .addBlockFilters(GRASS)
                .setBlockWhitelist());

        list.add(new StructureConfigTemplate("tr_tokra", 0.0005F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ocean", "type:river")
                .addBlockFilters(SAND, GRASS)
                .setBlockWhitelist());

        list.add(new StructureConfigTemplate("ancient_totem", 0.00008F, 1)
                .addDimFilter("id:0")
                .addBiomeFilters("type:ocean", "type:river")
                .addBlockFilters(GRASS, SAND, STONE, DIRT, GRAVEL)
                .setBlockWhitelist());

        return list;
    }

    public static StructureConfigTemplate getDefaultNetherConfig()
    {
        return new StructureConfigTemplate("sg_nether_milkyway", 0F, 1)
                .addDimFilter("id:-1")
                .addBlockFilters(NETHERRACK, QUARTZ_ORE, NETHER_BRICK, SOUL_SAND)
                .setBlockWhitelist()
                .disable();
    }

    public static StructureConfigTemplate getDefaultEndConfig()
    {
        return new StructureConfigTemplate("sg_end_universe", 0.00007F, 1)
                .addDimFilter("id:1")
                .addBlockFilters(END_STONE)
                .setBlockWhitelist();
    }
    public static void regenerateDefaultConfigs()
    {
        for (StructureConfigTemplate template : getDefaultOverworldConfigs())
        {
            if(!new File(String.format(JSG.modConfigDir + "//jsg//structures//overworld//%s.json", template.structureName)).exists()) template.serialize("overworld");
        }
        if(!new File(JSG.modConfigDir + "//jsg//structures//nether//sg_nether_milkyway.json").exists()) getDefaultNetherConfig().serialize("nether");
        if(!new File(JSG.modConfigDir + "//jsg//structures//end//sg_end_universe.json").exists()) getDefaultEndConfig().serialize("end");
    }
}
