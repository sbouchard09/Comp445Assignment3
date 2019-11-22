package UDPClient;

/**************************************
Using template as provided on Moodle
 **************************************/
import Packet.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.Buffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {

    // constants
    private final int DATA = 0;
    private final int SYN = 1;
    private final int SYN_ACK = 2;
    private final int ACK = 3;
    private final int NACK = 4;
    private final int FIN = 5;

    //private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private String message = "";
    private String response = "";

    public void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
            HashMap<Long, Packet> packets = new HashMap<Long, Packet>();
            ArrayList<Packet> ackdPackets = new ArrayList<Packet>();
            long sequenceNumber = handshake(channel, serverAddr);
            if(sequenceNumber > -1) {
                byte[] payload = message.getBytes();

                if(payload.length > Packet.MAX_PAYLOAD) {
                    Packet p = new Packet.Builder()
                            .setType(DATA)
                            .setSequenceNumber(sequenceNumber + 1)
                            .setPortNumber(serverAddr.getPort())
                            .setPeerAddress(serverAddr.getAddress())
                            .setPayload(payload)
                            .create();

                    packets.put(p.getSequenceNumber(), p);
                    sequenceNumber = sequenceNumber + p.getPayload().length + Packet.MIN_LEN;
                } else {
                    System.out.println("Multiple packet support not yet implemented :(");
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
                        Packet p = new Packet.Builder()
                                .setType(DATA)
                                .setSequenceNumber(sequenceNumber)
                                .setPortNumber(serverAddr.getPort())
                                .setPeerAddress(serverAddr.getAddress())
                                .setPayload(packetPayload)
                                .create();
                        packets.put(sequenceNumber, p);
                        currentIndex = maxIndex + 1;
                    }
                }
            }

            for(Map.Entry<Long, Packet> packet : packets.entrySet()) {
                channel.send(packet.getValue().toBuffer(), routerAddr);
                // Try to receive a packet within timeout.
                Packet resp = sendReceive(packet.getValue(), channel);

                if(resp.getType() == ACK) {
                    ackdPackets.add(resp);
                }
            }

            while(ackdPackets.size() != packets.size()) {
                for(Map.Entry<Long, Packet> packet : packets.entrySet()) {
                    if(!ackdPackets.contains(packet.getValue())) {
                        channel.send(packet.getValue().toBuffer(), routerAddr);
                        // Try to receive a packet within timeout.
                        Packet resp = sendReceive(packet.getValue(), channel);

                        if(resp.getType() == ACK) {
                            ackdPackets.add(resp);
                        }
                    }
                }
            }

            // send FIN packet, let server know we're done sending
            Packet p = new Packet.Builder()
                    .setType(FIN)
                    .setSequenceNumber(sequenceNumber)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .create();

            Packet resp = sendReceive(p, channel);
            response = new String(resp.getPayload(), StandardCharsets.UTF_8);
        }
    }

    private Packet sendReceive(Packet p, DatagramChannel channel) throws IOException {
        // Try to receive a packet within timeout.
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(5000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if(keys.isEmpty()){
            return null;
        }

        // We just want a single response.
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        SocketAddress router = channel.receive(buf);
        buf.flip();
        keys.clear();
        return Packet.fromBuffer(buf);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return this.response;
    }

    private long handshake(DatagramChannel channel, InetSocketAddress address) throws IOException {
        Packet handshakePacket = new Packet.Builder()
                .setType(SYN)
                .setSequenceNumber(1L)
                .setPortNumber(address.getPort())
                .setPeerAddress(address.getAddress())
                .create();

        // send SYN
        channel.send(handshakePacket.toBuffer(), address);

        // Try to receive a packet within timeout.
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(5000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if(keys.isEmpty()){
            System.out.println("Connection timeout");
            return -1;
        }

        // We just want a single response.
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        SocketAddress router = channel.receive(buf);
        buf.flip();
        Packet resp = Packet.fromBuffer(buf); // receive SYN-ACK?
        if(resp.getType() == SYN_ACK) {
            // send ACK
            Packet ackPacket = new Packet.Builder()
                    .setType(ACK)
                    .setSequenceNumber(3L)
                    .setPortNumber(address.getPort())
                    .setPeerAddress(address.getAddress())
                    .create();
            return ackPacket.getSequenceNumber();
        } else {
            return -1;
        }
    }

    private Packet assemblePackets(Buffer buffer, String directory, SocketAdresse router) {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        Packet packet = null;

        do {
            buffer.flip();
            packet = Packet.fromBuffer(buffer);
            buffer.flip();
            String payload = new String(packet.getPayload(), StandardCharsets.UTF_8);
            map.put(packet.getType == SYN) {
                return handleHandshake(packet);
            } else if(packet.getType == DATA) {
                sendAck(packet, router);
            }else if(packet.getType == FIN) {
                // assemble message in order
                StringBuilder messageBuilder = new StringBuilder();
                SortedSet<Integer> keys = new Treeset<>(map.ketSet());
                for(Integer key : keys) {
                    messageBuilder.append(map.get(key));
                }

                Response response = new Response(directory);
                response.handleResquest(messageBuilder.toString());
                String response = response.getResponse();

                return packet.toBuilder().setPayload(response.getBytes(StandardCharsets.UTF_8)).create();
            }

        }while(payload.getType !=  FIN);
    }

}

