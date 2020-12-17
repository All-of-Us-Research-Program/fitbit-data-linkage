# Fitbit data linkage

### Data pipeline for Fitbit data in NiFi

### Introduction
All of Us is interested in Fitbit data to be able to have another set of measurements of participants’ heath, and in order to look at the impact of activity level on health. AoU currently has some Fitbit data, but I was working on setting up a more sustainable pipeline for repeated use.

### Linkage
This pipeline links participants with their own Fitbit data. For only the first time a participant's data is linked, they must follow a link through the portal to give authorization. At any point after that, the participant may revoke access, and the API calls with their credentials will no longer work.

### Models
The Fitbit data is stored in 4 tables, each with a different kind of data collected. See `fitbit.py` for schemas.

### NiFi Pipeline
##### Overview:
This pipeline works by reading in a user’s refresh access token and the previous date and time that their data was synced and put into the database. The flow then uses the refresh token to get an access token, and the latest time that the participant synced their Fitbit. During this flow, all data between the previous sync and latest sync will be fetched. The refresh token and latest sync are now saved (latest sync is saved as previous sync), and the next time the flow is run, all data between the latest sync and next time the participant syncs their Fitbit will be fetched, and so on. This ensures that data is not queried for multiple times. After getting the access token, the the Fitbit web API is called to get all of the data. The database is queried to ensure that no duplicate entries are being added, and finally the records are written to the database. Failures during API calls and database queries are handled by writing the flowfile out to a local error folder.  
Open template `Fitbit.xml` in NiFi to see specifics.  

Specifics on process groups:  

1. get access token  
This process group reads in a file with a participant’s refresh token and previous sync datetime. The Fitbit API is called with the refresh token to get an access token. Tokens and the userID are added to flowfile attributes.
2. get latest sync  
This process group queries the API for the user’s profile in order to get the latest sync. The latest refresh token and the latest sync (as previous_sync) are written to a file for a future flow.
3. get fitbit data from API  
A script divides the time between the previous sync and latest sync into <24 hour slices, and the API is invoked 4 times to fetch the 4 data types:
    - Daily activity summary
    - Daily heart rate summary
    - Intraday heart rate
    - Intraday steps  
The data is cleaned up and an ID is added for the record to be added to the database.
4. check if distinct entry  
This process group checks if the entry is already in the database by executing sql statements. For the daily summaries, a script queries the database, and will delete the existing entry if the current record has updated values for the same day. Duplicate entries are routed to failure.
5. write to database  
The record has to be converted to Avro Record format in order to be written to the database. A DBCPConnectionPool controller also must be set up with a JDBC SQLite driver to connect to the database (the same controller can be used for the check if distinct entry process group as well).  
NOTE: Check if distinct entry and writing to the database cannot both be running at the same time, or else you will get errors that the database file is locked.

### Next steps
- All of Us will need to register a new Fitbit API Application in order to get access to the API. In order to get intraday data, AoU will have to submit a request, because that data is currently only available to personal applications for the primary user. However, Fitbit says that they are very supportive of nonprofits and research projects, and it should not be too hard to get access.
- The pipeline will have to be set up for participants. The flow currently is just reading in tokens and syncs from a local file, but the participant ID is necessary as well and all of this will no doubt be coming from another source.
- Finally, it would be good to set up better error handling.


### Files:

NiFi template:
- Fitbit.xml

Model:
- fitbit.py

Scripts:
- db_script.groovy
- fitbit_date_script.groovy
