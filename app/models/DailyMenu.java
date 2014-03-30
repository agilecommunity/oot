package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

/**
 * DailyLunchMenu entity managed by Ebean
 */
@Entity
public class DailyMenu extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Date menu_date;

    public String status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "daily_menu")
    public List<DailyMenuItem> detail_items = new ArrayList<DailyMenuItem>();

    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,DailyMenu> find = new Finder<Long,DailyMenu>(Long.class, DailyMenu.class);

    public static DailyMenu find_by(Date menu_date) {
        List<DailyMenu> candidate = DailyMenu.find.where().eq("menu_date", menu_date).findList();

        if (candidate.size() != 1) {
            return null;
        }

        return candidate.get(0);
    }
}
