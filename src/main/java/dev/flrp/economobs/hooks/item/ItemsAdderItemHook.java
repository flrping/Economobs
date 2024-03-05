package dev.flrp.economobs.hooks.item;

import dev.flrp.economobs.Economobs;
import dev.flrp.espresso.configuration.Configuration;
import dev.flrp.espresso.hook.item.ItemsAdderItemProvider;

import java.util.ArrayList;
import java.util.Collections;

public class ItemsAdderItemHook extends ItemsAdderItemProvider {

    Configuration iaItemConfig;

    public ItemsAdderItemHook(Economobs plugin) {
        iaItemConfig = new Configuration(plugin, "hooks/ItemsAdder");
        iaItemConfig.load();
    }

    public void build() {

        if(!iaItemConfig.exists("items")) {
            iaItemConfig.getConfiguration().set("multipliers.example.items", new ArrayList<>(Collections.singletonList("emerald_sword 1.2")));
            iaItemConfig.save();
        }

    }

}
