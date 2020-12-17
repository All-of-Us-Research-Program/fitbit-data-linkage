/*
 * Splitting date range into 24 hour-1 minute increments for the Fitbit API
 */

import java.text.SimpleDateFormat
import java.util.Calendar

def flowFile = session.get();

if (flowFile == null) {
    return
}

try {
    def previous_sync = flowFile.getAttribute("previous_sync")
    def latest_sync = flowFile.getAttribute("latest_sync")

    SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    TimeZone utcZone = TimeZone.getTimeZone("America/New_York")
    inputDateFormat.setTimeZone(utcZone)

    SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    TimeZone outputZone = TimeZone.getTimeZone("America/New_York")
    outputDateFormat.setTimeZone(outputZone)

    Date prev = inputDateFormat.parse(previous_sync)
    Date late = inputDateFormat.parse(latest_sync)

    Date tmp1 = prev

    // adding 1 minute less than 24 hours to tmp1:
    Calendar cal = Calendar.getInstance();
    cal.setTime(inputDateFormat.parse(outputDateFormat.format(tmp1)))
    cal.add(Calendar.MINUTE, 1439)
    tmp2 = inputDateFormat.parse(outputDateFormat.format(cal.getTime()))

    while(tmp2.before(late)) {

        def newFF = session.create(flowFile)
        newFF = session.putAllAttributes(newFF, [
            "previous_day": tmp1.format("yyyy-MM-dd"),
            "previous_time": tmp1.format("HH:mm"),
            "latest_day": tmp2.format("yyyy-MM-dd"),
            "latest_time": tmp2.format("HH:mm")
        ])
        session.transfer(newFF, REL_SUCCESS)

        tmp1 = tmp2

        Calendar c = Calendar.getInstance();
        c.setTime(inputDateFormat.parse(outputDateFormat.format(tmp1)))
        c.add(Calendar.MINUTE, 1439)
        tmp2 = inputDateFormat.parse(outputDateFormat.format(c.getTime()))
    }

    flowFile = session.putAllAttributes(flowFile, [
        "previous_day": tmp1.format("yyyy-MM-dd"),
        "previous_time": tmp1.format("HH:mm"),
        "latest_day": late.format("yyyy-MM-dd"),
        "latest_time": late.format("HH:mm")
    ])
    session.transfer(flowFile, REL_SUCCESS)

} catch (Exception ex) {
    flowFile = session.putAttribute(flowFile, "datetime.error", ex.getMessage())
    session.transfer(flowFile, REL_FAILURE)
}
