#!/bin/bash
# Test runner for AttachmentDAO tests
# This script compiles and runs the AttachmentDAO test suite

echo "========================================"
echo "AttachmentDAO Test Runner"
echo "========================================"
echo ""

echo "Building project..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Running tests..."
echo ""

# Find the JAR file
JAR_FILE=$(find target -name "tilitin-*.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "JAR file not found. Building package..."
    mvn package -DskipTests -q
    JAR_FILE=$(find target -name "tilitin-*.jar" | head -1)
fi

# Build classpath
CP="$JAR_FILE"
for jar in target/lib/*.jar; do
    if [ -f "$jar" ]; then
        CP="$CP:$jar"
    fi
done

# Run the test
java -cp "$CP" kirjanpito.test.AttachmentDAOTest

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "All tests passed!"
    echo "========================================"
else
    echo ""
    echo "========================================"
    echo "Some tests failed!"
    echo "========================================"
    exit 1
fi

