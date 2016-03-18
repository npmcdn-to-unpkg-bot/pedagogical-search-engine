package utils.java;

public class Pair<U, V> {
    U m_left;
    V m_right;

    public Pair(U left, V right) {
        m_left = left;
        m_right = right;
    }

    public void setLeft(U left) {
        m_left = left;
    }

    public void setRight(V right) {
        m_right = right;
    }

    public U getLeft() {
        return m_left;
    }

    public V getRight() {
        return m_right;
    }
}
