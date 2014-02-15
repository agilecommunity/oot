package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dailyMenu")
    public List<DailyMenuItem> detailItems = new ArrayList<DailyMenuItem>();    
    
    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,DailyMenu> find = new Finder<Long,DailyMenu>(Long.class, DailyMenu.class); 
}
