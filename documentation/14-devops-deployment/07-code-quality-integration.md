# Code Quality and Static Analysis Integration

**Document Identifier:** DOC-OPS-007  
**Traceability References:** DOC-AUTO-006, DOC-AUTO-013, DOC-OPS-004  
**Tools:** Checkstyle 10.x, SpotBugs 4.8.x, JaCoCo, ESLint, Gitleaks  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the integration of static code quality analyzers, linter rules, secret detectors, and test coverage checkers within the automated build pipeline.

---

## 2. Quality Scan Inventory & Enforcement Rules

| Analyzer Tool | Target Codebase | Execution Goal / Command | Zero-Tolerance Threshold |
|---|---|---|---|
| **Checkstyle** | Backend Java 21 | `mvn checkstyle:check` | 0 style violations (Google Java Style). |
| **SpotBugs** | Backend Java bytecode | `mvn spotbugs:check` | 0 High or Medium priority bugs. |
| **JaCoCo** | Java execution data | `mvn jacoco:check` | $< 80\%$ line coverage fails build. |
| **ESLint / Prettier**| React 18 / TypeScript | `npm run lint` | 0 lint errors; 0 unhandled `any` types. |
| **Gitleaks** | Git commit tree | `gitleaks detect -v` | 0 detected high-entropy keys or passwords. |

---

## 3. Checkstyle Maven Configuration

Configured in the parent `pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <dependencies>
        <dependency>
            <groupId>com.puppycrawl.tools</groupId>
            <artifactId>checkstyle</artifactId>
            <version>10.14.2</version>
        </dependency>
    </dependencies>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
        <violationSeverity>warning</violationSeverity>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
    </configuration>
</plugin>
```

---

## 4. SpotBugs Maven Configuration

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.3.1</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <failOnError>true</failOnError>
        <excludeFilterFile>spotbugs-exclude.xml</excludeFilterFile>
    </configuration>
</plugin>
```
Any potential null-pointer dereference, unclosed stream, or SQL injection pattern immediately terminates the build with an actionable error.

