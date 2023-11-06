package dev.aurelium.auraskills.bukkit.menus.skills;

import com.archyx.slate.component.ComponentData;
import com.archyx.slate.component.ComponentProvider;
import com.archyx.slate.item.provider.ListBuilder;
import com.archyx.slate.item.provider.PlaceholderData;
import com.archyx.slate.menu.ActiveMenu;
import dev.aurelium.auraskills.api.ability.Ability;
import dev.aurelium.auraskills.api.mana.ManaAbilities;
import dev.aurelium.auraskills.api.mana.ManaAbility;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.bukkit.menus.AbstractComponent;
import dev.aurelium.auraskills.common.user.User;
import dev.aurelium.auraskills.api.util.NumberUtil;
import dev.aurelium.auraskills.common.util.math.RomanNumber;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class SkillComponents {

    public static class StatsLeveled extends AbstractComponent implements ComponentProvider {

        public StatsLeveled(AuraSkills plugin) {
            super(plugin);
        }

        @Override
        public <T> String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data, ComponentData componentData, T context) {
            if (placeholder.equals("entries")) {
                String entry = activeMenu.getFormat("stat_leveled_entry");
                ListBuilder builder = new ListBuilder(data.getListData());
                Skill skill = (Skill) context;
                Locale locale = plugin.getUser(player).getLocale();

                for (Stat stat : plugin.getRewardManager().getRewardTable(skill).getStatsLeveled()) {
                    builder.append(entry, "{color}", stat.getColor(locale), "{stat}", stat.getDisplayName(locale));
                }

                return builder.build();
            }
            return replaceMenuMessage(placeholder, player, activeMenu);
        }

        @Override
        public <T> boolean shouldShow(Player player, ActiveMenu activeMenu, T context) {
            return !plugin.getRewardManager().getRewardTable((Skill) context).getStatsLeveled().isEmpty();
        }
    }

    public static class AbilityLevels extends AbstractComponent implements ComponentProvider {

        public AbilityLevels(AuraSkills plugin) {
            super(plugin);
        }

        @Override
        public <T> String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data, ComponentData componentData, T context) {
            if (placeholder.equals("entries")) {
                ListBuilder builder = new ListBuilder(data.getListData());
                Skill skill = (Skill) context;

                User user = plugin.getUser(player);
                Locale locale = user.getLocale();

                for (Ability ability : skill.getAbilities()) {
                    if (!ability.isEnabled()) continue;
                    // Get the format depending on whether ability is unlocked
                    String entry;
                    int level = user.getAbilityLevel(ability);
                    if (level > 0) {
                        entry = activeMenu.getFormat("unlocked_ability_entry");
                    } else {
                        entry = activeMenu.getFormat("locked_ability_entry");
                    }
                    builder.append(entry, "{name}", ability.getDisplayName(locale), "{level}", RomanNumber.toRoman(level, plugin), "{info}", TextUtil.replace(ability.getInfo(locale),
                            "{value}", NumberUtil.format2(ability.getValue(level)), "{value_2}", NumberUtil.format2(ability.getSecondaryValue(level))));
                }

                return builder.build();
            }
            return replaceMenuMessage(placeholder, player, activeMenu);
        }

        @Override
        public <T> boolean shouldShow(Player player, ActiveMenu activeMenu, T context) {
            return !((Skill) context).getAbilities().isEmpty();
        }
    }

    public static class ManaAbilityInfo extends AbstractComponent implements ComponentProvider {

        public ManaAbilityInfo(AuraSkills plugin) {
            super(plugin);
        }

        @Override
        public <T> String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data,ComponentData componentData, T context) {
            ManaAbility manaAbility = ((Skill) context).getManaAbility();
            if (manaAbility == null) return placeholder;

            User user = plugin.getUser(player);
            Locale locale = user.getLocale();

            switch (placeholder) {
                case "name" -> {
                    return manaAbility.getDisplayName(locale);
                }
                case "level" -> {
                    return RomanNumber.toRoman(user.getManaAbilityLevel(manaAbility), plugin);
                }
                case "entries" -> {
                    ListBuilder builder = new ListBuilder(data.getListData());
                    int level = user.getManaAbilityLevel(manaAbility);
                    for (String format : getFormatEntries(manaAbility)) {
                        String message = replaceMenuMessages(activeMenu.getFormat(format), player, activeMenu);
                        switch (format) {
                            case "duration_entry" ->
                                    builder.append(message, "{duration}", NumberUtil.format1(getDuration(manaAbility, level)));
                            case "mana_cost_entry", "max_mana_cost_entry" ->
                                    builder.append(message, "{mana_cost}", NumberUtil.format1(manaAbility.getManaCost(level)));
                            case "cooldown_entry" ->
                                    builder.append(message, "{cooldown}", NumberUtil.format1(manaAbility.getCooldown(level)));
                            case "damage_entry", "damage_per_mana_entry", "attack_speed_entry" ->
                                    builder.append(message, "{value}", NumberUtil.format1(manaAbility.getValue(level)));
                        }
                    }
                    return builder.build();
                }
            }
            return replaceMenuMessage(placeholder, player, activeMenu);
        }

        @Override
        public <T> boolean shouldShow(Player player, ActiveMenu activeMenu, T context) {
            ManaAbility manaAbility = ((Skill) context).getManaAbility();
            return manaAbility != null && plugin.getUser(player).getManaAbilityLevel(manaAbility) > 0;
        }

        private double getDuration(ManaAbility manaAbility, int level) {
            if (manaAbility.equals(ManaAbilities.LIGHTNING_BLADE)) {
                double baseDuration = ManaAbilities.LIGHTNING_BLADE.optionDouble("base_duration");
                double durationPerLevel = ManaAbilities.LIGHTNING_BLADE.optionDouble("duration_per_level");
                return baseDuration + (durationPerLevel * (level - 1));
            } else {
                return manaAbility.getValue(level);
            }
        }

        private List<String> getFormatEntries(ManaAbility manaAbility) {
            if (manaAbility.equals(ManaAbilities.SHARP_HOOK)) {
                return List.of("damage_entry", "mana_cost_entry", "cooldown_entry");
            } else if (manaAbility.equals(ManaAbilities.CHARGED_SHOT)) {
                return List.of("damage_per_mana_entry", "max_mana_cost_entry");
            } else if (manaAbility.equals(ManaAbilities.LIGHTNING_BLADE)) {
                return List.of("attack_speed_entry", "duration_entry", "mana_cost_entry", "cooldown_entry");
            } else {
                return List.of("duration_entry", "mana_cost_entry", "cooldown_entry");
            }
        }

    }

    public static class Progress extends AbstractComponent implements ComponentProvider {

        public Progress(AuraSkills plugin) {
            super(plugin);
        }

        @Override
        public <T> String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data, ComponentData componentData, T context) {
            Skill skill = (Skill) context;
            User user = plugin.getUser(player);
            int skillLevel = user.getSkillLevel(skill);
            double currentXp = user.getSkillXp(skill);
            double xpToNext = plugin.getXpRequirements().getXpRequired(skill, skillLevel + 1);

            return switch (placeholder) {
                case "next_level" -> RomanNumber.toRoman(skillLevel + 1, plugin);
                case "percent" -> NumberUtil.format2(currentXp / xpToNext * 100);
                case "current_xp" -> NumberUtil.format2(currentXp);
                case "level_xp" -> String.valueOf((int) xpToNext);
                default -> replaceMenuMessage(placeholder, player, activeMenu);
            };
        }

        @Override
        public <T> boolean shouldShow(Player player, ActiveMenu activeMenu, T context) {
            return ((Skill) context).getMaxLevel() > plugin.getUser(player).getSkillLevel((Skill) context);
        }
    }

    public static class MaxLevel extends AbstractComponent implements ComponentProvider {

        public MaxLevel(AuraSkills plugin) {
            super(plugin);
        }

        @Override
        public <T> String onPlaceholderReplace(String placeholder, Player player, ActiveMenu activeMenu, PlaceholderData data, ComponentData componentData, T context) {
            return replaceMenuMessage(placeholder, player, activeMenu);
        }

        @Override
        public <T> boolean shouldShow(Player player, ActiveMenu activeMenu, T context) {
            return ((Skill) context).getMaxLevel() == plugin.getUser(player).getSkillLevel((Skill) context);
        }
    }

}
