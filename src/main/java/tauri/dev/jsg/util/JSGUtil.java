package tauri.dev.jsg.util;

import net.minecraft.util.Tuple;

import java.util.List;
import java.util.Random;

public final class JSGUtil
{

    /**
     * @param random The random object to use
     * @param input A list of weight-attached tuples
     * @param emptyChance How much "nothing" should be added to the total weight sum.
     *                    Higher = higher chance of nothing being selected
     * @return The selected tuple or null if nothing was selected
     */
    public static <T> Tuple<T, Float> getWeightedRandom(Random random, List<Tuple<T, Float>> input, float emptyChance)
    {
        float sum = emptyChance;
        for (Tuple<?, Float> tuple : input)
        {
            sum += tuple.getSecond();
        }
        for (Tuple<T, Float> tuple : input)
        {
            if(random.nextFloat() * sum < tuple.getSecond()) return tuple;
            sum -= tuple.getSecond();
        }
        return null;
    }


}
