/*
 * ═══════════════════════════════════════════════════════════════════════════
 *                      SERVER STATUS SERVICE
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Real-time Minecraft server status monitoring with latency prediction.
 * Uses Kalman filter for optimal latency estimation.
 */
package world.haorenfu.domain.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import world.haorenfu.core.algorithm.KalmanFilter;
import world.haorenfu.core.algorithm.MonteCarloSimulator;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service for monitoring Minecraft server status.
 */
@Service
public class ServerStatusService {

    @Value("${app.minecraft.server.host:localhost}")
    private String serverHost;

    @Value("${app.minecraft.server.port:25565}")
    private int serverPort;

    // Cached status
    private volatile ServerStatus cachedStatus;
    private volatile Instant lastCheck;

    // Kalman filter for latency prediction
    private final KalmanFilter latencyFilter = KalmanFilter.forLatencyEstimation();

    // Historical data for trend analysis
    private final List<LatencyRecord> latencyHistory = new CopyOnWriteArrayList<>();
    private static final int MAX_HISTORY = 1440; // 24 hours at 1-minute intervals

    // Monte Carlo simulator for predictions
    private final MonteCarloSimulator simulator = new MonteCarloSimulator();

    /**
     * Gets the current server status.
     */
    @Cacheable(value = "serverStatus", unless = "#result == null")
    public ServerStatus getStatus() {
        if (cachedStatus == null || shouldRefresh()) {
            refreshStatus();
        }
        return cachedStatus;
    }

    /**
     * Forces a status refresh.
     */
    public ServerStatus refreshStatus() {
        try {
            long startTime = System.currentTimeMillis();
            ServerStatus status = queryServerStatus();
            long latency = System.currentTimeMillis() - startTime;

            // Update Kalman filter with new measurement
            double predictedLatency = latencyFilter.update(latency);

            // Record history
            recordLatency(latency);

            // Enhance status with predictions
            status = status.withPredictedLatency(predictedLatency);
            status = status.withLatencyConfidence(latencyFilter.getState().confidenceLevel());

            cachedStatus = status;
            lastCheck = Instant.now();

            return status;
        } catch (Exception e) {
            // Return offline status
            cachedStatus = ServerStatus.offline(e.getMessage());
            lastCheck = Instant.now();
            return cachedStatus;
        }
    }

    /**
     * Scheduled task to refresh status every minute.
     */
    @Scheduled(fixedRateString = "${app.minecraft.status.check-interval:60000}")
    public void scheduledRefresh() {
        refreshStatus();
    }

    /**
     * Gets the total number of online players.
     */
    public int getTotalOnlinePlayers() {
        ServerStatus status = getStatus();
        return status != null ? status.onlinePlayers() : 0;
    }

    /**
     * Gets the maximum player capacity.
     */
    public int getMaxPlayers() {
        ServerStatus status = getStatus();
        return status != null ? status.maxPlayers() : 0;
    }

    /**
     * Checks if the server is online.
     */
    public boolean isServerOnline() {
        ServerStatus status = getStatus();
        return status != null && status.online();
    }

    /**
     * Gets all configured servers.
     * Returns a list with the primary server for now.
     */
    public List<MinecraftServer> getAllServers() {
        MinecraftServer server = new MinecraftServer("主服务器", serverHost, serverPort);
        server.setPrimary(true);
        ServerStatus status = getStatus();
        if (status != null) {
            server.updateStatus(status);
        }
        return List.of(server);
    }

    /**
     * Gets latency statistics.
     */
    public LatencyStatistics getLatencyStatistics() {
        if (latencyHistory.isEmpty()) {
            return new LatencyStatistics(0, 0, 0, 0, 0, Collections.emptyList());
        }

        double[] values = latencyHistory.stream()
            .mapToDouble(LatencyRecord::latency)
            .toArray();

        double sum = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double v : values) {
            sum += v;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }

        double mean = sum / values.length;

        double variance = 0;
        for (double v : values) {
            variance += (v - mean) * (v - mean);
        }
        variance /= values.length;

        // Predicted latency from Kalman filter
        double predicted = latencyFilter.getEstimate();

        // Recent history for charts
        List<LatencyRecord> recent = latencyHistory.size() > 60
            ? latencyHistory.subList(latencyHistory.size() - 60, latencyHistory.size())
            : new ArrayList<>(latencyHistory);

