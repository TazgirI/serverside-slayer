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
                    Codec.FLOAT.fieldOf("slayerExpPerMob").forGetter(currentQuestRecord::slayerExpPerMob),
                    Codec.STRING.fieldOf("questName").forGetter(currentQuestRecord::questName),
                    Codec.STRING.fieldOf("questTier").forGetter(currentQuestRecord::questTier)
            ).apply(instance, currentQuestRecord::new)
    );

    public static final Codec<slayerExperienceRecord> SLAYER_EXPERIENCE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("slayerExpPerMob").forGetter(slayerExperienceRecord::exp),
                    Codec.INT.fieldOf("slayerLevel").forGetter(slayerExperienceRecord::level)
            ).apply(instance, slayerExperienceRecord::new)
    );

    public static final Codec<questHolderRecord> QUEST_HOLDER_CODEC = RecordCodecBuilder.create(instance ->
            instance.group
                    (Codec.STRING.fieldOf("tierName").forGetter(questHolderRecord::tierName),
                    Codec.STRING.fieldOf("questName").forGetter(questHolderRecord::questName),
                    Codec.LONG.fieldOf("timeWhenStored").forGetter(questHolderRecord::timeWhenStored),
                    Codec.INT.fieldOf("sourceUuid").forGetter(questHolderRecord::sourceUuid)
                    ).apply(instance, questHolderRecord::new));

    public static final Codec<mobBonusRecord> MOB_BONUS_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("mobBonus").forGetter(mobBonusRecord::bonusAmount)).apply(instance, mobBonusRecord::new));

    public static final Codec<extendsNitwitBehaviourRecord> EXTENDS_NITWIT_BEHAVIOUR_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.BOOL.fieldOf("extendsNitwitBehaviour").forGetter(extendsNitwitBehaviourRecord::extendsNitwitBehaviour)).apply(instance, extendsNitwitBehaviourRecord::new));


    public static final Supplier<AttachmentType<currentQuestRecord>> CURRENT_QUEST = ATTACHMENT_TYPES.register("current_quest",() -> AttachmentType.builder(() -> new currentQuestRecord("",0,0,0,"","")).serialize(CURRENT_QUEST_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<slayerExperienceRecord>> SLAYER_EXPERIENCE = ATTACHMENT_TYPES.register("slayer_experience",() -> AttachmentType.builder(() -> new slayerExperienceRecord(0,1)).serialize(SLAYER_EXPERIENCE_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<questHolderRecord>> QUEST_HOLDER = ATTACHMENT_TYPES.register("quest_to_give",() -> AttachmentType.builder(() -> new questHolderRecord("","",null,0)).serialize(QUEST_HOLDER_CODEC).build());
    public static final Supplier<AttachmentType<mobBonusRecord>> MOB_BONUS = ATTACHMENT_TYPES.register("mob_bonus",() -> AttachmentType.builder(() -> new mobBonusRecord(0)).serialize(MOB_BONUS_CODEC).build());
    public static final Supplier<AttachmentType<extendsNitwitBehaviourRecord>> EXTENDS_NITWIT_BEHAVIOUR = ATTACHMENT_TYPES.register("extends_nitwit_behaviour",() -> AttachmentType.builder(() -> new extendsNitwitBehaviourRecord(false)).serialize(EXTENDS_NITWIT_BEHAVIOUR_CODEC).build());



    public record currentQuestRecord(String mob, int questCurrent, int questCap, float slayerExpPerMob, String questName, String questTier){}
    public record slayerExperienceRecord(float exp, int level){}
    public record questHolderRecord(String tierName, String questName, Long timeWhenStored, int sourceUuid){}
    public record mobBonusRecord(int bonusAmount){}
    public record extendsNitwitBehaviourRecord(boolean extendsNitwitBehaviour){}








    public static void register(IEventBus eventBus)
    {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
