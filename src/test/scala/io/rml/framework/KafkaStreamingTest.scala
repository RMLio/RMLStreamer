package io.rml.framework

class KafkaStreamingTest extends StreamTest (
  "Kafka",
  Array(
    ("stream/kafka", "noopt"),            // standard streaming tests
    ("stream/kafka_json_ld", "json-ld")   // test with json-ld as output
  )
)
