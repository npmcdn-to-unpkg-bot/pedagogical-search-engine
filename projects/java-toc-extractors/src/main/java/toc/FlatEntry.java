package toc;

public interface FlatEntry<E> {
    /**
     * @return depth of the entry, should not be lower than 0
     */
    int getLevel();

    E getContent();
}
