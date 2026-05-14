# Logging Infrastructure Design

**Role**: java-spring-architect
**Story**: STORY-011
**Status**: Complete

## ActivityLogger Interface

```java
public interface ActivityLogger {
    void log(String role, String activity, String storyId, Map<String, Object> details, String outcome);
    void logSuccess(String role, String activity, String storyId, Map<String, Object> details);
    void logFailure(String role, String activity, String storyId, String error, Map<String, Object> details);
}
```

## JSONLinesWriter Implementation

```java
@Component
public class JSONLinesWriter {
    private final Path logDirectory;
    private final ObjectMapper objectMapper;
    private final Map<String, FileWriter> writerCache = new ConcurrentHashMap<>();

    public synchronized void write(String role, LogEntry entry) {
        String filename = String.format("%s__%s.log", role, LocalDate.now());
        FileWriter writer = getOrCreateWriter(filename);
        writer.write(objectMapper.writeValueAsString(entry) + "\n");
        writer.flush();
    }

    @PreDestroy
    void closeAll() {
        writerCache.values().forEach(this::closeQuietly);
    }
}
```

## Log File Rotation Strategy
- **Daily rotation**: One file per role per day
- **Format**: `<role>__<yyyy-mm-dd>.log`
- **Thread-safe**: Synchronized writes
- **Auto-cleanup**: Close on shutdown

## Test Design Outline
```java
@Test void shouldWriteJSONLinesFormat()
@Test void shouldCreateDailyFiles()
@Test void shouldHandleConcurrentWrites() // Multiple threads
@Test void shouldRotateOnDateChange()
@Test void shouldCloseFilesOnShutdown()
```
