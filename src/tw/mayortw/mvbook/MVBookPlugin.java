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
import com.onarandombox.MultiverseCore.utils.PermissionTools;

public class MVBookPlugin extends JavaPlugin {

    MVWorldManager worldManager;
    PermissionTools permTool;

    @Override
    public void onEnable() {
        MVPlugin mvPlugin = (MVPlugin) getServer().getPluginManager().getPlugin("Multiverse-Core");

        if(mvPlugin == null) {
            getLogger().log(java.util.logging.Level.SEVERE, "Multiverse-Core not found or not enabled");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        worldManager = mvPlugin.getCore().getMVWorldManager();
        permTool = new PermissionTools(mvPlugin.getCore());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!cmd.getName().equals("mvbook")) return true;
        if (sender instanceof Player) {
            List<String> pages = new ArrayList<String>();
            LinkedList<String> lines = new LinkedList<String>();

            for(MultiverseWorld world : worldManager.getMVWorlds()) {
                if(!checkPermission((Player) sender, world)) continue;

                String display = world.getAlias();
                if(display == null || display == "")
                    display = world.getName();
                display = display.replace("\\", "\\\\");
                display = display.replace("\"", "\\\"");

                lines.add(String.format("{\"text\":\"%s\\n\",\"color\":\"black\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mvtp %s\"}}", display, world.getName()));
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
            BookUtil.setPages(meta, pages);
            book.setItemMeta(meta);
            BookUtil.openBook(book, (Player) sender);

        } else {
            sender.sendMessage("Only player can do this");
        }
        return true;
    }

    private boolean checkPermission(Player player, MultiverseWorld world) {
        String name = world.getName();
        return (player.hasPermission("multiverse.teleport.*") || player.hasPermission("multiverse.teleport." + name)) &&
            permTool.playerCanGoFromTo(worldManager.getMVWorld(player.getLocation().getWorld()), world, null, player) &&
            player.hasPermission("multiverse.teleport.self." + name);
    }
}
