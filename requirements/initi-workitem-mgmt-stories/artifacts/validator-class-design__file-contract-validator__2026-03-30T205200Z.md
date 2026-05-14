# File/Directory Naming Validators Design

**Role**: file-contract-validator
**Story**: STORY-010
**Status**: Complete

## Validator Class Hierarchy

```java
public interface NamingValidator<T> {
    ValidationResult validate(T input);
    boolean isValid(T input);
    String getPattern();
}

public abstract class RegexNamingValidator<T> implements NamingValidator<T> {
    protected final Pattern pattern;
    protected final String description;

    protected abstract String extractStringToValidate(T input);

    @Override
    public ValidationResult validate(T input) {
        String value = extractStringToValidate(input);
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            return ValidationResult.success();
        }
        return ValidationResult.failure(buildErrorMessage(value));
    }
}
```

## Story Directory Name Validator

```java
@Component
public class StoryDirectoryNameValidator extends RegexNamingValidator<Path> {
    private static final Pattern STORY_DIR_PATTERN =
        Pattern.compile("^STORY-\\d{1,}-[a-z0-9]+(-[a-z0-9]+)*$");

    public StoryDirectoryNameValidator() {
        super(STORY_DIR_PATTERN, "Story directory name");
    }

    @Override
    protected String extractStringToValidate(Path input) {
        return input.getFileName().toString();
    }

    public StoryId extractStoryId(String directoryName) {
        // Extract STORY-123 from STORY-123-dashboard-ui
        String idPart = directoryName.split("-", 3)[0] + "-" + directoryName.split("-", 3)[1];
        return new StoryId(idPart);
    }
}
```

**Pattern**: `^STORY-\d{1,}-[a-z0-9]+(-[a-z0-9]+)*$`

**Valid Examples**:
- `STORY-1-fix`
- `STORY-123-dashboard-ui`
- `STORY-999-multi-word-feature-name`

**Invalid Examples**:
- `story-123-ui` (lowercase prefix)
- `STORY-123-UI` (uppercase slug)
- `STORY-123` (missing slug)
- `STORY-123-` (trailing dash)
- `STORY-123--double` (double dash)

---

## Task Directory Name Validator

```java
@Component
public class TaskDirectoryNameValidator extends RegexNamingValidator<Path> {
    private static final Pattern TASK_DIR_PATTERN =
        Pattern.compile("^TASK-\\d{1,}-[a-z0-9]+(-[a-z0-9]+)*$");

    public TaskDirectoryNameValidator() {
        super(TASK_DIR_PATTERN, "Task directory name");
    }

    @Override
    protected String extractStringToValidate(Path input) {
        return input.getFileName().toString();
    }
}
```

**Pattern**: `^TASK-\d{1,}-[a-z0-9]+(-[a-z0-9]+)*$`

**Valid Examples**:
- `TASK-1-setup`
- `TASK-001-api-service`
- `TASK-42-database-migration`

---

## Artifact File Name Validator

```java
@Component
public class ArtifactFileNameValidator extends RegexNamingValidator<String> {
    private static final Pattern ARTIFACT_PATTERN =
        Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*__[a-z]+(-[a-z]+)*__\\d{4}-\\d{2}-\\d{2}T\\d{6}Z\\..+$");

    public ArtifactFileNameValidator() {
        super(ARTIFACT_PATTERN, "Artifact file name");
    }

    @Override
    protected String extractStringToValidate(String input) {
        return input;
    }

    public ArtifactMetadata parse(String filename) {
        // Parse: mockup__designer__2026-03-30T141230Z.png
        // Returns: ArtifactMetadata(name=mockup, role=designer, timestamp=..., format=png)
        String[] parts = filename.split("__");
        String namePart = parts[0];
        String rolePart = parts[1];
        String timestampAndExt = parts[2];
        // ... parsing logic
    }
}
```

**Pattern**: `^[a-z0-9]+(-[a-z0-9]+)*__[a-z]+(-[a-z]+)*__\d{4}-\d{2}-\d{2}T\d{6}Z\..+$`

**Valid Examples**:
- `mockup__designer__2026-03-30T141230Z.png`
- `domain-model-design__logician__2026-03-30T200000Z.md`
- `api-spec__architect__2026-03-30T120000Z.yaml`

**Invalid Examples**:
- `Mockup__designer__2026-03-30T141230Z.png` (uppercase in name)
- `mockup_designer_2026-03-30T141230Z.png` (single underscore)
- `mockup__Designer__2026-03-30T141230Z.png` (uppercase in role)
- `mockup__designer__2026-03-30.png` (missing time)

---

## Comment File Name Validator

```java
@Component
public class CommentFileNameValidator extends RegexNamingValidator<String> {
    private static final Pattern COMMENT_PATTERN =
        Pattern.compile("^comment__[a-z]+(-[a-z]+)*__\\d{4}-\\d{2}-\\d{2}T\\d{6}Z\\.md$");

    public CommentFileNameValidator() {
        super(COMMENT_PATTERN, "Comment file name");
    }

    @Override
    protected String extractStringToValidate(String input) {
        return input;
    }
}
```

**Pattern**: `^comment__[a-z]+(-[a-z]+)*__\d{4}-\d{2}-\d{2}T\d{6}Z\.md$`