        return new LatencyStatistics(mean, Math.sqrt(variance), min, max, predicted, recent);
    }

    /**
     * Predicts future server load.
     */
    public LoadPrediction predictLoad(int hoursAhead) {
        if (latencyHistory.size() < 24) {
            return null; // Not enough data
        }

        double[] historicalData = latencyHistory.stream()
            .skip(Math.max(0, latencyHistory.size() - 168)) // Last week
            .mapToDouble(LatencyRecord::latency)
            .toArray();

        return simulator.predictServerLoad(historicalData, hoursAhead, 1000);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Private Methods
    // ═══════════════════════════════════════════════════════════════════════

    private boolean shouldRefresh() {
        return lastCheck == null ||
               Instant.now().isAfter(lastCheck.plusSeconds(30));
    }

    private void recordLatency(double latency) {
        latencyHistory.add(new LatencyRecord(Instant.now(), latency));

        // Trim old records
        while (latencyHistory.size() > MAX_HISTORY) {
            latencyHistory.remove(0);
        }
    }

    /**
     * Queries the Minecraft server using the Server List Ping protocol.
     */
    private ServerStatus queryServerStatus() throws IOException {
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(5000);
            socket.connect(new InetSocketAddress(serverHost, serverPort), 5000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // Send handshake
            ByteArrayOutputStream handshake = new ByteArrayOutputStream();
            DataOutputStream handshakeData = new DataOutputStream(handshake);

            writeVarInt(handshakeData, 0x00); // Packet ID
            writeVarInt(handshakeData, 765);  // Protocol version (1.20.4)
            writeString(handshakeData, serverHost);
            handshakeData.writeShort(serverPort);
            writeVarInt(handshakeData, 1);    // Next state: Status

            byte[] handshakePacket = handshake.toByteArray();
            writeVarInt(out, handshakePacket.length);
            out.write(handshakePacket);

            // Send status request
            writeVarInt(out, 1);
            writeVarInt(out, 0x00);

            // Read response
            int length = readVarInt(in);
            int packetId = readVarInt(in);

            if (packetId != 0x00) {
                throw new IOException("Invalid packet ID: " + packetId);
            }

            String json = readString(in);

            // Parse JSON response
            return parseStatusJson(json);
        }
    }

    /**
     * Parses the JSON status response.
     * Uses simple parsing to avoid external dependencies.
     */
    private ServerStatus parseStatusJson(String json) {
        // Simple JSON parsing
        String version = extractJsonString(json, "name");
        int protocol = extractJsonInt(json, "protocol");
        int maxPlayers = extractJsonInt(json, "max");
        int onlinePlayers = extractJsonInt(json, "online");
        String description = extractJsonString(json, "text");

        if (description == null || description.isEmpty()) {
            // Try direct description field
            int descStart = json.indexOf("\"description\"");
            if (descStart >= 0) {
                int valueStart = json.indexOf(":", descStart) + 1;
                if (json.charAt(valueStart) == '"') {
                    description = extractJsonString(json.substring(descStart), "description");
                }
            }
        }

        // Extract player sample if available
        List<String> playerSample = new ArrayList<>();
        int sampleStart = json.indexOf("\"sample\"");
        if (sampleStart >= 0) {
            int arrayStart = json.indexOf("[", sampleStart);
            int arrayEnd = json.indexOf("]", arrayStart);
            if (arrayStart >= 0 && arrayEnd > arrayStart) {
                String sampleJson = json.substring(arrayStart, arrayEnd + 1);
                // Extract names
                int namePos = 0;
                while ((namePos = sampleJson.indexOf("\"name\"", namePos)) >= 0) {
                    String name = extractJsonString(sampleJson.substring(namePos), "name");
                    if (name != null) {
                        playerSample.add(name);
                    }
                    namePos++;
                }
            }
        }

        return new ServerStatus(
            true,
            serverHost,
            serverPort,
            version != null ? version : "Unknown",
            protocol,
            onlinePlayers,
            maxPlayers,
            description != null ? description : "",
            playerSample,
            Instant.now(),
            0, // Will be set later
            0, // Will be set later
            null
        );
    }

    private String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyPos = json.indexOf(search);
        if (keyPos < 0) return null;

        int colonPos = json.indexOf(":", keyPos);
        if (colonPos < 0) return null;

        // Skip whitespace
        int valueStart = colonPos + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
            return null;
        }

        valueStart++; // Skip opening quote
        int valueEnd = json.indexOf("\"", valueStart);
        if (valueEnd < 0) return null;

        return json.substring(valueStart, valueEnd);
    }

    private int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\"";
        int keyPos = json.indexOf(search);
        if (keyPos < 0) return 0;

        int colonPos = json.indexOf(":", keyPos);
        if (colonPos < 0) return 0;

        int valueStart = colonPos + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        int valueEnd = valueStart;
        while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
            valueEnd++;
        }

        if (valueEnd == valueStart) return 0;

        try {
            return Integer.parseInt(json.substring(valueStart, valueEnd));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // VarInt reading/writing for Minecraft protocol
    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    private static int readVarInt(DataInputStream in) throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        do {
            currentByte = in.readByte();
            value |= (currentByte & 0x7F) << position;
            position += 7;
            if (position >= 32) throw new IOException("VarInt too big");
        } while ((currentByte & 0x80) != 0);

        return value;
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = readVarInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Data Records
    // ═══════════════════════════════════════════════════════════════════════

    public record LatencyRecord(Instant timestamp, double latency) {}

    public record LatencyStatistics(
        double average,
        double stdDev,
        double min,
        double max,
        double predicted,
        List<LatencyRecord> recentHistory
    ) {}

    public record LoadPrediction(
        double[] predictedMeans,
        double[] lowerBounds,
        double[] upperBounds
    ) {
        public double getPrediction(int hour) {
            return predictedMeans[hour];
        }
    }
}

/**
 * Server status data record.
 */
record ServerStatus(
    boolean online,
    String host,
    int port,
    String version,
    int protocol,
    int onlinePlayers,
    int maxPlayers,
    String motd,
    List<String> playerSample,
    Instant lastCheck,
    double predictedLatency,
    double latencyConfidence,
    String errorMessage
) {
    public static ServerStatus offline(String error) {
        return new ServerStatus(
            false, "", 0, "", 0, 0, 0, "", List.of(),
            Instant.now(), 0, 0, error
        );
    }

    public ServerStatus withPredictedLatency(double latency) {
        return new ServerStatus(
            online, host, port, version, protocol, onlinePlayers, maxPlayers,
            motd, playerSample, lastCheck, latency, latencyConfidence, errorMessage
        );
    }

    public ServerStatus withLatencyConfidence(double confidence) {
        return new ServerStatus(
            online, host, port, version, protocol, onlinePlayers, maxPlayers,
            motd, playerSample, lastCheck, predictedLatency, confidence, errorMessage
        );
    }

    public String getPlayerCountDisplay() {
        return onlinePlayers + "/" + maxPlayers;
    }

    public double getCapacityPercent() {
        if (maxPlayers == 0) return 0;
        return (onlinePlayers * 100.0) / maxPlayers;
    }
}
