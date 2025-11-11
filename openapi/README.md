# Jira API Client - OpenAPI Generator

This directory contains the OpenAPI specification for the Atlassian Jira Cloud REST API and instructions for generating the Java client.

## 📄 OpenAPI Specification

- **File**: `swagger-v3.v3.json`
- **Source**: https://developer.atlassian.com/cloud/jira/platform/swagger-v3.v3.json
- **Official Documentation**: https://developer.atlassian.com/cloud/jira/platform/rest/v3/intro/

## 🔄 How to Update the OpenAPI JSON File

When Atlassian releases a new version of the Jira API, you can update the specification:

### Option 1: Download Latest Version (Recommended)

```bash
# Navigate to the project root
cd /workspaces/plugin-atlassian-jira

# Download the latest OpenAPI specification
curl -o openapi/swagger-v3.v3.json \
  https://developer.atlassian.com/cloud/jira/platform/swagger-v3.v3.json
```

### Option 2: Manual Download

1. Visit: https://developer.atlassian.com/cloud/jira/platform/swagger-v3.v3.json
2. Save the JSON content to `openapi/swagger-v3.v3.json`
3. Verify the file is valid JSON:
   ```bash
   cat openapi/swagger-v3.v3.json | jq . > /dev/null && echo "Valid JSON" || echo "Invalid JSON"
   ```

### Verify the Update

After downloading, check the file size and version:

```bash
# Check file size (should be ~2-3 MB)
ls -lh openapi/swagger-v3.v3.json

# Check version info in the spec (if available)
grep -i "version" openapi/swagger-v3.v3.json | head -5
```

## 🚀 How to Run OpenAPI Generator

### Prerequisites

- ✅ OpenAPI specification file exists at `openapi/swagger-v3.v3.json`
- ✅ Gradle is configured (see `build.gradle`)
- ✅ OpenAPI Generator plugin is added (version 7.17.0)

### Step-by-Step Generation

#### 1. Clean Old Generated Code (Optional but Recommended)

If you're regenerating, remove the old client first:

```bash
# From project root
rm -rf src/main/java/io/kestra/plugin/jira/client/
```

#### 2. Run the Generator

```bash
# From project root
./gradlew openApiGenerate
```

This will:
- Read the OpenAPI spec from `openapi/swagger-v3.v3.json`
- Generate Java client code in `src/main/java/io/kestra/plugin/jira/client/`
- Create API classes in `client/api/`
- Create model classes in `client/model/`
- Create infrastructure classes in `client/invoker/`

#### 3. Review Generated Code

```bash
# Check what was generated
ls -la src/main/java/io/kestra/plugin/jira/client/

# See API classes
ls src/main/java/io/kestra/plugin/jira/client/api/ | head -10

# See model classes
ls src/main/java/io/kestra/plugin/jira/client/model/ | head -10
```

#### 4. Build and Verify

```bash
# Compile the generated code
./gradlew compileJava

# Run full build
./gradlew build
```

#### 5. Review Changes (If Regenerating)

```bash
# See what changed in the generated code
git diff src/main/java/io/kestra/plugin/jira/client/

# See what changed in the spec (if you updated it)
git diff openapi/swagger-v3.v3.json
```

#### 6. Commit Changes

Once you've verified everything works:

```bash
# Stage the changes
git add openapi/swagger-v3.v3.json
git add src/main/java/io/kestra/plugin/jira/client/

# Commit with a descriptive message
git commit -m "chore: update Jira API client to latest version"
```

## 📦 Generated Client Structure

After generation, you'll have:

```
src/main/java/io/kestra/plugin/jira/client/
├── api/           # API endpoint classes (e.g., IssuesApi, ProjectsApi)
├── model/         # Data model classes (e.g., IssueBean, ProjectBean)
└── invoker/       # HTTP client infrastructure (ApiClient, ApiException)
```

## ⚙️ Generator Configuration

The generator is configured in `build.gradle` with these settings:

- **Generator**: `java` (Java client library)
- **Library**: `native` (Java 11+ HttpClient - no external HTTP dependencies)
- **Date Library**: `java8` (uses `java.time.*` classes)
- **Serialization**: `jackson` (JSON serialization)
- **Documentation**: **ENABLED** ✅ (Javadoc included in source code)
- **Package**: `io.kestra.plugin.jira.client.*`

## 🔍 Troubleshooting

### Generator Fails

**Problem**: `openApiGenerate` task fails

**Solutions**:
1. Check the OpenAPI spec is valid:
   ```bash
   cat openapi/swagger-v3.v3.json | jq . > /dev/null
   ```
2. Verify the file path in `build.gradle` matches your file location
3. Check Gradle version compatibility:
   ```bash
   ./gradlew --version
   ```

### Compilation Errors After Generation

**Problem**: Generated code doesn't compile

**Solutions**:
1. Ensure all dependencies are in `build.gradle`:
   - `swagger-annotations`
   - `jsr305`
   - `jackson-databind`
   - `jackson-datatype-jsr310`
   - `jackson-databind-nullable`
2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```
3. Check Java version compatibility (should be Java 21)

### Files Overwritten

**Problem**: Generator overwrites your project files

**Solution**: The `.openapi-generator-ignore` file should prevent this. If files are still being overwritten, add them to the ignore file.

## 📝 Notes

- **Manual Generation**: The generator is NOT run automatically on every build. This is intentional to ensure stable, predictable builds.
- **Version Control**: The generated client code is committed to version control for stability and reviewability.
- **Documentation**: Full Javadoc is included in the generated code - hover over methods in your IDE to see documentation!

## 🔗 Related Files

- `build.gradle` - Generator configuration
- `.openapi-generator-ignore` - Files to protect from generator
- `src/main/java/io/kestra/plugin/jira/client/` - Generated client location

## 📚 Additional Resources

- [OpenAPI Generator Documentation](https://openapi-generator.tech/docs/generators/java)
- [Jira Cloud REST API Documentation](https://developer.atlassian.com/cloud/jira/platform/rest/v3/intro/)
- [OpenAPI Specification](https://swagger.io/specification/)

