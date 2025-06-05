package net.tazgirl.slayerquests;

import java.util.List;

public class StoreJSON
{
    public record MobSets(String mob, int questMean, int questSkew){}
    public record QuestLevel(List<MobSets> possibleQuests){}
    public List<QuestLevel> tiers;

    public void ProcessJSON()
    {

    }



}