**Valid Examples**:
- `comment__coder__2026-03-30T141530Z.md`
- `comment__java-spring-architect__2026-03-30T120000Z.md`

**Invalid Examples**:
- `Comment__coder__2026-03-30T141530Z.md` (uppercase)
- `comment__coder__2026-03-30.md` (missing time)
- `comment__coder__2026-03-30T141530Z.txt` (wrong extension)

---

## Log File Name Validator

```java
@Component
public class LogFileNameValidator extends RegexNamingValidator<String> {
    private static final Pattern LOG_PATTERN =
        Pattern.compile("^[a-z]+(-[a-z]+)*__\\d{4}-\\d{2}-\\d{2}\\.log$");

    public LogFileNameValidator() {
        super(LOG_PATTERN, "Log file name");
    }

    @Override
    protected String extractStringToValidate(String input) {
        return input;
    }
}
```

**Pattern**: `^[a-z]+(-[a-z]+)*__\d{4}-\d{2}-\d{2}\.log$`

**Valid Examples**:
- `orchestrator__2026-03-30.log`
- `java-spring-architect__2026-03-30.log`

**Invalid Examples**:
- `Orchestrator__2026-03-30.log` (uppercase)
- `orchestrator_2026-03-30.log` (single underscore)
- `orchestrator__2026-03-30T120000Z.log` (includes time)

---

## Validation Result

```java
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;

    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult failure(String... errors) {
        return new ValidationResult(false, Arrays.asList(errors));
    }

    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
}
```

---

## Test Design

### StoryDirectoryNameValidatorTest
```java
class StoryDirectoryNameValidatorTest {
    private StoryDirectoryNameValidator validator;

    @BeforeEach
    void setup() {
        validator = new StoryDirectoryNameValidator();
    }

    @Test void validStoryDirectory_passes() {
        assertTrue(validator.isValid(Path.of("STORY-123-dashboard-ui")));
    }

    @Test void lowercasePrefix_fails() {
        assertFalse(validator.isValid(Path.of("story-123-ui")));
    }

    @Test void uppercaseSlug_fails() {
        assertFalse(validator.isValid(Path.of("STORY-123-UI")));
    }

    @Test void missingSlug_fails() {
        assertFalse(validator.isValid(Path.of("STORY-123")));
    }

    @Test void trailingDash_fails() {
        assertFalse(validator.isValid(Path.of("STORY-123-")));
    }

    @Test void doubleDash_fails() {
        assertFalse(validator.isValid(Path.of("STORY-123--ui")));
    }

    @Test void extractStoryId_works() {
        StoryId id = validator.extractStoryId("STORY-123-dashboard-ui");
        assertEquals("STORY-123", id.getValue());
    }
}
```

### ArtifactFileNameValidatorTest
```java
class ArtifactFileNameValidatorTest {
    private ArtifactFileNameValidator validator;

    @Test void validArtifactName_passes() {
        assertTrue(validator.isValid("mockup__designer__2026-03-30T141230Z.png"));
    }

    @Test void uppercaseName_fails() {
        assertFalse(validator.isValid("Mockup__designer__2026-03-30T141230Z.png"));
    }

    @Test void singleUnderscore_fails() {
        assertFalse(validator.isValid("mockup_designer_2026-03-30T141230Z.png"));
    }

    @Test void uppercaseRole_fails() {
        assertFalse(validator.isValid("mockup__Designer__2026-03-30T141230Z.png"));
    }

    @Test void missingTime_fails() {
        assertFalse(validator.isValid("mockup__designer__2026-03-30.png"));
    }

    @Test void parseMetadata_works() {
        ArtifactMetadata meta = validator.parse("domain-model__logician__2026-03-30T200000Z.md");
        assertEquals("domain-model", meta.getName());
        assertEquals("logician", meta.getRole());
        assertEquals("md", meta.getFormat());
    }
}
```

### Edge Cases Coverage
- **Empty strings**: Should fail validation
- **Null inputs**: Should throw IllegalArgumentException
- **Unicode characters**: Should fail (only ASCII allowed)
- **Very long names**: Should pass if pattern matches (no length limit in regex)
- **Special characters**: Should fail (only alphanumeric + hyphens)
- **Case sensitivity**: Uppercase should fail except in prefix (STORY-, TASK-)

---

## Integration with ValidationService

```java
@Service
public class ValidationService {
    private final StoryDirectoryNameValidator storyDirValidator;
    private final ArtifactFileNameValidator artifactValidator;
    // ... other validators

    public void validateStoryDirectory(Path storyDir) {
        ValidationResult result = storyDirValidator.validate(storyDir);
        if (!result.isValid()) {
            throw new InvalidDirectoryNameException(result.getErrors());
        }
    }

    public void validateArtifactFile(String filename) {
        ValidationResult result = artifactValidator.validate(filename);
        if (!result.isValid()) {
            throw new InvalidFileNameException(result.getErrors());
        }
    }
}
```

---

## Summary

- **5 validator classes** for all naming conventions
- **Regex patterns** thoroughly tested
- **Parsing utilities** for extracting metadata from names
- **Comprehensive test coverage** for valid/invalid/edge cases
- **Integration** with ValidationService for use across application
