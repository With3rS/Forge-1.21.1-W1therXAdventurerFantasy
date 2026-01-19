package com.w1therx.adventurerfantasy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.w1therx.adventurerfantasy.capability.IAddStats;
import com.w1therx.adventurerfantasy.capability.IDirtyStats;
import com.w1therx.adventurerfantasy.capability.IMultStats;
import com.w1therx.adventurerfantasy.capability.ModCapabilities;
import com.w1therx.adventurerfantasy.common.enums.IndependentStatType;
import com.w1therx.adventurerfantasy.common.enums.StatType;
import com.w1therx.adventurerfantasy.event.custom.*;
import com.w1therx.adventurerfantasy.util.ModTags;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ModCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("health")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("heal")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> result = new ArrayList<>(List.of());
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                if (entity instanceof LivingEntity living) {
                                                    MinecraftForge.EVENT_BUS.post(new HealingEvent(null, living, amount));
                                                    result.add(living);
                                                }
                                            }
                                            if (result.isEmpty()) {
                                                source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                return 0;
                                            } else {
                                                StringBuilder text = new StringBuilder();
                                                for (Entity entity : result) {
                                                    text.append(entity.getName().getString());
                                                    text.append(" ");
                                                }
                                                source.sendSuccess(() -> Component.literal("Healed " + text + "by " + amount), true);
                                                return 1;
                                            }
                                        }))
                                .then(Commands.argument("healer", EntityArgument.entity())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                    Entity healer = EntityArgument.getEntity(ctx, "healer");
                                                    if (healer instanceof LivingEntity livingHealer) {
                                                        List<Entity> result = new ArrayList<>(List.of());
                                                        for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                            if (entity instanceof LivingEntity living) {

                                                                MinecraftForge.EVENT_BUS.post(new HealingEvent(livingHealer, living, amount));
                                                                result.add(entity);
                                                            }
                                                        }
                                                        if (result.isEmpty()) {
                                                            source.sendFailure(Component.literal("No alive target among the selected ones"));
                                                            return 0;
                                                        } else {
                                                            StringBuilder text = new StringBuilder();
                                                            for (Entity entity : result) {
                                                                text.append(entity.getName().getString());
                                                                text.append(" ");
                                                            }
                                                            source.sendSuccess(() -> Component.literal(healer.getName().getString() + " healed " + text + "by " + amount), true);
                                                        }
                                                    } else {
                                                        source.sendFailure(Component.literal("Healer does not exist"));
                                                        return 0;
                                                    }
                                                    return 0;
                                                })))
                        ))
                .then(Commands.literal("consume")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> result = new ArrayList<>(List.of());
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                if (entity instanceof LivingEntity living) {
                                                    MinecraftForge.EVENT_BUS.post(new HealthConsumptionEvent(living, amount));
                                                    result.add(living);
                                                }
                                            }
                                            if (result.isEmpty()) {
                                                source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                return 0;
                                            } else {
                                                StringBuilder text = new StringBuilder();
                                                for (Entity entity : result) {
                                                    text.append(entity.getName().getString());
                                                    text.append(" ");
                                                }
                                                source.sendSuccess(() -> Component.literal("Consumed " + amount + " health from " + text), true);
                                                return 1;
                                            }
                                        }))
                        ))
                .then(Commands.literal("bind")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> result = new ArrayList<>(List.of());
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                if (entity instanceof LivingEntity living) {
                                                    MinecraftForge.EVENT_BUS.post(new BindByLifeEvent(living, null, amount));
                                                    result.add(living);
                                                }
                                            }
                                            if (result.isEmpty()) {
                                                source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                return 0;
                                            } else {
                                                StringBuilder text = new StringBuilder();
                                                for (Entity entity : result) {
                                                    text.append(entity.getName().getString());
                                                    text.append(" ");
                                                }
                                                source.sendSuccess(() -> Component.literal("Bound by life " + text + "by " + amount), true);
                                                return 1;
                                            }
                                        }))
                                .then(Commands.argument("applier", EntityArgument.entity())
                                        .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    List<Entity> result = new ArrayList<>(List.of());
                                                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                    if (EntityArgument.getEntity(ctx, "applier") instanceof LivingEntity applier) {
                                                        for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                            if (entity instanceof LivingEntity living) {
                                                                MinecraftForge.EVENT_BUS.post(new BindByLifeEvent(living, applier, amount));
                                                                result.add(living);
                                                            }
                                                        }
                                                        if (result.isEmpty()) {
                                                            source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                            return 0;
                                                        } else {
                                                            StringBuilder text = new StringBuilder();
                                                            for (Entity entity : result) {
                                                                text.append(entity.getName().getString());
                                                                text.append(" ");
                                                            }
                                                            source.sendSuccess(() -> Component.literal("Bound by life " + text + "by " + amount), true);
                                                            return 1;
                                                        }
                                                    } else {
                                                        source.sendFailure(Component.literal("Applier is not alive"));
                                                        return 0;
                                                    }
                                                })))
                        ))
                .then(Commands.literal("shield")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("duration", DoubleArgumentType.doubleArg())
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    List<Entity> result = new ArrayList<>(List.of());
                                                    double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                    int duration = (int) (DoubleArgumentType.getDouble(ctx, "duration") * 20);
                                                    for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                        if (entity instanceof LivingEntity living) {
                                                            MinecraftForge.EVENT_BUS.post(new ShieldingEvent(null, living, amount, duration));
                                                            result.add(living);
                                                        }
                                                    }
                                                    if (result.isEmpty()) {
                                                        source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                        return 0;
                                                    } else {
                                                        StringBuilder text = new StringBuilder();
                                                        for (Entity entity : result) {
                                                            text.append(entity.getName().getString());
                                                            text.append(" ");
                                                        }
                                                        source.sendSuccess(() -> Component.literal("Given a shield of " + amount + " hp lasting for " + duration / 20 + " seconds to " + text), true);
                                                        return 1;
                                                    }
                                                })
                                                .then(Commands.argument("shield_provider", EntityArgument.entity())
                                                        .executes(ctx -> {
                                                            CommandSourceStack source = ctx.getSource();
                                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                                            int duration = (int) (DoubleArgumentType.getDouble(ctx, "duration") * 20);
                                                            Entity shieldProvider = EntityArgument.getEntity(ctx, "shield_provider");
                                                            if (shieldProvider instanceof LivingEntity livingProvider) {
                                                                List<Entity> result = new ArrayList<>(List.of());
                                                                for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                                    if (entity instanceof LivingEntity living) {

                                                                        MinecraftForge.EVENT_BUS.post(new ShieldingEvent(livingProvider, living, amount, duration));
                                                                        result.add(entity);
                                                                    }
                                                                }
                                                                if (result.isEmpty()) {
                                                                    source.sendFailure(Component.literal("No alive target among the selected ones"));
                                                                    return 0;
                                                                } else {
                                                                    StringBuilder text = new StringBuilder();
                                                                    for (Entity entity : result) {
                                                                        text.append(entity.getName().getString());
                                                                        text.append(" ");
                                                                    }
                                                                    source.sendSuccess(() -> Component.literal(shieldProvider.getName().getString() + " gave a shield of " + amount + " lasting " + duration / 20 + " seconds to " + text), true);
                                                                }
                                                            } else {
                                                                source.sendFailure(Component.literal("Shield provider does not exist"));
                                                                return 0;
                                                            }
                                                            return 0;
                                                        }))))
                        ))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> overMax = new ArrayList<>(List.of());
                                            List<Entity> healed = new ArrayList<>(List.of());
                                            List<Entity> notAlive = new ArrayList<>(List.of());
                                            List<Entity> noCaps = new ArrayList<>(List.of());
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                if (entity instanceof LivingEntity living) {
                                                    living.getCapability(ModCapabilities.FINAL_STATS).ifPresent(statsF -> {
                                                        living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                                                            if (amount > statsF.getFinalStat(StatType.MAX_HEALTH)) {
                                                                statsI.setIndependentStat(IndependentStatType.HEALTH, statsF.getFinalStat(StatType.MAX_HEALTH));
                                                                overMax.add(living);
                                                            } else {
                                                                statsI.setIndependentStat(IndependentStatType.HEALTH, amount);
                                                                healed.add(living);
                                                                if (amount <= 0) {
                                                                    MinecraftForge.EVENT_BUS.post(new DeathHandlerEvent(living, null, null));
                                                                }
                                                            }
                                                        });
                                                    });

                                                    if (!(overMax.contains(living) || healed.contains(living))) {
                                                        noCaps.add(living);
                                                    }
                                                } else {
                                                    notAlive.add(entity);
                                                }
                                            }

                                            if (overMax.isEmpty() && healed.isEmpty()) {
                                                source.sendFailure(Component.literal("No alive target found or with necessary capabilities"));
                                                return 0;
                                            } else {
                                                StringBuilder overMaxString = new StringBuilder();
                                                for (Entity entity : overMax) {
                                                    overMaxString.append(entity.getName().getString());
                                                    overMaxString.append(" ");
                                                }

                                                StringBuilder healedString = new StringBuilder();
                                                for (Entity entity : healed) {
                                                    healedString.append(entity.getName().getString());
                                                    healedString.append(" ");
                                                }

                                                StringBuilder notAliveString = new StringBuilder();
                                                for (Entity entity : notAlive) {
                                                    notAliveString.append(entity.getName().getString());
                                                    notAliveString.append(" ");
                                                }

                                                StringBuilder noCapsString = new StringBuilder();
                                                for (Entity entity : noCaps) {
                                                    noCapsString.append(entity.getName().getString());
                                                    noCapsString.append(" ");
                                                }

                                                Component msgOverMax;

                                                if (!overMax.isEmpty()) {
                                                    msgOverMax = Component.literal("Health couldn't be set to " + amount + " to " + overMaxString + "because it was too high, so it was set to their max hp. ");
                                                } else {
                                                    msgOverMax = Component.literal("");
                                                }

                                                Component msgHealed;

                                                if (!healed.isEmpty()) {
                                                    msgHealed = Component.literal("Set hp to " + healedString + "to " + amount + ". ");
                                                } else {
                                                    msgHealed = Component.literal("");
                                                }

                                                Component msgNotAlive;

                                                if (!notAlive.isEmpty()) {
                                                    msgNotAlive = Component.literal("Couldn't set max hp to " + notAliveString + "because they aren't alive. ");
                                                } else {
                                                    msgNotAlive = Component.literal("");
                                                }

                                                Component msgNoCaps;

                                                if (!noCaps.isEmpty()) {
                                                    msgNoCaps = Component.literal("Couldn't set max hp to " + noCapsString + "because they didn't have the required capabilities to complete the action. ");
                                                } else {
                                                    msgNoCaps = Component.literal("");
                                                }

                                                Component finalComponent = Component.literal(msgHealed.getString() + msgOverMax.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                source.sendSuccess(() -> finalComponent, true);


                                                return 1;
                                            }
                                        }))))

        );

        dispatcher.register(Commands.literal("stat")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("multiplicative")
                        .then(Commands.literal("set")
                                .then(Commands.argument("stat_type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            StatType[] statList = StatType.values();

                                            for (StatType stat : Arrays.stream(statList).toList()) {
                                                builder.suggest(stat.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("entity", EntityArgument.entities())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String statName = StringArgumentType.getString(ctx, "stat_type");
                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                StatType stat = StatType.valueOf(statName.toUpperCase());
                                                                double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                List<Entity> changedTo = new ArrayList<>(List.of());
                                                                List<Entity> notAlive = new ArrayList<>(List.of());
                                                                List<Entity> noCaps = new ArrayList<>(List.of());

                                                                for (Entity entity : EntityArgument.getEntities(ctx, "entity")) {
                                                                    if (entity instanceof LivingEntity living) {
                                                                        LazyOptional<IMultStats> statsML = living.getCapability(ModCapabilities.MULT_STATS);
                                                                        LazyOptional<IDirtyStats> statsDL = living.getCapability(ModCapabilities.DIRTY_STATS);

                                                                        if (!statsDL.isPresent() || !statsML.isPresent()) {
                                                                            noCaps.add(living);
                                                                        } else {
                                                                            IMultStats statsM = statsML.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                                                                            IDirtyStats statsD = statsDL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                                                                            statsM.setMultStat(stat, value);
                                                                            statsD.setDirtyStat(stat, true);

                                                                            changedTo.add(living);
                                                                        }
                                                                    } else {
                                                                        notAlive.add(entity);
                                                                    }
                                                                }

                                                                if (changedTo.isEmpty()) {
                                                                    source.sendFailure(Component.literal("Couldn't change stat because no entity was alive or had the necessary capabilities."));
                                                                    return 0;
                                                                } else {

                                                                    StringBuilder changedToString = new StringBuilder();
                                                                    for (Entity entity : changedTo) {
                                                                        changedToString.append(entity.getName().getString());
                                                                        changedToString.append(" ");
                                                                    }

                                                                    StringBuilder notAliveString = new StringBuilder();
                                                                    for (Entity entity : notAlive) {
                                                                        notAliveString.append(entity.getName().getString());
                                                                        notAliveString.append(" ");
                                                                    }

                                                                    StringBuilder noCapsString = new StringBuilder();
                                                                    for (Entity entity : noCaps) {
                                                                        noCapsString.append(entity.getName().getString());
                                                                        noCapsString.append(" ");
                                                                    }

                                                                    Component msgChangedTo = Component.literal("Multiplicative stat " + statName + " has been set to " + value + " to " + changedToString + ". ");


                                                                    Component msgNoCaps;

                                                                    if (!noCaps.isEmpty()) {
                                                                        msgNoCaps = Component.literal("Multiplicative stat " + statName + " couldn't be set to " + noCapsString + "to " + value + " because they didn't have the capabilities necessary to complete this action. ");
                                                                    } else {
                                                                        msgNoCaps = Component.literal("");
                                                                    }

                                                                    Component msgNotAlive;

                                                                    if (!notAlive.isEmpty()) {
                                                                        msgNotAlive = Component.literal("Multiplicative stat " + statName + " couldn't be set to " + notAliveString + "because they aren't alive. ");
                                                                    } else {
                                                                        msgNotAlive = Component.literal("");
                                                                    }

                                                                    Component finalComponent = Component.literal(msgChangedTo.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                                    source.sendSuccess(() -> finalComponent, true);


                                                                    return 1;
                                                                }
                                                            } catch (IllegalArgumentException e) {
                                                                source.sendFailure(Component.literal("Unknown stat: " + statName));
                                                                return 0;
                                                            }
                                                        })))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("stat_type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            StatType[] statList = StatType.values();

                                            for (StatType stat : Arrays.stream(statList).toList()) {
                                                builder.suggest(stat.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("entity", EntityArgument.entities())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String statName = StringArgumentType.getString(ctx, "stat_type");
                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                StatType stat = StatType.valueOf(statName.toUpperCase());
                                                                double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                List<Entity> changedTo = new ArrayList<>(List.of());
                                                                List<Entity> notAlive = new ArrayList<>(List.of());
                                                                List<Entity> noCaps = new ArrayList<>(List.of());

                                                                for (Entity entity : EntityArgument.getEntities(ctx, "entity")) {
                                                                    if (entity instanceof LivingEntity living) {
                                                                        LazyOptional<IMultStats> statsML = living.getCapability(ModCapabilities.MULT_STATS);
                                                                        LazyOptional<IDirtyStats> statsDL = living.getCapability(ModCapabilities.DIRTY_STATS);

                                                                        if (!statsDL.isPresent() || !statsML.isPresent()) {
                                                                            noCaps.add(living);
                                                                        } else {
                                                                            IMultStats statsM = statsML.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                                                                            IDirtyStats statsD = statsDL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                                                                            statsM.setMultStat(stat, value + statsM.getMultStat(stat));
                                                                            statsD.setDirtyStat(stat, true);

                                                                            changedTo.add(living);
                                                                        }
                                                                    } else {
                                                                        notAlive.add(entity);
                                                                    }
                                                                }

                                                                if (changedTo.isEmpty()) {
                                                                    source.sendFailure(Component.literal("Couldn't change stat because no entity was alive or had the necessary capabilities."));
                                                                    return 0;
                                                                } else {

                                                                    StringBuilder changedToString = new StringBuilder();
                                                                    for (Entity entity : changedTo) {
                                                                        changedToString.append(entity.getName().getString());
                                                                        changedToString.append(" ");
                                                                    }

                                                                    StringBuilder notAliveString = new StringBuilder();
                                                                    for (Entity entity : notAlive) {
                                                                        notAliveString.append(entity.getName().getString());
                                                                        notAliveString.append(" ");
                                                                    }

                                                                    StringBuilder noCapsString = new StringBuilder();
                                                                    for (Entity entity : noCaps) {
                                                                        noCapsString.append(entity.getName().getString());
                                                                        noCapsString.append(" ");
                                                                    }

                                                                    Component msgChangedTo = Component.literal("Multiplicative stat " + statName + " has been increased by " + value + " to " + changedToString + ". ");


                                                                    Component msgNoCaps;

                                                                    if (!noCaps.isEmpty()) {
                                                                        msgNoCaps = Component.literal("Multiplicative stat " + statName + " couldn't be increased to " + noCapsString + "by " + value + " because they didn't have the capabilities necessary to complete this action. ");
                                                                    } else {
                                                                        msgNoCaps = Component.literal("");
                                                                    }

                                                                    Component msgNotAlive;

                                                                    if (!notAlive.isEmpty()) {
                                                                        msgNotAlive = Component.literal("Multiplicative stat " + statName + " couldn't be increased to " + notAliveString + "because they aren't alive. ");
                                                                    } else {
                                                                        msgNotAlive = Component.literal("");
                                                                    }

                                                                    Component finalComponent = Component.literal(msgChangedTo.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                                    source.sendSuccess(() -> finalComponent, true);


                                                                    return 1;
                                                                }
                                                            } catch (IllegalArgumentException e) {
                                                                source.sendFailure(Component.literal("Unknown stat: " + statName));
                                                                return 0;
                                                            }
                                                        })))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("stat_type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            StatType[] statList = StatType.values();

                                            for (StatType stat : Arrays.stream(statList).toList()) {
                                                builder.suggest(stat.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("entity", EntityArgument.entities())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String statName = StringArgumentType.getString(ctx, "stat_type");
                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                StatType stat = StatType.valueOf(statName.toUpperCase());
                                                                double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                List<Entity> changedTo = new ArrayList<>(List.of());
                                                                List<Entity> notAlive = new ArrayList<>(List.of());
                                                                List<Entity> noCaps = new ArrayList<>(List.of());

                                                                for (Entity entity : EntityArgument.getEntities(ctx, "entity")) {
                                                                    if (entity instanceof LivingEntity living) {
                                                                        LazyOptional<IMultStats> statsML = living.getCapability(ModCapabilities.MULT_STATS);
                                                                        LazyOptional<IDirtyStats> statsDL = living.getCapability(ModCapabilities.DIRTY_STATS);

                                                                        if (!statsDL.isPresent() || !statsML.isPresent()) {
                                                                            noCaps.add(living);
                                                                        } else {
                                                                            IMultStats statsM = statsML.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                                                                            IDirtyStats statsD = statsDL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                                                                            statsM.setMultStat(stat, statsM.getMultStat(stat) - value);
                                                                            statsD.setDirtyStat(stat, true);

                                                                            changedTo.add(living);
                                                                        }
                                                                    } else {
                                                                        notAlive.add(entity);
                                                                    }
                                                                }

                                                                if (changedTo.isEmpty()) {
                                                                    source.sendFailure(Component.literal("Couldn't change stat because no entity was alive or had the necessary capabilities."));
                                                                    return 0;
                                                                } else {

                                                                    StringBuilder changedToString = new StringBuilder();
                                                                    for (Entity entity : changedTo) {
                                                                        changedToString.append(entity.getName().getString());
                                                                        changedToString.append(" ");
                                                                    }

                                                                    StringBuilder notAliveString = new StringBuilder();
                                                                    for (Entity entity : notAlive) {
                                                                        notAliveString.append(entity.getName().getString());
                                                                        notAliveString.append(" ");
                                                                    }

                                                                    StringBuilder noCapsString = new StringBuilder();
                                                                    for (Entity entity : noCaps) {
                                                                        noCapsString.append(entity.getName().getString());
                                                                        noCapsString.append(" ");
                                                                    }

                                                                    Component msgChangedTo = Component.literal("Multiplicative stat " + statName + " has been decreased by " + value + " to " + changedToString + ". ");


                                                                    Component msgNoCaps;

                                                                    if (!noCaps.isEmpty()) {
                                                                        msgNoCaps = Component.literal("Multiplicative stat " + statName + " couldn't be decreased to " + noCapsString + "by " + value + " because they didn't have the capabilities necessary to complete this action. ");
                                                                    } else {
                                                                        msgNoCaps = Component.literal("");
                                                                    }

                                                                    Component msgNotAlive;

                                                                    if (!notAlive.isEmpty()) {
                                                                        msgNotAlive = Component.literal("Multiplicative stat " + statName + " couldn't be decreased to " + notAliveString + "because they aren't alive. ");
                                                                    } else {
                                                                        msgNotAlive = Component.literal("");
                                                                    }

                                                                    Component finalComponent = Component.literal(msgChangedTo.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                                    source.sendSuccess(() -> finalComponent, true);


                                                                    return 1;
                                                                }
                                                            } catch (IllegalArgumentException e) {
                                                                source.sendFailure(Component.literal("Unknown stat: " + statName));
                                                                return 0;
                                                            }
                                                        }))))))
                .then(Commands.literal("additive")
                        .then(Commands.literal("set")
                                .then(Commands.argument("stat_type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            StatType[] statList = StatType.values();

                                            for (StatType stat : Arrays.stream(statList).toList()) {
                                                builder.suggest(stat.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("entity", EntityArgument.entities())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String statName = StringArgumentType.getString(ctx, "stat_type");
                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                StatType stat = StatType.valueOf(statName.toUpperCase());
                                                                double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                List<Entity> changedTo = new ArrayList<>(List.of());
                                                                List<Entity> notAlive = new ArrayList<>(List.of());
                                                                List<Entity> noCaps = new ArrayList<>(List.of());

                                                                for (Entity entity : EntityArgument.getEntities(ctx, "entity")) {
                                                                    if (entity instanceof LivingEntity living) {
                                                                        LazyOptional<IAddStats> statsAL = living.getCapability(ModCapabilities.ADD_STATS);
                                                                        LazyOptional<IDirtyStats> statsDL = living.getCapability(ModCapabilities.DIRTY_STATS);

                                                                        if (!statsDL.isPresent() || !statsAL.isPresent()) {
                                                                            noCaps.add(living);
                                                                        } else {
                                                                            IAddStats statsA = statsAL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                                                                            IDirtyStats statsD = statsDL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                                                                            statsA.setAddStat(stat, value);
                                                                            statsD.setDirtyStat(stat, true);

                                                                            changedTo.add(living);
                                                                        }
                                                                    } else {
                                                                        notAlive.add(entity);
                                                                    }
                                                                }

                                                                if (changedTo.isEmpty()) {
                                                                    source.sendFailure(Component.literal("Couldn't change stat because no entity was alive or had the necessary capabilities."));
                                                                    return 0;
                                                                } else {

                                                                    StringBuilder changedToString = new StringBuilder();
                                                                    for (Entity entity : changedTo) {
                                                                        changedToString.append(entity.getName().getString());
                                                                        changedToString.append(" ");
                                                                    }

                                                                    StringBuilder notAliveString = new StringBuilder();
                                                                    for (Entity entity : notAlive) {
                                                                        notAliveString.append(entity.getName().getString());
                                                                        notAliveString.append(" ");
                                                                    }

                                                                    StringBuilder noCapsString = new StringBuilder();
                                                                    for (Entity entity : noCaps) {
                                                                        noCapsString.append(entity.getName().getString());
                                                                        noCapsString.append(" ");
                                                                    }

                                                                    Component msgChangedTo = Component.literal("Additive stat " + statName + " has been set to " + value + " to " + changedToString + ". ");


                                                                    Component msgNoCaps;

                                                                    if (!noCaps.isEmpty()) {
                                                                        msgNoCaps = Component.literal("Additive stat " + statName + " couldn't be set to " + noCapsString + "to " + value + " because they didn't have the capabilities necessary to complete this action. ");
                                                                    } else {
                                                                        msgNoCaps = Component.literal("");
                                                                    }

                                                                    Component msgNotAlive;

                                                                    if (!notAlive.isEmpty()) {
                                                                        msgNotAlive = Component.literal("Additive stat " + statName + " couldn't be set to " + notAliveString + "because they aren't alive. ");
                                                                    } else {
                                                                        msgNotAlive = Component.literal("");
                                                                    }

                                                                    Component finalComponent = Component.literal(msgChangedTo.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                                    source.sendSuccess(() -> finalComponent, true);


                                                                    return 1;
                                                                }
                                                            } catch (IllegalArgumentException e) {
                                                                source.sendFailure(Component.literal("Unknown stat: " + statName));
                                                                return 0;
                                                            }
                                                        })))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("stat_type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            StatType[] statList = StatType.values();

                                            for (StatType stat : Arrays.stream(statList).toList()) {
                                                builder.suggest(stat.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("entity", EntityArgument.entities())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String statName = StringArgumentType.getString(ctx, "stat_type");
                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                StatType stat = StatType.valueOf(statName.toUpperCase());
                                                                double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                List<Entity> changedTo = new ArrayList<>(List.of());
                                                                List<Entity> notAlive = new ArrayList<>(List.of());
                                                                List<Entity> noCaps = new ArrayList<>(List.of());

                                                                for (Entity entity : EntityArgument.getEntities(ctx, "entity")) {
                                                                    if (entity instanceof LivingEntity living) {
                                                                        LazyOptional<IAddStats> statsAL = living.getCapability(ModCapabilities.ADD_STATS);
                                                                        LazyOptional<IDirtyStats> statsDL = living.getCapability(ModCapabilities.DIRTY_STATS);

                                                                        if (!statsDL.isPresent() || !statsAL.isPresent()) {
                                                                            noCaps.add(living);
                                                                        } else {
                                                                            IAddStats statsA = statsAL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                                                                            IDirtyStats statsD = statsDL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                                                                            statsA.setAddStat(stat, value + statsA.getAddStat(stat));
                                                                            statsD.setDirtyStat(stat, true);

                                                                            changedTo.add(living);
                                                                        }
                                                                    } else {
                                                                        notAlive.add(entity);
                                                                    }
                                                                }

                                                                if (changedTo.isEmpty()) {
                                                                    source.sendFailure(Component.literal("Couldn't change stat because no entity was alive or had the necessary capabilities."));
                                                                    return 0;
                                                                } else {

                                                                    StringBuilder changedToString = new StringBuilder();
                                                                    for (Entity entity : changedTo) {
                                                                        changedToString.append(entity.getName().getString());
                                                                        changedToString.append(" ");
                                                                    }

                                                                    StringBuilder notAliveString = new StringBuilder();
                                                                    for (Entity entity : notAlive) {
                                                                        notAliveString.append(entity.getName().getString());
                                                                        notAliveString.append(" ");
                                                                    }

                                                                    StringBuilder noCapsString = new StringBuilder();
                                                                    for (Entity entity : noCaps) {
                                                                        noCapsString.append(entity.getName().getString());
                                                                        noCapsString.append(" ");
                                                                    }

                                                                    Component msgChangedTo = Component.literal("Additive stat " + statName + " has been increased by " + value + " to " + changedToString + ". ");


                                                                    Component msgNoCaps;

                                                                    if (!noCaps.isEmpty()) {
                                                                        msgNoCaps = Component.literal("Additive stat " + statName + " couldn't be increased to " + noCapsString + "by " + value + " because they didn't have the capabilities necessary to complete this action. ");
                                                                    } else {
                                                                        msgNoCaps = Component.literal("");
                                                                    }

                                                                    Component msgNotAlive;

                                                                    if (!notAlive.isEmpty()) {
                                                                        msgNotAlive = Component.literal("Additive stat " + statName + " couldn't be increased to " + notAliveString + "because they aren't alive. ");
                                                                    } else {
                                                                        msgNotAlive = Component.literal("");
                                                                    }

                                                                    Component finalComponent = Component.literal(msgChangedTo.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                                    source.sendSuccess(() -> finalComponent, true);


                                                                    return 1;
                                                                }
                                                            } catch (IllegalArgumentException e) {
                                                                source.sendFailure(Component.literal("Unknown stat: " + statName));
                                                                return 0;
                                                            }
                                                        })))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("stat_type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            StatType[] statList = StatType.values();

                                            for (StatType stat : Arrays.stream(statList).toList()) {
                                                builder.suggest(stat.name().toLowerCase());
                                            }
                                            return builder.buildFuture();
                                        })
                                        .then(Commands.argument("entity", EntityArgument.entities())
                                                .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ctx -> {
                                                            String statName = StringArgumentType.getString(ctx, "stat_type");
                                                            CommandSourceStack source = ctx.getSource();
                                                            try {
                                                                StatType stat = StatType.valueOf(statName.toUpperCase());
                                                                double value = DoubleArgumentType.getDouble(ctx, "value");

                                                                List<Entity> changedTo = new ArrayList<>(List.of());
                                                                List<Entity> notAlive = new ArrayList<>(List.of());
                                                                List<Entity> noCaps = new ArrayList<>(List.of());

                                                                for (Entity entity : EntityArgument.getEntities(ctx, "entity")) {
                                                                    if (entity instanceof LivingEntity living) {
                                                                        LazyOptional<IAddStats> statsAL = living.getCapability(ModCapabilities.ADD_STATS);
                                                                        LazyOptional<IDirtyStats> statsDL = living.getCapability(ModCapabilities.DIRTY_STATS);

                                                                        if (!statsDL.isPresent() || !statsAL.isPresent()) {
                                                                            noCaps.add(living);
                                                                        } else {
                                                                            IAddStats statsA = statsAL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));
                                                                            IDirtyStats statsD = statsDL.orElseThrow(() -> new IllegalStateException("Failed an Impossible-to-Fail Capability Check"));

                                                                            statsA.setAddStat(stat, statsA.getAddStat(stat) - value);
                                                                            statsD.setDirtyStat(stat, true);

                                                                            changedTo.add(living);
                                                                        }
                                                                    } else {
                                                                        notAlive.add(entity);
                                                                    }
                                                                }

                                                                if (changedTo.isEmpty()) {
                                                                    source.sendFailure(Component.literal("Couldn't change stat because no entity was alive or had the necessary capabilities."));
                                                                    return 0;
                                                                } else {

                                                                    StringBuilder changedToString = new StringBuilder();
                                                                    for (Entity entity : changedTo) {
                                                                        changedToString.append(entity.getName().getString());
                                                                        changedToString.append(" ");
                                                                    }

                                                                    StringBuilder notAliveString = new StringBuilder();
                                                                    for (Entity entity : notAlive) {
                                                                        notAliveString.append(entity.getName().getString());
                                                                        notAliveString.append(" ");
                                                                    }

                                                                    StringBuilder noCapsString = new StringBuilder();
                                                                    for (Entity entity : noCaps) {
                                                                        noCapsString.append(entity.getName().getString());
                                                                        noCapsString.append(" ");
                                                                    }

                                                                    Component msgChangedTo = Component.literal("Additive stat " + statName + " has been decreased by " + value + " to " + changedToString + ". ");


                                                                    Component msgNoCaps;

                                                                    if (!noCaps.isEmpty()) {
                                                                        msgNoCaps = Component.literal("Additive stat " + statName + " couldn't be decreased to " + noCapsString + "by " + value + " because they didn't have the capabilities necessary to complete this action. ");
                                                                    } else {
                                                                        msgNoCaps = Component.literal("");
                                                                    }

                                                                    Component msgNotAlive;

                                                                    if (!notAlive.isEmpty()) {
                                                                        msgNotAlive = Component.literal("Additive stat " + statName + " couldn't be decreased to " + notAliveString + "because they aren't alive. ");
                                                                    } else {
                                                                        msgNotAlive = Component.literal("");
                                                                    }

                                                                    Component finalComponent = Component.literal(msgChangedTo.getString() + msgNotAlive.getString() + msgNoCaps.getString());
                                                                    source.sendSuccess(() -> finalComponent, true);


                                                                    return 1;
                                                                }
                                                            } catch (IllegalArgumentException e) {
                                                                source.sendFailure(Component.literal("Unknown stat: " + statName));
                                                                return 0;
                                                            }
                                                        })))))));
        dispatcher.register(Commands.literal("mana")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("restore")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> result = new ArrayList<>(List.of());
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                if (entity instanceof LivingEntity living) {
                                                    MinecraftForge.EVENT_BUS.post(new ManaRestorationEvent(living, amount));
                                                    result.add(living);
                                                }
                                            }
                                            if (result.isEmpty()) {
                                                source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                return 0;
                                            } else {
                                                StringBuilder text = new StringBuilder();
                                                for (Entity entity : result) {
                                                    text.append(entity.getName().getString());
                                                    text.append(" ");
                                                }
                                                source.sendSuccess(() -> Component.literal("Restored mana of " + text + "by " + amount), true);
                                                return 1;
                                            }
                                        }))))
                .then(Commands.literal("consume")
                        .then(Commands.argument("target", EntityArgument.entities())
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> result = new ArrayList<>(List.of());
                                            double amount = DoubleArgumentType.getDouble(ctx, "amount");
                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                if (entity instanceof LivingEntity living) {
                                                    MinecraftForge.EVENT_BUS.post(new ManaConsumptionEvent(living, amount));
                                                    result.add(living);
                                                }
                                            }
                                            if (result.isEmpty()) {
                                                source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                return 0;
                                            } else {
                                                StringBuilder text = new StringBuilder();
                                                for (Entity entity : result) {
                                                    text.append(entity.getName().getString());
                                                    text.append(" ");
                                                }
                                                source.sendSuccess(() -> Component.literal("Consumed mana of " + text + "by " + amount), true);
                                                return 1;
                                            }
                                        })))));
        dispatcher.register(Commands.literal("effect")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("core")
                        .then(Commands.literal("give")
                                .then(Commands.argument("target", EntityArgument.entities())
                                        .then(Commands.argument("effect", ResourceLocationArgument.id())
                                                .suggests((ctx, builder) -> {
                                                    BuiltInRegistries.MOB_EFFECT.entrySet().forEach(entry -> {
                                                        BuiltInRegistries.MOB_EFFECT.getHolder(entry.getKey()).ifPresent(holder -> {
                                                            if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                                                builder.suggest(entry.getKey().location().toString());
                                                            }
                                                        });
                                                    });
                                                    return builder.buildFuture();
                                                })
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    List<Entity> result = new ArrayList<>(List.of());
                                                    ResourceLocation resourceLocation = ResourceLocationArgument.getId(ctx, "effect");
                                                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(resourceLocation);
                                                    if (effect == null) {
                                                        source.sendFailure(Component.literal("Effect does not exist."));
                                                        return 0;
                                                    } else {
                                                        Optional<Holder.Reference<MobEffect>> holderOptional = BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation);
                                                        if (holderOptional.isPresent()) {
                                                            Holder<MobEffect> holder = holderOptional.get();
                                                            for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                                if (entity instanceof LivingEntity living && (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI))) {
                                                                    MinecraftForge.EVENT_BUS.post(new EffectApplicationEvent(null, living, effect, 600, 1, 1, 1, 1, true));
                                                                    result.add(living);
                                                                }
                                                            }
                                                            if (result.isEmpty()) {
                                                                source.sendFailure(Component.literal("No alive target among the selected ones or the effect is not supported by the core."));
                                                                return 0;
                                                            } else {
                                                                StringBuilder text = new StringBuilder();
                                                                for (Entity entity : result) {
                                                                    text.append(entity.getName().getString());
                                                                    text.append(" ");
                                                                }
                                                                source.sendSuccess(() -> Component.literal("Applied the " + effect.getDisplayName().getString() + " effect to " + text + "for 30 seconds with amplifier equal to 1 (stacks = 1; max stacks = 1 )."), true);
                                                                return 1;
                                                            }

                                                        } else {
                                                            source.sendFailure(Component.literal("Effect not initialised."));
                                                            return 0;
                                                        }
                                                    }
                                                })
                                                .then(Commands.argument("duration", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("amplifier", DoubleArgumentType.doubleArg())
                                                                .then(Commands.argument("stacks", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("maxStacks", IntegerArgumentType.integer())
                                                                                .executes(ctx -> {
                                                                                    CommandSourceStack source = ctx.getSource();
                                                                                    List<Entity> result = new ArrayList<>(List.of());
                                                                                    int duration = (int) (DoubleArgumentType.getDouble(ctx, "duration") * 20);
                                                                                    double amplifier = DoubleArgumentType.getDouble(ctx, "amplifier");
                                                                                    ResourceLocation resourceLocation = ResourceLocationArgument.getId(ctx, "effect");
                                                                                    int maxStacks = IntegerArgumentType.getInteger(ctx, "maxStacks");
                                                                                    int stacks = Math.min(maxStacks, IntegerArgumentType.getInteger(ctx, "stacks"));
                                                                                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(resourceLocation);
                                                                                    if (effect == null) {
                                                                                        source.sendFailure(Component.literal("Effect does not exist."));
                                                                                        return 0;
                                                                                    } else {
                                                                                        Optional<Holder.Reference<MobEffect>> holderOptional = BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation);
                                                                                        if (holderOptional.isPresent()) {
                                                                                            Holder<MobEffect> holder = holderOptional.get();
                                                                                                for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                                                                    if (entity instanceof LivingEntity living && (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI))) {
                                                                                                        MinecraftForge.EVENT_BUS.post(new EffectApplicationEvent(null, living, effect, duration, 1, amplifier, stacks, maxStacks, true));
                                                                                                        result.add(living);
                                                                                                    }
                                                                                                }
                                                                                            if (result.isEmpty()) {
                                                                                                source.sendFailure(Component.literal("No alive target among the selected ones or the effect is not supported by the core."));
                                                                                                return 0;
                                                                                            } else {
                                                                                                StringBuilder text = new StringBuilder();
                                                                                                for (Entity entity : result) {
                                                                                                    text.append(entity.getName().getString());
                                                                                                    text.append(" ");
                                                                                                }
                                                                                                source.sendSuccess(() -> Component.literal("Applied the " + effect.getDisplayName().getString() + " effect to " + text + "for " + duration / 20 + " seconds with amplifier equal to " + amplifier + " (stacks = " + stacks + "; max stacks = " + maxStacks + ")."), true);
                                                                                                return 1;
                                                                                            }

                                                                                        } else {
                                                                                            source.sendFailure(Component.literal("Effect not initialised."));
                                                                                            return 0;
                                                                                        }
                                                                                    }
                                                                                })))))
                                                .then(Commands.literal("infinite")
                                                        .then(Commands.argument("amplifier", DoubleArgumentType.doubleArg())
                                                                .then(Commands.argument("stacks", IntegerArgumentType.integer())
                                                                        .then(Commands.argument("maxStacks", IntegerArgumentType.integer())
                                                                                .executes(ctx -> {
                                                                                    CommandSourceStack source = ctx.getSource();
                                                                                    System.out.println("Trying to apply an effect with infinite duration");
                                                                                    List<Entity> result = new ArrayList<>(List.of());
                                                                                    double amplifier = DoubleArgumentType.getDouble(ctx, "amplifier");
                                                                                    ResourceLocation resourceLocation = ResourceLocationArgument.getId(ctx, "effect");
                                                                                    int maxStacks = IntegerArgumentType.getInteger(ctx, "maxStacks");
                                                                                    int stacks = Math.min(maxStacks, IntegerArgumentType.getInteger(ctx, "stacks"));
                                                                                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(resourceLocation);
                                                                                    if (effect == null) {
                                                                                        System.out.println("Effect does not exist");
                                                                                        source.sendFailure(Component.literal("Effect does not exist."));
                                                                                        return 0;
                                                                                    } else {
                                                                                        Optional<Holder.Reference<MobEffect>> holderOptional = BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation);
                                                                                        if (holderOptional.isPresent()) {
                                                                                            System.out.println("Effect has been successfully initialised.");
                                                                                            Holder<MobEffect> holder = holderOptional.get();
                                                                                            if ((holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI))) {
                                                                                                System.out.println("Effect is supported");
                                                                                                for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                                                                        if (entity instanceof LivingEntity living) {
                                                                                                            MinecraftForge.EVENT_BUS.post(new EffectApplicationEvent(null, living, effect, Integer.MAX_VALUE, 1, amplifier, stacks, maxStacks, true));
                                                                                                            result.add(living);
                                                                                                        }
                                                                                                    }
                                                                                                if (result.isEmpty()) {
                                                                                                    System.out.println("No alive targets among the selected ones");
                                                                                                    source.sendFailure(Component.literal("No alive target among the selected ones."));
                                                                                                    return 0;
                                                                                                } else {
                                                                                                    System.out.println("Effect applied successfully");
                                                                                                    StringBuilder text = new StringBuilder();
                                                                                                    for (Entity entity : result) {
                                                                                                        text.append(entity.getName().getString());
                                                                                                        text.append(" ");
                                                                                                    }
                                                                                                    source.sendSuccess(() -> Component.literal("Applied the " + effect.getDisplayName().getString() + " effect to " + text + "with infinite duration and with amplifier equal to " + amplifier + " (stacks = " + stacks + "; max stacks = " + maxStacks + ")."), true);
                                                                                                    return 1;
                                                                                                }

                                                                                            } else {
                                                                                                System.out.println("Effect is not supported");
                                                                                                source.sendFailure(Component.literal("Effect is not supported by the core."));
                                                                                                return 0;
                                                                                            }
                                                                                        } else {
                                                                                            System.out.println("Effect not yet initialised");
                                                                                            source.sendFailure(Component.literal("Effect has not yet been initialised."));
                                                                                            return 0;
                                                                                        }
                                                                                    }
                                                                                }))))))))
                .then(Commands.literal("clear")
                        .executes(ctx -> {
                            CommandSourceStack source = ctx.getSource();
                            Entity sender = source.getEntity();
                            if (sender instanceof LivingEntity player && player.getCapability(ModCapabilities.INDEPENDENT_STATS).isPresent()) {
                                player.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                                    statsI.removeAllActiveEffects(player);
                                });
                                source.sendSuccess(() -> Component.literal("Cleared all effects from " + player.getName().getString()), true);
                                return 1;
                            } else {
                                source.sendFailure(Component.literal("Targeted entity is not alive or does not have the necessary capability"));
                                return 0;
                            }
                        })
                        .then(Commands.argument("target", EntityArgument.entities())
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    List<Entity> list = new ArrayList<>(List.of());
                                    for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                        if (entity instanceof LivingEntity living && living.getCapability(ModCapabilities.INDEPENDENT_STATS).isPresent()) {
                                            living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                                                statsI.removeAllActiveEffects(living);
                                                list.add(living);
                                            });
                                        }
                                    }

                                    if (list.isEmpty()) {
                                        source.sendFailure(Component.literal("No target among the selected ones is alive or has the necessary capability."));
                                        return 0;
                                    } else {
                                        StringBuilder text = new StringBuilder();
                                        for (Entity entity : list) {
                                            text.append(entity.getName().getString());
                                            text.append(" ");
                                        }

                                        source.sendSuccess(() -> Component.literal("Removed all effects from " + text + "."), true);
                                        return 1;
                                    }
                                })
                                .then(Commands.argument("effect", ResourceLocationArgument.id())
                                        .suggests((ctx, builder) -> {
                                            BuiltInRegistries.MOB_EFFECT.entrySet().forEach(entry -> {
                                                BuiltInRegistries.MOB_EFFECT.getHolder(entry.getKey()).ifPresent(holder -> {
                                                    if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                                        builder.suggest(entry.getKey().location().toString());
                                                    }
                                                });
                                            });
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            CommandSourceStack source = ctx.getSource();
                                            List<Entity> list = new ArrayList<>(List.of());
                                            ResourceLocation resourceLocation = ResourceLocationArgument.getId(ctx, "effect");
                                            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(resourceLocation);
                                            if (effect == null) {
                                                source.sendFailure(Component.literal("Effect does not exist."));
                                                return 0;
                                            } else {
                                                Optional<Holder.Reference<MobEffect>> holderOptional = BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation);
                                                if (holderOptional.isPresent()) {
                                                    Holder<MobEffect> holder = holderOptional.get();
                                                    if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                                        for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                            if (entity instanceof LivingEntity living && living.getCapability(ModCapabilities.INDEPENDENT_STATS).isPresent()) {
                                                                living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                                                                    statsI.removeActiveEffect(effect, living);
                                                                    list.add(living);
                                                                });
                                                            }
                                                        }

                                                        if (list.isEmpty()) {
                                                            source.sendFailure(Component.literal("No target among the selected ones is alive or has the necessary capability."));
                                                            return 0;
                                                        } else {
                                                            StringBuilder text = new StringBuilder();
                                                            for (Entity entity : list) {
                                                                text.append(entity.getName().getString());
                                                                text.append(" ");
                                                            }

                                                            source.sendSuccess(() -> Component.literal("Removed the " + effect.getDisplayName().getString() + " effect from " + text + "."), true);
                                                            return 1;
                                                        }
                                                    } else {
                                                        source.sendFailure(Component.literal("The effect is not supported by the core."));
                                                        return 0;
                                                    }
                                                } else {
                                                    source.sendFailure(Component.literal("The effect has not yet been initialised."));
                                                    return 0;
                                                }
                                            }
                                        })
                                        .then(Commands.argument("hasConsequences", BoolArgumentType.bool())
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    List<Entity> list = new ArrayList<>(List.of());
                                                    ResourceLocation resourceLocation = ResourceLocationArgument.getId(ctx, "effect");
                                                    MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(resourceLocation);
                                                    boolean hasConsequences = BoolArgumentType.getBool(ctx, "hasConsequences");
                                                    if (effect == null) {
                                                        source.sendFailure(Component.literal("Effect does not exist."));
                                                        return 0;
                                                    } else {
                                                        Optional<Holder.Reference<MobEffect>> holderOptional = BuiltInRegistries.MOB_EFFECT.getHolder(resourceLocation);
                                                        if (holderOptional.isPresent()) {
                                                            Holder<MobEffect> holder = holderOptional.get();
                                                            if (holder.is(ModTags.DEBUFFS) || holder.is(ModTags.BUFFS) || holder.is(ModTags.NEUTRAL_EFFECTS) || holder.is(ModTags.UNDISPELLABLE_EFFECTS) || holder.is(ModTags.EFFECTS_NOT_SHOWN_IN_GUI)) {
                                                                for (Entity entity : EntityArgument.getEntities(ctx, "target")) {
                                                                    if (entity instanceof LivingEntity living && living.getCapability(ModCapabilities.INDEPENDENT_STATS).isPresent()) {
                                                                        living.getCapability(ModCapabilities.INDEPENDENT_STATS).ifPresent(statsI -> {
                                                                            if (hasConsequences) {
                                                                                statsI.removeActiveEffect(effect, living);
                                                                            } else {
                                                                                statsI.removeActiveEffectWithoutConsequences(effect, living);
                                                                            }
                                                                            list.add(living);
                                                                        });
                                                                    }
                                                                }

                                                                if (list.isEmpty()) {
                                                                    source.sendFailure(Component.literal("No target among the selected ones is alive or has the necessary capability."));
                                                                    return 0;
                                                                } else {
                                                                    StringBuilder text = new StringBuilder();
                                                                    for (Entity entity : list) {
                                                                        text.append(entity.getName().getString());
                                                                        text.append(" ");
                                                                    }

                                                                    source.sendSuccess(() -> Component.literal("Removed the " + effect.getDisplayName().getString() + " effect from " + text + "."), true);
                                                                    return 1;
                                                                }
                                                            } else {
                                                                source.sendFailure(Component.literal("The effect is not supported by the core."));
                                                                return 0;
                                                            }
                                                        } else {
                                                            source.sendFailure(Component.literal("The effect has not yet been initialised."));
                                                            return 0;
                                                        }
                                                    }
                                                })))))));
    }
}

