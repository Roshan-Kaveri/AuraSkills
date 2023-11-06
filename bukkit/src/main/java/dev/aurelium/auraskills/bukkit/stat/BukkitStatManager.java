package dev.aurelium.auraskills.bukkit.stat;

import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.trait.Trait;
import dev.aurelium.auraskills.bukkit.trait.BukkitTraitManager;
import dev.aurelium.auraskills.bukkit.user.BukkitUser;
import dev.aurelium.auraskills.common.AuraSkillsPlugin;
import dev.aurelium.auraskills.common.stat.StatManager;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.api.bukkit.BukkitTraitHandler;

public class BukkitStatManager extends StatManager {

    public BukkitStatManager(AuraSkillsPlugin plugin) {
        super(plugin);
    }

    @Override
    public void reloadPlayer(User user) {
        // Reload traits
        for (Trait trait : plugin.getTraitManager().getEnabledTraits()) {
            BukkitTraitHandler traitImpl = ((BukkitTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
            if (traitImpl == null) continue;

            traitImpl.onReload(((BukkitUser) user).getPlayer(), user.toApi(), trait);
        }
    }

    @Override
    public <T> void reload(User user, T type) {
        if (type instanceof Stat stat) {
            reloadStat(user, stat);
        }
    }

    @Override
    public void reloadStat(User user, Stat stat) {
        if (!stat.isEnabled()) return;
        // Reload traits
        for (Trait trait : stat.getTraits()) {
            BukkitTraitHandler traitImpl = ((BukkitTraitManager) plugin.getTraitManager()).getTraitImpl(trait);
            if (traitImpl == null) continue;

            traitImpl.onReload(((BukkitUser) user).getPlayer(), user.toApi(), trait);
        }
    }
}
