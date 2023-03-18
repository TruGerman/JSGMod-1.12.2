package tauri.dev.jsg.worldgen.structures.V1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import tauri.dev.jsg.JSG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;


//Describes a single structure, heavily inspired by GregTech CE(u)
public class StructureConfigTemplate
{
    public String structureName;
    public boolean enabled;
    public float baseWeight, version;
    public List<String> dimension_filter;
    public boolean isBiomeWhitelist;
    public List<String> restrictedBiomes;
    public Map<String, Float> biomeModifiers;
    public boolean isBlockWhitelist;
    public List<String> restrictedBlocks;


    public StructureConfigTemplate(String name, float weight, int version, List<String> dimFilters, boolean isWhitelist, boolean isEnabled, List<String> restrictedBiomes, Map<String, Float> modifiers, boolean isBlockWhitelist, List<String> restrictedBlocks)
    {
        structureName= name;
        baseWeight = weight;
        this.version = version;
        dimension_filter = dimFilters;
        isBiomeWhitelist = isWhitelist;
        enabled = isEnabled;
        this.restrictedBiomes = restrictedBiomes;
        biomeModifiers = modifiers;
        this.isBlockWhitelist = isBlockWhitelist;
        this.restrictedBlocks = restrictedBlocks;
    }

    public StructureConfigTemplate(String name, float weight, int version)
    {
        this(name, weight, version, new ObjectArrayList<>(), false, true, new ObjectArrayList<>(), new Object2FloatOpenHashMap<>(), false, null);
    }

    public StructureConfigTemplate addDimFilter(String filter)
    {
        if(dimension_filter == null) dimension_filter = new ObjectArrayList<>();
        dimension_filter.add(filter);
        return this;
    }

    public StructureConfigTemplate addBiomeFilters(String... filters)
    {
        if(restrictedBiomes == null) restrictedBiomes = new ObjectArrayList<>();
        Collections.addAll(restrictedBiomes, filters);
        return this;
    }

    public StructureConfigTemplate setBiomeWhitelist()
    {
        isBiomeWhitelist = true;
        return this;
    }

    public StructureConfigTemplate setBlockWhitelist()
    {
        isBlockWhitelist = true;
        return this;
    }

    public StructureConfigTemplate addBlockFilters(Block... filters)
    {
        if(restrictedBlocks == null) restrictedBlocks = new ObjectArrayList<>();
        for(Block block : filters)
        {
            restrictedBlocks.add(block.getRegistryName().toString());
        }
        return this;
    }

    public StructureConfigTemplate disable()
    {
        enabled = false;
        return this;
    }

    /**
     * Serializes this instance and writes it to the specified location
     * @param folderName The subfolder within the structures directory to write this file to
     * @param fileName The name of the file to be written
     */
    public void serialize(String folderName, String fileName)
    {
        File folder = new File(JSG.modConfigDir, "jsg/" + "structures/" + folderName);
        File file = new File(JSG.modConfigDir, "jsg/" + "structures/" + folderName + "/" + fileName + ".json");

        try
        {
            if(!folder.exists()) folder.mkdirs();
            if(!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(new GsonBuilder().setPrettyPrinting().setVersion(1.0).create().toJson(this));
            writer.close();
        }
        catch (Exception e){
            JSG.error("Couldn't write default structure configs! Do you have write permission?");
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes this instance and writes it to the folder with the structure's name as file name
     * @param folderName The subfolder within the structures directory to write this file to
     */
    public void serialize(String folderName)
    {
        serialize(folderName, structureName);
    }

    /**
     * @param file The file to deserialize
     * @return A new StructureConfigTemplate instance containing the information encoded in the file
     */
    public static StructureConfigTemplate deserialize(File file)
    {
        try
        {
            return new Gson().fromJson(new FileReader(file), StructureConfigTemplate.class);
        }
        catch (FileNotFoundException e)
        {
            JSG.error("Tried deserializing a non-existent structure file!");
            throw new RuntimeException(e);
        }
    }

}
