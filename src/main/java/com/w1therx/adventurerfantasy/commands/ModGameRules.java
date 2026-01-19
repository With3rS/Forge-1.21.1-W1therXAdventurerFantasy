package com.w1therx.adventurerfantasy.commands;

import net.minecraft.world.level.GameRules;

public class ModGameRules {

    public static void register() {
    }

    public static final GameRules.Key<GameRules.BooleanValue> SHOW_CUSTOM_DEATH_MESSAGES = GameRules.register("showCustomDeathMessages", GameRules.Category.CHAT, GameRules.BooleanValue.create(true));

}
