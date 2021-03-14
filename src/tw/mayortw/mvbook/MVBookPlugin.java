package tw.mayortw.mvbook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

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
            LinkedList<TextComponent> lines = new LinkedList<>();

            for(MultiverseWorld world : worldManager.getMVWorlds()) {
                if(!checkPermission((Player) sender, world)) continue;

                String name = world.getName();
                String alias = world.getAlias();

                final int maxLength = 12;
                if(alias.length() > maxLength) {
                    alias = alias.substring(0, maxLength - 6) + "…" + alias.substring(alias.length() - 6);
                }

                TextComponent line = new TextComponent(alias + "\n");
                line.setColor(ChatColor.BLACK);
                line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mvtp " + name));
                lines.add(line);
            }

            if(lines.size() == 0) {
                TextComponent line = new TextComponent("無世界\n");
                line.setColor(ChatColor.BLACK);
                lines.add(line);
            }

            Collections.sort(lines, Comparator.comparing(TextComponent::getText));

            List<BaseComponent[]> pages = new ArrayList<>();
            while(lines.size() > 0) {
                List<TextComponent> page = new ArrayList<>(10);
                for(int i = 0; i < 10 && lines.size() > 0; i++) { // max 10 lines per page
                    page.add(lines.removeFirst());
                }
                pages.add(page.toArray(new BaseComponent[0]));
            }

            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setAuthor("");
            meta.setTitle("");
            meta.spigot().setPages(pages);
            book.setItemMeta(meta);
            ((Player) sender).openBook(book);

        } else {
            sender.sendMessage("Only player can do this");
        }
        return true;
    }

    private boolean checkPermission(Player player, MultiverseWorld world) {
        String name = world.getName();
        return (player.hasPermission("multiverse.teleport.self." + name) || player.hasPermission("multiverse.teleport.self.*")) &&
            permTool.playerCanGoFromTo(worldManager.getMVWorld(player.getLocation().getWorld()), world, null, player);
    }
}
