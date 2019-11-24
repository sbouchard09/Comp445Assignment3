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
import java.util.Map;
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


                HashMap<Long, Packet> response = assemblePackets(buf, directory, router, channel);

                // Send the response to the router not the client.
                Map.Entry<Long, Packet> packet = response.entrySet().iterator().next();
                if(packet.getValue().getType() != DATA) {
                    channel.send(packet.getValue().toBuffer(), router);
                } else {
                    sendResponsePackets(response, channel, router);
                }
            }
        }
    }

    private void sendResponsePackets(HashMap<Long, Packet> response, DatagramChannel channel, SocketAddress router) throws IOException {
        for(Map.Entry<Long, Packet> packet : response.entrySet()) {
            channel.send(packet.getValue().toBuffer(), router);
        }
    }

    private HashMap<Long, Packet> assemblePackets(ByteBuffer buffer, String directory, SocketAddress router, DatagramChannel channel) throws IOException {
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

            return handleResponse(resp, packet, router, channel);
        } else if(packet.getType() == ACK) {
            System.out.println("Handshake ACK received");
            return null;
        } else {
            System.out.println("Invalid packet type");
            return null;
        }
    }

    private HashMap<Long, Packet> handleResponse(String response, Packet packet, SocketAddress router, DatagramChannel channel) throws IOException {
        HashMap<Long, Packet> responseMap = new HashMap<>();
        long sequenceNumber = packet.getSequenceNumber() + 1;
        byte[] payload = response.getBytes();

        if(payload.length <= Packet.MAX_PAYLOAD) {
            Packet p = packet.toBuilder().setSequenceNumber(sequenceNumber).setType(DATA).setPayload(payload).create();

            responseMap.put(p.getSequenceNumber(), p);
            sequenceNumber = sequenceNumber + p.getPayload().length + Packet.MIN_LEN;
        } else {
            int currentIndex = 0;
            while(currentIndex < payload.length) {
                int maxIndex = Math.min(currentIndex + Packet.MAX_PAYLOAD, payload.length);
                byte[] packetPayload = new byte[maxIndex - currentIndex];
                int j = 0;
                for(int i = currentIndex; i < maxIndex; i++) {
                    packetPayload[j] = payload[i];
                    j++;
                }
                sequenceNumber = sequenceNumber + currentIndex;
                Packet p = packet.toBuilder().setSequenceNumber(sequenceNumber).setType(DATA).setPayload(payload).create();
                responseMap.put(sequenceNumber, p);
                currentIndex = maxIndex + 1;
            }
        }
        return responseMap;
    }

    private HashMap<Long, Packet> sendAck(Packet packet, SocketAddress router, DatagramChannel channel) throws IOException {
        HashMap<Long, Packet> responseMap = new HashMap<>();
        Packet response = packet.toBuilder().setSequenceNumber(packet.getSequenceNumber()).setType(ACK).create();
        responseMap.put(packet.getSequenceNumber(), response);
        return responseMap;
    }

    private HashMap<Long, Packet> handleHandshake(Packet packet) {
        HashMap<Long, Packet> responseMap = new HashMap<>();
        String message = "SYNACK";
        Packet response = packet.toBuilder()
                .setSequenceNumber(packet.getSequenceNumber()+1)
                .setType(SYNACK).setPayload(message
                        .getBytes(StandardCharsets.UTF_8)).create();
        responseMap.put(packet.getSequenceNumber(), response);
        return responseMap;
    }
}