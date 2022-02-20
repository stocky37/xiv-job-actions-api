package dev.stocky37.xiv.actions.json;

import dev.stocky37.xiv.actions.data.Ability;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class AbilityDeserializer extends JsonNodeDeserializer<Ability> {

	public static final String ID = "ID";
	public static final String NAME = "Name";
	public static final String CATEGORY = "ActionCategory.Name";
	public static final String DESCRIPTION = "Description";
	public static final String ICON = "Icon";
	public static final String ICON_HD = "IconHD";
	public static final String COMBO_ACTION = "ActionComboTargetID";
	public static final String COOLDOWN_GROUP = "CooldownGroup";
	public static final String COOLDOWN_GROUP_ALT = "AdditionalCooldownGroup";
	public static final String CAST = "Cast100ms";
	public static final String RECAST = "Recast100ms";
	public static final String ROLE_ACTION = "IsRoleAction";
	public static final String LEVEL = "ClassJobLevel";
	public static final String DAMAGE_TYPE = "AttackTypeTargetID";

	public static final List<String> ALL_FIELDS = List.of(
		ID,
		NAME,
		ICON,
		CATEGORY,
		DESCRIPTION,
		ICON,
		ICON_HD,
		COMBO_ACTION,
		COOLDOWN_GROUP,
		COOLDOWN_GROUP_ALT,
		CAST,
		RECAST,
		ROLE_ACTION,
		LEVEL,
		DAMAGE_TYPE
	);

	private final String gcdCdGroup;

	@Inject
	public AbilityDeserializer(
		@ConfigProperty(name = "gcd-cd-group") String gcdCdGroup,
		@ConfigProperty(name = "xivapi/mp-rest/uri") String baseUri
	) {
		super(Ability.class, baseUri);
		this.gcdCdGroup = gcdCdGroup;
	}

	@Override
	public Ability apply(JsonNodeWrapper json) {
		final Set<String> cooldownGroups = cooldownGroups(json);
		return Ability.builder()
			.withId(json.get(ID).asText())
			.withName(json.get(NAME).asText())
			.withCategory(json.get(CATEGORY).asText())
			.withDescription(json.get(DESCRIPTION).asText())
			.withIcon(getUri(json, ICON))
			.withIconHD(getUri(json, ICON_HD))
			.withComboFrom(comboAction(json))
			.withCooldownGroups(cooldownGroups)
			.withRecast(get100ms(json, RECAST))
			.withCast(get100ms(json, CAST))
			.withRoleAction(json.get(ROLE_ACTION).asBoolean())
			.withLevel(json.get(LEVEL).asInt())
			.withOnGCD(cooldownGroups.contains(gcdCdGroup))
			.withDamageType(damageType(json.get(DAMAGE_TYPE).asInt()))
			.build();
	}

	public Duration get100ms(JsonNodeWrapper json, String key) {
		return Duration.ofMillis(json.get(key).asLong() * 100);
	}


	public String comboAction(JsonNodeWrapper json) {
		return json.get(COMBO_ACTION).asInt() == 0 ? null : json.get(COMBO_ACTION).asText();
	}

	public Set<String> cooldownGroups(JsonNodeWrapper json) {
		final Set<String> cooldownGroups = new HashSet<>();
		if(json.get(COOLDOWN_GROUP).asInt() != 0) {
			cooldownGroups.add(json.get(COOLDOWN_GROUP).asText());
		}
		if(json.get(COOLDOWN_GROUP_ALT).asInt() != 0) {
			cooldownGroups.add(json.get(COOLDOWN_GROUP_ALT).asText());
		}
		return cooldownGroups;
	}

	public Ability.DamageType damageType(int damageType) {
		return switch(damageType) {
			case -1, 1 -> Ability.DamageType.PHYSICAL;
			case 0 -> null;
			case 5 -> Ability.DamageType.MAGICAL;
			default -> throw new RuntimeException("Unkown damage type: " + damageType);
		};
	}
}