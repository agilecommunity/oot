package models;

import com.avaje.ebean.annotation.Sql;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.annotations.JodaTimestamp;
import org.joda.time.DateTime;
import play.db.ebean.Model;
import utils.json.JodaTimestampOperator;

import javax.persistence.*;

@Entity
@Sql
public class DailyOrderAggregate extends Model {

    @JodaTimestamp
    @JsonSerialize(using=JodaTimestampOperator.JodaTimestampSerializer.class)
    @JsonDeserialize(using=JodaTimestampOperator.JodaTimestampDeserializer.class)
    public DateTime orderDate;

    public Integer menuItemId;

    public String code;

    public Integer numOrders;

}
