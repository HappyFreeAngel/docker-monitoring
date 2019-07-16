/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linkaixin.scala

import java.util.{Date, Properties}

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.api.scala._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.util.Date

case class Memory(raw: String, percent: Float)

case class DockerContainerStatus(time: Date, ID: String, name: String, cpu: Float,
                                 memory: Memory, netIO: String, blockIO: String, PIDs: String)
/**
  * Skeleton for a Flink Streaming Job.
  *
  * For a tutorial how to write a Flink streaming application, check the
  * tutorials and examples on the <a href="http://flink.apache.org/docs/stable/">Flink Website</a>.
  *
  * To package your application into a JAR file for execution, run
  * 'mvn clean package' on the command line.
  *
  * If you change the name of the main class (with the public static void main(String[] args))
  * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
  */
object StreamingJob {
  def main(args: Array[String]) {

    playJsonTest()
    //    放这里没有用.
//    implicit val memoryReads: Reads[Memory] = (
//      (JsPath \ "raw").read[String] and
//        (JsPath \ "percent").read[String].map(x=>x.replace("%","").trim.toFloat/100.0f)
//      ) (Memory.apply _)
//
//    implicit val memoryWrites = new Writes[Memory] {
//      def writes(memory: Memory) = Json.obj(
//        "raw" -> memory.raw,
//        "percent" -> memory.percent
//      )
//    }
//
//    implicit val dockerContainerStatusReads: Reads[DockerContainerStatus] = (
//      (JsPath \ "time").read[Date] and
//        (JsPath \ "ID").read[String] and
//        (JsPath \ "name").read[String] and
//        (JsPath \ "cpu").read[String].map(x=>x.replace("%","").trim.toFloat/100.0f) and
//        (JsPath \ "memory").read[Memory] and
//        (JsPath \ "netIO").read[String] and
//        (JsPath \ "blockIO").read[String] and
//        (JsPath \ "PIDs").read[String]) (DockerContainerStatus.apply _)
//
//
//    implicit val dockerContainerStatusWrites = new Writes[DockerContainerStatus] {
//      def writes(d: DockerContainerStatus) = Json.obj(
//        "time" -> d.time,
//        "ID" -> d.ID,
//        "name" -> d.name,
//        "cpu".replaceAll("%", "").trim -> d.cpu,
//        "memory" -> d.memory,
//        "netIO" -> d.netIO,
//        "blockIO" -> d.blockIO,
//        "PIDs" -> d.PIDs
//      )
//    }
    // set up the streaming execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // use event time for the application
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    // configure watermark interval
    env.getConfig.setAutoWatermarkInterval(5000L)

    val properties = new Properties()

    val hostname = "localhost"
    //val hostname = "10.5.6.31"
    properties.setProperty("bootstrap.servers", hostname + ":9092")
    // only required for Kafka 0.8
    properties.setProperty("zookeeper.connect", hostname + ":2181")
    properties.setProperty("group.id", "test")

    val kafkaQueueTopic = "log"

    val sourceStream = new FlinkKafkaConsumer[String](kafkaQueueTopic, new SimpleStringSchema(), properties)
      .setStartFromTimestamp(1)
    //这个必须定义,否则返回警告: no implicits found for parameter formats: Formats, Manifest
    //implicit val formats = DefaultFormats

    //    val dataStream = env.addSource[String](sourceStream).filter(s => {
    //      !s.contains("CONTAINER")
    //    })

    val dataStream = env.addSource[String](sourceStream)
//
//    //必须用lazy 否则会报错. 这个注释的代码没有用.
//    lazy implicit val memoryFormat = Json.format[Memory]
//    lazy implicit val dockerContainerStatusFormat = Json.format[DockerContainerStatus]



    val dockerContainerStatusStream = dataStream.map(new MapFunction[String, DockerContainerStatus]() {

      @throws[Exception]
      override def map(input: String): DockerContainerStatus = {

        //必须定义在函数这里，
        implicit val memoryReads: Reads[Memory] = (
          (JsPath \ "raw").read[String] and
            (JsPath \ "percent").read[String].map(x=>x.replace("%","").trim.toFloat/100.0f)
          ) (Memory.apply _)

        implicit val memoryWrites = new Writes[Memory] {
          def writes(memory: Memory) = Json.obj(
            "raw" -> memory.raw,
            "percent" -> memory.percent
          )
        }

        implicit val dockerContainerStatusReads: Reads[DockerContainerStatus] = (
          (JsPath \ "time").read[Date] and
            (JsPath \ "ID").read[String] and
            (JsPath \ "name").read[String] and
            (JsPath \ "cpu").read[String].map(x=>x.replace("%","").trim.toFloat/100.0f) and
            (JsPath \ "memory").read[Memory] and
            (JsPath \ "netIO").read[String] and
            (JsPath \ "blockIO").read[String] and
            (JsPath \ "PIDs").read[String]) (DockerContainerStatus.apply _)


        implicit val dockerContainerStatusWrites = new Writes[DockerContainerStatus] {
          def writes(d: DockerContainerStatus) = Json.obj(
            "time" -> d.time,
            "ID" -> d.ID,
            "name" -> d.name,
            "cpu".replaceAll("%", "").trim -> d.cpu,
            "memory" -> d.memory,
            "netIO" -> d.netIO,
            "blockIO" -> d.blockIO,
            "PIDs" -> d.PIDs
          )
        }

        val jsonString = input.replaceAll("\\[2J\\[H", "").replace("%","").stripMargin
//        val pos = jsonString.indexOf(",")
//        print("pos=" + pos + " \n")
        var dockerContainerStatus = new DockerContainerStatus(new Date, "0000", "aaaaa", 0.0f, new Memory("0MB", 0.0f), "0KB", "kkk", "222")

        try {
          val jsonObject = Json.parse(jsonString)
          //print("标准格式的JSON="+jsonObject.toString()+"\n")
          //val json2=Json.parse(json.toString())
          //val result=jsonObject.validate[DockerContainerStatus]
          //print("结果="+result.get+"\n")
          //print("\n"+jsonString)
          dockerContainerStatus = jsonObject.as[DockerContainerStatus]
        } catch {
          case ex: Exception => print("转换异常:"+ex+"  \njsonString=" + jsonString)
        }
        finally {
          // your scala code here, such as to close a database connection
          //print("最后....")
        }

        dockerContainerStatus
      }
    }
    )


   // dockerContainerStatusStream.print()
    //dockerContainerStatusStream.keyBy(_.ID).reduce()
   // dockerContainerStatusStream.keyBy(1).print()

    //dockerContainerStatusStream.filter(d=>d.name.equals("mesos-c9f7bdf0-c876-4f9b-8238-751d2617e1fd") ).print()

    dockerContainerStatusStream.map(d=>d.name).print()

    //dockerContainerStatusStream.print()
    /*
     * Here, you can start creating your execution plan for Flink.
     *
     * Start with getting some data from the environment, like
     *  env.readTextFile(textPath);
     *
     * then, transform the resulting DataStream[String] using operations
     * like
     *   .filter()
     *   .flatMap()
     *   .join()
     *   .group()
     *
     * and many more.
     * Have a look at the programming guide:
     *
     * http://flink.apache.org/docs/latest/apis/streaming/index.html
     *
     */
    //2.创造测试数据
    env.execute("Flink Streaming Scala API Skeleton")
  }

