package tw.mayortw.mvbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.api.MVWorldManager;

public class MVBookPlugin extends JavaPlugin {

    MVWorldManager worldManager;

    @Override
    public void onEnable() {
        MVPlugin mvPlugin = (MVPlugin) getServer().getPluginManager().getPlugin("Multiverse-Core");

        if(mvPlugin == null) {
            getLogger().log(java.util.logging.Level.SEVERE, "Multiverse-Core not found or not enabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        worldManager = mvPlugin.getCore().getMVWorldManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!cmd.getName().equals("mvbook")) return true;
        if (sender instanceof Player) {
            List<String> pages = new ArrayList<String>();
            LinkedList<String> lines = new LinkedList<String>();

            for(MultiverseWorld world : worldManager.getMVWorlds()) {
                String name = world.getName();
                if(!sender.hasPermission("multiverse.teleport.*") && !sender.hasPermission("multiverse.teleport." + name)) continue;
                lines.add("{\"text\":\"" + name + "\\n\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mvtp " + name + "\"}}");
            }
            Collections.sort(lines, new NaturalOrderComparator());

            while(lines.size() > 0) {
                String page = "[\"\",";
                for(int i = 0; i < 14 && lines.size() > 0; i++) { // max 14 lines per page
                    page += lines.removeFirst() + ",";
                }
                page = page.substring(0, page.length()-1) + "]";
                pages.add(page);
            }

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle("Worlds");
            meta.setAuthor("");
            BookUtil.setPages(meta, pages);
            book.setItemMeta(meta);
            BookUtil.openBook(book, (Player) sender);

        } else {
            sender.sendMessage("Only player can do this");
        }
        return true;
    }
}
