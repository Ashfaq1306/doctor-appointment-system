// Any class that can be searched by a keyword implements this interface.
public interface Searchable {

    boolean matches(String keyword);
}