  def playJsonTest()={
    //
    implicit val memoryReads: Reads[Memory] = (
      (JsPath \ "raw").read[String] and
        (JsPath \ "percent").read[String].map(x=>x.replace("%","").trim.toFloat/100.0f)
      ) (Memory.apply _)

    implicit val memoryWrites = new Writes[Memory] {
      def writes(memory: Memory) = Json.obj(
        "raw" -> memory.raw,
        "percent" -> memory.percent
      )
    }

    implicit val dockerContainerStatusReads: Reads[DockerContainerStatus] = (
      (JsPath \ "time").read[Date] and
        (JsPath \ "ID").read[String] and
        (JsPath \ "name").read[String] and
        (JsPath \ "cpu").read[String].map(x=>x.replace("%","").trim.toFloat/100.0f) and
        (JsPath \ "memory").read[Memory] and
        (JsPath \ "netIO").read[String] and
        (JsPath \ "blockIO").read[String] and
        (JsPath \ "PIDs").read[String]) (DockerContainerStatus.apply _)


    implicit val dockerContainerStatusWrites = new Writes[DockerContainerStatus] {
      def writes(d: DockerContainerStatus) = Json.obj(
        "time" -> d.time,
        "ID" -> d.ID,
        "name" -> d.name,
        "cpu".replaceAll("%", "").trim -> d.cpu,
        "memory" -> d.memory,
        "netIO" -> d.netIO,
        "blockIO" -> d.blockIO,
        "PIDs" -> d.PIDs
      )
    }

    val k=
      """
       {
         "time":"2019-07-15 22:54:34",
         "ID": "62fecefa76c2",
         "name": "mesos-e2bee601-9b8d-48e1-9711-df00e8b8e957",
         "cpu": "0.00%",
         "memory": {"raw": "67.91MiB / 4GiB","percent": "1.66%"},
         "netIO": "27.9kB / 28.8kB",
         "blockIO": "1.37MB / 81.9kB",
         "PIDs": "8"
       }
      """.stripMargin
    val json=Json.parse(k)

    //val p=json.asOpt[DockerContainerStatus]
    val dockerContainerStatus=json.as[DockerContainerStatus]
    print("JSON 格式验证通过,play-json工作正常!"+dockerContainerStatus)
  }

//    def t1()={
//      //implicit val memoryFormat = Json.format[Memory]
//      //implicit val dockerContainerStatusFormat = Json.format[DockerContainerStatus]
//      implicit val memoryFormat = Json.format[Memory]
//      implicit val dockerContainerStatusFormat = Json.format[DockerContainerStatus]
//      val k=
//        """
//         {
//           "time":"2019-07-15 22:54:34",
//           "ID": "62fecefa76c2",
//           "name": "mesos-e2bee601-9b8d-48e1-9711-df00e8b8e957",
//           "cpu": "0.00%",
//           "memory": {"raw": "67.91MiB / 4GiB","percent": "1.66%"},
//           "netIO": "27.9kB / 28.8kB",
//           "blockIO": "1.37MB / 81.9kB",
//           "PIDs": "8"
//         }
//        """.stripMargin
//      val json=Json.parse(k)
//      val dockerContainerStatus=json.as[DockerContainerStatus]
//      print(dockerContainerStatus)
//    }

}
