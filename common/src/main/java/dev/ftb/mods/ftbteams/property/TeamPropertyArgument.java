package dev.ftb.mods.ftbteams.property;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbteams.event.TeamCollectPropertiesEvent;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author LatvianModder
 */
public class TeamPropertyArgument implements ArgumentType<TeamProperty> {
	private static final SimpleCommandExceptionType PROPERTY_NOT_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("ftbteams.property_not_found"));

	public static TeamPropertyArgument create() {
		return new TeamPropertyArgument();
	}

	public static TeamProperty get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		return context.getArgument(name, TeamProperty.class);
	}

	private TeamPropertyArgument() {
	}

	@Override
	public TeamProperty parse(StringReader reader) throws CommandSyntaxException {
		ResourceLocation id = ResourceLocation.read(reader);
		Map<ResourceLocation, TeamProperty> map = new LinkedHashMap<>();
		TeamEvent.COLLECT_PROPERTIES.invoker().accept(new TeamCollectPropertiesEvent(property -> map.put(property.id, property)));
		TeamProperty property = map.get(id);

		if (property != null) {
			return property;
		}

		throw PROPERTY_NOT_FOUND.create();
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		try {
			Map<String, TeamProperty> map = new LinkedHashMap<>();
			TeamEvent.COLLECT_PROPERTIES.invoker().accept(new TeamCollectPropertiesEvent(property -> map.put(property.id.toString(), property)));
			return SharedSuggestionProvider.suggest(map.keySet(), builder);
		} catch (Exception ex) {
			return Suggestions.empty();
		}
	}
}