package metadata;

import lombok.Getter;
import lombok.Setter;
/**
 * Created by SohmenL on 15.09.2017.
 */
@Getter
@Setter
public class PublicationDate {
    String day;
    String month;
    String year;
    int rank;
    public String toString(){
        return("day: "+day+"month: "+month+"year: "+year);
    }
}
