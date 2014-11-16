package models;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

import play.data.validation.Constraints;
import play.db.ebean.Model;

@Entity
public class DailyMenuItem extends Model {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    
    @ManyToOne
    @JoinColumn(name = "daily_menu_id")
    @JsonIgnore
    public DailyMenu dailyMenu;
    
    @OneToOne(cascade=CascadeType.REFRESH) // 参照のみだからREFRESHでいいはず
    @JoinColumn(name="menu_item_id")
    public MenuItem menuItem;
    
    /**
     * Generic query helper for entity Lunch with id Long
     */
    public static Finder<Long,DailyMenuItem> find = new Finder<Long,DailyMenuItem>(Long.class, DailyMenuItem.class); 
}
