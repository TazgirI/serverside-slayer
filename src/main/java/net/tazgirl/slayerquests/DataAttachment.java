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

    public static final Codec<CurrentQuestRecord> CURRENT_QUEST_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("mob").forGetter(CurrentQuestRecord::mob),
                    Codec.INT.fieldOf("questCurrent").forGetter(CurrentQuestRecord::questCurrent),
                    Codec.INT.fieldOf("questCap").forGetter(CurrentQuestRecord::questCap),
                    Codec.INT.fieldOf("slayerExp").forGetter(CurrentQuestRecord::slayerExp)
            ).apply(instance, CurrentQuestRecord::new)
    );

    public static final Codec<SlayerExperienceRecord> SLAYER_EXPERIENCE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("slayerExp").forGetter(SlayerExperienceRecord::exp),
                    Codec.INT.fieldOf("slayerLevel").forGetter(SlayerExperienceRecord::level)
            ).apply(instance, SlayerExperienceRecord::new)
    );

    public static final Supplier<AttachmentType<CurrentQuestRecord>> CURRENT_QUEST = ATTACHMENT_TYPES.register("current_quest",() -> AttachmentType.builder(() -> new CurrentQuestRecord(null,0,0,0)).serialize(CURRENT_QUEST_CODEC).copyOnDeath().build());
    public static final Supplier<AttachmentType<SlayerExperienceRecord>> SLAYER_EXPERIENCE = ATTACHMENT_TYPES.register("slayer_experience",() -> AttachmentType.builder(() -> new SlayerExperienceRecord(0,0)).serialize(SLAYER_EXPERIENCE_CODEC).copyOnDeath().build());


    public record CurrentQuestRecord(String mob, int questCurrent, int questCap, int slayerExp){}
    public record SlayerExperienceRecord(int exp, int level){}







    public static void register(IEventBus eventBus)
    {
        ATTACHMENT_TYPES.register(eventBus);
    }

}
