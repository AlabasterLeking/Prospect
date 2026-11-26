package alabaster.prospect.common.utilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MinecartLinks {

    public static final int MAX_LINKS = 2;
    private static final String KEY = "prospect_links";

    private MinecartLinks() {}

    public static List<UUID> get(Entity cart) {
        CompoundTag data = cart.getPersistentData();
        if (!data.contains(KEY, Tag.TAG_LIST)) return List.of();
        ListTag list = data.getList(KEY, Tag.TAG_INT_ARRAY);
        List<UUID> links = new ArrayList<>(list.size());
        for (Tag tag : list) {
            links.add(NbtUtils.loadUUID(tag));
        }
        return links;
    }

    public static boolean has(Entity cart) {
        return cart.getPersistentData().contains(KEY, Tag.TAG_LIST);
    }

    public static boolean isLinked(Entity cart, Entity other) {
        if (!has(cart)) return false;
        UUID id = other.getUUID();
        for (Tag tag : cart.getPersistentData().getList(KEY, Tag.TAG_INT_ARRAY)) {
            if (NbtUtils.loadUUID(tag).equals(id)) return true;
        }
        return false;
    }

    public static void add(Entity cart, UUID id) {
        List<UUID> links = new ArrayList<>(get(cart));
        if (!links.contains(id)) links.add(id);
        set(cart, links);
    }

    public static void remove(Entity cart, UUID id) {
        List<UUID> links = new ArrayList<>(get(cart));
        if (links.remove(id)) set(cart, links);
    }

    private static void set(Entity cart, List<UUID> links) {
        if (links.isEmpty()) {
            cart.getPersistentData().remove(KEY);
            return;
        }
        ListTag list = new ListTag();
        for (UUID id : links) {
            list.add(NbtUtils.createUUID(id));
        }
        cart.getPersistentData().put(KEY, list);
    }
}
