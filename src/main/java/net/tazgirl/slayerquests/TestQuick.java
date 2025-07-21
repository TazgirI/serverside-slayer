package net.tazgirl.slayerquests;

import java.util.ArrayList;
import java.util.List;

class Main
{


    public static void main(String[] args)
    {
        List<Integer> boundries = List.of(0, 4, 10, 25, 50, 75);

        List<List<Integer>> allTiers = new ArrayList<>();
        allTiers = CalcLevelBoundriesList(allTiers, boundries);

        System.out.println(allTiers.get(0) + " " + (allTiers.get(1).getFirst() - allTiers.get(0).getFirst()));
        System.out.println(allTiers.get(1) + " " + (allTiers.get(2).getFirst() - allTiers.get(1).getFirst()));
        System.out.println(allTiers.get(2) + " " + (allTiers.get(3).getFirst() - allTiers.get(2).getFirst()));
        System.out.println(allTiers.get(3) + " " + (allTiers.get(4).getFirst() - allTiers.get(3).getFirst()));
        System.out.println(allTiers.get(4) + " " + (allTiers.get(5).getFirst() - allTiers.get(4).getFirst()));
        System.out.println(allTiers.get(5) + " " + (allTiers.get(5).getLast() - allTiers.get(5).getFirst()));
    }

    private static List<List<Integer>> CalcLevelBoundriesList(List<List<Integer>> allTiers, List<Integer> boundries)
    {
        for(int i = 0; i < 6; i++)
        {
            allTiers.add(new ArrayList<>());
        }

        for(int i = 1; i < 101; i++)
        {
            allTiers.get(TierFromLevel(i, boundries)).add(CalcLevelToExpTotal(i));
        }

        return allTiers;
    }

    public static int CalcLevelToExpTotal(int level)
    {
        int total = 0;
        for(int i = 0; i < level; i++)
        {
            total += CalcLevelToExp(i + 1);
        }

        return total;
    }

    public static int CalcLevelToExp(int level)
    {
        level += 3;
        return (int) Math.round(((Math.pow(level, 1.5) + 2.4 * level) * level / 50) + (Math.min(level, 7) * 3));
    }

    public static int TierFromLevel(int level, List<Integer> boundries)
    {
        for(int i = 0; i < boundries.size(); i++)
        {
            if (level < boundries.get(i))
            {
                return i - 1;
            }
        }
        return 5;
    }

}
