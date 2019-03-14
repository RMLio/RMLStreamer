#!/bin/bash
KAFKA_DIR='kafka_2.11-0.10.2.2'

./kafka-test-server-setup.sh --kafka-dir ${KAFKA_DIR}/ \
--zookeeper-property ${KAFKA_DIR}/config/zookeeper.properties \
--broker-property ${KAFKA_DIR}/config/server.properties
