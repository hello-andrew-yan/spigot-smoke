package io.github.helloandrewyan.smoke.entity;

import io.github.helloandrewyan.smoke.Smoke;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.lang.Math.*;
import static org.bukkit.Particle.CAMPFIRE_COSY_SMOKE;

public class SmokeSphere {

		private static final List<BukkitTask> activeTasks = new ArrayList<>();

		private static final double BLOCK_THRESHOLD = 1.5;

		private static final long PERIOD = 5;
		private static final int TICKS_PER_SECOND = 20;

		private static final int PARTICLE_AMOUNT = 1;
		private static final double PARTICLE_OFFSET = 0.5;
		private static final double PARTICLE_SPEED = 0.01;

		private static final Predicate<Entity> ENTITY_FILTER = entity ->
						(entity instanceof Mob mob && mob.getTarget() != null)
										|| (entity instanceof Player);

		private static List<Location> getSmokeSurface(Location center, double radius) {
				List<Location> cells = new ArrayList<>();
				double stepSize = PI / (2 * radius);

				for (double theta = 0; theta < 2 * PI; theta += stepSize) {
						for (double phi = 0; phi <= PI; phi += stepSize) {
								double x = radius * cos(theta) * sin(phi);
								double y = radius * sin(theta) * sin(phi);
								double z = radius * cos(phi);

								Location point = center.clone().add(x, y, z);
								World world = point.getWorld();
								if (world == null) continue;
								cells.add(point);
						}
				}
				return cells;
		}

		public static void castSmoke(Location center, double radius, int duration) {
				World world = center.getWorld();
				if (world == null) return;

				List<Location> surface = getSmokeSurface(center, radius);
				BukkitTask task = new BukkitRunnable() {
						int elapsedSeconds = 0;
						long ticks = 0;

						@Override
						public void run() {
								if (ticks % TICKS_PER_SECOND == 0) elapsedSeconds++;
								if (elapsedSeconds >= duration) {
										cancel();
										return;
								}
								for (Location location : surface) {
										// Calculate direction vector.
										Vector direction = location.clone().subtract(center).toVector();

										// Ray trace for blocks.
										RayTraceResult blockResult = world.rayTraceBlocks(center, direction, radius);
										Location finalPointLocation = (blockResult != null && blockResult.getHitBlock() != null)
														? blockResult.getHitBlock().getLocation().subtract(direction.multiply(BLOCK_THRESHOLD))
														: location;

										// Spawn particles.
										world.spawnParticle(CAMPFIRE_COSY_SMOKE, finalPointLocation, PARTICLE_AMOUNT,
														PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_OFFSET, PARTICLE_SPEED);

										// Ray trace for entities.
										RayTraceResult entityResult = world.rayTraceEntities(center, direction, radius, ENTITY_FILTER);

										// Process entity if found.
										if (entityResult != null && entityResult.getHitEntity() != null) {
												Entity entity = entityResult.getHitEntity();

												if (entity instanceof Player player) {
														player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 30, 1));
												}
												if (entity instanceof Mob mob) {
														mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 2));
												}
										}
								}
								ticks += PERIOD;
						}
				}.runTaskTimer(Smoke.getInstance(), 0, PERIOD);

				activeTasks.add(task);
		}

		public static void clearAllSmokeInstances() {
				activeTasks.forEach(BukkitTask::cancel);
				activeTasks.clear();
		}
}

