package app.nrs.specialtopic.tag;

import android.content.Context;

import com.orm.SugarRecord;

/**
 * Created by ravisvi on 05/11/14.
 */
public class Entries extends SugarRecord<Entries> {
    String name;
    String creationDate;
    String dueDate;
    String amount;
    String type; // plus or minus
    String owner;
    public Entries(){
        //use sugar orm 1.3, else uncomment the following line.
        // super(context);
    }

    public Entries(String name, String creationDate, String dueDate, String amount, String type, String owner){
        this.name = name;
        this.creationDate = creationDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.type = type;
        this.owner = owner;
    }
}
