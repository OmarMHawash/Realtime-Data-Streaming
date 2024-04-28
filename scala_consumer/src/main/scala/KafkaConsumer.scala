import org.apache.log4j.BasicConfigurator
import org.apache.log4j.varia.NullAppender
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.KafkaUtils

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import org.mongodb.scala.MongoClient
import org.mongodb.scala._

import com.helpers.Helpers._

object KafkaConsumer {
  def main(args: Array[String]): Unit = {

    val nullAppender = new NullAppender
    BasicConfigurator.configure(nullAppender)

    val conf = new SparkConf().setMaster("local[2]").setAppName("twitterConsumer")
    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "tweets_group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("tweets")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    val lines = stream.map(_.value)
    lines.print()

    lines.foreachRDD { rdd =>
      rdd.foreachPartition { records =>
        // had to connect to mongo here because of serialization issues
        val mongoClient = MongoClient()
        val database = mongoClient.getDatabase("tweets")
        val collection = database.getCollection("tweets")

        val documents = records.map { record =>
          val stripRecord = record.replace("\\\"", "")
          Document(stripRecord)
        }.toList

        if (documents.nonEmpty) {
          collection.insertMany(documents).results()
        }
        mongoClient.close()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}