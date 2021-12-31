package ca.encodeous.virtualedit.Utils;

import net.imprex.orebfuscator.config.*;
import org.bukkit.World;

public class StubConfig implements Config {
    @Override
    public GeneralConfig general() {
        return null;
    }

    @Override
    public CacheConfig cache() {
        return new StubCacheConfig();
    }

    @Override
    public BlockFlags blockFlags(World world) {
        return null;
    }

    @Override
    public boolean needsObfuscation(World world) {
        return false;
    }

    @Override
    public ObfuscationConfig obfuscation(World world) {
        return null;
    }

    @Override
    public boolean proximityEnabled() {
        return false;
    }

    @Override
    public ProximityConfig proximity(World world) {
        return null;
    }

    @Override
    public byte[] configHash() {
        return new byte[0];
    }
}
