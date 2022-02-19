package valoeghese.loadimmunity;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("loadimmunity")
public class LoadImmunity
{
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger("Load Immunity");

	public LoadImmunity() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event)
	{
		LOGGER.info("Imagine dying to lag amirite");
		LOGGER.debug("Is this in production? {}", FMLLoader.isProduction());
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		PlayerEntity player = event.getPlayer();
		addImmune(player, player.blockPosition());
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.debug("Player logged in at {}", event.getPlayer().blockPosition());
		PlayerEntity player = event.getPlayer();
		addImmune(player, player.blockPosition());
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		LOGGER.debug("Plauer logged out: {}", event.getPlayer());
		removeImmune(event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		UUID uuid = event.player.getUUID();

		if (IMMUNE.contains(uuid)) {
			if (!LAST_POS.get(uuid).equals(event.player.blockPosition())) {
				LOGGER.debug("Immune player moved! Removing immunity of {}", uuid);
				removeImmune(event.player);
			}
		}
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event) {
		if (!event.getWorld().isClientSide()) {
			UUID uuid = event.getPlayer().getUUID();

			if (IMMUNE.contains(uuid)) {
				LOGGER.debug("Immune player interacted! Removing immunity of {}", uuid);
				removeImmune(event.getPlayer());
			}
		}
	}

	private static final Map<UUID, BlockPos> LAST_POS = new HashMap<>();
	private static final Set<UUID> IMMUNE = new HashSet<>();
	private static final Object2BooleanMap<UUID> OLD_INVULNERABILITY = new Object2BooleanArrayMap<>();

	private static void removeImmune(PlayerEntity player) {
		UUID uuid = player.getUUID();
		player.setInvulnerable(OLD_INVULNERABILITY.removeBoolean(uuid));

		IMMUNE.remove(uuid);
		LAST_POS.remove(uuid);
	}

	private static void addImmune(PlayerEntity player, BlockPos position) {
		UUID uuid = player.getUUID();

		OLD_INVULNERABILITY.put(player.getUUID(), player.isInvulnerable());
		player.setInvulnerable(true);
		IMMUNE.add(uuid);
		LAST_POS.put(uuid, position);
	}

	public static boolean isImmune(UUID uuid) {
		return IMMUNE.contains(uuid);
	}
}
