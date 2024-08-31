<p align="center"><img src="assets/smoke.png" alt="Smoke Icon"></p>
<h1 align="center">Smoke</h1>

<p align="center">This guide provides a basic breakdown of how to implement a smoke sphere in Minecraft using the SpigotAPI.</p>

---

## Requirements

To achieve the desired smoke effect, we need to meet the following criteria:

- **Particle Generation**: Generate particles across the surface of a sphere rather than filling the entire sphere with particles. This approach reduces the computational intensity.
- **Player Interaction**: Ensure that players who walk into the smoke region experience blindness. Conversely, players should regain visibility when leaving the smoke.
- **Mob Interaction**: When mobs enter the smoke region ~~their target should be removed~~ they will experience slowness (Constantly setting the mob target to `null` is too slow compared to the actual mob targetting event in native Minecraft). This adds complexity and depth to the smoke effect to extend to mobs as well.
- **Block Collision**: The smoke should dynamically conform to surrounding geometry by correctly colliding with obstructing blocks.

## Implementation

### Parametric Equation of a Sphere

The surface of a sphere can be described using the following [parametric equation](https://en.wikipedia.org/wiki/Sphere#Parametric):

$(x, y, z) = (\rho \cos \theta \sin \phi, \rho \sin \theta \sin \phi, \rho \cos \phi)$

where:

- $\rho\$ is the **radius** of the sphere,
- $\theta \in [0, 2\pi)$ is the **longitude**,
- $\phi \in [0, \pi]$ is the **colatitude**.

We will be iterating through values of $\theta$ and $\phi$ using a set step size, generating the coordinates on the surface of a sphere with radius $\rho$, centered at $(0, 0, 0)$. These coordinates will be used to offset the position the smoke correctly around some set center coordinate.

We will be storing the points in a `List` .

```java
List<Location> cells = new ArrayList<>();
```

Through experimentation, I found that using the step size $\pi / 2\rho$ in our nested for loops works the best in terms of accurately depicting the surface of the sphere with limited holes. Addtionally using the `CAMPFIRE_COSY_SMOKE` particle allowed for a sufficiently large particle that can create a detailed smoke sphere.

```java
double step_size = PI/radius;

for (double theta = 0; theta < 2 * PI; theta += step_size) {
        for (double phi = 0; phi <= PI; phi += step_size) {
        double x = radius * cos(theta) * sin(phi);
        double y = radius * sin(theta) * sin(phi);
        double z = radius * cos(phi);
  }
}
```

Within this loop, we will now iterate through all the surface points of the sphere and add those results to our `List`.

```java
Location point = center.clone().add(x, y, z);
World world = point.getWorld();
if (world == null) continue;
cells.add(point);
```

### Block Collision

With the position points for the sphere’s surface generated, we need to address block collision. If particles are spawned without considering block collisions, the effect may not conform to the surrounding environment realistically and it'll appear to go through walls.

To handle this, we use RayTraceResult. By casting a line the length of the radius from each point towards the center, we can determine if there is a block in the way. If so, we adjust the smoke's position to the maximum distance it can reach before hitting the block. We will additionally 

```java
World world = point.getWorld();
if (world == null) continue;

Vector direction = point.clone().subtract(center).toVector();
RayTraceResult result = world.rayTraceBlocks(center, direction, radius);

if (result != null && result.getHitBlock() != null) {
        Block block = result.getHitBlock();
        point = block.getLocation().subtract(direction.multiply(BLOCK_THRESHOLD));
}
```

With these code implementations we have the following code that will return us a map of all the points in which we want our particles to spawn.

```java
private static final double BLOCK_THRESHOLD = 1.5;

private static HashMap<Location, Integer> getSmokeSurface(Location center, int radius) {
        HashMap<Location, Integer> cells = new HashMap<>();
        double step_size = PI/radius;
        for (double theta = 0; theta < 2 * PI; theta += step_size) {
                for (double phi = 0; phi <= PI; phi += step_size) {
                        double x = radius * cos(theta) * sin(phi);
                        double y = radius * sin(theta) * sin(phi);
                        double z = radius * cos(phi);

                        Location point = center.clone().add(x, y, z);
                        if (cells.containsKey(point)) continue;

                        World world = point.getWorld();
                        if (world == null) continue;

                        Vector direction = point.clone().subtract(center).toVector();
                        RayTraceResult result = world.rayTraceBlocks(center, direction, radius);

                        if (result != null && result.getHitBlock() != null) {
                                        Block block = result.getHitBlock();
                                        point = block.getLocation().subtract(direction.multiply(BLOCK_THRESHOLD));
                        }
                        cells.putIfAbsent(point, 1);
                }
        }
        return cells;
}
```

## Contributing
Feel free to contribute to this project by submitting bug reports, feature requests, or pull requests on the [GitHub repository](https://github.com/hello-andrew-yan/spigot-smoke).

## License
This project is licensed under the [MIT License](LICENSE).

---

<p align="right"><a target="_blank" href="https://icons8.com/icons/set/smoke-grenade">Smoke Grenade</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a></p>
