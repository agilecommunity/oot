package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.annotations.JodaTimestamp;
import org.joda.time.DateTime;
import play.db.ebean.Model;
import utils.json.JodaTimestampOperator;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DailyOrderStatItem extends Model {

    @JodaTimestamp
    @JsonSerialize(using=JodaTimestampOperator.JodaTimestampSerializer.class)
    @JsonDeserialize(using=JodaTimestampOperator.JodaTimestampDeserializer.class)
    @Id
    public DateTime orderDate;

    public Integer numOrders;

    public Integer numUsers;

    public Integer totalFixedOnOrder;

    public Integer totalDiscountOnOrder;

}
