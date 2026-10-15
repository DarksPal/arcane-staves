package dev.arcanestaves.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Единый конфиг мода. Значения читаются в момент использования (.get()).
 * Прочность намеренно НЕ в конфиге: она задаётся при регистрации предмета,
 * а в этот момент конфиг может быть ещё не загружен.
 */
public final class StaffConfig {
    public static final ModConfigSpec SPEC;

    // --- Огненный посох ---
    public static final ModConfigSpec.IntValue FIRE_COOLDOWN;
    public static final ModConfigSpec.DoubleValue FIRE_DAMAGE;
    public static final ModConfigSpec.IntValue FIRE_IGNITE_SECONDS;
    public static final ModConfigSpec.BooleanValue FIRE_IGNITE_BLOCKS;

    // --- Посох Обмена ---
    public static final ModConfigSpec.IntValue SWAP_COOLDOWN;
    public static final ModConfigSpec.IntValue SWAP_RESISTANCE_SECONDS;
    public static final ModConfigSpec.IntValue SWAP_DEBUFF_SECONDS;

    // --- Посох Сингулярности ---
    public static final ModConfigSpec.IntValue SING_COOLDOWN;
    public static final ModConfigSpec.IntValue SING_LIFETIME;
    public static final ModConfigSpec.DoubleValue SING_PULL_RADIUS;
    public static final ModConfigSpec.DoubleValue SING_PULL_STRENGTH;
    public static final ModConfigSpec.DoubleValue SING_PULSE_RADIUS;
    public static final ModConfigSpec.DoubleValue SING_PULSE_DAMAGE;
    public static final ModConfigSpec.DoubleValue SING_COLLAPSE_RADIUS;
    public static final ModConfigSpec.DoubleValue SING_COLLAPSE_DAMAGE;
    public static final ModConfigSpec.DoubleValue SING_COLLAPSE_KNOCKBACK;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Огненный посох").push("fire_staff");
        FIRE_COOLDOWN = b.comment("Кулдаун, тики").defineInRange("cooldown", 20, 0, 12000);
        FIRE_DAMAGE = b.comment("Урон при попадании").defineInRange("damage", 6.0D, 0.0D, 1000.0D);
        FIRE_IGNITE_SECONDS = b.comment("Длительность поджога цели, секунды").defineInRange("ignite_seconds", 5, 0, 600);
        FIRE_IGNITE_BLOCKS = b.comment("Поджигать горючие блоки (дополнительно требует gamerule mobGriefing)")
                .define("ignite_blocks", true);
        b.pop();

        b.comment("Посох Обмена").push("swap_staff");
        SWAP_COOLDOWN = b.comment("Кулдаун, тики (при промахе по блоку возвращается 50%)")
                .defineInRange("cooldown", 40, 0, 12000);
        SWAP_RESISTANCE_SECONDS = b.comment("Сопротивление I для получателя бонуса, секунды")
                .defineInRange("resistance_seconds", 3, 0, 600);
        SWAP_DEBUFF_SECONDS = b.comment("Замедление II и Слепота для цели, секунды")
                .defineInRange("debuff_seconds", 2, 0, 600);
        b.pop();

        b.comment("Посох Сингулярности").push("singularity_staff");
        SING_COOLDOWN = b.comment("Кулдаун, тики").defineInRange("cooldown", 100, 0, 12000);
        SING_LIFETIME = b.comment("Время жизни аномалии, тики").defineInRange("lifetime", 80, 10, 1200);
        SING_PULL_RADIUS = b.comment("Радиус притяжения, блоки").defineInRange("pull_radius", 6.0D, 1.0D, 32.0D);
        SING_PULL_STRENGTH = b.comment("Сила притяжения (ускорение у центра ~ strength*2 за тик)")
                .defineInRange("pull_strength", 0.10D, 0.0D, 2.0D);
        SING_PULSE_RADIUS = b.comment("Радиус периодического урона").defineInRange("pulse_radius", 1.5D, 0.5D, 16.0D);
        SING_PULSE_DAMAGE = b.comment("Урон за импульс (раз в 10 тиков)").defineInRange("pulse_damage", 1.0D, 0.0D, 1000.0D);
        SING_COLLAPSE_RADIUS = b.comment("Радиус ударной волны при коллапсе").defineInRange("collapse_radius", 5.0D, 1.0D, 32.0D);
        SING_COLLAPSE_DAMAGE = b.comment("Урон коллапса").defineInRange("collapse_damage", 4.0D, 0.0D, 1000.0D);
        SING_COLLAPSE_KNOCKBACK = b.comment("Сила отброса коллапса").defineInRange("collapse_knockback", 1.6D, 0.0D, 10.0D);
        b.pop();

        SPEC = b.build();
    }

    private StaffConfig() {}
}
