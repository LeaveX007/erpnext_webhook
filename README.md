# ERPNext Webhook Service

Ein leichtgewichtiger und performanter Webhook-Dienst auf Basis von **Kotlin** und **Ktor (CIO)**, der Webhook-Ereignisse aus **ERPNext** entgegennimmt, über **HMAC-SHA256** kryptografisch validiert und empfangene Kundendaten asynchron in einer CSV-Datei im Dateisystem speichert.

---

## Inhaltsverzeichnis

- [Überblick](#überblick)
- [Funktionen & Highlights](#funktionen--highlights)
- [Architektur & Technologien](#architektur--technologien)
- [Voraussetzungen](#voraussetzungen)
- [Konfiguration](#konfiguration)
- [Sicherheit & HMAC-Validierung](#sicherheit--hmac-validierung)
- [API-Dokumentation](#api-dokumentation)
  - [Endpunkt: Kunde empfangen](#endpunkt-kunde-empfangen)
- [CSV-Ausgabeformat](#csv-ausgabeformat)
- [Bauen & Starten](#bauen--starten)
- [Testen des Dienstes](#testen-des-dienstes)
  - [HTTP-Client-Datei](#http-client-datei)
  - [cURL](#curl)
- [ERPNext-Integration (Webhook-Einrichtung)](#erpnext-integration-webhook-einrichtung)

---

## Überblick

Dieser Service fungiert als Schnittstelle zwischen ERPNext (bzw. dem Frappe-Framework) und lokalen oder nachgelagerten Systemen. Sobald in ERPNext ein Kunde angelegt oder gespeichert wird, übermittelt ERPNext einen Webhook an diesen Dienst.

Der Ablauf:
1. Der Dienst empfängt die Anfrage am `/customer`-Endpunkt.
2. Der im Header `X-Frappe-Webhook-Signature` übermittelte Hash wird mittels HMAC-SHA256 (Base64-kodiert) gegen den konfigurierten Sicherheitsschlüssel validiert.
3. Bei gültiger Signatur wird das JSON deserialisiert und die Kundendaten werden über Coroutines asynchron in die konfigurierte CSV-Datei angehängt.
4. Existiert die Zieldatei noch nicht, wird sie automatisch inklusive CSV-Header angelegt.

---

## Funktionen & Highlights

- **Kryptografische Absicherung:** Verifikation der Webhook-Signatur (`X-Frappe-Webhook-Signature`) verhindert unbefugte Anfragen und Manipulation.
- **Asynchrone I/O-Verarbeitung:** Dateischreiboperationen laufen über Kotlin Coroutines im `Dispatchers.IO`-Scope, um den Ktor-Worker-Thread nicht zu blockieren.
- **Automatisches CSV-Dateimanagement:** Erstellt bei Bedarf die Zieldatei inklusive Kopfzeile und hängt neue Einträge zeilenweise an.
- **Tolerante JSON-Deserialisierung:** Unbekannte oder zusätzliche Felder im JSON-Payload werden ignoriert (`ignoreUnknownKeys = true`).

---

## Architektur & Technologien

- **Programmiersprache:** [Kotlin](https://kotlinlang.org/) (JVM Toolchain 21)
- **Web-Framework:** [Ktor](https://ktor.io/) mit CIO Engine
- **Asynchronität:** Kotlin Coroutines
- **JSON-Serialisierung:** `kotlinx.serialization`
- **Kryptografie:** Java Cryptography Architecture (JCA / `HmacSHA256`) & Kotlin Base64
- **Logging:** Logback Classic / SLF4J
- **Build-Tool:** Gradle (Kotlin DSL)

---

## Voraussetzungen

- **Java Development Kit (JDK) 21** oder höher
- Optional: ERPNext- / Frappe-Instanz mit aktivierter Webhook-Funktion

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

file-output: C:/Temp/customers.csv

hmac-signature-key: "fdfjekucuc32"
```

### Konfigurationsparameter:

| Parameter | Beschreibung | Beispiel / Standard |
| :--- | :--- | :--- |
| `ktor.deployment.port` | Port, auf dem der Webhook-Server lauscht | `8080` |
| `file-output` | Vollständiger Dateipfad zur Ziel-CSV-Datei | `C:/Temp/customers.csv` (Windows) oder `/var/data/customers.csv` (Linux/macOS) |
| `hmac-signature-key` | Geheimer Schlüssel für die HMAC-SHA256-Signaturüberprüfung (muss mit dem Webhook Secret in ERPNext übereinstimmen) | `"fdfjekucuc32"` |

> **Hinweis:** Stellen Sie sicher, dass das übergeordnete Verzeichnis von `file-output` existiert und der Prozess über die erforderlichen Schreibrechte verfügt.

---

## Sicherheit & HMAC-Validierung

ERPNext sendet bei konfigurierter Webhook-Sicherheit einen `X-Frappe-Webhook-Signature`-Header mit. Dieser enthält den Base64-kodierten HMAC-SHA256-Hash des rohen Request-Bodys, berechnet mit dem konfigurierten `hmac-signature-key`.

Der Dienst prüft jede Anfrage wie folgt:
- Ist der Header `X-Frappe-Webhook-Signature` vorhanden?
- Stimmt der berechnete Base64-HMAC-SHA256-Wert mit dem Header-Wert überein?

Anfragen mit ungültiger oder fehlender Signatur werden sofort mit HTTP-Status `400 Bad Request` abgewiesen.

---

## API-Dokumentation

### Endpunkt: Kunde empfangen

Nimmt Kundendaten im JSON-Format entgegen, prüft die Signatur und speichert sie in der CSV-Datei.

- **URL:** `/customer`
- **Methode:** `POST`
- **Headers:**
  - `Content-Type: application/json`
  - `X-Frappe-Webhook-Signature: <base64_hmac_sha256>`

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
  "customer_type": "Company",
  "email_id": "michael@michael-pfeffer-consulting.com"
}
```

#### Antworten:
- **`200 OK`**: `"Kunde erfolgreich angelegt"` (Payload wurde validiert und asynchron in die CSV-Datei geschrieben).
- **`400 Bad Request`**: Fehlende/ungültige HMAC-Signatur, leerer Sicherheitsschlüssel oder ungültiges JSON-Payload.

---

## CSV-Ausgabeformat

Alle empfangenen Kunden werden in die in `file-output` definierte CSV-Datei geschrieben.

- **Trennzeichen:** Semikolon (`;`)
- **Kopfzeile:** `customer_name;customer_type;email_id`
- **Dateiaufbau:**
  ```csv
  customer_name;customer_type;email_id
  Michael Pfeffer;Company;michael@michael-pfeffer-consulting.com
  Mustermann GmbH;Company;kontakt@mustermann.de
  ```

---

## Bauen & Starten

Das Projekt verwendet den Gradle-Wrapper.

### Server starten
- **Windows (PowerShell / CMD):**
  ```powershell
  .\gradlew.bat run
  ```
- **Linux / macOS:**
  ```bash
  ./gradlew run
  ```

Sobald der Server läuft, erscheint im Log:
```text
INFO  Application - Responding at http://0.0.0.0:8080
```

### Tests ausführen
```powershell
.\gradlew.bat test
```

### Artefakt bauen
```powershell
.\gradlew.bat build
```

---

## Testen des Dienstes

### HTTP-Client-Datei

Für IntelliJ IDEA oder den VS Code REST Client steht unter `request/add-customer.http` eine vorkonfigurierte Anfrage bereit:

```http
### POST Request
POST http://127.0.0.1:8080/customer
Content-Type: application/json
X-Frappe-Webhook-Signature: RWuWldMfjOoT+utFjWrlxZlH1ia8ULDvJhg0IDqPuCs=

{
  "customer_name": "Michael Pfeffer",
  "customer_type": "Company",
  "email_id": "michael@michael-pfeffer-consulting.com"
}
```

### cURL

```bash
curl -X POST http://localhost:8080/customer \
  -H "Content-Type: application/json" \
  -H "X-Frappe-Webhook-Signature: RWuWldMfjOoT+utFjWrlxZlH1ia8ULDvJhg0IDqPuCs=" \
  -d '{
    "customer_name": "Michael Pfeffer",
    "customer_type": "Company",
    "email_id": "michael@michael-pfeffer-consulting.com"
  }'
```

---

## ERPNext-Integration (Webhook-Einrichtung)

Um ERPNext anzubinden, richten Sie unter **ERPNext > Setup > Integrations > Webhook** (bzw. Frappe Webhook) einen neuen Webhook ein:

1. **DocType:** `Customer`
2. **Doc Event:** `after_insert` (oder `on_update` / `after_save` je nach gewünschtem Auslöser)
3. **Request URL:** `http://<server-ip>:8080/customer`
4. **Request Method:** `POST`
5. **Request Structure:** `JSON`
6. **Webhook Secret:** Tragen Sie denselben Schlüssel ein wie in `application.yaml` unter `hmac-signature-key` (z. B. `fdfjekucuc32`).
7. **Webhook Headers:** `Content-Type: application/json`
8. **Webhook Data (Felder):**
   - `customer_name` -> `doc.customer_name`
   - `customer_type` -> `doc.customer_type`
   - `email_id` -> `doc.email_id`
