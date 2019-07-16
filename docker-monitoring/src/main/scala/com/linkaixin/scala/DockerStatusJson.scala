package com.linkaixin.scala

import scala.io.Source
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.util.Date;

//case class Memory(raw: String, percent: Float)
//
//case class DockerContainerStatus(time: Date, ID: String, name: String, cpu: Float,
//                                 memory: Memory, netIO: String, blockIO: String, PIDs: String)

object DockerStatusJson {

  //
  implicit val memoryReads: Reads[Memory] = (
    (JsPath \ "raw").read[String] and
      (JsPath \ "percent").read[String].map(x=>x.replace("%","").toFloat/(100.0f).toFloat)
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
      (JsPath \ "cpu").read[String].map(x=>x.replace("%","").trim.toFloat/(100.0f).toFloat) and
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

  def main(args: Array[String]): Unit = {
//    t1()
//    t2()
//    t3()
//    t4()
    t5()
  }

  def t1()={
    val str=
      """
        |{"raw":"46.57MiB / 1.943GiB","percent":"2.34%"}
      """.stripMargin
    val json = Json.parse(str)
    println("json="+json)

    val jsonResult: JsResult[Memory] = json.validate[Memory]
    //val scala_case_object: Memory = jsonResult.get
    val scala_case_object = jsonResult.getOrElse("失败了！")

    println("scala_case_object info="+scala_case_object)
  }

  def t2()={
    val str1 =
      """
        {"time":"2019-07-15 09:56:52" ,"ID": "719cc29845ab","name": "k8s_etcd_etcd-k8s-master1.cityworks.cn_kube-system_255ffff081268208f074e489f1ad1f86_4","cpu":"1.30%","memory":{"raw":"46.57MiB / 1.943GiB","percent":"2.34%"},"netIO":"0B / 0B","blockIO":"127MB / 23.4MB","PIDs":"11" }
      """.stripMargin

    val str=
      """
  {"time":"2019-07-15 18:06:22" ,"ID": "710cb0bc5d5f","name": "mesos-83167e45-18be-4436-b7ae-ebe74e1c6e91","cpu":"0.00%","memory":{"raw":"4.117MiB / 64MiB","percent":"6.43%"},"netIO":"11.1MB / 23.9MB","blockIO":"0B / 0B","PIDs":"2" }

      """.stripMargin

    val json = Json.parse(str)

    println("hashCode="+str.hashCode+" json="+json)

    val jsonResult: JsResult[DockerContainerStatus] = json.validate[DockerContainerStatus]
    //val scala_case_object: DockerContainerStatus = jsonResult.get
    val scala_case_object = jsonResult.getOrElse("失败了！")

    println("scala_case_object info="+scala_case_object)
  }

  def t3()={
   //hashCode=1780807198  json={"time":"2019-07-15 18:07:28" ,"ID": "708174b2ccea","name": "mesos-c38c1b96-f1c9-44d5-b01c-a01cb7540faf","cpu":"0.00%","memory":{"raw":"3.613MiB / 64MiB","percent":"5.65%"},"netIO":"11MB / 23.3MB","blockIO":"0B / 0B","PIDs":"2" }
    val hashCode=1780807198
    val str=
      """{"time":"2019-07-15 18:07:28" ,"ID": "708174b2ccea","name": "mesos-c38c1b96-f1c9-44d5-b01c-a01cb7540faf","cpu":"0.00%","memory":{"raw":"3.613MiB / 64MiB","percent":"5.65%"},"netIO":"11MB / 23.3MB","blockIO":"0B / 0B","PIDs":"2" }""".stripMargin

    if(str.hashCode!=hashCode){
      println("字符串不相等.")
    }
    else {
      println("字符串相等")
    }
    val json = Json.parse(str)
    val value=json.as[DockerContainerStatus]
    print("hashCode=value="+value)
  }

  def t4() ={

    val filename="/tmp/docker_container/1.log"

    var count=0
    var lineNumber=0

    for (line <- Source.fromFile(filename).getLines) {
      //println(line)
      lineNumber+=1
      val pos=line.indexOf(",")
      print("行号="+lineNumber+" pos="+pos )
      if(pos==30){
        val json = Json.parse(line)
        val p=json.asOpt[DockerContainerStatus]
        print("lineNumber="+lineNumber+" count="+count+"  转换成功="+p.get+"\n")
        count+=1

        //println(line.indexOf(",")+"  hashCode="+line.hashCode+" json="+json)

        val jsonResult: JsResult[DockerContainerStatus] = json.validate[DockerContainerStatus]
        val scala_case_object = jsonResult.getOrElse("失败了！")
        //println("scala_case_object info="+scala_case_object)
      }
      else {
        print("失败的行号="+lineNumber+" pos="+pos )
      }

    }
  }

  def t5()={
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
    print(dockerContainerStatus)
  }
}
