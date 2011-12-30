/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Chat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mmo.Core.ChatAPI.MMOChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class MMOChatEventAPI extends Event implements MMOChatEvent {

	private final List<String> filters;
	private final Map<Player, String> messages = new HashMap<Player, String>();
	private final Map<Player, String> formats = new HashMap<Player, String>();
	private final Set<Player> recipients;
	protected Player player;
	private String message;
	private String format;
	private boolean cancel = false;
	private Map<String,String[]> args = null;

	public MMOChatEventAPI(final Player player, final List<String> filters, final Map<String,String[]> args, final String format, final String message) {
		super("mmoChatEvent");
		this.recipients = new HashSet<Player>(Arrays.asList(player.getServer().getOnlinePlayers()));
		this.player = player;
		this.filters = filters;
		this.args = args;
		this.format = format;
		this.message = message;
	}

	@Override
	public boolean hasFilter(String filter) {
		return filters.contains(filter);
	}

	@Override
	public void setMessage(Player player, String message) {
		messages.put(player, message);
	}

	@Override
	public String[] getArgs(String filter) {
		return args.containsKey(filter) ? args.get(filter) : new String[0];
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getMessage(Player player) {
		if (messages.containsKey(player)) {
			return messages.get(player);
		}
		return getMessage();
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public void setFormat(Player player, String format) {
		formats.put(player, format);
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public String getFormat(Player player) {
		if (formats.containsKey(player)) {
			return formats.get(player);
		}
		return getFormat();
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Set<Player> getRecipients() {
		return recipients;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
