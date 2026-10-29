# ERPNext Webhook Service

Ein leichtgewichtiger und performanter Webhook-Dienst auf Basis von **Kotlin** und **Ktor (CIO)**, der Webhook-Ereignisse (z. B. aus **ERPNext**) entgegennimmt, validiert und Kundendaten als CSV-Dateien im Dateisystem ablegt.

---

## Inhaltsverzeichnis

- [Überblick](#überblick)
- [Architektur & Technologien](#architektur--technologien)
- [Voraussetzungen](#voraussetzungen)
- [Konfiguration](#konfiguration)
- [API-Dokumentation](#api-dokumentation)
  - [Endpunkt: Kunde anlegen](#endpunkt-kunde-anlegen)
- [CSV-Ausgabeformat](#csv-ausgabeformat)
- [Bauen & Starten](#bauen--starten)
- [Testen des Dienstes](#testen-des-dienstes)
  - [cURL](#curl)
  - [HTTP-Client-Datei](#http-client-datei)
- [ERPNext-Integration](#erpnext-integration)

---

## Überblick

Dieser Service dient als Schnittstelle zwischen ERPNext (oder ähnlichen Drittsystemen) und lokalen/nachgelagerten Prozessen. Sobald in ERPNext ein neuer Kunde angelegt oder aktualisiert wird, sendet ERPNext einen Webhook an diesen Dienst. Der Dienst deserialisiert das JSON-Payload und speichert die Kundendaten strukturiert als CSV-Datei in einem konfigurierten Verzeichnis ab.

---

## Architektur & Technologien

- **Programmiersprache:** [Kotlin](https://kotlinlang.org/) (JVM Toolchain 21)
- **Framework:** [Ktor](https://ktor.io/) mit CIO Engine
- **JSON-Serialisierung:** `kotlinx.serialization` (mit automatischer Ignorierung unbekannter JSON-Schlüssel)
- **Logging:** Logback Classic / SLF4J
- **Build-Tool:** Gradle (Kotlin DSL)

---

## Voraussetzungen

- **Java Development Kit (JDK) 21** oder höher
- Optional: ERPNext-Instanz mit aktivierter Webhook-Funktion

---

## Konfiguration

Die Basiskonfiguration befindet sich in `src/main/resources/application.yaml`:

```yaml
ktor:
  deployment:
    port: 8080
  application:
    modules:
      - com.michaelpfefferconsulting.SerializationKt.configureSerialization
      - com.michaelpfefferconsulting.RoutingKt.configureCustomerRouting

file-output: C:/Temp
```

### Konfigurationsparameter:
- `ktor.deployment.port`: Port, auf dem der Webhook-Server lauscht (Standard: `8080`).
- `file-output`: Zielverzeichnis für die generierten CSV-Dateien (z. B. `C:/Temp` unter Windows oder `/var/data/customers` unter Linux/macOS). **Hinweis:** Stellen Sie sicher, dass das Verzeichnis existiert und Schreibrechte vorhanden sind.

---

## API-Dokumentation

### Endpunkt: Kunde anlegen

Nimmt Kundendaten im JSON-Format entgegen und speichert sie als CSV.

- **URL:** `/customer`
- **Methode:** `POST`
- **Headers:** `Content-Type: application/json`

#### Request-Body

| Feld | Typ | Pflichtfeld | Beschreibung |
| :--- | :--- | :--- | :--- |
| `customer_name` | `String` | Ja | Name des Kunden / Unternehmens |
| `customer_type` | `String` | Ja | Kundentyp (z. B. `Company`, `Individual`) |
| `email_id` | `String` | Nein | E-Mail-Adresse des Kunden |

#### Beispiel-Payload:
```json
{
  "customer_name": "Michael Pfeffer",
  "customer_type": "Individual",
  "email_id": "info@michaelpfefferconsulting.com"
}
```

#### Antworten:
- **`200 OK`**: `"Kunde erfolgreich angelegt"`
- **`400 Bad Request`**: Ungültiges JSON-Format oder fehlende Pflichtfelder.

---

## CSV-Ausgabeformat

Für jeden empfangenen Kunden wird eine neue CSV-Datei mit folgendem Namensschema im konfigurierten `file-output`-Verzeichnis erzeugt:

```text
<customer_name>_<timestamp>.csv
```

### Aufbau der CSV-Datei:
- **Trennzeichen:** Semikolon (`;`)
- **Kopfzeile:** `customer_name;customer_type;email_id`
- **Beispielinhalt:**
  ```csv
  customer_name;customer_type;email_id
  Michael Pfeffer;Individual;info@michaelpfefferconsulting.com
  ```

---

## Bauen & Starten

Das Projekt enthält den Gradle-Wrapper. Führen Sie die folgenden Befehle im Projektstammverzeichnis aus:

### Server starten
- **Windows (PowerShell/CMD):**
  ```powershell
  .\gradlew.bat run
  ```
- **Linux / macOS:**
  ```bash
  ./gradlew run
  ```

Sobald der Server gestartet ist, wird in der Konsole folgende Ausgabe angezeigt:
```text
INFO  Application - Responding at http://0.0.0.0:8080
```

### Projekt bauen & testen
```powershell
# Tests ausführen
.\gradlew.bat test

# Fat-JAR / Distributionsartefakt erstellen
.\gradlew.bat build
```

---

## Testen des Dienstes

### cURL

```bash
curl -X POST http://localhost:8080/customer \
  -H "Content-Type: application/json" \
  -d '{
    "customer_name": "Mustermann GmbH",
    "customer_type": "Company",
    "email_id": "kontakt@mustermann.de"
  }'
```

### HTTP-Client-Datei

Für IntelliJ IDEA oder VS Code REST Client steht unter `request/add-customer.http` eine vorgefertigte HTTP-Anfrage zur Verfügung.

---

## ERPNext-Integration

Um ERPNext anzubinden, kann unter **ERPNext > Setup > Integrations > Webhook** ein neuer Webhook eingerichtet werden:

1. **DocType:** `Customer`
2. **Event:** `After Insert` / `After Save`
3. **Request URL:** `http://<ihr-server-ip>:8080/customer`
4. **Request Method:** `POST`
5. **Request Structure:** `JSON`
6. **Webhook Data (Felder):**
   - `customer_name` -> `doc.customer_name`
   - `customer_type` -> `doc.customer_type`
   - `email_id` -> `doc.email_id`
