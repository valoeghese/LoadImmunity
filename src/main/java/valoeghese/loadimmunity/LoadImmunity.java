package valoeghese.loadimmunity;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
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

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Imagine dying to lag amirite");
		LOGGER.debug("Is this in production? {}", FMLLoader.isProduction());
	}

	@SubscribeEvent
	public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		LOGGER.debug("Player logged in at {}", event.getPlayer().blockPosition());
		PlayerEntity player = event.getPlayer();
		tryAddImmune(player, player.blockPosition());
	}

	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		LOGGER.debug("Player logged out: {}", event.getPlayer());
		removeImmune(event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		UUID uuid = event.player.getUUID();

		if (!event.player.level.isClientSide() && isImmune(uuid)) {
			if (!LOGIN_POS.get(uuid).equals(event.player.blockPosition())) {
				LOGGER.debug("Immune player moved! Removing immunity of {}", uuid);
				removeImmune(event.player);
			}
		}
	}

	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent event) {
		if (event.side == LogicalSide.SERVER) { // server world
			for (PlayerEntity player : event.world.players()) {
				if (isImmune(player.getUUID())) {
					for (LivingEntity le : event.world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(2.0), e -> e != player)) {
						le.setDeltaMovement((le.getX() - player.getX()) * 0.5, (le.getY() - player.getY()) * 0.5, (le.getZ() - player.getZ()) * 0.5);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onInteract(PlayerInteractEvent event) {
		if (!event.getWorld().isClientSide()) {
			UUID uuid = event.getPlayer().getUUID();

			if (isImmune(uuid)) {
				LOGGER.debug("Immune player interacted! Removing immunity of {}", uuid);
				removeImmune(event.getPlayer());
			}
		}
	}

	private static final Map<UUID, BlockPos> LOGIN_POS = new HashMap<>();
	private static final Set<UUID> IMMUNE = new HashSet<>();
	private static final Object2BooleanMap<UUID> OLD_INVULNERABILITY = new Object2BooleanArrayMap<>();

	private static void removeImmune(PlayerEntity player) {
		UUID uuid = player.getUUID();
		player.setInvulnerable(OLD_INVULNERABILITY.removeBoolean(uuid));

		IMMUNE.remove(uuid);
		LOGIN_POS.remove(uuid);
	}

	private static void tryAddImmune(PlayerEntity player, BlockPos position) {
		if (player.level.dimension().location().getNamespace().equals("the_vault")) {
			UUID uuid = player.getUUID();

			OLD_INVULNERABILITY.put(player.getUUID(), player.isInvulnerable());
			player.setInvulnerable(true);
			IMMUNE.add(uuid);
			LOGIN_POS.put(uuid, position);
		}
	}

	public static boolean isImmune(UUID uuid) {
		return IMMUNE.contains(uuid);
	}
}
