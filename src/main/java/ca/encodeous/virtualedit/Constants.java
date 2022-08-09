package ca.encodeous.virtualedit;

public class Constants {
    public static final int DS_NULL_VALUE = -1;
    public static final int MAX_WORLD_SIZE = 60_000_000;
    public static final int MAX_CHUNKS_AXIS = (MAX_WORLD_SIZE * 2) >> 4;
    public static final int UPDATE = 1;
    public static final int NO_UPDATE = 1;
    public static int VIEW_UPDATE_THREADS = 2;
}
