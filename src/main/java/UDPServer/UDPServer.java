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

    private final String routerAddress = "localhost";
    private HashMap<Long, String> requestMap = new HashMap<Long, String>();

    //private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);

    public void listenAndServe(int port, String directory) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(routerAddress, port));
            System.out.println("Listening on port: " + port + " :)");
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {
                buf.clear();
                SocketAddress router = channel.receive(buf);


                Packet response = assemblePackets(buf, directory, router, channel);

                // Send the response to the router not the client.
                channel.send(response.toBuffer(), router);
            }
        }
    }

    private Packet assemblePackets(ByteBuffer buffer, String directory, SocketAddress router, DatagramChannel channel) throws IOException {
        Packet packet = null;
        HashMap<Long, Packet> packets = new HashMap<>();

        buffer.flip();
        packet = Packet.fromBuffer(buffer);
        buffer.flip();
        String payload = new String(packet.getPayload(), StandardCharsets.UTF_8);
        if(packet.getType() == SYN) {
            return handleHandshake(packet);
        } else if(packet.getType() == DATA) {
            requestMap.put(packet.getSequenceNumber(), payload);
            return sendAck(packet, router, channel);
            //return null;
        }else if(packet.getType() == FIN) {
            // assemble message in order
            StringBuilder messageBuilder = new StringBuilder();
            SortedSet<Long> keys = new TreeSet<>(requestMap.keySet());
            for (Long key : keys) {
                messageBuilder.append(requestMap.get(key));
            }

            Response response = new Response(directory);
            response.handleRequest(messageBuilder.toString());
            String resp = response.getResponse();
            System.out.println("Response:\n" + resp);

            return packet.toBuilder().setType(DATA).setPayload(resp.getBytes(StandardCharsets.UTF_8)).create();
        } else if(packet.getType() == ACK) {
            System.out.println("Handshake ACK received");
            return null;
        } else {
            System.out.println("Invalid packet type");
            return null;
        }
    }

    private Packet sendAck(Packet packet, SocketAddress router, DatagramChannel channel) throws IOException {
        return packet.toBuilder().setSequenceNumber(packet.getSequenceNumber()).setType(ACK).create();
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