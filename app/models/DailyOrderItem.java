package models;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class DailyOrderItem extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Short numOrders;

    @ManyToOne
    @JoinColumn(name = "daily_order_id")
    @JsonIgnore
    public DailyOrder dailyOrder;

    @OneToOne(cascade=CascadeType.REFRESH) // 参照のみだからREFRESHでいいはず
    @JoinColumn(name="menu_item_id")
    public MenuItem menuItem;

    public static Finder<Long,DailyOrderItem> find = new Finder<Long,DailyOrderItem>(Long.class, DailyOrderItem.class);
}
