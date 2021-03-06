package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.annotations.JodaTimestamp;
import org.joda.time.DateTime;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.db.ebean.Model;
import utils.json.JodaTimestampOperator;

@Entity
public class DailyMenu extends Model {

    public static final String StatusPrepared = "prepared";
    public static final String StatusOpen = "open";
    public static final String StatusClosed = "closed";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    @JodaTimestamp
    @JsonSerialize(using= JodaTimestampOperator.JodaTimestampSerializer.class)
    @JsonDeserialize(using=JodaTimestampOperator.JodaTimestampDeserializer.class)
    public DateTime menuDate;

    @Constraints.Required
    @Constraints.MaxLength(10)
    public String status = StatusPrepared;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dailyMenu")
    @OrderBy("id")
    public List<DailyMenuItem> detailItems = new ArrayList<DailyMenuItem>();

    public List<ValidationError> validate() {
        ArrayList<ValidationError> errors = new ArrayList<ValidationError>();

        this.validateOverlappedItem(errors);

        return errors.isEmpty() ? null : errors;
    }

    private void validateOverlappedItem(ArrayList<ValidationError> errors) {
        HashMap<Long, Boolean> itemIds = new HashMap<Long, Boolean>();

        for (DailyMenuItem item : this.detailItems) {
            if (itemIds.containsKey(item.menuItem.id)) {
                errors.add(new ValidationError("detailItems", "error.oot.dailyMenu.overlappedItem"));
                return;
            }
            itemIds.put(item.menuItem.id, true);
        }
    }

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,DailyMenu> find = new Finder<Long,DailyMenu>(Long.class, DailyMenu.class);

    public static DailyMenu findBy(DateTime menuDate) {
        List<DailyMenu> candidate = DailyMenu.find.where().eq("menuDate", menuDate).findList();

        if (candidate.size() != 1) {
            return null;
        }

        return candidate.get(0);
    }

    public static List<DailyMenu> findBetween(java.sql.Date menuDateFrom, java.sql.Date menuDateTo) {
        List<DailyMenu> items = DailyMenu.find.where().ge("menuDate", menuDateFrom).le("menuDate", menuDateTo).findList();

        return items;
    }
}
