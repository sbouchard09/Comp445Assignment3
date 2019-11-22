package UDPServer;

/**************************************
 Using template as provided on Moodle
 **************************************/
import Packet.Packet;

import Packet.Packet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;


import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class UDPServer {

    private static final int DATA = 0;
    private static final int SYN = 1;
    private static final int SYNACK = 2;
    private static final int ACK = 3;
    private static final int NACK = 4;
    private static final int FIN = 5;



    //private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);

    public void listenAndServe(int port, String directory) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            System.out.println("Listening on port: " + port + " :)");
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                buf.clear();
                SocketAddress router = channel.receive(buf);


                Packet response = assemblePackets(buf, directory, router, channel);
                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                String payload = new String(packet.getPayload(), UTF_8);

                // Send the response to the router not the client.
                channel.send(response.toBuffer(), router);

                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.
                Packet resp = packet.toBuilder()
                        .setPayload(payload.getBytes())
                        .create();
                channel.send(resp.toBuffer(), router);

            }
        }
    }

    private Packet assemblePackets(ByteBuffer buffer, String directory, SocketAddress router, DatagramChannel channel) throws IOException {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        Packet packet = null;

        do {
            buffer.flip();
            packet = Packet.fromBuffer(buffer);
            buffer.flip();
            String payload = new String(packet.getPayload(), StandardCharsets.UTF_8);
            if(packet.getType() == SYN) {
                return handleHandshake(packet);
            } else if(packet.getType() == DATA) {
                sendAck(packet, router, channel);
            }else if(packet.getType() == FIN) {
                // assemble message in order
                StringBuilder messageBuilder = new StringBuilder();
                SortedSet<Integer> keys = new TreeSet<>(map.keySet());
                for(Integer key : keys) {
                    messageBuilder.append(map.get(key));
                }

                Response response = new Response(directory);
                response.handleRequest(messageBuilder.toString());
                String resp = response.getResponse();

                return packet.toBuilder().setPayload(resp.getBytes(StandardCharsets.UTF_8)).create();
            } else {
                return null;
            }

        }while(packet.getType() !=  FIN);

        return null;
    }

    private void sendAck(Packet packet, SocketAddress router, DatagramChannel channel) throws IOException {
        Packet response = packet.toBuilder().setSequenceNumber(packet.getSequenceNumber()).setType(ACK).create();
        channel.send(response.toBuffer(), router);
    }

    private Packet handleHandshake(Packet packet) {
        String message = "SYNACK";
        Packet response = packet.toBuilder()
                .setSequenceNumber(packet.getSequenceNumber()+1)
                .setType(SYNACK).setPayload(message
                        .getBytes(StandardCharsets.UTF_8)).create();

        return response;
    }
}