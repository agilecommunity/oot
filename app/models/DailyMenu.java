package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
public class DailyMenu extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    public java.sql.Date menuDate;

    @Constraints.Required
    @Constraints.MaxLength(10)
    public String status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dailyMenu")
    public List<DailyMenuItem> detailItems = new ArrayList<DailyMenuItem>();

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,DailyMenu> find = new Finder<Long,DailyMenu>(Long.class, DailyMenu.class);

    public static DailyMenu findBy(java.sql.Date menuDate) {
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
