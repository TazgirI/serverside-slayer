package net.tazgirl.slayerquests;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class DataAttachment
{
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SlayerQuests.MODID);

    public static final Codec<currentQuestRecord> CURRENT_QUEST_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("mob").forGetter(currentQuestRecord::mob),
                    Codec.INT.fieldOf("questCurrent").forGetter(currentQuestRecord::questCurrent),
                    Codec.INT.fieldOf("questCap").forGetter(currentQuestRecord::questCap),
                    Codec.INT.fieldOf("slayerExpPerMob").forGetter(currentQuestRecord::slayerExpPerMob),
                    Codec.STRING.fieldOf("questName").forGetter(currentQuestRecord::questName)
            ).apply(instance, currentQuestRecord::new)
    );

    public static final Codec<slayerExperienceRecord> SLAYER_EXPERIENCE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("slayerExpPerMob").forGetter(slayerExperienceRecord::exp),
                    Codec.INT.fieldOf("slayerLevel").forGetter(slayerExperienceRecord::level)
            ).apply(instance, slayerExperienceRecord::new)
    );

    public static final Codec<questToGiveRecord> QUEST_TO_GIVE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group
                    (Codec.STRING.fieldOf("tierName").forGetter(questToGiveRecord::tierName),
                    Codec.STRING.fieldOf("questName").forGetter(questToGiveRecord::questName
                    )).apply(instance, questToGiveRecord::new));

    public static final Supplier<AttachmentType<currentQuestRecord>> CURRENT_QUEST = ATTACHMENT_TYPES.register("current_quest",() -> AttachmentType.builder(() -> new currentQuestRecord(null,0,0,0,"")).serialize(CURRENT_QUEST_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<slayerExperienceRecord>> SLAYER_EXPERIENCE = ATTACHMENT_TYPES.register("slayer_experience",() -> AttachmentType.builder(() -> new slayerExperienceRecord(0,0)).serialize(SLAYER_EXPERIENCE_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<questToGiveRecord>> QUEST_TO_GIVE = ATTACHMENT_TYPES.register("quest_to_give",() -> AttachmentType.builder(() -> new questToGiveRecord(null,null)).serialize(QUEST_TO_GIVE_CODEC).build());

    public record currentQuestRecord(String mob, int questCurrent, int questCap, int slayerExpPerMob, String questName){}
    public record slayerExperienceRecord(int exp, int level){}
    public record questToGiveRecord(String tierName, String questName){}







    public static void register(IEventBus eventBus)
    {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
