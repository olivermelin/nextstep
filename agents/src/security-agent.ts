/**
 * Säkerhetsansvarig – Security Agent
 *
 * Granskar kodbasen efter:
 * - Säkerhetsbrister (OWASP Top 10)
 * - Känsliga värden hårdkodade i koden
 * - Autentisering och auktorisering
 * - Beroenden med kända CVE:er
 */

import { query, type ClaudeAgentOptions } from "@anthropic-ai/claude-agent-sdk";
import path from "path";
import { fileURLToPath } from "url";

const PROJECT_ROOT = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../.."
);

const options: ClaudeAgentOptions = {
  cwd: PROJECT_ROOT,
  allowedTools: ["Read", "Glob", "Grep", "Bash"],
  permissionMode: "dontAsk",
  systemPrompt: `Du är en erfaren säkerhetsexpert (CISSP-certifierad) som granskar kod i ett Java Spring Boot + React TypeScript-projekt.

Ditt uppdrag:
1. Sök efter hårdkodade hemligheter, API-nycklar, lösenord
2. Granska autentisering och auktorisering (Spring Security, JWT, OAuth2)
3. Kontrollera för vanliga sårbarheter: SQL-injektion, XSS, CSRF, SSRF
4. Identifiera beroenden med kända CVE:er (kör 'npm audit' och titta i pom.xml)
5. Kontrollera att känslig data loggas korrekt (inte lösenord, tokens i loggar)
6. Granska CORS-konfiguration

Rapportera alltid med:
- Allvarlighetsgrad: KRITISK / HÖG / MEDEL / LÅG
- Fil och radnummer
- Konkret åtgärdsförslag

Svara på svenska.`,
  maxTurns: 20,
};

console.log("🔐 Säkerhetsansvarig-agenten startar...\n");

for await (const message of query({
  prompt: `Genomför en fullständig säkerhetsgranskning av projektet i ${PROJECT_ROOT}.

Fokusera på:
- backend/src/ (Java/Spring Boot)
- frontend/src/ (React/TypeScript)
- Konfigurationsfiler (.env, application.properties, application.yml)
- package.json och pom.xml för sårbara beroenden

Ge en strukturerad rapport med prioriterade åtgärder.`,
  options,
})) {
  if ("result" in message) {
    console.log("\n✅ Säkerhetsrapport klar:\n");
    console.log(message.result);
  }
}
