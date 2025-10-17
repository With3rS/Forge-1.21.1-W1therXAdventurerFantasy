package com.w1therx.adventurerfantasy.capability;

public class AggroData {
    private double aggroValue;
    private double attackedCount;

    public AggroData(double aggroValue, double attackedCount) {
        this.aggroValue = aggroValue;
        this.attackedCount = attackedCount;
    }

    public double getAggroValue() {
        return aggroValue;
    }

    public void setAggroValue(double aggroValue) {
        this.aggroValue = aggroValue;
    }

    public double getAttackedCount() {
        return attackedCount;
    }

    public void setAttackedCount(double attackedCount) {
        this.attackedCount = attackedCount;
    }
}
