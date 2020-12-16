/*
 * Checking if DB already has entries for the data coming in.
 * If data is there but not as recent, existing entry is deleted
 * and this data can be added.
 */

import groovy.sql.Sql
import java.util.*
import java.sql.*

def flowFile = session.get()

if (flowFile == null) {
    return
}

def tablename = flowFile.getAttribute("tablename")
def datetime = flowFile.getAttribute("datetime")

def db = [url:'jdbc:sqlite:/Users/aviva/nifi/fitbit/fitbit2.db', user:'aviva', password:'', driver:'org.sqlite.JDBC']
def sql = Sql.newInstance(db.url, db.user, db.password, db.driver)

if(tablename == "fitbit_hr_summary") {
    def name = flowFile.getAttribute("name")
    def minutes = flowFile.getAttribute("minutes")
    def changed = false
    def rows = sql.rows("select * from fitbit_hr_summary where datetime=${datetime} and name=${name}")
    if(rows.size() > 0) {
        def deleted = false
        for(def row : rows) {
            if (row.minutes < minutes.toInteger()) {
                def params = ["datetime": datetime, "name": name, "mins": row.minutes]
                sql.execute "delete from fitbit_hr_summary where datetime=$params.datetime and name=$params.name and minutes=$params.minutes"
                deleted = true
            }
        }
        if(!deleted) { changed = false }
    }
    if(changed) {
        flowFile = session.putAttribute(flowFile, "executesql.row.count", "0")
    } else {
        flowFile = session.putAttribute(flowFile, "executesql.row.count", "1")
    }
} else if(tablename == "fitbit_daily_activity") {
    def steps = flowFile.getAttribute("steps")
    def rows = sql.rows("select * from fitbit_daily_activity where datetime=${datetime}")
    def changed = true
    if(rows.size() > 0) {
        def deleted = false
        for(def row : rows) {
          if(row.steps < steps.toInteger()) {
              def params = ["datetime":datetime, "s":row.steps]
              sql.execute"delete from fitbit_daily_activity where datetime=$params.datetime and steps=$params.s"
              deleted = true
              break
          }
        }
        if(!deleted) {
            changed = false
        }
    }
    if(changed) {
        flowFile = session.putAttribute(flowFile, "executesql.row.count", "0")
    } else {
        flowFile = session.putAttribute(flowFile, "executesql.row.count", "1")
    }
}

session.transfer(flowFile, REL_SUCCESS)

sql.close()