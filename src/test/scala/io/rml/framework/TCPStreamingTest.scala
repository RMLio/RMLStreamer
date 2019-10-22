package io.rml.framework

/**
  * <p>Copyright 2019 IDLab (Ghent University - imec)</p>
  *
  * @author Gerald Haesendonck
  */
class TCPStreamingTest extends StreamTest(
  "TCP",
    Array(("stream/tcp", "noopt"))            // standard streaming tests
)
