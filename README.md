# Readme - Big Data Project 2017

This project is the z of the BigData Engineering Modul of Master of Science in Informations System at the University of Applied Science in Munster
The Project is about a NFL Pipeline and use twitter input to count and show trends regarding to NFL and American Football.
You find the presentation of the project with all assessment criteria in the assets/folder.

You have to use the following instructions to get Started with the Pipeline.

## *Analyzing nfl-twitter data by using CDH 5.8*

Before you get started with the application, you will first need to install CDH 5.8. Specifically, you will need Hadoop, Flume, Oozie, and Hive. The easiest way to get the core components is to use Cloudera Manager to set up your initial environment. You can download the vm image here: (http://www.cloudera.com/downloads/quickstart_vms/5-8.html) How to set up the configuration will be explained in detail within the following steps.

## 1. **Clone git project and build artifacts**
```
$ git clone https://github.com/Cubaner/BigDataProjekt.git
$ cd BigDataProjekt
$ mvn clean install
```

## 2. **Configure the Flume agent**

Start HDFS-Service in the Cloudera Manager Web UI
Create the HDFS directory hierarchy for the Flume sink.
Make sure that it will be accessible by the user running the Oozie workflow to start the application.  
```
$ su -l
```

```
$ hdfs dfs -mkdir /user/cloudera/tweets
$ hadoop fs -chown -R cloudera:cloudera /user/cloudera
$ hadoop fs -chmod -R 777 /user/cloudera
$ /etc/init.d/flume-ng-agent start
```
Create the Flume agent in Cloudera Manager Web UI
Configure the Agent and add your twitter key and token
- Create a flume agent by clicking the arrow next to `Cloudera Quickstart (CDH 5..) [v]`
- Select `flume` and click `continue`
- Select the row with: `HBase, HDFS, Solr, Zookeeper` and click again `continue`
- The last step include the selecting of a host. You will find only one possible choice. Select them and click three times `continue`

You continue with the configuration of the flume agent

- Go to the Flume Service page, click on the `Configuration` tab
- Go to Agent on the left side
- Set the Agent Name property to `TwitterAgent`
- Copy the content of the TwitterAgents below and paste them into `Configure File`

```
TwitterAgent.sources = Twitter
TwitterAgent.channels = MemChannel
TwitterAgent.sinks = HDFS

TwitterAgent.sources.Twitter.type = de.fhm.bigdata.projekt.dataimport.TwitterSource
TwitterAgent.sources.Twitter.channels = MemChannel
TwitterAgent.sources.Twitter.consumerKey = <--->
TwitterAgent.sources.Twitter.consumerSecret = <---> #specific user Tokens
TwitterAgent.sources.Twitter.accessToken = <--->
TwitterAgent.sources.Twitter.accessTokenSecret = <--->
TwitterAgent.sources.Twitter.keywords = nfl, nflran, gopacksgo, hawks, probowl, rannfl, superbowl, brady, super bowl, mattryan, brady, pats, americanfootball, football, rodgers, draft, touchdown, quarterback, flag, 49ers, cardinals

TwitterAgent.sinks.HDFS.channel = MemChannel
TwitterAgent.sinks.HDFS.type = hdfs
TwitterAgent.sinks.HDFS.hdfs.path = hdfs://quickstart.cloudera:8020/user/cloudera/tweets/%Y/%m/%d/%H/
TwitterAgent.sinks.HDFS.hdfs.fileType = DataStream
TwitterAgent.sinks.HDFS.hdfs.writeFormat = Text
TwitterAgent.sinks.HDFS.hdfs.batchSize = 100
TwitterAgent.sinks.HDFS.hdfs.rollSize = 10000
TwitterAgent.sinks.HDFS.hdfs.rollCount = 10000
TwitterAgent.sinks.HDFS.hdfs.rollInterval = 600

TwitterAgent.channels.MemChannel.type = memory
TwitterAgent.channels.MemChannel.capacity = 10000
TwitterAgent.channels.MemChannel.transactionCapacity = 100

#Proxy Settings if necessary
TwitterAgent.sources.Twitter.enableProxy = false
TwitterAgent.sources.Twitter.proxyHost = 10.60.17.102
TwitterAgent.sources.Twitter.proxyPort = 8080
```
- Click `Save Changes`

Add the dataimport.jar to the Flume classpath
Create first the directory
```
$ mkdir /var/lib/flume-ng/plugins.d
$ mkdir /var/lib/flume-ng/plugins.d/twitter-streaming
$ mkdir /var/lib/flume-ng/plugins.d/twitter-streaming/lib

$ mkdir /usr/lib/flume-ng/plugins.d
$ mkdir /usr/lib/flume-ng/plugins.d/twitter-streaming
$ mkdir /usr/lib/flume-ng/plugins.d/twitter-streaming/lib

$ cp ~/BigDataProjekt/bigdata-nfl-dataimport/target/bigdata-nfl-dataimport-1.0.0-SNAPSHOT.jar /var/lib/flume-ng/plugins.d/twitter-streaming/lib/
$ cp ~/BigDataProjekt/bigdata-nfl-dataimport/target/bigdata-nfl-dataimport-1.0.0-SNAPSHOT.jar /usr/lib/flume-ng/plugins.d/twitter-streaming/lib/
```

## 3. **Configure Oozie and create the relevant directory in HDFS**

Ensures the accessibility to copy files

Copy the dataprocessing-SNAPSHOT.jar into oozie-workflows/lib/ (@ ~ you have to insert your own directory path)
If asking to overwrite, say yes!
```
$ cp ~/BigDataProjekt/bigdata-nfl-dataprocessing/target/bigdata-nfl-dataprocessing-1.0.0-SNAPSHOT.jar
~/BigData/BigDataProjekt/bigdata-nfl-oozie/oozie-workflows/lib/
```
Change permissions in HDFS user/cloudera/ to 777 (read 7, write 7, execute 7)
Click in Cloudera Manager Web UI and start Hue-Service, then go to the Hue Web UI and click on: `File Browser` -> `User` -> right click on cloudera -> `actions`

Copy bigdata-nfl-oozie Ordners in HDFS
```
$ hdfs dfs -put ~/BigDataProjekt/bigdata-nfl-oozie /user/cloudera/bigdata-nfl-oozie
```
Create oozie-workflows directory in HDFS
```
$ hdfs dfs -mkdir /user/cloudera/oozie-workflows
```
Copy oozie-workflows (directory below with same name and necessary oozie data) into HDFS folder oozieworkflows
```
$ hdfs dfs -copyFromLocal ~/BigDataProjekt/bigdata-nfl-oozie/oozie-workflows/oozie-workflows /user/cloudera/oozie-workflows/
```

## 4. **Configure Hive**

Add hive.jar to /usr/lib/hive/lib/bigdata-nfl-hive-1.0.0-SNAPSHOT.jar
```
$ cp ~/BigDataProjekt/bigdata-nfl-hive/target/bigdata-nfl-hive-1.0.0-SNAPSHOT.jar /usr/lib/hive/lib/
```
Start Hive-Service in Cloudera Manager Web UI.
Open the Hue Webclient, Create new table and execute the commands below in the textfield
-> `Hue`-> `Hue Web UI` -> `Query Editors` -> `Hive`
```
$ ADD JAR /usr/lib/hive/lib/bigdata-nfl-hive-1.0.0-SNAPSHOT.jar;

CREATE EXTERNAL TABLE tweets (
  id BIGINT,
  created_at STRING,
  source STRING,
  favorited BOOLEAN,
  retweeted_status STRUCT<
    text:STRING,
    user:STRUCT<screen_name:STRING,name:STRING>,
    retweet_count:INT>,
  entities STRUCT<
    urls:ARRAY<STRUCT<expanded_url:STRING>>,
    user_mentions:ARRAY<STRUCT<screen_name:STRING,name:STRING>>,
    hashtags:ARRAY<STRUCT<text:STRING>>>,
  text STRING,
  user STRUCT<
    screen_name:STRING,
    name:STRING,
    friends_count:INT,
    followers_count:INT,
    statuses_count:INT,
    verified:BOOLEAN,
    utc_offset:INT,
    time_zone:STRING>,
  in_reply_to_screen_name STRING
)
PARTITIONED BY (datehour INT)
ROW FORMAT SERDE 'de.fhm.bigdata.projekt.hive.JSONSerDe'
LOCATION '/user/cloudera/tweets';
```

## 5. **Configure HBase**

Execute the following command in HBase Shell:
```
$ create "hashtags", { NAME => "hashtag_family", VERSIONS => 3 }
$ create ""teams"", "team", "rank', "devision", "synonyms"
```

## 6. **Start the workflow coordinator**

Navigate to the project and enter the folder bigdata-nfl-oozie.
Afterwards run the following command in terminal and start the workflow coordinator job
```
$ ~/BigDataProjekt/bigdata-nfl-oozie/
$ oozie job -run -oozie http://quickstart.cloudera:11000/oozie -config job.properties
```

## 7. **Set up the tomcat**

Download and unzip Tomcat 8
(https://tomcat.apache.org/download-80.cgi#8.0.41)

Copy unziped folder to /opt/ (su-Permissions needed)

Add the following wars to Tomcat directory in /opt/
```
$ cp /home/cloudera/BigDataProjekt/bigdata-nfl-hbase-interface/target/bigdata-nfl-hbase-interface.war /opt/apache-tomcat-8.0.41/webapps/
$ cp /home/cloudera/BigDataProjekt/bigdata-nfl-presentation/target/bigdata-nfl-presentation.war /opt/apache-tomcat-8.0.41/webapps/
```
Configure the Apache Port in the configuration file.
Browse to:
```
$ /opt/apache-tomcat-8.0.32/conf/
```
Open the server.xml and change port to 8081:
```
$ <Connector port="8081" protocol="HTTP/1.1"
	  				 connectionTimeout="20000"
             redirectPort="8443" />
```
Start the tomcat with sudo command
```
$ sudo bash /opt/apache-tomcat-8.0.41/bin/catalina.sh start
```
Website available by using following uri
```
$ http://localhost:8081/bigdata-nfl-presentation
```
