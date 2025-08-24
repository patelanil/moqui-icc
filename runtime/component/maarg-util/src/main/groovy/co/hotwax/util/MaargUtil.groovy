package co.hotwax.util

import co.hotwax.auth.JWTManager
import com.cronutils.descriptor.CronDescriptor
import com.cronutils.model.time.ExecutionTime
import groovy.transform.CompileStatic
import org.moqui.entity.EntityValue
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.math.RoundingMode
import java.text.NumberFormat
import java.time.ZonedDateTime

@CompileStatic
class MaargUtil {
    protected final static Logger logger = LoggerFactory.getLogger(MaargUtil.class);

    @Deprecated
    public static String getOmsJwtToken(ExecutionContextFactoryImpl ecfi , String userName) {
        return JWTManager.createJwt(ecfi.getExecutionContext(), ["userLoginId": userName]);
    }

    @Deprecated
    public static String getOmsJwtToken(ExecutionContextFactoryImpl ecfi) {
        return getOmsJwtToken(ecfi, ecfi.getExecutionContext().getUser().getUsername());
    }

    @Deprecated
    public static String getOmsInstanceUrl(ExecutionContextFactoryImpl ecfi) {
        return System.getProperty("ofbiz.instance.url");
    }


    static String getCronDescription(String cronExpression, Locale locale) {
        if (cronExpression == null || cronExpression.isEmpty()) return null
        if (locale == null) locale = Locale.getDefault()
        try {
            return "${CronDescriptor.instance(locale).describe(org.moqui.impl.service.ScheduledJobRunner.getCron(cronExpression))} ${TimeZone.getDefault().getID()} time"
        } catch (UnsupportedOperationException e) {
            logger.error("Error while generating cron description ${cronExpression} ${e.message}")
            return "Description " + e.message
        }  catch (Exception e) {
            logger.error("Error while generating cron description ${cronExpression}: ${e.message}")
            return e.message
        }
    }
    static ZonedDateTime getNextExecutionTime(String cronExpression, TimeZone timeZone) {
        if (cronExpression == null || cronExpression.isEmpty()) {
            return null
        }
        if (timeZone == null) timeZone = TimeZone.getDefault()

        ExecutionTime cronExecutionTime = org.moqui.impl.service.ScheduledJobRunner.getExecutionTime(cronExpression)
        return cronExecutionTime.nextExecution(java.time.ZonedDateTime.now()).orElse(null).toInstant().atZone(timeZone.toZoneId())
    }

    // Added getCompactNumber method.
    // In JDK 11 and below, NumberFormat does not provide a compact number formatter.
    // From JDK 12+, NumberFormat.getCompactNumberInstance(Locale, NumberFormat.Style)
    // can be used directly. This implementation provides a fallback for JDK 11
    static String getCompactNumber(Number value, Locale locale) {
        if (value == null) return null;

        double d = value.doubleValue();
        double abs = Math.abs(d);
        double scaled;
        String suffix;

        if (abs < 1_000d) {
            scaled = d;        suffix = "";
        } else if (abs < 1_000_000d) {
            scaled = d / 1_000d;       suffix = "K";   // thousands
        } else if (abs < 1_000_000_000d) {
            scaled = d / 1_000_000d;   suffix = "M";   // millions
        } else if (abs < 1_000_000_000_000d) {
            scaled = d / 1_000_000_000d; suffix = "B"; // billions
        } else {
            scaled = d / 1_000_000_000_000d; suffix = "T"; // trillions
        }

        NumberFormat nf = NumberFormat.getNumberInstance(locale != null ? locale : Locale.US);
        nf.setMaximumFractionDigits(1);
        nf.setRoundingMode(RoundingMode.HALF_UP);

        return nf.format(scaled) + suffix;
    }

}
