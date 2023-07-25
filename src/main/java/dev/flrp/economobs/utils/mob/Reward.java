package dev.flrp.economobs.utils.mob;

import java.util.HashMap;
import java.util.Map;

public class Reward {

    private double total;
    private final HashMap<Double, Double> dropList = new HashMap<>();

    public void setTotal(double total) {
        this.total = total;
    }

    public HashMap<Double, Double> getDropList() {
        return dropList;
    }

    public double getTotal() {
        return total;
    }

    /**
     * A method that calculates what amount a mob death would give to the player.
     * It's more distributions due to the function not being limited to 100 total percentage.
     * Totals under 100 will have the roll generated between 0-100 to let drops not be given.
     */
    public double calculateReward() {
        double roll;
        if(total < 100) {
            roll = Math.random() * 100;
        } else {
            roll = Math.random() * total;
        }

        for(Map.Entry<Double, Double> entry : dropList.entrySet()) {
            if(roll <= entry.getValue()) {
                return entry.getKey();
            } else {
                roll = roll - entry.getValue();
            }
        }
        return 0;
    }

}
