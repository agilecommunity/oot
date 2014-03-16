package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class DailyOrder extends Model {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public Date order_date;

    @OneToOne(cascade=CascadeType.REFRESH) // 参照のみだからREFRESHでいいはず
    @JoinColumn(name="user_id")
    public LocalUser local_user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "daily_order")
    public List<DailyOrderItem> detail_items = new ArrayList<DailyOrderItem>();

    public static Finder<Long,DailyOrder> find = new Finder<Long,DailyOrder>(Long.class, DailyOrder.class);

    public static DailyOrder find_by(Date order_date, String user_id) {
        List<DailyOrder> candidate = DailyOrder.find.where().eq("order_date", order_date).eq("user_id", user_id).findList();

        if (candidate.size() != 1) {
            return null;
        }

        return candidate.get(0);
    }
}
