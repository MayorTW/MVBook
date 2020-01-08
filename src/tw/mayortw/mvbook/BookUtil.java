package tw.mayortw.mvbook;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.Material;

import tw.mayortw.mvbook.ReflectionUtils.PackageType;

class VersionUtil {
    /*
     * Check if server is at least ver version
     */
    public static boolean atLeast(String verString) {
        String[] compVer = verString.split("\\.");
        String[] servVer = org.bukkit.Bukkit.getBukkitVersion().split("\\.");

        for(int i = 0; i < compVer.length; i++) {
            if(i >= servVer.length) return false;

            int compNum = 0;
            int servNum = 0;
            try {
                compNum = Integer.parseInt(compVer[i]);
                servNum = Integer.parseInt(servVer[i]);
            } catch (NumberFormatException e) {}

            if(compNum != servNum) return compNum < servNum;
        }

        return true;
    }
}

/**
 * Create a "Virtual" book gui that doesn't require the user to have a book in their hand.
 * Requires ReflectionUtil class.
 * Built for Minecraft 1.9
 * @author Jed
 *
 */
public class BookUtil {

    private static boolean initialised = false;
    private static Method getHandle;
    private static Method openBook;

    static {
        if(VersionUtil.atLeast("1.14")) {
            // There's API for this since 1.14
            initialised = false;
        } else {
            try {
                getHandle = ReflectionUtils.getMethod("CraftPlayer", PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
                openBook = ReflectionUtils.getMethod("EntityPlayer", PackageType.MINECRAFT_SERVER, "a", PackageType.MINECRAFT_SERVER.getClass("ItemStack"), PackageType.MINECRAFT_SERVER.getClass("EnumHand"));
                initialised = true;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                Bukkit.getServer().getLogger().warning("Cannot force open book!");
                initialised = false;
            }
        }
    }

    public static boolean isInitialised(){
        return initialised;
    }

    /**
     * Open a "Virtual" Book ItemStack.
     * @param i Book ItemStack.
     * @param p Player that will open the book.
     * @return
     */
    public static boolean openBook(ItemStack i, Player p) {

        if(VersionUtil.atLeast("1.14")) {
            // Book won't open without title and authro after 1.14
            BookMeta meta = (BookMeta) i.getItemMeta();
            if(!meta.hasTitle()) meta.setTitle("");
            if(!meta.hasAuthor()) meta.setAuthor("");
            i.setItemMeta(meta);

            p.openBook(i);
            return true;
        }

        if (!initialised) return false;
        ItemStack held = p.getInventory().getItemInMainHand();
        try {
            p.getInventory().setItemInMainHand(i);
            sendPacket(i, p);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            initialised = false;
        }
        p.getInventory().setItemInMainHand(held);
        return initialised;
    }

    private static void sendPacket(ItemStack i, Player p) throws ReflectiveOperationException {
        Object entityplayer = getHandle.invoke(p);
        Class<?> enumHand = PackageType.MINECRAFT_SERVER.getClass("EnumHand");
        Object[] enumArray = enumHand.getEnumConstants();
        openBook.invoke(entityplayer, getItemStack(i), enumArray[0]);
    }

    public static Object getItemStack(ItemStack item) {
        try {
            Method asNMSCopy = ReflectionUtils.getMethod(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), "asNMSCopy", ItemStack.class);
            return asNMSCopy.invoke(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), item);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set the pages of the book in JSON format.
     * @param metadata BookMeta of the Book ItemStack.
     * @param pages Each page to be added to the book.
     */
    @SuppressWarnings("unchecked")
    public static void setPages(BookMeta metadata, List<String> pages) {
        List<Object> p;
        Object page;

        try {
            p = (List<Object>) ReflectionUtils.getField(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftMetaBook"), true, "pages").get(metadata);
            for (String text : pages) {
                page = ReflectionUtils.invokeMethod(ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("IChatBaseComponent$ChatSerializer").newInstance(), "a", text);
                p.add(page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
