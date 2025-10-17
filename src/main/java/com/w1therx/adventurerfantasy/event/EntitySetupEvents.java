package com.w1therx.adventurerfantasy.event;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import com.w1therx.adventurerfantasy.capability.*;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod.EventBusSubscriber(modid = AdventurerFantasy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class EntitySetupEvents {

    private static final Logger log = LoggerFactory.getLogger(EntitySetupEvents.class);


    @SubscribeEvent
    public static void entitySetup(LivingEvent.LivingTickEvent event) {
        Level level = event.getEntity().level();
        if (level.isClientSide) return;

        if (event.getEntity() == null) return;

        if (event.getEntity().getTags().contains("shouldSetMaxHP")) {

            LazyOptional<IBaseStats> baseStatsL = event.getEntity().getCapability(ModCapabilities.BASE_STATS);
            LazyOptional<IDirtyStats> dirtyStatsL = event.getEntity().getCapability(ModCapabilities.DIRTY_STATS);
            LazyOptional<IIndependentStats> independentStatsL = event.getEntity().getCapability(ModCapabilities.INDEPENDENT_STATS);
            if (!baseStatsL.isPresent() || !independentStatsL.isPresent() || !dirtyStatsL.isPresent()) {
                return;
            }
            LivingEntity entity = event.getEntity();
            IBaseStats baseStats = baseStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IIndependentStats independentStats = independentStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            IDirtyStats dirtyStats = dirtyStatsL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
            if (entity instanceof Player) {
                baseStats.setBaseStat(StatType.MAX_HEALTH, 20);
                independentStats.setIndependentStat(IndependentStatType.HEALTH, 20);
                baseStats.setBaseStat(StatType.CRIT_RATE, 0.05);
                baseStats.setBaseStat(StatType.HEALTH_REGENERATION_AMP, 1);
                baseStats.setBaseStat(StatType.MAX_BOND_OF_LIFE, 40);
                event.getEntity().getTags().remove("shouldSetMaxHP");

            } else {
                double max_health = event.getEntity().getMaxHealth();
                baseStats.setBaseStat(StatType.MAX_HEALTH, max_health);
                independentStats.setIndependentStat(IndependentStatType.HEALTH, max_health);
                event.getEntity().getTags().remove("shouldSetMaxHP");
            }

            dirtyStats.setDirtyStat(StatType.MAX_BOND_OF_LIFE, true);



        }

    }
}
