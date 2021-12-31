package ca.encodeous.virtualedit.Utils;

import net.imprex.orebfuscator.config.CacheConfig;
import net.imprex.orebfuscator.util.ChunkPosition;

import java.nio.file.Path;

public class StubCacheConfig implements CacheConfig {
    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public void enabled(boolean b) {

    }

    @Override
    public Path baseDirectory() {
        return null;
    }

    @Override
    public Path regionFile(ChunkPosition chunkPosition) {
        return null;
    }

    @Override
    public int maximumOpenRegionFiles() {
        return 0;
    }

    @Override
    public void maximumOpenRegionFiles(int i) {

    }

    @Override
    public long deleteRegionFilesAfterAccess() {
        return 0;
    }

    @Override
    public void deleteRegionFilesAfterAccess(long l) {

    }

    @Override
    public int maximumSize() {
        return 0;
    }

    @Override
    public void maximumSize(int i) {

    }

    @Override
    public long expireAfterAccess() {
        return 0;
    }

    @Override
    public void expireAfterAccess(long l) {

    }

    @Override
    public int maximumTaskQueueSize() {
        return 0;
    }

    @Override
    public void maximumTaskQueueSize(int i) {

    }

    @Override
    public int protocolLibThreads() {
        return 0;
    }

    @Override
    public void protocolLibThreads(int i) {

    }
}
