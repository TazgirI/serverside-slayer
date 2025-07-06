package net.tazgirl.slayerquests;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.sql.DataTruncation;
import java.util.Objects;

@EventBusSubscriber(modid = SlayerQuests.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SlayerQuestDetection
{
    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event)
    {
        if(event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity target && !player.level().isClientSide)
        {
            DataAttachment.currentQuestRecord currentQuest = player.getData(DataAttachment.CURRENT_QUEST);
            if(Objects.equals(currentQuest.mob(), BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString()))
            {
                player.setData(DataAttachment.CURRENT_QUEST.get(), new DataAttachment.currentQuestRecord(currentQuest.mob(), currentQuest.questCurrent() + 1 + target.getData(DataAttachment.MOB_BONUS.get()).bonusAmount(),currentQuest.questCap(),currentQuest.slayerExpPerMob(),currentQuest.questName()));
            }
        }
    }

}
