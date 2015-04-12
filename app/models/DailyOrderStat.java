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
    public DateTime orderDate;

    public String menuStatus;

    public DailyOrderStatItem allStat;

    public DailyOrderStatItem bentoStat;

    public DailyOrderStatItem sideStat;
}
