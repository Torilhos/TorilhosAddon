package mdsol.torilhosaddon.feature.bosstracker;

import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public enum BossData {
    ANUBIS(
            "Anubis",
            Pattern.compile("^\\[Anubis] Kneel before your final reckoning, (.+)!"),
            new BlockPos(458, 0, -467),
            Identifier.of("telos:material/boss/anubis")),
    ASTAROTH(
            "Astaroth",
            Pattern.compile(
                    "^\\[Astaroth] Your futile struggles are mere entertainment for the denizens of the void, (.+)"),
            new BlockPos(250, 0, 60),
            Identifier.of("telos:material/boss/astaroth")),
    CHUNGUS(
            "Chungus",
            Pattern.compile("^\\[Chungus] The void strengthens me, (.+)!"),
            new BlockPos(61, 0, -490),
            Identifier.of("telos:material/boss/chungus")),
    FREDDY(
            "Freddy",
            Pattern.compile("^\\[Freddy] YOU WILL NOT BE SPARED! YOU WILL NOT BE SAVED, (.+)!"),
            new BlockPos(-136, 0, 653),
            Identifier.of("telos:material/boss/freddy")),
    GLUMI(
            "Glumi",
            Pattern.compile("^\\[Glumi] You will not access the sacred caverns, (.+)!"),
            new BlockPos(339, 0, 552),
            Identifier.of("telos:material/boss/glumi")),
    ILLARIUS(
            "Illarius",
            Pattern.compile("^\\[Illarius] Don't send me back to Loa, (.+)!"),
            new BlockPos(478, 0, -45),
            Identifier.of("telos:material/boss/illarius")),
    LOTIL(
            "Lotil",
            Pattern.compile("^\\[Lotil] You will NOT take my symbolic shield away from me, (.+)!"),
            new BlockPos(-138, 0, 17),
            Identifier.of("telos:material/boss/lotil")),
    OOZUL(
            "Oozul",
            Pattern.compile("^\\[Oozul] Don't expose mortals such as (.+) to Chronos!"),
            new BlockPos(-424, 0, 91),
            Identifier.of("telos:material/boss/oozul")),
    TIDOL(
            "Tidol",
            Pattern.compile("^\\[Tidol] Face my trident, (.+)!"),
            new BlockPos(-543, 0, 364),
            Identifier.of("telos:material/boss/tidol")),
    VALUS(
            "Valus",
            Pattern.compile("^\\[Valus] You are not worthy of joining our worship, (.+)!"),
            new BlockPos(35, 0, 307),
            Identifier.of("telos:material/boss/valus")),
    HOLLOWBANE(
            "Hollowbane",
            Pattern.compile("^\\[Hollowbane] Hollow is your fate, as it is mine (.+)!"),
            new BlockPos(232, 0, 696),
            Identifier.of("telos:material/boss/hollowbane"));

    public final String label;
    public final Pattern playerCallPattern;
    public final BlockPos spawnPosition;
    public final Identifier modelIdentifier;

    BossData(String label, Pattern playerCallPattern, BlockPos spawnPosition, Identifier modelIdentifier) {
        this.label = label;
        this.playerCallPattern = playerCallPattern;
        this.spawnPosition = spawnPosition;
        this.modelIdentifier = modelIdentifier;
    }

    public static Optional<BossData> fromString(String name) {
        try {
            return Optional.of(valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
