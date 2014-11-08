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
    public java.sql.Date menu_date;

    @Constraints.Required
    @Constraints.MaxLength(10)
    public String status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "daily_menu")
    public List<DailyMenuItem> detail_items = new ArrayList<DailyMenuItem>();

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,DailyMenu> find = new Finder<Long,DailyMenu>(Long.class, DailyMenu.class);

    public static DailyMenu find_by(java.sql.Date menu_date) {
        List<DailyMenu> candidate = DailyMenu.find.where().eq("menu_date", menu_date).findList();

        if (candidate.size() != 1) {
            return null;
        }

        return candidate.get(0);
    }

    public static List<DailyMenu> find_between(java.sql.Date menu_date_from, java.sql.Date menu_date_to) {
        List<DailyMenu> items = DailyMenu.find.where().ge("menu_date", menu_date_from).le("menu_date", menu_date_to).findList();

        return items;
    }
}
