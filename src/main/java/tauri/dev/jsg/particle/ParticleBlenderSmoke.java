package tauri.dev.jsg.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleBlenderSmoke extends ParticleBlender {

	public ParticleBlenderSmoke(float x, float y, float z, int moduloTicks, int moduloTicksSlower, float motionX, float motionZ, boolean falling, RandomizeInterface randomize) {
		super(x, y, z, moduloTicks, moduloTicksSlower, motionX, motionZ, falling, randomize);
	}
	
	public ParticleBlenderSmoke(float x, float y, float z, int moduloTicks, int moduloTicksSlower, boolean falling, RandomizeInterface randomize) {
		super(x, y, z, moduloTicks, moduloTicksSlower, falling, randomize);
	}

	@Override
	protected Particle createParticle(World world, double x, float y, double z, double motionX, double motionZ, boolean falling) {
		return new ParticleWhiteSmoke(world, x, y, z, motionX, motionZ, falling);
	}

}
