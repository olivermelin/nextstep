/**
 * Arkitekt – Architecture Agent
 *
 * Granskar kodbasen efter:
 * - Arkitekturmönster och separation of concerns
 * - API-design och kontrakt mellan backend/frontend
 * - Kodkvalitet och underhållbarhet
 * - Prestandarisker
 * - Teknisk skuld
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
  systemPrompt: `Du är en senior mjukvaruarkitekt med 15+ års erfarenhet av Java-ekosystemet och modern webbutveckling.

Projektet är ett monorepo med:
- Backend: Java 25, Spring Boot 4, Spring Security, JPA/PostgreSQL
- Frontend: React 18, TypeScript, React Query, React Router

Ditt uppdrag:
1. Bedöm övergripande arkitektur och lager-separation (Controller → Service → Repository)
2. Granska API-design (REST-konventioner, felhantering, statuskoder)
3. Identifiera möjliga prestandaproblem (N+1-queries, onödiga anrop, saknade index)
4. Hitta teknisk skuld och anti-mönster
5. Granska state management i frontend (React Query, lokalt state)
6. Bedöm testbarhet – hur lätt är koden att testa?
7. Föreslå förbättringar i projektstruktur

Rapportera med:
- Fil/klass och radnummer
- Förklaring av problemet och dess konsekvenser
- Konkret åtgärd med kodexempel
- Prioritering: KRITISK / VIKTIG / FÖRBÄTTRING

Svara på svenska.`,
  maxTurns: 25,
};

console.log("🏗️  Arkitekt-agenten startar...\n");

for await (const message of query({
  prompt: `Genomför en arkitektuell kodgranskning av projektet i ${PROJECT_ROOT}.

Börja med att förstå projektstrukturen, sedan:
- backend/src/main/java/ – Spring Boot-lager, tjänster, repositories
- frontend/src/ – komponenter, tjänster, hooks, routing
- API-kontraktet mellan backend och frontend

Ge en strukturerad arkitekturrapport med prioriterade åtgärder och eventuella refaktoreringsförslag.`,
  options,
})) {
  if ("result" in message) {
    console.log("\n✅ Arkitekturrapport klar:\n");
    console.log(message.result);
  }
}
