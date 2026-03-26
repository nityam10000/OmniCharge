# Running Zipkin Without Docker - Setup Guide

## 📋 Prerequisites

- Java 11 or later (you already have Java 17)
- Network access to localhost:9411
- OperatorPlanManagement with Zipkin dependencies added

---

## Option 1: Download Pre-built Zipkin JAR (Easiest)

### Step 1: Download Zipkin

Download the latest Zipkin release:

**URL:** https://zipkin.io/pages/quickstart.html

Or download directly:
```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
```

This creates a `zipkin.jar` file in your current directory.

### Step 2: Run Zipkin

```bash
java -jar zipkin.jar
```

**Expected Output:**
```
2024-03-24 10:00:00.000 INFO Starting Zipkin
2024-03-24 10:00:00.000 INFO Zipkin Server started
2024-03-24 10:00:00.000 INFO listening on 0.0.0.0:9411
```

### Step 3: Verify Zipkin is Running

Open browser: **http://localhost:9411**

You should see the Zipkin dashboard with "Zipkin" logo and empty services list.

---

## Option 2: Build Zipkin from Source

### Step 1: Clone Zipkin Repository

```bash
git clone https://github.com/openzipkin/zipkin.git
cd zipkin
```

### Step 2: Build with Maven

```bash
mvn clean install -DskipTests
```

### Step 3: Run Zipkin

```bash
cd zipkin-server
java -jar target/zipkin-server-*.jar
```

---

## Option 3: Download Specific Version

If you prefer a specific version:

**Latest Stable:** https://github.com/openzipkin/zipkin/releases/latest

Choose the release and download `zipkin-server-*.jar`

Then run:
```bash
java -jar zipkin-server-2.26.0.jar
```

---

## After Starting Zipkin

### Access Zipkin UI
```
http://localhost:9411
```

### What You'll See
- Service dropdown (empty initially)
- Trace search form
- Settings and admin options
- Empty trace list

---

## Step-by-Step: Run Everything

### Terminal 1: Start Zipkin Server
```bash
cd C:\path\to\zipkin
java -jar zipkin.jar
```

Wait for:
```
listening on 0.0.0.0:9411
```

### Terminal 2: Build OperatorPlanManagement
```bash
cd C:\Users\nitya\OneDrive\Desktop\Sprint_Project\OmniCharge\OperatorPlanManagement
mvn clean install -DskipTests
```

Wait for:
```
BUILD SUCCESS
```

### Terminal 3: Run OperatorPlanManagement
```bash
cd C:\Users\nitya\OneDrive\Desktop\Sprint_Project\OmniCharge\OperatorPlanManagement
mvn spring-boot:run
```

Wait for:
```
OperatorPlanManagement started in X seconds
Zipkin tracer initialized
```

### Terminal 4: Generate Traces
```bash
# Make several API calls
curl http://localhost:8086/operators/list
curl http://localhost:8086/plans/get/1
curl http://localhost:8086/operators/list
curl http://localhost:8086/plans/get/2
```

### Browser: View Traces
Open: **http://localhost:9411**

1. Select "OperatorPlanManagement" from Service dropdown
2. Click "Find Traces"
3. You should see 4 traces listed!
4. Click on any trace to see detailed breakdown

---

## Troubleshooting

### Issue: "Connection refused: localhost:9411"

**Solution:** Zipkin is not running
```bash
# Check if process is running
netstat -ano | findstr :9411

# If not running, start it
java -jar zipkin.jar
```

### Issue: "Failed to connect to Zipkin server"

**Solution:** Check your application.properties configuration

Verify these lines exist:
```properties
management.zipkin.tracing.endpoint=http://localhost:9411
management.tracing.sampling.probability=1.0
```

### Issue: Service not appearing in Zipkin

**Solution:** 
1. Wait 5-10 seconds (it takes time to send first trace)
2. Refresh browser (F5)
3. Check OperatorPlanManagement logs for errors
4. Verify Zipkin is running on port 9411

### Issue: Zipkin won't start

**Solution:**
```bash
# Check Java version
java -version
# Should show Java 11+

# If Java not in PATH
"C:\Program Files\Java\jdk-17\bin\java.exe" -jar zipkin.jar

# If port 9411 is in use
# Find and kill the process or use different port
java -jar zipkin.jar -Dserver.port=9412
```

---

## Configuration Options for Zipkin Server

### Change Port
```bash
java -jar zipkin.jar -Dserver.port=9412
```

### Set Storage Type
```bash
java -jar zipkin.jar -DSTORAGE_TYPE=mem
```

### Enable Debug Logging
```bash
java -jar zipkin.jar --logging.level.root=DEBUG
```

### Complete Example
```bash
java -jar zipkin.jar \
  -Dserver.port=9411 \
  -DSTORAGE_TYPE=mem \
  --logging.level.zipkin=DEBUG
```

---

## Updating Zipkin Endpoint (if different)

If you run Zipkin on a different port, update `application.properties`:

```properties
management.zipkin.tracing.endpoint=http://localhost:9412
```

---

## Next Steps

1. ✅ Download/run Zipkin
2. ✅ Build OperatorPlanManagement
3. ✅ Start OperatorPlanManagement service
4. ✅ Make API calls to generate traces
5. ✅ View traces in Zipkin UI

---

## Summary Commands

**Quick Start (All in one)**
```bash
# Terminal 1
java -jar zipkin.jar

# Terminal 2
cd OperatorPlanManagement && mvn clean install -DskipTests && mvn spring-boot:run

# Terminal 3
curl http://localhost:8086/operators/list

# Browser
# Open http://localhost:9411
```

---

## Performance Notes

**Zipkin Without Docker:**
- Memory usage: ~200MB
- CPU usage: <5%
- Network: Minimal
- Startup time: ~5 seconds
- Data storage: In-memory (lost on restart)

**To persist data between restarts:**
```bash
java -jar zipkin.jar -DSTORAGE_TYPE=mysql
```

(Requires MySQL/PostgreSQL setup)

---

## Useful Links

- **Zipkin Quickstart:** https://zipkin.io/pages/quickstart.html
- **GitHub Releases:** https://github.com/openzipkin/zipkin/releases
- **Documentation:** https://zipkin.io/

---

**Status:** ✅ Ready to run Zipkin without Docker!
