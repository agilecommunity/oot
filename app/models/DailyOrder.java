package models;

import java.util.ArrayList;
import java.util.Collection;
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
import javax.persistence.Transient;

import play.Logger;
import play.data.validation.Validation;
import play.db.ebean.Model;

import com.avaje.ebean.validation.NotNull;

@Entity
public class DailyOrder extends Model {

    Logger.ALogger logger = Logger.of("application.models.DailyOrder");

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @NotNull
    public Date order_date;

    @OneToOne(cascade=CascadeType.REFRESH, optional = false) // 参照のみだからREFRESHでいいはず
    @JoinColumn(name="user_id", nullable = false)
    public LocalUser local_user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "daily_order")
    public List<DailyOrderItem> detail_items = new ArrayList<DailyOrderItem>();

    @Transient
    public Collection errors;

    public Boolean is_valid() {

        if (local_user == null) { // なぜかValidateでチェックしてくれないので独自にやる
            logger.debug("is_valid local_user is null");
            return false;
        }

        if (order_date == null) { // なぜかValidateでチェックしてくれないので独自にやる
            logger.debug("is_valid order_date is null");
            return false;
        }

        errors = Validation.getValidator().validate(this);

        logger.debug(String.format("is_valid errors:%s", errors.toString()));

        return errors.size() == 0;
    }

    public static Finder<Long,DailyOrder> find = new Finder<Long,DailyOrder>(Long.class, DailyOrder.class);

    public static DailyOrder find_by(Date order_date, String user_id) {
        List<DailyOrder> candidate = DailyOrder.find.where().eq("order_date", order_date).eq("user_id", user_id).findList();

        if (candidate.size() != 1) {
            return null;
        }

        return candidate.get(0);
    }

    public static List<DailyOrder> find_by(Date order_date) {
        return DailyOrder.find.where().eq("order_date", order_date).findList();
    }
}
