package models;

import com.avaje.ebean.annotation.Sql;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import models.annotations.JodaDate;
import org.joda.time.DateTime;
import play.db.ebean.Model;
import utils.json.JodaDateOperator;

import javax.persistence.*;

@Entity
@Sql
public class DailyOrderAggregate extends Model {

    @JodaDate
    @JsonSerialize(using=JodaDateOperator.JodaDateSerializer.class)
    @JsonDeserialize(using=JodaDateOperator.JodaDateDeserializer.class)
    public DateTime orderDate;

    public Integer menuItemId;

    public String code;

    public Integer numOrders;

}
