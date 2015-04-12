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

    public Integer numOrders;

    public Integer numUsers;

    public Integer totalFixedOnOrder;

    public Integer totalDiscountOnOrder;

}
