package com.w1therx.adventurerfantasy.common.enums;

import com.w1therx.adventurerfantasy.effect.ModEffects;
import net.minecraft.server.level.ServerPlayer;

public enum DeathMessageByEntity {
    a ("$player was slain by $killer."),
    b ("$player's incompetence was put on display by $killer."),
    c ("$killer made $player's innards outards."),
    d ("$killer fulfilled $player's death wish."),
    e ("$killer outshone $player in combat"),
    f ("$killer sent $player to Anastaphorus's realm."),
    g ("$killer showed $player no mercy."),
    h ("$killer helped $player understand the meaning of 'being a mortal being'."),
    i ("$killer rearranged $player's body structure."),
    j ("$player's recklessness was stopped by $killer");

    private final String string;

    DeathMessageByEntity(String string) {
        this.string = string;
    }

    public String getMessage() {
        return string;
    }
}
