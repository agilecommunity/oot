package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.annotations.JodaTimestamp;
import org.joda.time.DateTime;
import play.db.ebean.Model;
import utils.json.JodaTimestampOperator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class DailyOrderStat extends Model {

    @JodaTimestamp
    @JsonSerialize(using=JodaTimestampOperator.JodaTimestampSerializer.class)
    @JsonDeserialize(using=JodaTimestampOperator.JodaTimestampDeserializer.class)
    @Id
    public DateTime orderDate;

    public String menuStatus;

    @OneToOne
    public DailyOrderStatItem allStat;

    // 本来はDailyOrderStatItemで表現したい
    // が、allStatと同じ実装をすると、bentoStat, sideStatがallStatと同じ値になってしまい
    // 上手くマッピングできない
    public Integer bentoNumUsers;
    public Integer bentoNumOrders;
    public Integer bentoTotalFixedOnOrder;
    public Integer bentoTotalDiscountOnOrder;

    public Integer sideNumUsers;
    public Integer sideNumOrders;
    public Integer sideTotalFixedOnOrder;
    public Integer sideTotalDiscountOnOrder;
}
