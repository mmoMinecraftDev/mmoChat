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

import java.util.*;
import mmo.Core.ChatAPI.MMOChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MMOChatEventAPI extends Event implements MMOChatEvent {

	protected static final HandlerList handlers = new HandlerList();
	private final List<String> filters;
	private final Map<Player, String> messages = new HashMap<Player, String>();
	private final Map<Player, String> formats = new HashMap<Player, String>();
	private final Map<String, String[]> args;
	private final Set<Player> recipients;
	protected Player player;
	private String message;
	private String format;
	private boolean cancel = false;

	public MMOChatEventAPI(final Player player, final List<String> filters, final Map<String, String[]> args, final String format, final String message) {
		super();
		this.recipients = new HashSet<Player>(Arrays.asList(player.getServer().getOnlinePlayers()));
		this.player = player;
		this.filters = filters;
		this.args = args;
		this.format = format;
		this.message = message;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean hasFilter(final String filter) {
		return filters.contains(filter.toLowerCase());
	}

	@Override
	public void setMessage(final Player player, final String message) {
		messages.put(player, message);
	}

	@Override
	public String[] getArgs(final String filter) {
		return args.containsKey(filter) ? args.get(filter) : new String[0];
	}

	@Override
	public void setMessage(final String message) {
		this.message = message;
	}

	@Override
	public String getMessage(final Player player) {
		return messages.containsKey(player) ? messages.get(player) : getMessage();
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public void setFormat(final String format) {
		this.format = format;
	}

	@Override
	public void setFormat(final Player player, final String format) {
		formats.put(player, format);
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public String getFormat(final Player player) {
		return formats.containsKey(player) ? formats.get(player) : getFormat();
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
	public void setCancelled(final boolean cancel) {
		this.cancel = cancel;
	}
}
