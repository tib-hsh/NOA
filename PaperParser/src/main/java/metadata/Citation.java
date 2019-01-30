package metadata;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 */
@Getter
@Setter
/**
 * Created by SohmenL on 21.08.2017.
 */
public class Citation {
    List<Author> Authors = new ArrayList<>();
    String year;
    String title;
    String journal;
    String volume;
    String issue;
    List<ID> IDs = new ArrayList<>();
    String text;
}
