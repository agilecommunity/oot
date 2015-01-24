package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.db.ebean.Model;

@Entity
public class DailyOrder extends Model {

    Logger.ALogger logger = Logger.of("application.models.DailyOrder");

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    @Constraints.Required
    public java.sql.Date orderDate;

    @OneToOne(cascade=CascadeType.REFRESH, optional = false) // 参照のみだからREFRESHでいいはず
    @JoinColumn(name="user_id", nullable = false)
    public LocalUser localUser;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dailyOrder")
    public List<DailyOrderItem> detailItems = new ArrayList<DailyOrderItem>();

    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<ValidationError>();

        if (localUser == null) { // なぜかValidateでチェックしてくれないので独自にやる
            logger.debug("is_valid localUser is null");
            errors.add(new ValidationError("localUser", "error.required"));
        }

        if (errors.size() == 0) {
            return null;
        }

        return errors;
    }

    public static Finder<Long,DailyOrder> find = new Finder<Long,DailyOrder>(Long.class, DailyOrder.class);

    public static DailyOrder findBy(java.sql.Date orderDate, String userId) {
        List<DailyOrder> candidate = DailyOrder.find.where().eq("order_date", orderDate).eq("user_id", userId).findList();

        if (candidate.size() != 1) {
            return null;
        }

        return candidate.get(0);
    }

    public static List<DailyOrder> findBy(java.sql.Date order_date) {
        return DailyOrder.find.where().eq("order_date", order_date).findList();
    }
}
