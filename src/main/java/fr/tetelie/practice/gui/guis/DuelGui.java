package fr.tetelie.practice.gui.guis;

import fr.tetelie.practice.Practice;
import fr.tetelie.practice.gui.Gui;
import fr.tetelie.practice.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

public class DuelGui implements Gui {

    @Override
    public Inventory inventory() {
        Inventory inv = Bukkit.createInventory(null, 18, "§6Duel");
        Practice.getInstance().ladders.forEach(ladder -> inv.addItem(new ItemBuilder(ladder.material(), 1, ladder.data()).setName(ladder.displayName()).toItemStack()));
        return inv;
    }

}
