package tech.slideshare.cache;

public class NullCache implements Cache {

    public static final Cache INSTANCE = new NullCache();

    private NullCache() {
    }

    @Override
    public boolean add(String entry) {
        return true;
    }

    @Override
    public void flush() {
        // do nothing
    }
}
