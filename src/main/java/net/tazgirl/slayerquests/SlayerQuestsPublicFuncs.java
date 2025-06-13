package net.tazgirl.slayerquests;

public class SlayerQuestsPublicFuncs
{
    public static int ExpToLevel(int exp)
    {
        int currentExp = exp;
        int nextLevelExp;

        for(int i = 1; i < 100; i++)
        {
            nextLevelExp = (int) (30 * Math.round(Math.pow(1.1f, i)));
            if (currentExp - nextLevelExp < 0)
            {
                return i;
            }
            currentExp -= nextLevelExp;
        }

        return 100;
    }
}
